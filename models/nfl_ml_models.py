import pandas as pd
from sklearn.linear_model import Ridge, BayesianRidge, ElasticNet
from sklearn.model_selection import cross_val_score, ShuffleSplit
from sklearn.metrics import mean_squared_error
from sklearn.ensemble import RandomForestRegressor, GradientBoostingRegressor
from sklearn.model_selection import GridSearchCV
import numpy as np
try:
    import cpickle as pickle
except:
    import pickle


def data_processing(file_name):
    df = pd.read_csv(file_name)
    df.sort_values(by = ['name', 'weeks'])
    df.loc[df.Team == 'LAR', 'Team'] = 'STL' #moved to LA in 2016
    df.loc[df.Oppt == 'LAR', 'Oppt'] = 'STL'

    #All box score player stats, except defensive statistics
    stats = ['pass.att', 'pass.comp', 'passyds', 'pass.tds', 'pass.ints',
             'pass.twopta', 'pass.twoptm', 'rush.att', 'rushyds', 'rushtds',
             'rushlng', 'rushlngtd', 'rush.twopta', 'rush.twoptm', 'recept',
             'recyds', 'rec.tds', 'reclng', 'reclngtd', 'rec.twopta',
             'rec.twoptm', 'kick.rets', 'kickret.avg', 'kickret.tds',
             'kick.ret.lng', 'kickret.lngtd', 'punt.rets', 'puntret.avg',
             'puntret.tds', 'puntret.lng', 'puntret.lngtd', 'fgm', 'fga',
             'fgyds', 'totpts.fg', 'xpmade','xpmissed','xpa','xpb','xppts.tot',
             'totalfumbs', 'fumbyds','fumbslost']
    #Game Characteristic Indicators (home/away, opponent, team)
    df, game_features = get_game_char_indicators(df)
    #Player Statistic Features (Season, last 4 weeks, previous week)
    df, player_features = get_player_averages(df, stats)

    #Combine features and return complete df and feature names
    features = game_features + player_features
    df = df.fillna(0)
    return df, features


def get_game_char_indicators(df):
    """Adds game indicator variables returns column names"""
    df['home'] = 1 * df['h/a'] == 'h'
    oppts = pd.get_dummies(df['Oppt'], prefix='Oppt')
    team = pd.get_dummies(df['Team'])
    df = pd.concat([df, oppts, team], axis=1)
    #return list of feature column names
    return df, ['home'] + list(oppts.columns) + list(team.columns)


def get_player_averages(df, stats):
    """Adds player averages for all stats and FanDuel point histories,
       for season-to-date, last 4 weeeks, and previous week"""
    feature_names = []
    for stat in df[stats + ['FD points']]:
        df['season_{}'.format(stat)] = df.groupby('name')[stat].apply(lambda x: rolling_average(x, 16))
        df['recent_{}'.format(stat)] = df.groupby('name')[stat].apply(lambda x: rolling_average(x, 4))
        df['prev_{}'.format(stat)] = df.groupby('name')[stat].apply(lambda x: rolling_average(x, 1))
        feature_names = feature_names + [time + "_" + stat for time in ['season', 'recent', 'prev']]
    return df, feature_names


def rolling_average(df, window):
    return df.rolling(min_periods=1, window=window).mean().shift(1)

#########################
#### main ###############
#########################
target = 'FD points'
try:
    train = pd.read_pickle('train_df.p')
    features = pickle.load(open('train_features.p', 'rb'))
except:
    train, features = data_processing('aggregated_2015.csv')
    pickle.dump(train, open('train_df.p', 'wb'))
    pickle.dump(features, open('train_features.p', 'wb'))

try:
    test = pd.read_pickle('test_df.p')
    features2 = pickle.load(open('test_features.p', 'rb'))
except:
    test, features2 = data_processing('aggregated_2016.csv')
    pickle.dump(test, open('test_df.p', 'wb'))
    pickle.dump(features2, open('test_features.p', 'wb'))

print sorted(train['Team'].unique())
print sorted(test['Team'].unique())
print sorted(train['Oppt'].unique())
print sorted(test['Oppt'].unique())

if(features != features2):
    print "Debug error about feature inconsistency"
    exit()

# Data initialization
positions = sorted(train['Pos'].unique())
estimators = [Ridge()]#,
              #BayesianRidge(compute_score = True),
              #ElasticNet(),
              #RandomForestRegressor(random_state = 0),
              #GradientBoostingRegressor()]
est_names = [str(est)[:str(est).find("(")] for est in estimators]
rmse_types = ['train', 'cv', 'test']
rmse_names = [x + '_' + y for y in rmse_types for x in est_names]
df_rmse = pd.DataFrame([['.' for i in range(len(positions))] for j in range(len(rmse_names))], index = rmse_names, columns = positions)

# Loop for all positions
for position in positions:
    print ('Learning %s ...' % [position])
    df_pos_train = train[train['Pos'] == position]
    df_pos_test = test[test['Pos'] == position]

    for i in range(len(estimators)):
        est = estimators[i]
        fit = est.fit(df_pos_train[features],df_pos_train[target])
        train_rmse = np.sqrt(mean_squared_error(df_pos_train[target], fit.predict(df_pos_train[features])))
        shuffle = ShuffleSplit(n_splits = 10, test_size = 0.2)
        cv_rmse = np.sqrt(abs(cross_val_score(fit, train[features], train[target], cv = shuffle, scoring = 'neg_mean_squared_error').mean()))
        test_rmse = np.sqrt(mean_squared_error(df_pos_test[target], fit.predict(df_pos_test[features])))

        for score_type in rmse_types:
            df_rmse.loc[est_names[i] + "_" + score_type, position] = eval(score_type + '_rmse')

        pickle.dump(fit, open(est_names[i] + "_" + position, 'wb'))

df_rmse.to_csv('rmse.csv', header = True, index=True)


'''
MSE of FD_2016_Projections.csv (Fantasydata.com)
'''

test['diff'] = (test['proj'] - test['FD points']) ** 2.0
FantasyData_rmse = (test.groupby(['Pos'])['diff'].mean()) ** 0.5
FantasyData_rmse.to_csv('FantasyData_rmse.csv', header = True, index = True)

print "Program finished normally"


################################
###Other Optional Processes####
################################
'''
Preprocessing using zero mean and unit variance scaling
'''
# scaler = StandardScaler().fit(train[features])
# X_trained_scaled = scaler.transform(train[features])
# X_test_scaled = scaler.transform(test[features])
# train.loc[:,features] = X_trained_scaled
# test.loc[:,features] = X_test_scaled


'''
Machine Learning Algorithm
'''

# Gradient Boosting Regressor with Grid Search for hyperparameters
#n_estimators = [50]
#learning_rate = [0.3]
#param_grid = {'n_estimators': n_estimators, 'learning_rate': learning_rate}
#grid_search = GridSearchCV(GradientBoostingRegressor(), param_grid, cv=5)
#grid_search.fit(df_pos_train[features], df_pos_train[target])
#n_estimators = grid_search.best_params_['n_estimators']
#learning_rate = grid_search.best_params_['learning_rate']

# # Multilayer Perceptrons (neural networks) with Gride Search for hyperparameters
# n_layer_nodes = 100
# alpha = [0.0001, 0.01, 0.1, 1]
# param_grid = {'alpha':alpha}
# grid_search = GridSearchCV(MLPRegressor(hidden_layer_sizes=[n_layer_nodes,n_layer_nodes],max_iter=200000), param_grid, cv=5)
# grid_search.fit(df_pos_train[features],df_pos_train[target])
# alpha = grid_search.best_params_['alpha']
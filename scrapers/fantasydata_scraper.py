from bs4 import BeautifulSoup
import pandas as pd
import urllib2
import sys
import numpy as np

def getSeasonProjections(season, weeks):
    df = pd.DataFrame()
    year = 2016 - season
    for week in weeks:
        for position in range(1, 7):
            stats = parseNFLFantasy(year, week, position)
            df2 = getDataFromStatsGrid(stats, position)
            df = df.append(df2, ignore_index=True)
    return df

def parseNFLFantasy(year, week, position):
    url = "https://fantasydata.com/nfl-stats/fantasy-football-weekly-projections.aspx?fs=0&stype=0&sn=%d&scope=1&w=%d&ew=%d&s=&t=0&p=%d&st=FantasyPoints&d=1&ls=&live=false&pid=false&minsnaps=4"
    url %= (year, week, week, position)
    req = urllib2.Request(url, headers={"User-Agent": "Resistance is futile"})
    response = urllib2.urlopen(req)
    html = BeautifulSoup(response, "html.parser")
    stats = html.find(id = "StatsGrid")
    return stats

def getDataFromStatsGrid(stats, position):
    if position in [2, 3]:
        max_row = 51 #top 50 RB and WR
    else: 
        max_row = 33 #top 32 QB, TE, K, DEF (starters for each team)
    df = pd.DataFrame()
    df['player'] = pd.Series([row.findAll('td')[1].text.encode('ascii', 'ignore') for row in stats.findAll('tr')[1:max_row]])
    df['team'] = pd.Series([row.findAll('td')[4].text.encode('ascii', 'ignore') for row in stats.findAll('tr')[1:max_row]])
    df['position'] = pd.Series([row.findAll('td')[2].text.encode('ascii', 'ignore') for row in stats.findAll('tr')[1:max_row]])
    df['week'] = pd.Series([row.findAll('td')[3].text.encode('ascii', 'ignore') for row in stats.findAll('tr')[1:max_row]])
    df['proj'] = pd.Series([row.findAll('td')[-1].text.encode('ascii', 'ignore') for row in stats.findAll('tr')[1:max_row]])
    return df

#######
#main#
######
season = int(sys.argv[1])
min_wk = int(sys.argv[2])
max_wk = int(sys.argv[3])
weeks = np.arange(min_wk - 1, max_wk)
df = getSeasonProjections(season, weeks)    
df.to_csv('FD_%d_Projections' % season, index = False)

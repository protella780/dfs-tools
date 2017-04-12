import pandas as pd
import urllib2
from bs4 import BeautifulSoup
import re
import numpy as np

def get_FD_season_results(season, weeks):
    """ Takes a season (2011 - 2016) and list of weeks (1 - 17) and returns dataframe of 
        all FD results for each week in that season
    """
    base_URL = "http://rotoguru1.com/cgi-bin/fyday.pl?week=%d&year=%d&game=fd&scsv=1"
    df = pd.DataFrame()
    for week in weeks:
        URL = base_URL % (week, season)
        response = urllib2.urlopen(URL)
        html = BeautifulSoup(response.read(), 'html.parser')
        stats = [line.split(';') for line in html.find_all('pre')[0].text.splitlines()]
        df = df.append(pd.DataFrame(stats[1:], columns = stats[0]))
    return df


FD_results_2015 = get_FD_season_results(2015, np.arange(1, 18))
FD_results_2015.to_csv('2015_Fanduel_Results.csv', index = False)

FD_results_2016 = get_FD_season_results(2016, np.arange(1, 13))
FD_results_2016.to_csv('2016_Fanduel_Results.csv', index = False)




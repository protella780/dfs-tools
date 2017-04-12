from selenium import webdriver
from Tkinter import *
import time
import numpy as np
import re

YAHOO_TEAMS = ['Ari', 'Atl', 'Bal', 'Buf', 'Car', 'Chi', 'Cin', 'Cle', 'Dal',
               'Den', 'Det', 'GB', 'Hou', 'Ind', 'Jax', 'KC', 'LA', 'Mia',
               'Min', 'NE', 'NO', 'NYG', 'NYJ', 'Oak', 'Phi', 'Pit', 'SD',
               'Sea', 'SF', 'TB', 'Ten', 'Was']
STANDARD_TEAMS = ['ARI', 'ATL', 'BAL', 'BUF', 'CAR', 'CHI', 'CIN', 'CLE', 'DAL', 'DEN', 'DET', 'GB', 'HOU', 'IND', 'JAC', 'KAN', 'LA', 'MIA', 'MIN', 'NE', 'NO', 'NYG', 'NYJ', 'OAK', 'PHI', 'PIT', 'SDG', 'SEA', 'SFO', 'TAM', 'TEN', 'WAS']
TEAMS_INDEX = dict(zip(YAHOO_TEAMS, STANDARD_TEAMS))

def get_projections_dict():
    """ main method call, returning a dictionary of Yahoo player projections"""
    USERNAME, PASSWORD, LEAGUEID, WEEK = get_parameters()
    return get_yahoo_data(WEEK, LEAGUEID, USERNAME, PASSWORD)

def get_parameters():
    """ gets username, password, leagueID, and week parameters from user"""
    master = Tk()
    master.wm_title("Parameters for Yahoo Projections")
    Label(master, text="Username").grid(row=0)
    Label(master, text="Password").grid(row=1)
    Label(master, text="League ID").grid(row=2)
    Label(master, text="Week for Projections").grid(row=3)
    user = Entry(master)
    password = Entry(master, show="*")
    league = Entry(master)
    week = Entry(master)
    user.grid(row=0, column=1)
    password.grid(row=1, column=1)
    league.grid(row=2, column=1)
    week.grid(row=3, column=1)
    Button(master, text='Submit', command=master.quit).grid(row=5, column=0,
                                                           sticky=W, pady=4)
    mainloop()
    u = user.get()
    p = password.get()
    l = league.get()
    w = week.get()
    master.destroy()
    return u, p, l, w


def get_yahoo_data(week, leagueid, username, password):
    """ logs in to Yahoo league, scrapes player projection data,
        cleans it, and returns a dictionary of player:points for FanDuel scoring"""
    driver = webdriver.Chrome()
    login_to_yahoo(driver, leagueid, username, password)
    time.sleep(1)
    positions = ['O', 'K', 'DEF']
    player_dict = {}
    for position in positions:
        player_data = get_positional_data(driver, position, leagueid, week)
        player_dict.update(get_player_dict(player_data, position))
    return player_dict


def login_to_yahoo(driver, leagueid, username, password):
    """# logs in to Yahoo league, with delay between entering username and password"""
    driver.get('https://football.fantasysports.yahoo.com/f1/%s/players' % leagueid)
    driver.find_element_by_id('login-username').send_keys(username)
    driver.find_element_by_id('login-signin').click()
    time.sleep(1)
    driver.find_element_by_xpath("//*[@id='login-passwd']").send_keys(password)
    driver.find_element_by_id('login-signin').click()


def get_positional_data(driver, position, leagueid, week):
    """ scrapes and returns player data, for top 250 offensive players and all
        kickers and defenses """
    base_url = "https://football.fantasysports.yahoo.com/f1/%s/players?&sort=PR&sdir=1&status=ALL&pos=%s&stat1=S_PW_%s&count=%s"
    if position == "O":
        pages = 10
    else:
        pages = 2
    data = []
    count = 0
    for i in range(pages):
        url = base_url % (leagueid, position, week, count)
        driver.get(url)
        table = driver.find_elements_by_tag_name("tbody")[1]
        for row in table.find_elements_by_tag_name("tr"):
            data.append([re.sub('Video Playlist\n', '',
                                col.text.encode('ascii', errors='ignore'))
                         for col in row.find_elements_by_tag_name("td")])
        count += 25
    return data


def get_player_dict(player_data, position):
    """creates lists of players and projected FD points, and returns dictionary"""
    players = [std_player(player[1]) for player in player_data]
    proj_FD_pts = [get_FD_pts(player, position) for player in player_data]
    return dict(zip(players, proj_FD_pts))


def std_player(player):
    """returns standard (name, team, position) player identifier from
       Yahoo players column info"""
    player = player[:player.index('\n')].split()
    position = player[-1]
    team = TEAMS_INDEX[player[-3]]
    if position == "DEF":
        position = "D"
        name = team + " " + "Defense"
    else:
        name = re.sub("[.']| [JjSs][Rr]$| III$","", " ".join(player[:-3]))
    return (name, team, position)


def get_FD_pts(player, position):
    """Calculates FD points based on Yahoo player stats"""
    if position == "O":
        proj_stats = np.array(player[-14:][:-1]).astype(float)
        stat_vals = np.array([.04, 4, -1, 0, .1, 6, 0, 0, .1, 6, 6, 2, -2])
    elif position == "K":
        proj_stats = np.array(player[-7:][:-1]).astype(float)
        stat_vals = np.array([3, 3, 3, 4, 5, 1])
    elif position == "DEF":
        proj_stats = np.array(player[-9:][:-1]).astype(float)
        stat_vals = np.array([pts_against_val(proj_stats[0]), 1, 2, 2, 2, 6, 2, 6])
    else:
        print "Invalid position. get_FD_pts() only accepts 'O', 'K', or 'DEF'"
        return
    return round(sum(proj_stats * stat_vals), 2)


def pts_against_val(pts):
    """Returns defense 'points against' FD score value"""
    if pts < 1:
        return 0
    if pts < 6.5:
        return 7
    if pts < 13.5:
        return 4
    if pts < 20.5:
        return 1
    if pts < 34.5:
        return -1
    return -4
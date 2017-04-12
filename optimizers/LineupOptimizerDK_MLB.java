/*
 * LineupOptimizerDK_MLB.java
 * by: Philip Rotella
 * Date: 2017-04-013
 * 
 * Given a csv file of MLB fantasy player data (4 columns: player name,
 * position, projected fantasy points, cost), uses recursive backtracking to 
 * find the top X rosters to maximize projected fantasy points. User modifies
 * main method to specify X and the filename). Positions must be one of the 
 * following: P, OF, 1B, 2B, 3B, SS, C.
 * 
 */

import java.io.*;
import java.util.*;

public class LineupOptimizerDK_MLB {

    ///Standard FanDuel Settings///

    public static final String[] POSITIONS =
            {"P", "P", "OF", "OF", "OF", "1B", "2B", "3B", "SS", "C"};
    public static final int LINEUP_SIZE = POSITIONS.length;
    public static final int SALARY = 50000;

    ///Variables for use in lineup optimizer
    public static final Player[] LINEUP = new Player[LINEUP_SIZE];
    public static final ArrayList<Roster> RESULTS = new ArrayList<Roster>();
    public static final ArrayList<Player> LIST_P = new ArrayList<Player>(),
            LIST_OF = new ArrayList<Player>(),
            LIST_1B = new ArrayList<Player>(),
            LIST_2B = new ArrayList<Player>(),
            LIST_3B = new ArrayList<Player>(),
            LIST_SS = new ArrayList<Player>(),
            LIST_C = new ArrayList<Player>();
    public static final ArrayList<ArrayList<Player>> ALL_PLAYERS =
            new ArrayList<ArrayList<Player>>();

    public static int MIN_P_SAL = SALARY,
            MIN_OF_SAL = SALARY,
            MIN_1B_SAL = SALARY,
            MIN_2B_SAL = SALARY,
            MIN_3B_SAL = SALARY,
            MIN_SS_SAL = SALARY,
            MIN_C_SAL = SALARY;
    public static final ArrayList<Integer> MIN_SALARIES =
            new ArrayList<Integer>();

    ////////////////////MAIN////////////////////////////////
    public static void main(String[] args)
            throws FileNotFoundException {

        //define file and specifications     
        Scanner input = new Scanner(new File("/tmp/test1.csv"));
        int rank = 15;

        //build lists for lineup optimizer
        buildPlayerPool(input);
        setALL_PLAYERS();

        //run optimizer and print final results and execution time
        double startTime = System.nanoTime();

        int salary = SALARY;
        int i = 0;
        int nFilled = 0;
        int points = 0;

        setRESULTS(rank);
        findRosters(salary, i, nFilled, points);
        System.out.println(RESULTS);

        double elapsedTime = System.nanoTime() - startTime;
        System.out.printf("%.10f seconds to execute\n", elapsedTime / 1000000000);

    }

    /*
     * setALL_PLAYERS - adds positional ArrayLists to ALL_PLAYERS
     */
    public static void setALL_PLAYERS() {
        Collections.sort(LIST_P, Collections.reverseOrder());
        Collections.sort(LIST_OF, Collections.reverseOrder());
        Collections.sort(LIST_1B, Collections.reverseOrder());
        Collections.sort(LIST_2B, Collections.reverseOrder());
        Collections.sort(LIST_3B, Collections.reverseOrder());
        Collections.sort(LIST_SS, Collections.reverseOrder());
        Collections.sort(LIST_C, Collections.reverseOrder());
        for (String position : POSITIONS) {
            if (position.equals("P")) {
                ALL_PLAYERS.add(LIST_P);
                MIN_SALARIES.add(MIN_P_SAL);
            } else if (position.equals("OF")) {
                ALL_PLAYERS.add(LIST_OF);
                MIN_SALARIES.add(MIN_OF_SAL);
            } else if (position.equals("1B")) {
                ALL_PLAYERS.add(LIST_1B);
                MIN_SALARIES.add(MIN_1B_SAL);
            } else if (position.equals("2B")) {
                ALL_PLAYERS.add(LIST_2B);
                MIN_SALARIES.add(MIN_2B_SAL);
            } else if (position.equals("3B")) {
                ALL_PLAYERS.add(LIST_3B);
                MIN_SALARIES.add(MIN_3B_SAL);
            } else if (position.equals("SS")) {
                ALL_PLAYERS.add(LIST_SS);
                MIN_SALARIES.add(MIN_SS_SAL);
            } else {
                ALL_PLAYERS.add(LIST_C);
                MIN_SALARIES.add(MIN_C_SAL);
            }
        }
    }

    /*
     * setRESULTS - creates default Player objects to build a default
     * roster, and populates RESULTS with default rosters
     */
    public static void setRESULTS(int rank) {
        for (int i = 0; i < rank; i++) {
            if (i > RESULTS.size() - 1) {
                RESULTS.add(new Roster(null, 0));
            } else {
                RESULTS.set(i, new Roster(null, 0));
            }
        }
    }

    /////////////////Player inner class//////////////////////
    public static class Player implements Comparable<Player> {
        public String name;
        public double points;
        public int cost;
        public String position;

        public Player(String name, double points, int cost, String position) {
            this.name = name;
            this.points = points;
            this.cost = cost;
            this.position = position;
        }

        public String toString() {
            return this.name + "(" + this.position + ")";
        }


        public int compareTo(Player other) {
            double diff = this.points - other.points;
            double costdiff = this.cost - other.cost;
            if (diff > 1e-6)
                return 1;
            else if (diff < -1e-6)
                return -1;
            else if (costdiff < -1e-6)
                return 1;
            else if (costdiff > -1e-6)
                return -1;
            else
                return 0;
        }
    }

    ///////////////Roster inner class////////////////////////
    //Note: this class has a natural ordering that is inconsistent with equals//

    public static class Roster implements Comparable<Roster> {
        public Player[] players;
        public double points;

        public Roster(Player[] players, double points) {
            this.players = players;
            this.points = points;
        }

        public String toString() {
            return Arrays.toString(this.players) + "(" + points + " points)\n";
        }

        public int compareTo(Roster other) {
            double diff = this.points - other.points;
            if (diff > 1e-6)
                return 1;
            else if (diff < -1e-6)
                return -1;
            else
                return 0;
        }
    }

    /////////buildPlayerPool() method////////////////////
    public static void buildPlayerPool(Scanner input)
            throws FileNotFoundException {
        while (input.hasNextLine()) {

            String[] line = input.nextLine().split(",");

            String name = line[0];
            String position = line[1];
            double points = Double.parseDouble(line[2]);
            int cost = Integer.parseInt(line[3]);

            Player p = new Player(name, points, cost, position);

            if (position.equals("P")) {
                if (p.cost < MIN_P_SAL) MIN_P_SAL = p.cost;
                LIST_P.add(p);
            } else if (position.equals("1B")) {
                if (p.cost < MIN_1B_SAL) MIN_1B_SAL = p.cost;
                LIST_1B.add(p);
            } else if (position.equals("OF")) {
                if (p.cost < MIN_OF_SAL) MIN_OF_SAL = p.cost;
                LIST_OF.add(p);
            } else if (position.equals("2B")) {
                if (p.cost < MIN_2B_SAL) MIN_2B_SAL = p.cost;
                LIST_2B.add(p);
            } else if (position.equals("3B")) {
                if (p.cost < MIN_3B_SAL) MIN_3B_SAL = p.cost;
                LIST_3B.add(p);
            } else if (position.equals("SS")) {
                if (p.cost < MIN_SS_SAL) MIN_SS_SAL = p.cost;
                LIST_SS.add(p);
            } else {
                if (p.cost < MIN_C_SAL) MIN_C_SAL = p.cost;
                LIST_C.add(p);
            }
        }
    }


    //////////////Recursive backtracking////////////////////////
    public static void findRosters(int salary, int i,
                                   int nFilled, double points) {
        //check if lineup is full, and whether it represents a top lineup
        if (nFilled == LINEUP_SIZE) {
            if (points <= RESULTS.get(0).points) {
                return;
            }
            RESULTS.set(0, new Roster(Arrays.copyOf(LINEUP, LINEUP_SIZE),
                    points));
            Collections.sort(RESULTS);
            return;            // search for higher scoring lineups
        }

        //set player list to correct position and try next player
        List<Player> list = ALL_PLAYERS.get(nFilled);
        for (int listIndex = i; listIndex < list.size() &&
                list.get(listIndex) != null; listIndex++) {

            Player p = list.get(listIndex);

            if (p.cost <= salary && !duplicatePlayer(p.name)) {

                double value = p.points;

                //check whether it is still possible to have a top lineup or 
                //legal-salary lineup
                if (targetPointsPossible(points + value, nFilled + 1,
                        listIndex, salary - p.cost)) {

                    LINEUP[nFilled] = p;

                    //recursive call to fill next position
                    if (nFilled < LINEUP_SIZE - 1 &&
                            ALL_PLAYERS.get(nFilled + 1) == list) {
                        findRosters(salary - p.cost, listIndex + 1,
                                nFilled + 1, points + value);
                    } else {
                        findRosters(salary - p.cost, 0,
                                nFilled + 1, points + value);
                    }

                    LINEUP[nFilled] = null;
                }
            }
        }
    }

    /*
     * targetPointsPossible - returns true if it's still possible to get 
     * target points based on current lineup and player selection
     */
    public static boolean targetPointsPossible(double maxLeft, int nextPos,
                                               int listIndex, int salary) {

        if (nextPos < LINEUP_SIZE &&
                POSITIONS[nextPos].equals(POSITIONS[nextPos - 1])) listIndex++;
        else listIndex = 0;

        while (nextPos < LINEUP_SIZE &&
                listIndex < ALL_PLAYERS.get(nextPos).size()) {
            maxLeft += ALL_PLAYERS.get(nextPos).get(listIndex).points;
            salary -= MIN_SALARIES.get(nextPos);
            nextPos++;
            if (nextPos < LINEUP_SIZE &&
                    POSITIONS[nextPos].equals(POSITIONS[nextPos - 1])) listIndex++;
            else listIndex = 0;
        }

        return (maxLeft >= RESULTS.get(0).points && salary >= 0);
    }

    /*
     * duplicatePlayer - returns true if player name is already in lineup
     */
    public static boolean duplicatePlayer(String name) {
        for (Player player : LINEUP) {
            if (player != null && player.name.equals(name)) {
                return true;
            }
        }
        return false;
    }
}


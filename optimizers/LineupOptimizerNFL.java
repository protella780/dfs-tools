/*
 * LineupOptimizerNFL.java
 * by: Philip Rotella
 * Date: 16 Sep 15
 * 
 * Given a csv file of NFL fantasy player data (4 columns: player name,
 * position, projected fantasy points, cost), uses recursive backtracking to 
 * find the top X rosters to maximize projected fantasy points. User modifies
 * main method to specify X and the filename). Positions must be one of the 
 * following: QB, RB, WR, TE, K, D. File needs to be presorted from high to low
 * fantasy points.
 * 
 */

import java.io.*;
import java.util.*;

public class LineupOptimizerNFL {
    
    ///Standard FanDuel Settings///
  
    public static final String[] POSITIONS = 
    {"RB", "RB", "WR", "WR", "WR", "QB", "D", "TE", "K"}; 
    public static final int LINEUP_SIZE = POSITIONS.length;
    public static final int SALARY = 60000 ;  
   
    ///Variables for use in lineup optimizer
    public static final Player[] LINEUP = new Player[LINEUP_SIZE];
    public static final ArrayList<Roster> RESULTS = new ArrayList<Roster>();
    public static final ArrayList<Player> LIST_QB = new ArrayList<Player>(),
        LIST_RB = new ArrayList<Player>(),
        LIST_WR = new ArrayList<Player>(), 
        LIST_TE = new ArrayList<Player>(), 
        LIST_K = new ArrayList<Player>(), 
        LIST_D = new ArrayList<Player>();
    public static final ArrayList<ArrayList<Player>> ALL_PLAYERS = 
        new ArrayList<ArrayList<Player>>(); 
    
    public static int MIN_QB_SAL = SALARY, 
        MIN_WR_SAL = SALARY, 
        MIN_RB_SAL = SALARY, 
        MIN_TE_SAL = SALARY, 
        MIN_K_SAL = SALARY, 
        MIN_D_SAL = SALARY; 
    public static final ArrayList<Integer> MIN_SALARIES = 
        new ArrayList<Integer>();

    ////////////////////MAIN////////////////////////////////
    public static void main(String[] args) 
        throws FileNotFoundException {

        //define file and specifications     
        Scanner input = new Scanner(new File("test.csv"));      
        int rank = 50;
        
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
        System.out.printf("%.10f seconds to execute\n", elapsedTime/1000000000);

    }
    /*
     * setALL_PLAYERS - adds positional ArrayLists to ALL_PLAYERS
     */
    public static void setALL_PLAYERS() {
        for (String position : POSITIONS) {                       
            if (position.equals("QB")) {
                ALL_PLAYERS.add(LIST_QB);
                MIN_SALARIES.add(MIN_QB_SAL);
            }  
            else if (position.equals("WR")) {
                ALL_PLAYERS.add(LIST_WR);
                MIN_SALARIES.add(MIN_WR_SAL);
            }
            else if (position.equals("RB")) {
                ALL_PLAYERS.add(LIST_RB);
                MIN_SALARIES.add(MIN_RB_SAL);
            }
            else if (position.equals("TE")) {
                ALL_PLAYERS.add(LIST_TE);
                MIN_SALARIES.add(MIN_TE_SAL);
            }
            else if (position.equals("K")) {
                ALL_PLAYERS.add(LIST_K);
                MIN_SALARIES.add(MIN_K_SAL);
            }
            else {
                ALL_PLAYERS.add(LIST_D);
                MIN_SALARIES.add(MIN_D_SAL);
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
            }
            else {
                RESULTS.set(i, new Roster(null, 0));
            }
        }
    }
    /////////////////Player inner class//////////////////////
    public static class Player {
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
    }
    
    ///////////////Roster inner class////////////////////////
    //Note: this class has a natural ordering that is inconsistent with equals//
    
    public static class Roster implements Comparable<Roster>{
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
        throws FileNotFoundException{
        while (input.hasNextLine()) {
            
            String[] line = input.nextLine().split(",");
            
            String name = line[0];
            String position = line[1];
            double points = Double.parseDouble(line[2]);
            int cost = Integer.parseInt(line[3]);           
            
            Player p = new Player(name, points, cost, position);
            
            if (position.equals("QB")) {
                if (p.cost < MIN_QB_SAL) MIN_QB_SAL = p.cost;
                LIST_QB.add(p);
            } else if (position.equals("RB")) {
                if (p.cost < MIN_RB_SAL) MIN_RB_SAL = p.cost;
                LIST_RB.add(p);
            } else if (position.equals("WR")) {
                if (p.cost < MIN_WR_SAL) MIN_WR_SAL = p.cost;
                LIST_WR.add(p);
            } else if (position.equals("TE")) {
                if (p.cost < MIN_TE_SAL) MIN_TE_SAL = p.cost;
                LIST_TE.add(p);
            } else if (position.equals("K")) {
                if (p.cost < MIN_K_SAL) MIN_K_SAL = p.cost;
                LIST_K.add(p);
            } else {
                if (p.cost < MIN_D_SAL) MIN_D_SAL = p.cost;
                LIST_D.add(p);
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
            
            if (p.cost <= salary) {                                                      

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
                    }
                    else {
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
}

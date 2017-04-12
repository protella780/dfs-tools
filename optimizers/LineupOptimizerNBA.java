/*
 * LineupOptimizerNBA.java
 * by: Philip Rotella
 * Date: 16 Sep 15
 * 
 * Given a csv file of NBA fantasy player data (4 columns: player name,
 * position, projected fantasy points, cost), uses recursive backtracking to 
 * find the top X rosters to maximize projected fantasy points. User modifies
 * main method to specify X and the filename). Positions must be one of the 
 * following: QB, PG, SG, SF, PF, D. File needs to be presorted from high to low
 * fantasy points.
 * 
 */

import java.io.*;
import java.util.*;

public class LineupOptimizerNBA {
    
    ///Standard FanDuel Settings///
  
    public static final String[] POSITIONS = 
    {"PG", "PG", "SG", "SG", "SF", "SF", "PF", "PF", "C"}; 
    public static final int LINEUP_SIZE = POSITIONS.length;
    public static final int SALARY = 60000 ;  
   
    ///Variables for use in lineup optimizer
    public static final Player[] LINEUP = new Player[LINEUP_SIZE];
    public static final ArrayList<Roster> RESULTS = new ArrayList<Roster>();
    public static final ArrayList<Player> 
        LIST_PG = new ArrayList<Player>(),
        LIST_SG = new ArrayList<Player>(), 
        LIST_SF = new ArrayList<Player>(), 
        LIST_PF = new ArrayList<Player>(), 
        LIST_C = new ArrayList<Player>();
    public static final ArrayList<ArrayList<Player>> ALL_PLAYERS = 
        new ArrayList<ArrayList<Player>>(); 
    
    public static int  
        MIN_SG_SAL = SALARY, 
        MIN_PG_SAL = SALARY, 
        MIN_SF_SAL = SALARY, 
        MIN_PF_SAL = SALARY, 
        MIN_C_SAL = SALARY; 
    public static final ArrayList<Integer> MIN_SALARIES = 
        new ArrayList<Integer>();

    ////////////////////MAIN////////////////////////////////
    public static void main(String[] args) 
        throws FileNotFoundException {

        //define file and specifications     
        Scanner input = new Scanner(new File("test.csv"));      
        int rank = 25;
        
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
            if (position.equals("SG")) {
                ALL_PLAYERS.add(LIST_SG);
                MIN_SALARIES.add(MIN_SG_SAL);
            }
            else if (position.equals("PG")) {
                ALL_PLAYERS.add(LIST_PG);
                MIN_SALARIES.add(MIN_PG_SAL);
            }
            else if (position.equals("SF")) {
                ALL_PLAYERS.add(LIST_SF);
                MIN_SALARIES.add(MIN_SF_SAL);
            }
            else if (position.equals("PF")) {
                ALL_PLAYERS.add(LIST_PF);
                MIN_SALARIES.add(MIN_PF_SAL);
            }
            else {
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
            }
            else {
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
        public String team;
        
        public Player(String name, double points, int cost, String position, String team) {
            this.name = name;
            this.points = points;
            this.cost = cost;
            this.position = position;
            this.team = team;
        }
            
        public String toString() {
            return this.name + "(" + this.position + ")";
        }
        
        public int compareTo(Player other) {
            double diff = this.points - other.points;
            if (diff > 1e-6) {
                return 1;
            }
            else if (diff < -1e-6) {
                return -1;
            }
            else {
                return 0;
            }
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
            String team = line[4];
            
            Player p = new Player(name, points, cost, position, team);
            
            if (position.equals("PG")) {
                if (p.cost < MIN_PG_SAL) MIN_PG_SAL = p.cost;
                LIST_PG.add(p);
            } else if (position.equals("SG")) {
                if (p.cost < MIN_SG_SAL) MIN_SG_SAL = p.cost;
                LIST_SG.add(p);
            } else if (position.equals("SF")) {
                if (p.cost < MIN_SF_SAL) MIN_SF_SAL = p.cost;
                LIST_SF.add(p);
            } else if (position.equals("PF")) {
                if (p.cost < MIN_PF_SAL) MIN_PF_SAL = p.cost;
                LIST_PF.add(p);
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
            if (points <= RESULTS.get(0).points || 
                moreThanFour() || 
        !(LINEUP[8].name.equals("Greg Monroe"))  ) {
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
    
    /*
     * moreThanFour - returns true if lineup has more than 4 players from same
     * team
     */
    public static boolean moreThanFour() {
        for (int i = 0; i < 5; i++) {
            int count = 0;
            for (int j = i; j < 9; j++) {
                if (LINEUP[i].team.equals(LINEUP[j].team)) {
                    count++;
                }
                if (count == 5) {
                    return true;
                }
            }
        }
        return false;
    }
}

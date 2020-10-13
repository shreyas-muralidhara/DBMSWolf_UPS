/* Method to initialize the connection variables*/
package wolfUPS.connection;

public class DbConnVariables {
    public static String getJDBCUrl(){
        return "jdbc:oracle:thin:@orca.csc.ncsu.edu:1521:orcl01";
    }    
    public static String getUser(){
        return "schikkb";
    } 
    public static String getPassword(){
        return "200314024";
    }
}

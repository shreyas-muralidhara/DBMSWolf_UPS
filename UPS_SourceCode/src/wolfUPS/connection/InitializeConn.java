package wolfUPS.connection;

import java.sql.*;
import static java.lang.Class.forName;

public class InitializeConn {
    public static Connection InitConn() throws SQLException 
    {
        Connection connection = null;
        String jdbcUrl = DbConnVariables.getJDBCUrl();
        String user = DbConnVariables.getUser();
        String password = DbConnVariables.getPassword();
        
        try{
            Class.forName("oracle.jdbc.Driver.OracleDriver");
        }
        catch (Exception e) {
			System.out.println("Oracle Driver not available!!");
		}  

        System.out.println("Initizalizing the connection"); 

        try {
			connection = DriverManager.getConnection(jdbcUrl, user, password);
        }catch (SQLException ex) {
            System.out.println(ex.getMessage());
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        System.out.println("Connection: " + connection + " successfuluy established.");
        return connection;
    }
}

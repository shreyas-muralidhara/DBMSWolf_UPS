package WolfUPS.connection;

import java.sql.*;

public class InitializeConnection {
    public static Connection InitConn() throws SQLException {
        Connection connection = null;
        String jdbcUrl = ConnectionVariables.getJDBCUrl();
        String user = ConnectionVariables.getUser();
        String password = ConnectionVariables.getPassword();

        try {
            Class.forName("oracle.jdbc.OracleDriver");
        } catch (Exception ex) {
            ex.printStackTrace();
            System.out.println("\nOracle Driver not available!");
            System.exit(0);
        }

        System.out.println("Initizalizing the connection");

        try {
            connection = DriverManager.getConnection(jdbcUrl, user, password);
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
            System.exit(0);
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(0);
        }

        System.out.println("Connection: " + connection + " successfully established.");
        return connection;
    }

    public static void close(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (Throwable whatever) {
            }
        }
    }

    public static void close(Statement st) {
        if (st != null) {
            try {
                st.close();
            } catch (Throwable whatever) {
            }
        }
    }

    public static void close(ResultSet rs) {
        if (rs != null) {
            try {
                rs.close();
            } catch (Throwable whatever) {
            }
        }
    }

}

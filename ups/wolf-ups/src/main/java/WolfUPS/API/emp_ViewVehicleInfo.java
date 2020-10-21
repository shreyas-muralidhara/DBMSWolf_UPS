package WolfUPS.API;

import java.io.BufferedReader;
import java.io.IOException;
import java.sql.SQLException;
import WolfUPS.connection.InitializeConnection;

import java.sql.*;

public class emp_ViewVehicleInfo {

    public static void viewvehicleinfo (BufferedReader reader, Connection conn, String emp_id) throws NumberFormatException, IOException, SQLException {
        Statement st = conn.createStatement();
        ResultSet rs = null;
        ResultSetMetaData rsmd = null;
        Boolean tableEmpty = true;

        try {
            String sql = "SELECT Distinct V.LICENSEPLATE, V.MANUFACTURER, V.MODEL, V.YEAR, V.COLOR, V.PERMITNO FROM VEHICLE V, ASSIGNMULTIPLE AV where V.PERMITNO = AV.PERMITNO and AV.UNIVID = \'"+ emp_id + "\'";
            rs = st.executeQuery(sql);

            rsmd = rs.getMetaData();
            int columnsNumber = rsmd.getColumnCount();
            while (rs.next()) {
                tableEmpty = false;
                for (int i = 1; i <= columnsNumber; i++) {
                    if (i > 1)
                        System.out.print(",  ");
                    String columnValue = rs.getString(i);
                    System.out.print(rsmd.getColumnName(i) + ": "+ columnValue);
                }
                System.out.println("\n");
            }
            if (tableEmpty){
                System.out.println("\nThis student does not have a permit associated vehicle.");
            }
        } catch (SQLException e) {
            System.out
                    .println("Caught SQL Exception!" + e.getErrorCode() + "/" + e.getSQLState() + " " + e.getMessage());
            e.printStackTrace();
        } finally {
            InitializeConnection.close(rs);
            InitializeConnection.close(st);
            InitializeConnection.close(conn);
        }

    }
}

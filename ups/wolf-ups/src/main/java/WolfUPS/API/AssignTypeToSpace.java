package WolfUPS.API;

import java.io.BufferedReader;
import java.io.IOException;
import java.sql.SQLException;

import WolfUPS.connection.*;

import java.sql.*;

public class AssignTypeToSpace {
    public static void assigntypetospace(BufferedReader reader,Connection conn) throws NumberFormatException, IOException, SQLException{
        Statement st = conn.createStatement();
        StringBuilder sb = new StringBuilder();
        PreparedStatement ps = null;
        ResultSet rs = null;
        String Lot_name, space_type;
        Integer space_id;

        // Prompt to enter the parking lot ID.
        System.out.println("Please enter the parking lot ID");
        Lot_name = reader.readLine();
        //Prompt to enter the Space ID
        System.out.println("Please enter the space ID");
        space_id= Integer.parseInt(reader.readLine());
        //Promt to enter the space type
        System.out.println("Please enter the space type to be assigned");
        space_type = reader.readLine();
        //return;
        /* Insert the new Parking lot*/
        try {
            //Update the space type
            String sql = "UPDATE SPACE SET SPACETYPE = \'" + space_type + "\' WHERE LOTNAME = \'" + Lot_name + "\' AND SPACEID = " + space_id;
            ResultSet rs12 = st.executeQuery(sql);
            
            if (rs12.next()) {
                System.out.println("Space type updated successfully");
            } else {
                System.out.println("Space type update failed");
            }
        }
        catch (SQLException e){
            System.out.println("Caught SQL Exception!" + e.getErrorCode() + "/" + e.getSQLState() + " " + e.getMessage());
            e.printStackTrace();
            return;
        }

        finally {
            InitializeConnection.close(rs);;
            InitializeConnection.close(st);
            InitializeConnection.close(conn);;
        } 
    }
    
}

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
            /* disable the auto commit*/
            conn.setAutoCommit(false);
            /* Seting the transaction Managment variables to capture the failure*/
            boolean trans1 = false;

            //Update the space type
            String sql = "UPDATE SPACE SET SPACETYPE = \'" + space_type + "\' WHERE LOTNAME = \'" + Lot_name + "\' AND SPACEID = " + space_id;
            ResultSet rs12 = st.executeQuery(sql);
            
            if (rs12.next()) {
                System.out.println("Space type updated successfully");
                trans1 = true;
            } else {
                System.out.println("Space type update failed");
                trans1 = false;
            }

            /* Transaction management check*/
            if (trans1){
                conn.commit();
                System.out.println("Transaction Successful!");
            }
            else{
                conn.rollback();
                System.out.println("Transaction Failed");
            }
            conn.setAutoCommit(true);
        }
        catch (SQLException e){
            System.out.println("Caught SQL Exception!" + e.getErrorCode() + "/" + e.getSQLState() + " " + e.getMessage());
            e.printStackTrace();
            conn.rollback();
            return;
        }

        finally {
            if(conn!=null)
                conn.setAutoCommit(true);
            InitializeConnection.close(rs);;
            InitializeConnection.close(st);
            InitializeConnection.close(conn);;
        } 
    }
    
}

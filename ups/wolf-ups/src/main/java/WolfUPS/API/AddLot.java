package WolfUPS.API;

import java.io.BufferedReader;
import java.io.IOException;
import java.sql.SQLException;

import WolfUPS.connection.*;

import java.sql.*;

public class AddLot {
    public static void addlot(BufferedReader reader,Connection conn) throws NumberFormatException, IOException, SQLException{
        Statement st = conn.createStatement();
        StringBuilder sb = new StringBuilder();
        PreparedStatement ps = null;
        ResultSet rs = null;
        String Lot_name, lot_addr, zone_desig;
        Integer startid, space_cnt;

        // Prompt admin to enter various details related to the lot.
        System.out.println("Please enter the parking lot ID");
        Lot_name = reader.readLine();
        System.out.println("Please enter the address of the lot");
        lot_addr= reader.readLine();
        System.out.println("Please enter the number of spaces in the lot");
        space_cnt = Integer.parseInt(reader.readLine());
        System.out.println("Please enter the beginning space number");
        startid = Integer.parseInt(reader.readLine());
        System.out.println("Please enter the initial zone designation");
        zone_desig = reader.readLine();
        
        /* extract only first 2 characters from the string */
        zone_desig = zone_desig.replaceAll("[^A-Za-z0-9]", "");
        zone_desig = zone_desig.length() < 2 ? zone_desig : zone_desig.substring(0, 2);
    
        
        /* Insert the new Parking lot*/
        try{
            /* disable the auto commit*/
            conn.setAutoCommit(false);
            /* Seting the transaction Managment variables to capture the failure*/
            boolean trans1 = false,trans2 = false,trans3 = false;

            try {
                String sql = "INSERT INTO PARKINGLOT VALUES(?, ?)";
                ps = conn.prepareStatement(sql);
                ps.setString(1, Lot_name);
                ps.setString(2, lot_addr);
                rs = ps.executeQuery();

                if (rs != null) {
                    System.out.println("Parking lot created successfully");
                    trans1 = true;
                } else {
                    System.out.println("Unable to add the Parking lot");
                    trans1 = false;
                }
                
            }
            catch (SQLException e){
                System.out.println("Caught SQL Exception!" + e.getErrorCode() + "/" + e.getSQLState() + " " + e.getMessage());
                e.printStackTrace();
                conn.rollback();
                trans1 = false;
                return;
            }


            /* Insert the Specified number of spaces into the parking lot*/
            try {
                Boolean flag = false;
                int id = 0;
                for(id = startid; id < (startid+space_cnt); id++)
                {
                    String sql = "INSERT INTO SPACE VALUES(?, ?, ?, ?, ?)";
                    ps = conn.prepareStatement(sql);
                    ps.setInt(1, id);
                    ps.setString(2, "regular");
                    ps.setString(3, Lot_name);
                    ps.setInt(4, 1);

                    if (zone_desig.charAt(0)=='V')
                        ps.setInt(5, 1);
                    else
                        ps.setInt(5, 0);
                    rs = ps.executeQuery();

                    if (rs == null) {
                        System.out.println("Unable to add the Space-" + id +" to the lot " + Lot_name);
                        flag = true;
                        trans2 = false;
                    }
                }
                if(!flag){
                    System.out.println("Spaces "+ startid + " - "+ id +" added to the lot.");
                    trans2 = true;
                }

            }       
            catch (SQLException e){
                System.out.println("Caught SQL Exception!" + e.getErrorCode() + "/" + e.getSQLState() + " " + e.getMessage());
                e.printStackTrace();
                conn.rollback();
                trans2 = false;
                return;
            }
            
            /* Allocate the zone to that lot*/
            try {
                
                String sql = "INSERT INTO REL_ALLOCATED VALUES(?, ?)";
                ps = conn.prepareStatement(sql);
                ps.setString(1, zone_desig);
                ps.setString(2, Lot_name);
                    
                rs = ps.executeQuery();

                if (rs != null) {
                    System.out.println("Allocated Zone to Parking lot successfully");
                    trans3 = true;
                }
                else{
                    System.out.println("Unable to allocate zone to the Parking lot");
                    trans3 = false;
                }
                
            }
            catch (SQLException e){
                System.out.println("Caught SQL Exception!" + e.getErrorCode() + "/" + e.getSQLState() + " " + e.getMessage());
                e.printStackTrace();
                conn.rollback();
                trans3 = false;
                return;
            }

            /* Transaction management check*/
            if (trans1 && trans2 && trans3){
                conn.commit();
                System.out.println("Transaction Successful!");
            }
            else{
                conn.rollback();
                System.out.println("Transaction Failed");
            }
            conn.setAutoCommit(true);
            
        }catch (SQLException e) {
            System.out.println("Caught SQL Exception!" + e.getErrorCode() + "/" + e.getSQLState() + " " + e.getMessage());
            e.printStackTrace();
            conn.rollback();
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

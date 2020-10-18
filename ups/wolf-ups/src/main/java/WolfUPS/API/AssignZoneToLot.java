package WolfUPS.API;

import java.io.BufferedReader;
import java.io.IOException;
import java.sql.SQLException;

import WolfUPS.connection.*;

import java.sql.*;

public class AssignZoneToLot {
    public static void assignzonetolot(BufferedReader reader,Connection conn) throws NumberFormatException, IOException, SQLException{
        Statement st = conn.createStatement();
        StringBuilder sb = new StringBuilder();
        PreparedStatement ps = null;
        ResultSet rs = null;
        String Lot_name, zone_desig;
        Integer space_cnt;

        // Prompt to enter the parking lot name.
        System.out.println("Please enter the parking lot name for which zone has to be allotted");
        Lot_name = reader.readLine();


        try{
            String parklot_check = "Select * from PARKINGLOT where NAME = \'" + Lot_name + "\'";
            ResultSet rs1 = st.executeQuery(parklot_check);
            

            if(!rs1.next()){
                
                System.out.println("Entered lot name is not a parking lot\n");
                return ;
            }
        }
        catch (SQLException e){
            System.out.println("Caught SQL Exception!" + e.getErrorCode() + "/" + e.getSQLState() + " " + e.getMessage());
            e.printStackTrace();
            return;
        }

        //Prompt to enter zone ID to be alloted
        System.out.println("Please enter the zone ID to be allotted");
        zone_desig = reader.readLine();
        
        
        
        /* extract only first 2 characters from the string */
        zone_desig = zone_desig.replaceAll("[^A-Za-z0-9]", "");
        zone_desig = zone_desig.length() < 2 ? zone_desig : zone_desig.substring(0, 2);
        
        try{
            /* disable the auto commit*/
            conn.setAutoCommit(false);
            /* Seting the transaction Managment variables to capture the failure*/
            boolean trans1 = false,trans2 = false,trans3 = false,trans4 = false;


            //Get the max SpaceID
            String space_id = "SELECT MAX(SPACEID) AS X from SPACE where LOTNAME = \'" + Lot_name + "\'";
            ResultSet rs4 = st.executeQuery(space_id);
            rs4.next();
            Integer mspace = rs4.getInt("X");

            //Get the minimum spaceID
            String space_id_min = "SELECT MIN(SPACEID) AS Y from SPACE where LOTNAME = \'" + Lot_name + "\'";
            ResultSet rs7 = st.executeQuery(space_id_min);
            rs7.next();
            Integer minspace = rs7.getInt("Y");

            //Check if the new zone to be alloted is visitor or not visitor
            if (zone_desig.charAt(0)=='V'){
                System.out.println("Please enter the number of spaces to be allotted");
                space_cnt= Integer.parseInt(reader.readLine());

                //Check if Visitor zone ID is already assigned to the parking lot
                String zone_check = "Select * from REL_ALLOCATED where NAME = \'" + Lot_name + "\' AND ZONEID=\'V\'";
                ResultSet rs2 = st.executeQuery(zone_check);
                if(!rs2.next()){
                    try{
                        //If not present, insert the zone ID in REL_ALLOCATED table
                        String sql = "INSERT INTO REL_ALLOCATED VALUES(?, ?)";
                        ps = conn.prepareStatement(sql);
                        ps.setString(1, zone_desig);
                        ps.setString(2, Lot_name);
                        
                        ResultSet rs3 = ps.executeQuery();
                        if (!rs3.next()){
                            System.out.println("Zone could not be inserted");
                            trans1 = false;
                        }
                        else{
                            System.out.println("Zone successfully assigned to the lot");
                            trans1 = true;
                        }
                    }
                    catch (SQLException e){
                        System.out.println("Caught SQL Exception!" + e.getErrorCode() + "/" + e.getSQLState() + " " + e.getMessage());
                        e.printStackTrace();
                        conn.rollback();
                        trans1 = false;
                        return;
                    }
                    
                }
                
                try{
                    //Update the ISVISITOR attribute in SPACE table;
                    String assign_vis = "UPDATE SPACE SET ISVISITOR = 1 where SPACEID BETWEEN (" + mspace + "-" + space_cnt + "+" + 1 + ") AND " + mspace;
                    ResultSet rs5 = st.executeQuery(assign_vis); 

                    if (!rs5.next()){
                        System.out.println("ISVISITOR update failed");
                        trans2 = false;
                    }
                    else{
                        System.out.println("ISVISITOR update successful");
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

            
            }
            else{
                
                //Check if REL_ALLOCATED has visitors, if it doesn't, no need to update SPACE table
                String zone_check1 = "Select * from REL_ALLOCATED where NAME = \'" + Lot_name + "\' AND ZONEID=\'V\'";
                ResultSet rs6 = st.executeQuery(zone_check1);
                if(rs6.next()){
                    System.out.println("The parking lot also has visitor zones. Please enter the number of spaces to be alloted to visitor");
                    space_cnt= Integer.parseInt(reader.readLine());

                    try{
                        String assign_vis1 = "UPDATE SPACE SET ISVISITOR = 1 where SPACEID BETWEEN (" + mspace + "-" + space_cnt + "+" + 1 + ") AND " + mspace;
                        ResultSet rs8 = st.executeQuery(assign_vis1);
                        String assign_vis2 = "UPDATE SPACE SET ISVISITOR = 0 where SPACEID BETWEEN " + minspace + " AND (" + mspace + "-" + space_cnt + ")";
                        ResultSet rs9 = st.executeQuery(assign_vis2);
                        /*
                        if (!rs8.next()){
                            System.out.println("Setting visitor spaces failed");
                            trans3 = false;
                        }
                        else{
                            System.out.println("Setting visitor spaces successful");
                            trans3 = true;
                        }
                        */
                    }
                    catch (SQLException e){
                        System.out.println("Caught SQL Exception!" + e.getErrorCode() + "/" + e.getSQLState() + " " + e.getMessage());
                        e.printStackTrace();
                        conn.rollback();
                        trans3 = false;
                        return;
                    }

                
                }
                
                //Check if REL_ALLOCATED already contains the zone ID to be assigned
                String zone_check2 = "Select * from REL_ALLOCATED where NAME = \'" + Lot_name + "\' AND ZONEID=\'" + zone_desig + "\'";
                ResultSet rs10 = st.executeQuery(zone_check2);
                if(!rs10.next()){
                    try{
                        String sql = "INSERT INTO REL_ALLOCATED VALUES(?, ?)";
                        ps = conn.prepareStatement(sql);
                        ps.setString(1, zone_desig);
                        ps.setString(2, Lot_name);
                        
                        ResultSet rs11 = ps.executeQuery();

                        if (!rs11.next()){
                            System.out.println("Zone could not be inserted");
                            trans4 = false;
                        }
                        else{
                            System.out.println("Zone successfully assigned to the lot");
                            trans4 = true;
                        }

                    }

                    catch (SQLException e){
                        System.out.println("Caught SQL Exception!" + e.getErrorCode() + "/" + e.getSQLState() + " " + e.getMessage());
                        e.printStackTrace();
                        conn.rollback();
                        trans4 = false;
                        return;
                    }
                
            }

        }

        /* Transaction management check*/
        if ((trans1 && trans2) || (trans4)){
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

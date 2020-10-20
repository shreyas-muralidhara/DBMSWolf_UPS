package WolfUPS.API;

import java.io.BufferedReader;
import java.io.IOException;
import java.sql.SQLException;

import WolfUPS.connection.*;

import java.sql.*;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Vis_Exit_Lot {
    public static void vis_exit_lot(BufferedReader reader,Connection conn) throws NumberFormatException, IOException, SQLException{
        Statement st = conn.createStatement();
        StringBuilder sb = new StringBuilder();
        PreparedStatement ps = null;
        ResultSet rs = null;
        ResultSet rs1 = null;
        String Ph_num, Vehicle_num, Lot_name, model, color, violation_cat;
        Integer violation_fee, myear;
        String Lot, Space_no, Permit_no, Lic_plate;
        Timestamp Expire_time, exitTime;

        // Prompt to enter the parking lot ID.
        System.out.println("Please enter your phone number - Enter digits only");
        Ph_num = reader.readLine();
        //Prompt to enter Vehicle number
        System.out.println("Please enter the Vehicle number");
        Vehicle_num= reader.readLine();


        /* Check if the visitor has permit, If not then issue NO permit Citation*/
        String sql = "Select * from VISITORPERMIT where PHONENO = \'" + Ph_num + "\' AND STARTTIME = (SELECT MAX(STARTTIME) FROM VISITORPERMIT where PHONENO = \'" + Ph_num + "\')";
        rs = st.executeQuery(sql); 

        if(!rs.next()){
            System.out.println("Incorrect phone number");
            return;
        }
    
        Lot = rs.getString("LOTNAME");
        Expire_time = rs.getTimestamp("EXPIRETIME");
        Space_no = rs.getString("SPACENO");
        Permit_no = rs.getString("PERMITNO");

        System.out.println(Lot+Expire_time+Space_no+Permit_no);    
        try{
            /* disable the auto commit*/
            conn.setAutoCommit(false);
            /* Seting the transaction Managment variables to capture the failure*/
            boolean trans1 = false,trans2 = false,trans3 = false;
        
            try{
                //Update SPACE table and make it unavailable.
                sql = "UPDATE SPACE SET ISAVAILABLE = 1 WHERE LOTNAME = \'" + Lot + "\' AND SPACEID = \'" + Space_no + "\' AND ISVISITOR = 1";
                rs = st.executeQuery(sql);
                trans1 = true;
                System.out.println("Parking space made available");
            }
            catch (SQLException e){
                System.out.println("Caught SQL Exception!" + e.getErrorCode() + "/" + e.getSQLState() + " " + e.getMessage());
                e.printStackTrace();
                conn.rollback();
                trans1 = false;
                return;
            }
            //Select vehicle details from vehicle table
            sql = "Select * from VEHICLE where PERMITNO = \'" + Permit_no + "\'";
            rs = st.executeQuery(sql);

            rs.next();
            model = rs.getString("MODEL");
            myear = rs.getInt("YEAR");
            color = rs.getString("COLOR");
            Lic_plate = rs.getString("LICENSEPLATE");

            try{
                //Delete the row
                //System.out.println(Lot+Expire_time+Space_no+Permit_no);    
                sql = "DELETE FROM VEHICLE WHERE LICENSEPLATE = \'" + Lic_plate + "\'";
                rs = st.executeQuery(sql);
                System.out.println("Deleted details from vehicles table");
                trans2 = true;
            }
            catch (SQLException e){
                System.out.println("Caught SQL Exception!" + e.getErrorCode() + "/" + e.getSQLState() + " " + e.getMessage());
                e.printStackTrace();
                conn.rollback();
                trans2 = false;
                return;
            }

            //Check for expire time
            /* Store the system time as exit time from the database */
            sql = "select to_char(current_timestamp,'YYYY-MM-DD hh24:mi:ss') as Timestamp from dual";
            rs = st.executeQuery(sql);
            rs.next();
            exitTime = rs.getTimestamp("Timestamp");

            /* Check if the exit time is greater than the expiry */
            int res = exitTime.compareTo(Expire_time);
            System.out.println(exitTime + " " + Expire_time + " " + res);

            //Call citation function
            /*
            if (res>0){
                IssueCitation.issuecitation(conn, Ph_num, Lic_plate, Lot_name, model, color, "Expired Permit", 25, "Visitor");
            }
            */
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

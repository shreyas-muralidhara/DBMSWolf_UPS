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

public class CheckVValidParking {
    public static void checkvvalidparking(BufferedReader reader,Connection conn) throws NumberFormatException, IOException, SQLException{
        Statement st = conn.createStatement();
        StringBuilder sb = new StringBuilder();
        PreparedStatement ps = null;
        ResultSet rs = null;
        String Licence, Lotname, Space_id;
        Timestamp Expire_time, exitTime;

        // Prompt to enter the Vehicle number.
        System.out.println("Please enter Car Licence number");
        Licence = reader.readLine();

        // Prompt to enter the Lot name.
        System.out.println("Please enter Lot name");
        Lotname = reader.readLine();

        // Prompt to enter the Space ID.
        System.out.println("Please enter Space ID");
        Space_id = reader.readLine();

        
        
        try {
            /* disable the auto commit*/
            conn.setAutoCommit(false);
            /* Seting the transaction Managment variables to capture the failure*/
            boolean trans1 = false;

            //Update the space type
            //String sql = "SELECT * FROM NONVISITORPERMIT WHERE PERMITNO = \'" + permit_no + "\'";
            String sql = "Select * from VISITORPERMIT VP, PERMIT P where VP.PERMITNO=P.PERMITNO and P.PRIMARYVEHICLENO = \'" + Licence + "\' AND VP.LOTNAME = \'" + Lotname + "\' AND VP.SPACENO = \'" + Space_id + "\' AND VP.EXPIREDATE = (SELECT MAX(EXPIREDATE) FROM VISITORPERMIT)";
            //String sql = "Select EXPIRETIME from VISITORPERMIT where PHONENO = \'" + Ph_num + "\' AND STARTTIME = (SELECT MAX(STARTTIME) FROM VISITORPERMIT where PHONENO = \'" + Ph_num + "\')";
            rs = st.executeQuery(sql);
            
            if(!rs.next()){
                System.out.println("No Permit found");
                return ;
            }

            Expire_time = rs.getTimestamp("EXPIRETIME");
        
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
            
            if (res>0){
                System.out.println("Permit is invalid");
            }
            else{
                System.out.println("Permit is Valid");
            }
            trans1 = true;

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

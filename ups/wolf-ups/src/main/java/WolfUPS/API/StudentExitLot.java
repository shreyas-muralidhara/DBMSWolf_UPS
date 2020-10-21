package WolfUPS.API;

import java.io.BufferedReader;
import java.io.IOException;
import java.sql.SQLException;

import java.sql.*;

public class StudentExitLot {
    public static void exitlot(BufferedReader reader,Connection conn, String univ_id) throws NumberFormatException, IOException, SQLException
    {
        Statement st = conn.createStatement();
        PreparedStatement ps = null;
        ResultSet rs = null;
        String LotName, PermitNo, model, car_color, VehicleNo;
        Timestamp EnterTime, exitTime, ExpireTime;
        Integer SpaceNo;

        /* Check if the student has a valid permit */
        String sql = "Select EXPIRETIME,PERMITNO from NONVISITORPERMIT where UNIVID = \'" + univ_id + "\'";rs=st.executeQuery(sql);

        if(!rs.next()){
            System.out.println("Invalid Request. No permit found for the specified Student.\nSwitching back to MAIN MENU");
            return;
        }
        ExpireTime=rs.getTimestamp("EXPIRETIME");
        PermitNo=rs.getString("PERMITNO");
        System.out.println(ExpireTime.toString()+" "+PermitNo+" "+ univ_id);

        System.out.println("Please enter the vehicle number.");
        VehicleNo=reader.readLine();

        /* Check if the student has permit, If not then issue NO permit Citation */
        sql="Select * from ASSIGNSINGLE WHERE PARKEDAT IS NOT NULL AND SPACENO IS NOT NULL AND LOTNAME IS NOT NULL AND UNIVID = \'"+ univ_id+"\' AND VEHICLENO = \'"+VehicleNo+"\'";
        rs=st.executeQuery(sql);

        if(!rs.next()){
            System.out.println("Invalid Request. University ID has no vehicle parked in the Lot.\nSwitching back to MAIN MENU");
            return;
        }
        LotName=rs.getString("LOTNAME");
        SpaceNo=rs.getInt("SPACENO");
        EnterTime=rs.getTimestamp("PARKEDAT");

        /* Store the system time as exit time from the database */
        sql="select to_char(current_timestamp,'YYYY-MM-DD hh24:mi:ss') as Timestamp from dual";
        rs=st.executeQuery(sql);
        rs.next();
        exitTime=rs.getTimestamp("Timestamp");

        /* Check if the exit time is greater than the expiry */
        int res = exitTime.compareTo(ExpireTime);
        System.out.println(EnterTime+" "+exitTime+" "+ExpireTime+" "+res);

        if(res>0)
        {
            /*
            * Indcates the epire time has crossed, while exiting the lot. Issue Expired
            * permit citation
            */
            System.out.println("Permit has expired.\nIssue Expired Permit Citation to " + PermitNo);

            sql = "Select MODEL,COLOR from VEHICLE where PERMITNO = \'" + PermitNo + "\'";
            rs = st.executeQuery(sql);
            rs.next();
            model = rs.getString("MODEL");
            car_color = rs.getString("COLOR");

            IssueCitation.issuecitation(conn, univ_id, VehicleNo, LotName, model, car_color, "Expired Permit", 25, "Student");
        }
        /* Unallocate the space for the vehicle and set the space as available */
        try
        {
            /* disable the auto commit */
            conn.setAutoCommit(false);
            /* Seting the transaction Managment variables to capture the failure */
            boolean trans1 = false, trans2 = false;

            /* Update Space table to make the parking space unavailable */
            try {
                sql = "UPDATE SPACE SET ISAVAILABLE = 1 WHERE LOTNAME = ? AND SPACEID = ? AND ISVISITOR = 0 ";
                ps = conn.prepareStatement(sql);
                ps.setString(1, LotName);
                ps.setInt(2, SpaceNo);

                rs = ps.executeQuery();

                if (rs != null) {
                    System.out.println("Parking space made available for other parkers");
                    trans1 = true;
                } else {
                    System.out.println("Unable to make the parking space available");
                    trans1 = false;
                }

            } catch (SQLException f) {
                System.out.println("Caught SQL Exception!" + f.getErrorCode() + "/" + f.getSQLState() + " " + f.getMessage());
                f.printStackTrace();
                conn.rollback();
                trans1 = false;
                return;
            }

            /*
            * Update Assign Multiple table and enter the Timestamp along with lot and
            * SpaceID
            */
            try {
                sql = "UPDATE ASSIGNSINGLE SET LOTNAME = NULL, SPACENO = NULL,PARKEDAT = NULL WHERE SPACENO = ? AND VEHICLENO=? AND LOTNAME = ?";
                ps = conn.prepareStatement(sql);
                ps.setInt(1, SpaceNo);
                ps.setString(2, VehicleNo);
                ps.setString(3, LotName);

                rs = ps.executeQuery();

                if (rs != null) {
                    System.out.println("Parking Space unallocated for the Student");
                    trans2 = true;
                } else {
                    System.out.println("Unable to de-allocated space for the Student");
                    trans2 = false;
                }

            } catch (SQLException f) {
                System.out
                        .println("Caught SQL Exception!" + f.getErrorCode() + "/" + f.getSQLState() + " " + f.getMessage());
                f.printStackTrace();
                conn.rollback();
                trans2 = false;
                return;
            }

            /* Transaction management check */
            if (trans1 && trans2) {
                conn.commit();
                System.out.println("Transaction Successful!");
            } else {
                conn.rollback();
                System.out.println("Transaction Failed");
            }
            conn.setAutoCommit(true);
        }catch(
        SQLException e)
        {
            System.out.println("Caught SQL Exception!" + e.getErrorCode() + "/" + e.getSQLState() + " " + e.getMessage());
            e.printStackTrace();
            conn.rollback();
        }
    }
}

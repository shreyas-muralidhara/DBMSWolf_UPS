package WolfUPS.API;

import java.io.BufferedReader;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import WolfUPS.connection.InitializeConnection;
import java.sql.*;

public class StudentEnterLot {
    
    public static void enterLot(BufferedReader reader,Connection conn, String univ_id) throws NumberFormatException, IOException, SQLException {

        Statement st = conn.createStatement();
        StringBuilder sb = new StringBuilder();
        PreparedStatement ps = null;
        ResultSet rs = null;
        String VehicleNo, PermitNo, Vehicle_permitNo, model, car_color, Invalid_LotName, issueDate, issueTime,
                PaymentDue, LotName, Zoneid, Spacetype;
        Integer CitationNo, SPC_ID;
        List<String> student_zones = new ArrayList<String>();
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());

        /* Check if the student has permit, If not then issue NO permit Citation */
        String sql = "Select * from NONVISITORPERMIT where UNIVID = \'" + univ_id + "\'";
        rs = st.executeQuery(sql);

        System.out.println("Please enter the vehicle number.");
        VehicleNo = reader.readLine();

        if (!rs.next()) {
            System.out.println("Please enter the lot name:");
            Invalid_LotName = reader.readLine();

            System.out.println("No Permit assigned for the student.\n\nIssue citation \"NO PERMIT\" to - " + VehicleNo + " for attempting to park without permit.");

            System.out.println("Please enter the details for " + VehicleNo + " to issue Citation \"NO PERMIT\".");
            System.out.println("Enter car model for " + VehicleNo);
            model = reader.readLine();
            System.out.println("Enter car color for " + VehicleNo);
            car_color = reader.readLine();

            /* Issue Citation No Permit */
            IssueCitation.issuecitation(conn, univ_id, VehicleNo, Invalid_LotName, model, car_color, "No Permit", 40, "Student");
            return;
        }

        PermitNo = rs.getString("PERMITNO");

        /*
         * Check if the vehicle is registered to a permit, If not the issue Citation -
         * No permit
         */
        sql = "Select * from VEHICLE where LICENSEPLATE = \'" + VehicleNo + "\'";
        rs = st.executeQuery(sql);

        if (!rs.next()) {
            System.out.println("Please enter the lot name:");
            Invalid_LotName = reader.readLine();

            System.out.println("Vehicle is not Registered to any permit.\n\nIssue citation \"Invalid Permit\" to - " + VehicleNo + " for attempting to park with unregistered Vehicle.");

            System.out.println("Please enter the details for " + VehicleNo + " to issue Citation \"Invalid Permit\".");
            System.out.println("Enter car model for " + VehicleNo);
            model = reader.readLine();
            System.out.println("Enter car color for " + VehicleNo);
            car_color = reader.readLine();

            /* Issue Citation No Permit */
            IssueCitation.issuecitation(conn, univ_id, VehicleNo, Invalid_LotName, model, car_color, "Invalid Permit", 20, "Student");

            return;
        }

        Vehicle_permitNo = rs.getString("PERMITNO");

        /* Check for Invalid permit, this vehicle is not assigned to the permit. */
        if (!PermitNo.equalsIgnoreCase(Vehicle_permitNo)) {
            System.out.println("Please enter the lot name:");
            Invalid_LotName = reader.readLine();

            System.out.println("Invalid permit, this vehicle is not assigned to the permit.\n\nIssue citation \"INVALID PERMIT\" to - " + VehicleNo + " for attempting to park with wrong Permit.");

            System.out.println("Please enter the details for " + VehicleNo + " to issue Citation \"Invalid Permit\".");
            System.out.println("Enter car model for " + VehicleNo);
            model = reader.readLine();
            System.out.println("Enter car color for " + VehicleNo);
            car_color = reader.readLine();

            IssueCitation.issuecitation(conn, univ_id, VehicleNo, Invalid_LotName, model, car_color, "Invalid Permit", 20, "Student");
            return;
        }
        /* Vehicle is already in the lot, need not enter again */
        sql = "Select * from ASSIGNMULTIPLE WHERE VEHICLENO = \'" + VehicleNo + "\' AND PARKEDAT IS NOT NULL";
        rs = st.executeQuery(sql);

        if (rs.next()) {
            System.out.println("Vehicle already in the lot. Invalid Request\n");
            return;
        }

        /*
         * Student had valid permit and allocated Vehicle for that permit, then display
         * the list of lots Available to Park
         */
        sql = "Select S.LOTNAME, A.ZONEID, S.SPACETYPE, count(*) AS COUNT " + "from SPACE S, REL_ALLOCATED A "
                + "where S.LOTNAME=A.NAME and (A.ZONEID = (Select ZONEID from PERMIT where PERMITNO=\'" + PermitNo
                + "\') " + "OR A.ZONEID = (Select replace(ZONEID||'S',chr(32),'')  from PERMIT where PERMITNO=\'"
                + PermitNo + "\')) "
                + "AND UPPER(S.SPACETYPE) = (Select DISTINCT UPPER(SPACETYPE) from PERMIT where PERMITNO=\'" + PermitNo
                + "\') AND ISAVAILABLE=1 " + "GROUP BY S.LOTNAME,A.ZONEID, S.SPACETYPE " + "ORDER BY S.LOTNAME";
        rs = st.executeQuery(sql);

        sql = "select S.LOTNAME, A.ZONEID, S.SPACETYPE, count(*) AS COUNT " + "from SPACE S, REL_ALLOCATED A " + "where S.LOTNAME = A.NAME and (A.ZONEID = (Select ZONEID from PERMIT where PERMITNO = \'" + PermitNo + "\')";
        if (timestamp.getHours() >=17){
            sql += "or A.ZONEID IN (\'A\',\'B\',\'C\',\'D\',\'AS\',\'BS\',\'CS\',\'DS\',\'R\',\'RS\')";
        }
        sql += ") AND UPPER(S.SPACETYPE) = (Select DISTINCT UPPER(SPACETYPE) from PERMIT where PERMITNO=\'" + PermitNo + "\') AND ISAVAILABLE=1 " + "GROUP BY S.LOTNAME,A.ZONEID, S.SPACETYPE " + "ORDER BY S.LOTNAME";
        rs = st.executeQuery(sql);


        System.out.println("Below are the list of Lots available to park");
        while (rs.next()) {
            student_zones.add(rs.getString("LOTNAME"));
            System.out.println(rs.getString("LOTNAME") + " | " + rs.getString("ZONEID") + " | " + rs.getString("SPACETYPE") + " | " + rs.getString("COUNT"));
        }

        System.out.println("Please enter the lot name:");
        LotName = reader.readLine();

        /*
         * If the entered lot name is not in the list then issue invalid permit citation
         */
        if (!student_zones.contains(LotName)) {
            System.out.println("Invalid permit, this vehicle is not allowed to enter this lot.\n\nIssue citation \"INVALID PERMIT\" to - " + VehicleNo + " for attempting to park in the wrong Lot.");

            sql = "Select MODEL,COLOR from VEHICLE where UPPER(LICENSEPLATE) = \'" + VehicleNo.toUpperCase() + "\'";
            rs = st.executeQuery(sql);
            rs.next();
            model = rs.getString("MODEL");
            car_color = rs.getString("COLOR");

            IssueCitation.issuecitation(conn, univ_id, VehicleNo, LotName, model, car_color, "Invalid Permit", 20, "Student");
            return;
        }

        sql = "Select ZONEID,SPACETYPE from PERMIT where PERMITNO = \'" + PermitNo + "\'";
        rs = st.executeQuery(sql);
        rs.next();
        Zoneid = rs.getString("ZONEID");
        Spacetype = rs.getString("SPACETYPE");

        /* Get the space ID that is next available */
        sql = "SELECT MIN(SPACEID) AS CNT FROM SPACE WHERE UPPER(LOTNAME) = \'" + LotName.toUpperCase()
                + "\' AND UPPER(SPACETYPE) = \'" + Spacetype.toUpperCase()
                + "\' AND ISVISITOR = '0' AND ISAVAILABLE = '1'";
        rs = st.executeQuery(sql);
        rs.next();
        SPC_ID = rs.getInt("CNT");

        // System.out.println(LotName + " " + SPC_ID + " " + Spacetype);
        // System.out.println(PermitNo + " " + emp_id);

        try {
            /* disable the auto commit */
            conn.setAutoCommit(false);
            /* Seting the transaction Managment variables to capture the failure */
            boolean trans1 = false, trans2 = false;

            /* Update Space table to make the space unavailable */
            try {
                sql = "UPDATE SPACE SET ISAVAILABLE = 0 WHERE LOTNAME = ? AND SPACEID = ? AND ISVISITOR = 0 AND UPPER(SPACETYPE) = ?";
                ps = conn.prepareStatement(sql);
                ps.setString(1, LotName);
                ps.setInt(2, SPC_ID);
                ps.setString(3, Spacetype.toUpperCase());

                rs = ps.executeQuery();

                if (rs != null) {
                    System.out.println("Parking space made unavailable for other parkers");
                    trans1 = true;
                } else {
                    System.out.println("Unable to give access to the parking space");
                    trans1 = false;
                }

            } catch (SQLException f) {
                System.out.println(
                        "Caught SQL Exception!" + f.getErrorCode() + "/" + f.getSQLState() + " " + f.getMessage());
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
                sql = "UPDATE ASSIGNSINGLE SET LOTNAME = ?, SPACENO = ?,PARKEDAT = current_timestamp WHERE PERMITNO = ? AND VEHICLENO=?";
                ps = conn.prepareStatement(sql);
                ps.setString(1, LotName);
                ps.setInt(2, SPC_ID);
                ps.setString(3, PermitNo);
                ps.setString(4, VehicleNo);

                rs = ps.executeQuery();

                if (rs != null) {
                    System.out.println("Parking Space allocated for the student");
                    trans2 = true;
                } else {
                    System.out.println("Unable to store allocated space for student");
                    trans2 = false;
                }

            } catch (SQLException f) {
                System.out.println("Caught SQL Exception!" + f.getErrorCode() + "/" + f.getSQLState() + " " + f.getMessage());
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
        } catch (SQLException e) {
            System.out.println("Caught SQL Exception!" + e.getErrorCode() + "/" + e.getSQLState() + " " + e.getMessage());
            e.printStackTrace();
            conn.rollback();
        }
        
    }

}

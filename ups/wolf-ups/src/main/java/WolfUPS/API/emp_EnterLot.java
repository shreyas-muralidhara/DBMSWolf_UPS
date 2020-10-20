package WolfUPS.API;

import java.io.BufferedReader;
import java.io.IOException;
import java.security.Permission;
import java.sql.SQLException;
import java.util.ArrayList;

import java.util.List;
import java.util.UUID;

import WolfUPS.connection.*;

import java.sql.*;

public class emp_EnterLot {
    public static void enterlot(BufferedReader reader,Connection conn, String emp_id) throws NumberFormatException, IOException, SQLException{
        Statement st = conn.createStatement();
        StringBuilder sb = new StringBuilder();
        PreparedStatement ps = null;
        ResultSet rs = null;
        String VehicleNo,PermitNo, Vehicle_permitNo, model, car_color,Invalid_LotName, issueDate, issueTime, PaymentDue,LotName, Zoneid,Spacetype;
        Integer CitationNo,SPC_ID;
        List<String> emp_zones = new ArrayList<String>();

        /* Check if the employee has permit, If not then issue NO permit Citation*/
        String sql = "Select * from NONVISITORPERMIT where UNIVID = \'" + emp_id + "\'";
        rs = st.executeQuery(sql); 

        System.out.println("Please enter the vehicle number.");
        VehicleNo = reader.readLine();

        if(!rs.next()){
            System.out.println("Please enter the lot name:");
            Invalid_LotName = reader.readLine();

            System.out.println("No Permit assigned for the employee.\n\nIssue citation \"NO PERMIT\" to - "+ VehicleNo +" for attempting to park without permit.");
            
            System.out.println("Please enter the details for "+ VehicleNo + " to issue Citation \"NO PERMIT\".");
            System.out.println("Enter car model for "+ VehicleNo);
            model = reader.readLine();
            System.out.println("Enter car color for "+ VehicleNo);
            car_color = reader.readLine();

            
            rs = st.executeQuery("select to_char(sysdate,\'YYYY-MM-DD\') as CreateDate, to_char(current_timestamp,\'YYYY-MM-DD hh24:mi:ss\') as Timestamp, to_char(sysdate+30,\'YYYY-MM-DD\') as DueDate from dual");
            rs.next();
            issueDate = rs.getString("CreateDate");
            issueTime = rs.getString("Timestamp");
            PaymentDue = rs.getString("DueDate");
            
            rs = st.executeQuery("Select Max(CITATIONNO) from CITATION");

            if(!rs.next())
                CitationNo = 10001;
            else
                CitationNo = rs.getInt(1) + 1;

            try{
                /* disable the auto commit*/
                conn.setAutoCommit(false);
                /* Seting the transaction Managment variables to capture the failure*/
                boolean trans1 = false,trans2 = false;

                /*Insert Citation into the Citation table*/
                try {
                    sql = "INSERT INTO CITATION VALUES(?, ?, ?, ?, TO_DATE(\'" + issueDate +"00:00:00\', \'YYYY-MM-DD hh24:mi:ss\'), ?, ?, ?, TO_TIMESTAMP(\'" + issueTime +"\', \'YYYY-MM-DD hh24:mi:ss\'), ?, TO_DATE(\'" + PaymentDue +"00:00:00\', \'YYYY-MM-DD hh24:mi:ss\'), ?)";
                    ps = conn.prepareStatement(sql);
                    ps.setInt(1, CitationNo);
                    ps.setString(2, VehicleNo);
                    ps.setString(3, model);
                    ps.setString(4, car_color);
                    ps.setString(5, "Unpaid");
                    ps.setString(6, "employee");
                    ps.setString(7, Invalid_LotName);
                    ps.setString(8, "No Permit");
                    ps.setInt(9, 40);
                    rs = ps.executeQuery();

                    if (rs != null) {
                        System.out.println("Citation "+ CitationNo +" entry created successfully");
                        trans1 = true;
                    } else {
                        System.out.println("Unable to create the Citation");
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


                /*Insert into the Notification Non visitor table*/
                try {
                    sql = "INSERT INTO NOTIFICATIONNONVISITOR VALUES(?, ?)";
                    ps = conn.prepareStatement(sql);
                    ps.setString(1, emp_id);
                    ps.setInt(2, CitationNo);
                
                    rs = ps.executeQuery();

                    if (rs != null) {
                        System.out.println("Notification for NO PERMIT citation sent successfully");
                        trans2 = true;
                    } else {
                        System.out.println("Unable to send notification to employee");
                        trans2 = false;
                    }
                    
                }
                catch (SQLException e){
                    System.out.println("Caught SQL Exception!" + e.getErrorCode() + "/" + e.getSQLState() + " " + e.getMessage());
                    e.printStackTrace();
                    conn.rollback();
                    trans2 = false;
                    return;
                }
                /* Transaction management check*/
                if (trans1 && trans2){
                    conn.commit();
                    System.out.println("Transaction Successful!");
                }
                else{
                    conn.rollback();
                    System.out.println("Transaction Failed");
                }
                conn.setAutoCommit(true);
            }
            catch (SQLException e) {
                System.out.println("Caught SQL Exception!" + e.getErrorCode() + "/" + e.getSQLState() + " " + e.getMessage());
                e.printStackTrace();
                conn.rollback();
            }
            return ;
        }
        
        PermitNo = rs.getString("PERMITNO");
        
        /* Check if the vehicle is registered to a permit, If not the issue Citation - No permit*/
        sql = "Select * from VEHICLE where LICENSEPLATE = \'" + VehicleNo + "\'";
        rs = st.executeQuery(sql); 

        if(!rs.next()){
            System.out.println("Please enter the lot name:");
            Invalid_LotName = reader.readLine();

            System.out.println("Vehicle is not Registered to any permit.\n\nIssue citation \"NO PERMIT\" to - "+ VehicleNo +" for attempting to park with unregistered Vehicle.");
            
            System.out.println("Please enter the details for "+ VehicleNo + " to issue Citation \"NO PERMIT\".");
            System.out.println("Enter car model for "+ VehicleNo);
            model = reader.readLine();
            System.out.println("Enter car color for "+ VehicleNo);
            car_color = reader.readLine();

            
            rs = st.executeQuery("select to_char(sysdate,\'YYYY-MM-DD\') as CreateDate, to_char(current_timestamp,\'YYYY-MM-DD hh24:mi:ss\') as Timestamp, to_char(sysdate+30,\'YYYY-MM-DD\') as DueDate from dual");
            rs.next();
            issueDate = rs.getString("CreateDate");
            issueTime = rs.getString("Timestamp");
            PaymentDue = rs.getString("DueDate");
            
            rs = st.executeQuery("Select Max(CITATIONNO) from CITATION");

            if(!rs.next())
                CitationNo = 10001;
            else
                CitationNo = rs.getInt(1) + 1;

            try{
                /* disable the auto commit*/
                conn.setAutoCommit(false);
                /* Seting the transaction Managment variables to capture the failure*/
                boolean trans1 = false,trans2 = false;

                /*Insert Citation into the Citation table*/
                try {
                    sql = "INSERT INTO CITATION VALUES(?, ?, ?, ?, TO_DATE(\'" + issueDate +"00:00:00\', \'YYYY-MM-DD hh24:mi:ss\'), ?, ?, ?, TO_TIMESTAMP(\'" + issueTime +"\', \'YYYY-MM-DD hh24:mi:ss\'), ?, TO_DATE(\'" + PaymentDue +"00:00:00\', \'YYYY-MM-DD hh24:mi:ss\'), ?)";
                    ps = conn.prepareStatement(sql);
                    ps.setInt(1, CitationNo);
                    ps.setString(2, VehicleNo);
                    ps.setString(3, model);
                    ps.setString(4, car_color);
                    ps.setString(5, "Unpaid");
                    ps.setString(6, "employee");
                    ps.setString(7, Invalid_LotName);
                    ps.setString(8, "No Permit");
                    ps.setInt(9, 40);
                    rs = ps.executeQuery();

                    if (rs != null) {
                        System.out.println("Citation "+ CitationNo +" entry created successfully");
                        trans1 = true;
                    } else {
                        System.out.println("Unable to create the Citation");
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


                /*Insert into the Notification Non visitor table*/
                try {
                    sql = "INSERT INTO NOTIFICATIONNONVISITOR VALUES(?, ?)";
                    ps = conn.prepareStatement(sql);
                    ps.setString(1, emp_id);
                    ps.setInt(2, CitationNo);
                
                    rs = ps.executeQuery();

                    if (rs != null) {
                        System.out.println("Notification for NO PERMIT citation sent successfully");
                        trans2 = true;
                    } else {
                        System.out.println("Unable to send notification to employee");
                        trans2 = false;
                    }
                    
                }
                catch (SQLException e){
                    System.out.println("Caught SQL Exception!" + e.getErrorCode() + "/" + e.getSQLState() + " " + e.getMessage());
                    e.printStackTrace();
                    conn.rollback();
                    trans2 = false;
                    return;
                }
                /* Transaction management check*/
                if (trans1 && trans2){
                    conn.commit();
                    System.out.println("Transaction Successful!");
                }
                else{
                    conn.rollback();
                    System.out.println("Transaction Failed");
                }
                conn.setAutoCommit(true);
            }
            catch (SQLException e) {
                System.out.println("Caught SQL Exception!" + e.getErrorCode() + "/" + e.getSQLState() + " " + e.getMessage());
                e.printStackTrace();
                conn.rollback();
            }
 
            return ;
        }

        Vehicle_permitNo = rs.getString("PERMITNO");
        
        
        /* Check for Invalid permit, this vehicle is not assigned to the permit.*/
        if(!PermitNo.equalsIgnoreCase(Vehicle_permitNo))
        {    
            System.out.println("Please enter the lot name:");
            Invalid_LotName = reader.readLine();

            System.out.println("Invalid permit, this vehicle is not assigned to the permit.\n\nIssue citation \"INVALID PERMIT\" to - "+ VehicleNo +" for attempting to park with wrong Permit.");
            
            sql = "Select MODEL,COLOR from VEHICLE where LICENSEPLATE = \'" + VehicleNo + "\'";
            rs = st.executeQuery(sql);
            rs.next();
            model = rs.getString("MODEL");
            car_color = rs.getString("COLOR");
            
            rs = st.executeQuery("select to_char(sysdate,\'YYYY-MM-DD\') as CreateDate, to_char(current_timestamp,\'YYYY-MM-DD hh24:mi:ss\') as Timestamp, to_char(sysdate+30,\'YYYY-MM-DD\') as DueDate from dual");
            rs.next();
            issueDate = rs.getString("CreateDate");
            issueTime = rs.getString("Timestamp");
            PaymentDue = rs.getString("DueDate");
            
            rs = st.executeQuery("Select Max(CITATIONNO) from CITATION");

            if(!rs.next())
                CitationNo = 10001;
            else
                CitationNo = rs.getInt(1) + 1;

            try{
                /* disable the auto commit*/
                conn.setAutoCommit(false);
                /* Seting the transaction Managment variables to capture the failure*/
                boolean trans1 = false,trans2 = false;

                /*Insert Citation into the Citation table*/
                try {
                    sql = "INSERT INTO CITATION VALUES(?, ?, ?, ?, TO_DATE(\'" + issueDate +"00:00:00\', \'YYYY-MM-DD hh24:mi:ss\'), ?, ?, ?, TO_TIMESTAMP(\'" + issueTime +"\', \'YYYY-MM-DD hh24:mi:ss\'), ?, TO_DATE(\'" + PaymentDue +"00:00:00\', \'YYYY-MM-DD hh24:mi:ss\'), ?)";
                    ps = conn.prepareStatement(sql);
                    ps.setInt(1, CitationNo);
                    ps.setString(2, VehicleNo);
                    ps.setString(3, model);
                    ps.setString(4, car_color);
                    ps.setString(5, "Unpaid");
                    ps.setString(6, "employee");
                    ps.setString(7, Invalid_LotName);
                    ps.setString(8, "Invalid Permit");
                    ps.setInt(9, 20);
                    rs = ps.executeQuery();

                    if (rs != null) {
                        System.out.println("Citation "+ CitationNo +" entry created successfully");
                        trans1 = true;
                    } else {
                        System.out.println("Unable to create the Citation");
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


                /*Insert into the Notification Non visitor table*/
                try {
                    sql = "INSERT INTO NOTIFICATIONNONVISITOR VALUES(?, ?)";
                    ps = conn.prepareStatement(sql);
                    ps.setString(1, emp_id);
                    ps.setInt(2, CitationNo);
                
                    rs = ps.executeQuery();

                    if (rs != null) {
                        System.out.println("Notification for Invalid Permit citation sent successfully");
                        trans2 = true;
                    } else {
                        System.out.println("Unable to send notification for Invalid Permit");
                        trans2 = false;
                    }
                    
                }
                catch (SQLException e){
                    System.out.println("Caught SQL Exception!" + e.getErrorCode() + "/" + e.getSQLState() + " " + e.getMessage());
                    e.printStackTrace();
                    conn.rollback();
                    trans2 = false;
                    return;
                }
                /* Transaction management check*/
                if (trans1 && trans2){
                    conn.commit();
                    System.out.println("Transaction Successful!");
                }
                else{
                    conn.rollback();
                    System.out.println("Transaction Failed");
                }
                conn.setAutoCommit(true);
            }
            catch (SQLException e) {
                System.out.println("Caught SQL Exception!" + e.getErrorCode() + "/" + e.getSQLState() + " " + e.getMessage());
                e.printStackTrace();
                conn.rollback();
            }

            return;
        }

        /* Employee had valid permit and allocated Vehicle for that permit, then display the list of lots Available to Park */
        sql = "Select S.LOTNAME, A.ZONEID, S.SPACETYPE, count(*) AS COUNT "
                +"from SPACE S, REL_ALLOCATED A "
                +"where S.LOTNAME=A.NAME and (A.ZONEID = (Select ZONEID from PERMIT where PERMITNO=\'"+ PermitNo +"\') "
                        +"OR A.ZONEID = (Select replace(ZONEID||'S',chr(32),'')  from PERMIT where PERMITNO=\'"+ PermitNo +"\')) "
                        +"AND UPPER(S.SPACETYPE) = (Select DISTINCT UPPER(SPACETYPE) from PERMIT where PERMITNO=\'"+ PermitNo +"\') "
                +"GROUP BY S.LOTNAME,A.ZONEID, S.SPACETYPE "
                +"ORDER BY S.LOTNAME";
        rs = st.executeQuery(sql);

        System.out.println("Below are the list of Lots available to park");
        while(rs.next()){
            emp_zones.add(rs.getString("LOTNAME"));
            System.out.println(rs.getString("LOTNAME") + " | " + rs.getString("ZONEID")  + " | " + rs.getString("SPACETYPE") + " | " + rs.getString("COUNT"));
        }

        System.out.println("Please enter the lot name:");
        LotName = reader.readLine();


        /* If the entered lot name is not in the list then issue invalid permit citation*/
        if(!emp_zones.contains(LotName)){
            System.out.println("Invalid permit, this vehicle is not allowed to enter this lot.\n\nIssue citation \"INVALID PERMIT\" to - "+ VehicleNo +" for attempting to park in the wrong Lot.");
            
            sql = "Select MODEL,COLOR from VEHICLE where UPPER(LICENSEPLATE) = \'" + VehicleNo.toUpperCase() + "\'";
            rs = st.executeQuery(sql);
            rs.next();
            model = rs.getString("MODEL");
            car_color = rs.getString("COLOR");

            
            rs = st.executeQuery("select to_char(sysdate,\'YYYY-MM-DD\') as CreateDate, to_char(current_timestamp,\'YYYY-MM-DD hh24:mi:ss\') as Timestamp, to_char(sysdate+30,\'YYYY-MM-DD\') as DueDate from dual");
            rs.next();
            issueDate = rs.getString("CreateDate");
            issueTime = rs.getString("Timestamp");
            PaymentDue = rs.getString("DueDate");
            
            rs = st.executeQuery("Select Max(CITATIONNO) from CITATION");

            if(!rs.next())
                CitationNo = 10001;
            else
                CitationNo = rs.getInt(1) + 1;

            try{
                /* disable the auto commit*/
                conn.setAutoCommit(false);
                /* Seting the transaction Managment variables to capture the failure*/
                boolean trans1 = false,trans2 = false;

                /*Insert Citation into the Citation table*/
                try {
                    sql = "INSERT INTO CITATION VALUES(?, ?, ?, ?, TO_DATE(\'" + issueDate +"00:00:00\', \'YYYY-MM-DD hh24:mi:ss\'), ?, ?, ?, TO_TIMESTAMP(\'" + issueTime +"\', \'YYYY-MM-DD hh24:mi:ss\'), ?, TO_DATE(\'" + PaymentDue +"00:00:00\', \'YYYY-MM-DD hh24:mi:ss\'), ?)";
                    ps = conn.prepareStatement(sql);
                    ps.setInt(1, CitationNo);
                    ps.setString(2, VehicleNo);
                    ps.setString(3, model);
                    ps.setString(4, car_color);
                    ps.setString(5, "Unpaid");
                    ps.setString(6, "employee");
                    ps.setString(7, LotName);
                    ps.setString(8, "Invalid Permit");
                    ps.setInt(9, 20);
                    rs = ps.executeQuery();

                    if (rs != null) {
                        System.out.println("Citation "+ CitationNo +" entry created successfully");
                        trans1 = true;
                    } else {
                        System.out.println("Unable to create the Citation");
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


                /*Insert into the Notification Non visitor table*/
                try {
                    sql = "INSERT INTO NOTIFICATIONNONVISITOR VALUES(?, ?)";
                    ps = conn.prepareStatement(sql);
                    ps.setString(1, emp_id);
                    ps.setInt(2, CitationNo);
                
                    rs = ps.executeQuery();

                    if (rs != null) {
                        System.out.println("Notification for Invalid Permit citation sent successfully");
                        trans2 = true;
                    } else {
                        System.out.println("Unable to send notification for Invalid Permit");
                        trans2 = false;
                    }
                    
                }
                catch (SQLException e){
                    System.out.println("Caught SQL Exception!" + e.getErrorCode() + "/" + e.getSQLState() + " " + e.getMessage());
                    e.printStackTrace();
                    conn.rollback();
                    trans2 = false;
                    return;
                }
                /* Transaction management check*/
                if (trans1 && trans2){
                    conn.commit();
                    System.out.println("Transaction Successful!");
                }
                else{
                    conn.rollback();
                    System.out.println("Transaction Failed");
                }
                conn.setAutoCommit(true);
            }
            catch (SQLException e) {
                System.out.println("Caught SQL Exception!" + e.getErrorCode() + "/" + e.getSQLState() + " " + e.getMessage());
                e.printStackTrace();
                conn.rollback();
            }

            return;
            
        }

        sql = "Select ZONEID,SPACETYPE from PERMIT where PERMITNO = \'" + PermitNo + "\'";
        rs = st.executeQuery(sql);
        rs.next();
        Zoneid = rs.getString("ZONEID");
        Spacetype = rs.getString("SPACETYPE");

        /* Get the space ID that is next available*/
        sql = "SELECT MIN(SPACEID) AS CNT FROM SPACE WHERE UPPER(LOTNAME) = \'" + LotName.toUpperCase() + "\' AND UPPER(SPACETYPE) = \'" + Spacetype.toUpperCase() + "\' AND ISVISITOR = '0' AND ISAVAILABLE = '1'";
        rs = st.executeQuery(sql);
        rs.next();
        SPC_ID = rs.getInt("CNT");

        System.out.println(LotName + " " + SPC_ID + " " + Spacetype);
        System.out.println(PermitNo + " " + emp_id);

        try{
            /* disable the auto commit*/
            conn.setAutoCommit(false);
            /* Seting the transaction Managment variables to capture the failure*/
            boolean trans1 = false,trans2 = false;

            /* Update Space table to make the space unavailable*/
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
                
            }
            catch (SQLException f){
                System.out.println("Caught SQL Exception!" + f.getErrorCode() + "/" + f.getSQLState() + " " + f.getMessage());
                f.printStackTrace();
                conn.rollback();
                trans1 = false;
                return;
            }


            /* Update Assign Multiple table and enter the Timestamp along with lot and SpaceID*/
            try {
                sql = "UPDATE ASSIGNMULTIPLE SET LOTNAME = ?, SPACENO = ?,PARKEDAT = current_timestamp WHERE PERMITNO = ? AND UNIVID=?";
                ps = conn.prepareStatement(sql);
                ps.setString(1, LotName);
                ps.setInt(2, SPC_ID);
                ps.setString(3, PermitNo);
                ps.setString(4, emp_id);

                rs = ps.executeQuery();

                if (rs != null) {
                    System.out.println("Parking Space allocated for the employee");
                    trans2 = true;
                } else {
                    System.out.println("Unable to store allocated space for employee");
                    trans2 = false;
                }
                
            }
            catch (SQLException f){
                System.out.println("Caught SQL Exception!" + f.getErrorCode() + "/" + f.getSQLState() + " " + f.getMessage());
                f.printStackTrace();
                conn.rollback();
                trans2 = false;
                return;
            }

            /* Transaction management check*/
            if (trans1 && trans2){
                conn.commit();
                System.out.println("Transaction Successful!");
            }
            else{
                conn.rollback();
                System.out.println("Transaction Failed");
            }
            conn.setAutoCommit(true);
        }
        catch (SQLException e) {
            System.out.println("Caught SQL Exception!" + e.getErrorCode() + "/" + e.getSQLState() + " " + e.getMessage());
            e.printStackTrace();
            conn.rollback();
        }
    }
}

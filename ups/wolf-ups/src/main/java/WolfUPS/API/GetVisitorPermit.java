package WolfUPS.API;

import java.io.BufferedReader;
import java.io.IOException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;


import WolfUPS.connection.*;

import java.sql.*;

public class GetVisitorPermit {
    public static void getvisitorpermit(BufferedReader reader,Connection conn) throws NumberFormatException, IOException, SQLException{
        Statement st = conn.createStatement();
        StringBuilder sb = new StringBuilder();
        PreparedStatement ps = null;
        ResultSet rs = null;
        String Date_time, space_type, permit_time, StartDate=null,VehicleNo, permitid="";
        String Lot_name, ph_num, manf, model, year, car_color;
        Integer duration, SPC_ID = null;

        // Prompt the visitor to enter phone number.
        System.out.println("Please enter your phone number - Enter digits only");
        ph_num = reader.readLine();
        String visitor_check = "Select P.PERMITNO from VISITORPERMIT P, VEHICLE V where P.PHONENO = \'" + ph_num + "\' AND P.PERMITNO = V.PERMITNO";
        rs = st.executeQuery(visitor_check);

        if(rs.next()){
            System.out.println("Visitor already has a permit. Cannot assign second permit");
            return;
        }

        System.out.println("Pick a parking lot from the list of available spaces below");
        String available_space = "Select LOTNAME, SPACETYPE, count(*) AS COUNT FROM SPACE WHERE ISVISITOR = '1' AND ISAVAILABLE = '1' GROUP BY LOTNAME, SPACETYPE";
        rs = st.executeQuery(available_space);

        while (rs.next()) {
            String s = rs.getString("LOTNAME");
            String t = rs.getString("SPACETYPE");
		    Integer n = rs.getInt("COUNT");
		    System.out.println(s + "   " + t + " " + n);
		}

        //System.out.println(rs.next());

        System.out.println("Please enter the lotname where permit is desired");
        Lot_name = reader.readLine();
        
        String lot_check = "Select * from PARKINGLOT where NAME = \'" + Lot_name + "\'";
        rs = st.executeQuery(lot_check);

        if(!rs.next()){
            System.out.println("Parking Lot does not exist");
            return;
        }

        System.out.println("Visitor permit is issued only at the time of entering the parking lot");
        System.out.println("Please enter the start time of permit in the format HH:MM AM/PM  --Enter current time");
        permit_time = reader.readLine();

        System.out.println("Please enter the permit duration in hours");
        duration = Integer.parseInt(reader.readLine());
        
        System.out.println("Please enter the space type desired - default is regular");
        space_type = reader.readLine();
        
        rs = st.executeQuery("Select to_char(sysdate,\'YYYY-MM-DD\') as startDate from dual");
        rs.next();
        StartDate=rs.getString("startDate");

        Date_time = StartDate + " " + permit_time;
        
       

        SimpleDateFormat sdt = new SimpleDateFormat("yyyy-MM-dd hh:mm a");

          
        

        permitid = permitid + StartDate.substring(2,4);
        permitid = permitid + "V";
        permitid = permitid + UUID.randomUUID().toString().substring(0, 5);


        System.out.println("Please enter the Vehicle number in the format <ABC1234>:");
        VehicleNo = reader.readLine();

        /*Check if the permit is already assigned for the Vehicle */
        rs = st.executeQuery("Select * from VEHICLE where LICENSEPLATE = \'" + VehicleNo + "\'");
        /*If exists then terminate*/    
        if(rs.next()){
            System.out.println("Permit already exists for the Vehicle-" + VehicleNo +",cannot assign new permit.\nSwitching back to MAIN MENU.");
            return ;
        }

        System.out.println("Enter car manufacturer for "+ VehicleNo);
        manf = reader.readLine();
        System.out.println("Enter car model for "+ VehicleNo);
        model = reader.readLine();
        System.out.println("Enter year of manufacture for the car - "+ VehicleNo);
        year = reader.readLine();
        System.out.println("Enter car color for "+ VehicleNo);
        car_color = reader.readLine();

        try{
            /* disable the auto commit*/
            conn.setAutoCommit(false);
            /* Seting the transaction Managment variables to capture the failure*/
            boolean trans1 = false,trans2 = false,trans3 = false,trans4=false,trans5=false,trans6=false,trans7=false;

            /*Insert the new permit*/
            try {
                
                Date start_date_time = sdt.parse(Date_time);
                Calendar cal = Calendar.getInstance();
                cal.setTime(start_date_time);
                String d = cal.getTime().toString();
                cal.add(Calendar.HOUR_OF_DAY,duration);
        
                String e = cal.getTime().toString();
                System.out.println(e);

                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                String Exp_Date = dateFormat.format(cal.getTime()).toString();
        
                    
            
                try{
                    //String sql = "INSERT INTO PERMIT VALUES(?, ?, TO_DATE(\'" + StartDate +"00:00:00\', \'YYYY-MM-DD hh24:mi:ss\'), ?, ?)";
                    String sql = "INSERT INTO PERMIT VALUES(?, \'V\', TO_DATE(\'" + StartDate + d.substring(11, 19) + "\', \'YYYY-MM-DD hh24:mi:ss\'), ?, ?)";
                    ps = conn.prepareStatement(sql);
                    ps.setString(1, permitid);
                    ps.setString(2, VehicleNo);
                    ps.setString(3, space_type);
                    System.out.println(sql);
                    rs = ps.executeQuery();

                    if (rs != null) {
                        System.out.println("Permit created successfully");
                        trans1 = true;
                    } else {
                        System.out.println("Unable to create the permit");
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

                

                /*Insert into Visitor Permit Table*/
                try {
                    String sel_space = "SELECT MAX(SPACEID) AS CNT FROM SPACE WHERE LOTNAME = \'" + Lot_name + "\' AND SPACETYPE = \'" + space_type + "\' AND ISVISITOR = '1' AND ISAVAILABLE = '1'";
                    rs = st.executeQuery(sel_space);
                    while (rs.next()) {
                        SPC_ID = rs.getInt("CNT");
                    
                    System.out.println(SPC_ID);
                    }


                    String sql = "INSERT INTO VISITORPERMIT VALUES(?, TO_TIMESTAMP(\'" + StartDate + d.substring(11, 19) + "\', \'YYYY-MM-DD hh24:mi:ss\'), TO_TIMESTAMP(\'" + Exp_Date + e.substring(11, 19) + "\', \'YYYY-MM-DD hh24:mi:ss\'), TO_DATE(\'" + Exp_Date + d.substring(11, 19) + "\', \'YYYY-MM-DD hh24:mi:ss\'), ?, ?, ?)";
                    ps = conn.prepareStatement(sql);
                    ps.setString(1, Lot_name);
                    ps.setInt(2, SPC_ID);
                    ps.setString(3, ph_num);
                    ps.setString(4, permitid);

                    rs = ps.executeQuery();

                    if (rs != null) {
                        System.out.println("Visitor entry created successfully");
                        trans2 = true;
                    } else {
                        System.out.println("Unable to create the Visitor entry");
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

                /*Insert into Visitor Zone Access*/
                try {
                    String sql = "INSERT INTO REL_VISITORZONEACCESS VALUES(?, \'V\')";
                    ps = conn.prepareStatement(sql);
                    ps.setString(1, permitid);

                    rs = ps.executeQuery();

                    if (rs != null) {
                        System.out.println("Visitor Zone access granted successfully");
                        trans3 = true;
                    } else {
                        System.out.println("Unable to grant the Visitor Zone access");
                        trans3 = false;
                    }
                    
                }
                catch (SQLException f){
                    System.out.println("Caught SQL Exception!" + f.getErrorCode() + "/" + f.getSQLState() + " " + f.getMessage());
                    f.printStackTrace();
                    conn.rollback();
                    trans3 = false;
                    return;
                }

                /* Insert the primary vehicle details*/
                try {
                    String sql = "INSERT INTO VEHICLE VALUES(?, ?, ?, ?, ?, ?)";
                    ps = conn.prepareStatement(sql);
                    ps.setString(1, VehicleNo);
                    ps.setString(2, manf);
                    ps.setString(3, model);
                    ps.setInt(4, Integer.parseInt(year));
                    ps.setString(5,car_color);
                    ps.setString(6, permitid);

                    rs = ps.executeQuery();

                    if (rs != null) {
                        System.out.println("Vehicle details inserted successfully");
                        trans4 = true;
                    } else {
                        System.out.println("Unable to insert vehicle details");
                        trans4 = false;
                    }
                    
                }
                catch (SQLException f){
                    System.out.println("Caught SQL Exception!" + f.getErrorCode() + "/" + f.getSQLState() + " " + f.getMessage());
                    f.printStackTrace();
                    conn.rollback();
                    trans4 = false;
                    return;
                }

                /* Update Space table to make the space unavailable*/
                try {
                    String sql = "UPDATE SPACE SET ISAVAILABLE = 0 WHERE LOTNAME = ? AND SPACEID = ? AND ISVISITOR = 1 AND SPACETYPE = ?";
                    ps = conn.prepareStatement(sql);
                    ps.setString(1, Lot_name);
                    ps.setInt(2, SPC_ID);
                    ps.setString(3, space_type);

                    rs = ps.executeQuery();

                    if (rs != null) {
                        System.out.println("Parking space made unavailable for other parkers");
                        trans5 = true;
                    } else {
                        System.out.println("Unable to give access to the parking space");
                        trans5 = false;
                    }
                    
                }
                catch (SQLException f){
                    System.out.println("Caught SQL Exception!" + f.getErrorCode() + "/" + f.getSQLState() + " " + f.getMessage());
                    f.printStackTrace();
                    conn.rollback();
                    trans5 = false;
                    return;
                }
                
            }
            catch (ParseException e){
                e.printStackTrace();

            }
            /* Transaction management check*/
            if (trans1 && trans2 && trans3 && trans4 && trans5){
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
        finally{
            if(conn!=null)
                conn.setAutoCommit(true);
            InitializeConnection.close(rs);;
            InitializeConnection.close(st);
            InitializeConnection.close(conn);;
        }
        
        
        InitializeConnection.close(conn);;
    }
}
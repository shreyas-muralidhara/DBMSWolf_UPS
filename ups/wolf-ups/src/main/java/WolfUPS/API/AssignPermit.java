package WolfUPS.API;

import java.io.BufferedReader;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import java.util.Date;
import java.util.Locale;
import java.text.SimpleDateFormat;

import WolfUPS.connection.*;

import java.sql.*;

public class AssignPermit {
    public static void assignpermit(BufferedReader reader,Connection conn) throws NumberFormatException, IOException, SQLException{
        Statement st = conn.createStatement();
        StringBuilder sb = new StringBuilder();
        PreparedStatement ps = null;
        ResultSet rs = null;
        String NV_univid, rec_type, StartDate=null,ExpireDate=null,Zoneid,VehicleNo,SpaceType, permitid="", manf, model, year,car_color;
        List emp_zones = new ArrayList();
            emp_zones.add("A");
            emp_zones.add("B");
            emp_zones.add("C");
            emp_zones.add("D");
            emp_zones.add("R");
        List stud_zones = new ArrayList();
            stud_zones.add("AS");
            stud_zones.add("BS");
            stud_zones.add("CS");
            stud_zones.add("DS");
            stud_zones.add("RS");
        

        /* Consider the input of UNIVID and check if it is valid Student/Employee ID */
        System.out.println("Please enter the University ID that needs to be Assigned a permit:");
        /* Check if the univid exists in the employee table*/
        NV_univid = reader.readLine() ;
        rs = st.executeQuery("Select * from EMPLOYEE where UNIVID = \'" + NV_univid + "\'");

        if(!rs.next()){
            /* If not exists check if the univid exists in the student table*/
            rs = st.executeQuery("Select * from STUDENT where UNIVID = \'" + NV_univid + "\'");
            
            if(!rs.next()){
                System.out.println("Not a valid NonVisitor university ID.\nSwitching back to MAIN MENU.");
                return ;
            }else{
                System.out.println("Student entry found.");
                rec_type = "student";    
            }
        }else{ 
            System.out.println("Employee entry found.");
            rec_type = "employee";
        }

        /*Check if the permit is already assigned for the Non Visitor */
        rs = st.executeQuery("Select * from NONVISITORPERMIT where UNIVID = \'" + NV_univid + "\'");
        /*If exists then terminate*/    
        if(rs.next()){
            System.out.println("Permit "+ rs.getString("PERMITNO") +" is already assigned for the " + rec_type +".\nSwitching back to MAIN MENU.");
            return ;
        }
        
        /* Extract the start date, end date and endtime for Student*/
        if(rec_type == "student"){
            /* For Student the Expiry date is 4 months from the issue*/
            rs = st.executeQuery("select to_char(sysdate,\'YYYY-MM-DD\') as startDate, TO_CHAR(ADD_MONTHS(sysdate, 4), \'YYYY-MM-DD\') as expireDate from dual");
            rs.next();
            StartDate=rs.getString("startDate");
            ExpireDate = rs.getString("expireDate");

        }else if(rec_type == "employee"){
            /* Extract the start date, end date and endtime for Employee */
            /* For employee the expire date is 12 months from the issue*/
            rs = st.executeQuery("select to_char(sysdate,\'YYYY-MM-DD\') as startDate, TO_CHAR(ADD_MONTHS(sysdate, 12), \'YYYY-MM-DD\') as expireDate from dual");
            rs.next();
            StartDate=rs.getString("startDate");
            ExpireDate = rs.getString("expireDate");
        }
        // Prompt to collect the various details for permit
        System.out.println("Please enter the zone ID to be allotted");
        Zoneid = reader.readLine();

        
        Zoneid = Zoneid.replaceAll("[^A-Za-z0-9]", "");

        if( (rec_type=="student" && (!stud_zones.contains(Zoneid))) || (rec_type=="employee" && (!emp_zones.contains(Zoneid))) ){
            System.out.println("Specified Zone cannot be assigned for the Universtiy ID.\nReturning to the Main Menu");
            return;
        }

        /* extract only first 2 characters from the string for student and 1 for employee */
        if(rec_type=="employee")
            Zoneid = Zoneid.length() < 2 ? Zoneid : Zoneid.substring(0, 1);
        else if (rec_type == "student")
            Zoneid = Zoneid.length() < 2 ? Zoneid : Zoneid.substring(0, 2);

        if(Zoneid.length()==1){

            permitid = permitid + StartDate.substring(2,4);
            permitid = permitid + Zoneid;
            permitid = permitid + UUID.randomUUID().toString().substring(0, 5);

        }else if(Zoneid.length()==2){
            permitid = permitid + StartDate.substring(2,4);
            permitid = permitid + Zoneid;
            permitid = permitid + UUID.randomUUID().toString().substring(0, 4);
        }
        //System.out.println("List " + permitid);

        System.out.println("Please enter the Space type designation in lowercase:");
        SpaceType = reader.readLine();
        System.out.println("Please enter the Primary Vehicle number format <ABC1234>:");
        VehicleNo = reader.readLine();

        /*Check if the permit is already assigned for the Vehicle */
        rs = st.executeQuery("Select * from VEHICLE where LICENSEPLATE = \'" + VehicleNo + "\'");
        /*If exists then terminate*/    
        if(rs.next()){
            System.out.println("Permit already exists for the Vehicle-" + VehicleNo +",cannot assign new permit.\nSwitching back to MAIN MENU.");
            return ;
        }

        System.out.println("Please enter the vehicle details for primary vehicle number - "+ VehicleNo);
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
                String sql = "INSERT INTO PERMIT VALUES(?, ?, TO_DATE(\'" + StartDate +"00:00:00\', \'YYYY-MM-DD hh24:mi:ss\'), ?, ?)";
                ps = conn.prepareStatement(sql);
                ps.setString(1, permitid);
                ps.setString(2, Zoneid);
                ps.setString(3, VehicleNo);
                ps.setString(4, SpaceType);

                rs = ps.executeQuery();

                if (rs != null) {
                    System.out.println("Permit created successfully");
                    trans1 = true;
                } else {
                    System.out.println("Unable to create the permit");
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

            /*Insert into Non Visitor Permit Table*/
            try {
                String sql = "INSERT INTO NONVISITORPERMIT VALUES(?, ?,TO_TIMESTAMP(\'" + ExpireDate +"23:59:00\', \'YYYY-MM-DD hh24:mi:ss\'), TO_DATE(\'" + ExpireDate +"23:59:00\', \'YYYY-MM-DD hh24:mi:ss\'))";
                ps = conn.prepareStatement(sql);
                ps.setString(1, permitid);
                ps.setString(2, NV_univid);

                rs = ps.executeQuery();

                if (rs != null) {
                    System.out.println("NonVisitor entry created successfully");
                    trans2 = true;
                } else {
                    System.out.println("Unable to create the Nonvisitor entry");
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

            /*Insert into Non Visitor Zone Access*/
            try {
                String sql = "INSERT INTO REL_NONVISITORZONEACCESS VALUES(?, ?)";
                ps = conn.prepareStatement(sql);
                ps.setString(1, permitid);
                ps.setString(2, Zoneid);

                rs = ps.executeQuery();

                if (rs != null) {
                    System.out.println("NonVisitor Zone access granted successfully");
                    trans3 = true;
                } else {
                    System.out.println("Unable to grant the Nonvisitor Zone access");
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
                    System.out.println("Primary Vehicle details inserted successfully");
                    trans4 = true;
                } else {
                    System.out.println("Unable to insert primary vehicle details");
                    trans4 = false;
                }
                
            }
            catch (SQLException e){
                System.out.println("Caught SQL Exception!" + e.getErrorCode() + "/" + e.getSQLState() + " " + e.getMessage());
                e.printStackTrace();
                conn.rollback();
                trans4 = false;
                return;
            }

            /* Insert into Assign-single for student and assign-multiple for employee*/
            try {
                /*If Student insert into Assign Single table */
                if(rec_type == "student"){
                    String sql = "INSERT INTO ASSIGNSINGLE(UNIVID, PERMITNO, VEHICLENO) VALUES(?, ?, ?)";
                    ps = conn.prepareStatement(sql);
                    ps.setString(1, NV_univid);
                    ps.setString(2, permitid);
                    ps.setString(3, VehicleNo);
                    rs = ps.executeQuery();

                    if (rs != null) {
                        System.out.println("Student Assigned-Single vehicle permit successfully");
                        trans5 = true;
                    } else {
                        System.out.println("Unable to assign single vehicle permit to student");
                        trans5 = false;
                    }
                }

                /* If Employee insert into Assign Multiple table*/
                else if(rec_type == "employee"){
                    String sql = "INSERT INTO ASSIGNMULTIPLE(UNIVID, PERMITNO, VEHICLENO) VALUES(?, ?, ?)";
                    ps = conn.prepareStatement(sql);
                    ps.setString(1, NV_univid);
                    ps.setString(2, permitid);
                    ps.setString(3, VehicleNo);
                    rs = ps.executeQuery();

                    if (rs != null) {
                        System.out.println("Employee Assigned-Multiple vehicle permit successfully");
                        trans5 = true;
                    } else {
                        System.out.println("Unable to assign Multiple vehicle permit to employee");
                        trans5 = false;
                    }
                }
            }
            catch (SQLException e){
                System.out.println("Caught SQL Exception!" + e.getErrorCode() + "/" + e.getSQLState() + " " + e.getMessage());
                e.printStackTrace();
                conn.rollback();
                trans5 = false;
                return;
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

            /* Option to insert second vehicle if that is employee*/
            if(rec_type=="employee"){

                sb.append("Would you like to add another vehicle to existing permit:\n");
                sb.append("1. Add another vehicle for "+ permitid +"\n");
                sb.append("otherwise back to Main Menu.");
                System.out.println(sb.toString());

                String choice = reader.readLine();
                switch (Integer.parseInt(choice)) {
                    case 1:
                        System.out.println("Please enter the second vehicle number");
                        VehicleNo = reader.readLine();

                        /*Check if the permit is already assigned for the 2nd Vehicle */
                        rs = st.executeQuery("Select * from VEHICLE where LICENSEPLATE = \'" + VehicleNo + "\'");
                        /*If exists then terminate*/    
                        if(rs.next()){
                            System.out.println("Permit already exists for the 2nd Vehicle-" + VehicleNo +",cannot add it to existing permit.\nSwitching back to MAIN MENU.");
                            return ;
                        }

                        System.out.println("Please enter the Second vehicle details for primary vehicle number - "+ VehicleNo);
                        System.out.println("Enter car manufacturer for "+ VehicleNo);
                        manf = reader.readLine();
                        System.out.println("Enter car model for "+ VehicleNo);
                        model = reader.readLine();
                        System.out.println("Enter year of manufacture for the car - "+ VehicleNo);
                        year = reader.readLine();
                        System.out.println("Enter car color for "+ VehicleNo);
                        car_color = reader.readLine();

                        

                         /* Insert the Second vehicle details*/
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
                                System.out.println("Second Vehicle details inserted successfully");
                                trans6 = true;
                            } else {
                                System.out.println("Unable to insert second vehicle details");
                                trans6 = false;
                            }
                            
                        }
                        catch (SQLException e){
                            System.out.println("Caught SQL Exception!" + e.getErrorCode() + "/" + e.getSQLState() + " " + e.getMessage());
                            e.printStackTrace();
                            conn.rollback();
                            trans6 = false;
                            return;
                        }

                        /* Insert into Assign-single for student and assign-multiple for employee*/
                        try {
                            String sql = "INSERT INTO ASSIGNMULTIPLE(UNIVID, PERMITNO, VEHICLENO) VALUES(?, ?, ?)";
                            ps = conn.prepareStatement(sql);
                            ps.setString(1, NV_univid);
                            ps.setString(2, permitid);
                            ps.setString(3, VehicleNo);
                            rs = ps.executeQuery();
        
                            if (rs != null) {
                                System.out.println("Employee Assigned-Multiple for 2nd vehicle permit successfully");
                                trans7 = true;
                            } else {
                                System.out.println("Unable to assign Multiple for 2nd vehicle permit to employee");
                                trans7 = false;
                            }
                        }
                        catch (SQLException e){
                            System.out.println("Caught SQL Exception!" + e.getErrorCode() + "/" + e.getSQLState() + " " + e.getMessage());
                            e.printStackTrace();
                            conn.rollback();
                            trans7 = false;
                            return;
                        }

                        /* Transaction management check*/
                        if (trans6 && trans7){
                            conn.commit();
                            System.out.println("Transaction Successful!");
                        }
                        else{
                            conn.rollback();
                            System.out.println("Transaction Failed");
                        }

                        break;

                    default:
                        return ;
                }

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

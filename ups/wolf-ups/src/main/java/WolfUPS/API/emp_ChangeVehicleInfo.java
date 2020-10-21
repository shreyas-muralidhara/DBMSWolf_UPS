package WolfUPS.API;

import java.io.BufferedReader;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

import WolfUPS.connection.InitializeConnection;
import java.sql.*;

public class emp_ChangeVehicleInfo {

    public static void changevehicleinfo(BufferedReader reader, Connection conn, String empid) throws NumberFormatException, IOException, SQLException {
        Statement st = conn.createStatement();
        ResultSet rs = null, rs_primary_vehicle = null, rs_assign_multiple = null, rs_old_vehicle = null, rs_new_vehicle = null,
                rs_new_assignmultiple = null, rs_assign_multiple_vehicle=null, rs_nonprimary_vehicle=null, rs_vehicle_to_change = null, rs_for_time=null;
        ResultSetMetaData rsmd = null;
        PreparedStatement ps = null;
        StringBuilder sb = new StringBuilder();
        String enteredPermit;
        String license_plate, manf_name, model, make_year, color;
        String values_to_update = "";
        String primary_vehicle_num, nonprimary_vehicle_num;
        String vehicle_to_change;
        int rs_temp = -1;
        int num_vehicles_on_permit = -1;
        boolean isPrimarySelected = false;
        Set<String> vehicles_set = new HashSet<String>();

        try {
            // get permits for ID
            String sql = "SELECT * FROM NONVISITORPERMIT WHERE UNIVID=\'" + empid + "\'";
            rs = st.executeQuery(sql);
            
            // if no permits
            if (!rs.next()) {
                System.out.println("You do not have any associated permits. Request UPS Admin for one.");
            } else {
                String permitNum = rs.getString("PERMITNO");
                System.out.println("Enter Permit ID to verify:\n");
                enteredPermit = reader.readLine();

                // if permit id entered by Employee is correct, then proceed
                if (permitNum.equals(enteredPermit)) {

                    sql = "SELECT PRIMARYVEHICLENO FROM PERMIT WHERE PERMITNO = \'" + permitNum + "\'";
                    rs_primary_vehicle = st.executeQuery(sql);
                    if (!rs_primary_vehicle.next()){
                        System.out.println("No primary vehicle associated with Permit.");
                        return;
                    } else {
                        primary_vehicle_num = rs_primary_vehicle.getString("PRIMARYVEHICLENO");
                        sql = "SELECT COUNT(*) AS CNT FROM ASSIGNMULTIPLE WHERE PERMITNO=\'" + permitNum + "\'";
                        rs_assign_multiple = st.executeQuery(sql);
                        if (!rs_assign_multiple.next()){
                            System.out.println("No vehicles are assigned to permit.");
                            return;
                        }
                        
                        // get number of vehicles on a permit
                        //rs_assign_multiple.last();
                        num_vehicles_on_permit = rs_assign_multiple.getInt("CNT");
                        
                        //rs_assign_multiple.beforeFirst();

                        System.out.println("Primary vehicle number associated with account is:" + primary_vehicle_num);
                        System.out.println("Vehicles associated with permit are :");
                        sql = "SELECT * FROM VEHICLE WHERE PERMITNO=\'" + permitNum + "\'";
                        rs_assign_multiple = st.executeQuery(sql);
                        rsmd = rs_assign_multiple.getMetaData();
                        int columnsNumber = rsmd.getColumnCount();
                        while (rs_assign_multiple.next()) {
                            vehicles_set.add(rs_assign_multiple.getString("LICENSEPLATE"));
                            for (int i = 1; i <= columnsNumber; i++) {
                                if (i > 1)
                                    System.out.print(",  ");
                                String columnValue = rs_assign_multiple.getString(i);
                                System.out.print(rsmd.getColumnName(i) + ": "+ columnValue);
                            }
                            System.out.println("");
                        }

                        if (num_vehicles_on_permit == 2){

                            sql = "SELECT * FROM ASSIGNMULTIPLE WHERE PERMITNO=\'" + permitNum + "\' AND VEHICLENO<>\'" + primary_vehicle_num + "\'";

                            rs_nonprimary_vehicle = st.executeQuery(sql);
                            rs_nonprimary_vehicle.next();
                            nonprimary_vehicle_num = rs_nonprimary_vehicle.getString("VEHICLENO");

                            sb.append("Please select from the following options:\n");
                            sb.append("1. Update Vehicle\n");
                            sb.append("2. Delete Vehicle\n");
                            sb.append("3. Switch Primary Vehicle\n");
                            System.out.println(sb.toString());
                            String entry01 = reader.readLine();
                            switch (entry01) {
                                case "1":
                                    /* disable the auto commit */
                                    conn.setAutoCommit(false);
                                    /* Seting the transaction Managment variables to capture the failure */
                                    boolean trans1 = false, trans2 = false, trans3 = false, trans4 = false;

                                    System.out.println("Enter the Vehicle Number of Vehicle to be updated:");
                                    vehicle_to_change = reader.readLine();
                                    if (!vehicles_set.contains(vehicle_to_change)){
                                        System.out.println("You do not have permission to Update this vehicle.");
                                        return;
                                    }
                                    sql = "SELECT * FROM ASSIGNMULTIPLE WHERE VEHICLENO=\'" + vehicle_to_change + "\'";
                                    rs_for_time = st.executeQuery(sql);
                                    rs_for_time.next();
                                    if (rs_for_time.getTimestamp("PARKEDAT") != null) {
                                        System.out.println("Cannot update a vehicle that is currently parked.");
                                        return;
                                    }
                                    
                                    // store old vehicle details for fututre updates
                                    sql = "SELECT * FROM VEHICLE WHERE LICENSEPLATE = \'" + vehicle_to_change + "\'";
                                    rs_old_vehicle = st.executeQuery(sql);

                                    System.out.println("Enter values to be updated for the vehicle.");
                                    System.out.println("\nEnter vehicle license plate value:");
                                    license_plate = reader.readLine();
                                    System.out.println("\nEnter vehicle manufacturer Name:");
                                    manf_name = reader.readLine();
                                    System.out.println("\nEnter vehicle model name:");
                                    model = reader.readLine();
                                    System.out.println("\nEnter vehicle make year:");
                                    make_year = reader.readLine();
                                    System.out.println("\nEnter vehicle color:");
                                    color = reader.readLine();

                                    if (license_plate != null && license_plate.strip() != "") {
                                        values_to_update += "LICENSEPLATE=\'" + license_plate + "\',";
                                    }
                                    if (manf_name != null && manf_name.strip() != "") {
                                        values_to_update += "MANUFACTURER=\'" + manf_name + "\',";
                                    }
                                    if (model != null && model.strip() != "") {
                                        values_to_update += "MODEL=\'" + model + "\',";
                                    }
                                    if (make_year != null && make_year.strip() != "") {
                                        values_to_update += "YEAR=\'" + make_year + "\',";
                                    }
                                    if (color != null && color.strip() != "") {
                                        values_to_update += "COLOR=\'" + color + "\',";
                                    }

                                    if (values_to_update==""){
                                        System.out.println("No new values were obtained to update.");
                                        return;
                                    }

                                    //isPrimarySelected = (primary_vehicle_num.equalsIgnoreCase(vehicle_to_change));
                                    
                                    // delete vehicle from vehicle list, expect cascade delete in assignsingle
                                    sql = "DELETE FROM VEHICLE WHERE PERMITNO = \'" + permitNum + "\' and LICENSEPLATE = \'" + vehicle_to_change + "\'";
                                    try {
                                        rs_temp = st.executeUpdate(sql);
                                        if (rs_temp != 1) {
                                            System.out.println("Old Vehicle details was not deleted successfully.");
                                            trans1 = false;
                                        } else {
                                            System.out.println("Old Vehicle details was deleted successfully!");
                                            trans1 = true;
                                        }
                                    } catch (SQLException e) {
                                        System.out.println("Caught SQL Exception!" + e.getErrorCode() + "/" + e.getSQLState() + " " + e.getMessage());
                                        e.printStackTrace();
                                        conn.rollback();
                                        trans1 = false;
                                        return;
                                    }
                                    if (primary_vehicle_num.equalsIgnoreCase(vehicle_to_change)){
                                        // update value of primary vehicle number for permit
                                        sql = "UPDATE PERMIT SET PRIMARYVEHICLENO = \'" + license_plate + "\' where PERMITNO = \'" + permitNum + "\'";
                                        try {
                                            rs_temp = st.executeUpdate(sql);
                                            if (rs_temp != 1) {
                                                System.out.println("Permit was not updated with Primary Vehicle Number successfully.");
                                                trans2=false;
                                            } else {
                                                System.out.println("Permit updated with Primary Vehicle Number successfully!");
                                                trans2=true;
                                            }
                                        } catch (SQLException e) {
                                            System.out.println("Caught SQL Exception!" + e.getErrorCode() + "/" + e.getSQLState() + " " + e.getMessage());
                                            e.printStackTrace();
                                            conn.rollback();
                                            trans2=false;
                                            return;
                                        }
                                    }

                                    // insert new vehicle values
                                    sql = "INSERT INTO VEHICLE VALUES(?, ?, ?, ?, ?, ?)";
                                    try {
                                        ps = conn.prepareStatement(sql);
                                        ps.setString(1, license_plate);
                                        if (manf_name != null && manf_name.strip() != "") {
                                            ps.setString(2, manf_name);
                                        } else {
                                            ps.setString(2, rs_old_vehicle.getString("MANUFACTURER"));
                                        }

                                        if (model != null && model.strip() != "") {
                                            ps.setString(3, model);
                                        } else {
                                            ps.setString(3, rs_old_vehicle.getString("MODEL"));
                                        }

                                        if (make_year != null && make_year.strip() != "") {
                                            ps.setString(4, make_year);
                                        } else {
                                            ps.setString(4, rs_old_vehicle.getString("YEAR"));
                                        }

                                        if (color != null && color.strip() != "") {
                                            ps.setString(5, manf_name);
                                        } else {
                                            ps.setString(5, rs_old_vehicle.getString("COLOR"));
                                        }
                                        ps.setString(6, permitNum);

                                        rs_new_vehicle = ps.executeQuery();

                                        if (rs_new_vehicle != null) {
                                            System.out.println("New Vehicle added successfully!");
                                            trans3=true;
                                        } else {
                                            System.out.println("Failed to add new vehicle.");
                                            trans3=false;

                                        }
                                    } catch (SQLException e) {
                                        System.out.println("Caught SQL Exception!" + e.getErrorCode() + "/" + e.getSQLState() + " " + e.getMessage());
                                        e.printStackTrace();
                                        conn.rollback();
                                        trans3=false;
                                        return;
                                    }

                                    // insert assign single association, that was deleted due to cascade
                                    sql = "INSERT INTO ASSIGNMULTIPLE VALUES(?, ?, ?, ? ,?, ?)";
                                    try {
                                        ps = conn.prepareStatement(sql);
                                        ps.setString(1, empid);
                                        ps.setString(2, permitNum);
                                        ps.setString(3, license_plate);
                                        ps.setString(4, null);
                                        ps.setString(5, null);
                                        ps.setString(6, null);

                                        rs_new_assignmultiple = ps.executeQuery();

                                        if (rs_new_assignmultiple != null) {
                                            System.out.println("New Vehicle associated to permit successfully.");
                                            trans4=true;
                                        } else {
                                            System.out.println("Failed to associate new vehicle with permit.");
                                            trans4=false;
                                        }
                                    } catch (SQLException e) {
                                        System.out.println("Caught SQL Exception!" + e.getErrorCode() + "/" + e.getSQLState() + " " + e.getMessage());
                                        e.printStackTrace();
                                        conn.rollback();
                                        trans4=false;
                                        return;
                                    }

                                    /* Transaction management check */
                                    if ((trans1 && trans3 && trans4) || (trans1 && trans2 && trans3 && trans4)) {
                                        conn.commit();
                                        System.out.println("Transaction Successful!");
                                    } else {
                                        conn.rollback();
                                        System.out.println("Transaction Failed");
                                    }
                                    conn.setAutoCommit(true);
                                    
                                    break;

                                case "2":
                                    
                                    // Delete Vehicle
                                    System.out.println("Enter the Vehicle Number of Vehicle to be deleted:");
                                    vehicle_to_change = reader.readLine();
                                    if (!vehicles_set.contains(vehicle_to_change)){
                                        System.out.println("You do not have permission to delete this vehicle.");
                                        return;
                                    } else {

                                        sql = "SELECT * FROM ASSIGNMULTIPLE WHERE VEHICLENO=\'"+ vehicle_to_change+"\'";
                                        rs_for_time = st.executeQuery(sql);
                                        rs_for_time.next();
                                        if (rs_for_time.getTimestamp("PARKEDAT")!=null){
                                            System.out.println("Cannot delete a vehicle that is currently parked.");
                                            return;
                                        }

                                        if (vehicle_to_change.equals(primary_vehicle_num)){
                                            sql = "UPDATE PERMIT SET PRIMARYVEHICLENO = \'" + nonprimary_vehicle_num + "\' where PERMITNO = \'" + permitNum + "\'";
                                            rs_temp = st.executeUpdate(sql);
                                            if (rs_temp != 1) {
                                                System.out.println("Permit was not updated with New Primary Vehicle Number successfully.");
                                            } else {
                                                System.out.println("Permit updated successfully!");
                                            }
                                        }
                                        
                                        sql = "DELETE FROM VEHICLE WHERE LICENSEPLATE = \'" + vehicle_to_change + "\'";
                                        rs_temp = st.executeUpdate(sql);
                                        if (rs_temp != 1) {
                                            System.out.println("Failed to delete Vehicle.");
                                        } else {
                                            System.out.println("Vehicle was deleted successfully!");
                                        }
                                    }
                                    break;

                                case "3":
                                // Switch primary vehicle
                                    sql = "UPDATE PERMIT SET PRIMARYVEHICLENO = \'" + nonprimary_vehicle_num + "\' where PERMITNO = \'" + permitNum + "\'";
                                    rs_temp = st.executeUpdate(sql);
                                    if (rs_temp != 1) {
                                        System.out.println("Permit was not updated with New Primary Vehicle Number successfully.");
                                    } else {
                                        System.out.println("New Primary Vehicle Number was set!");
                                    }
                                    break;
            
                                default:
                                    System.out.println("Incorrect Value Entered.");
                                    return;
                            }

                        } else if (num_vehicles_on_permit == 1) {
                            
                            sb = new StringBuilder();
                            sb.append("Please select from the following options:\n");
                            sb.append("1. Add a Vehicle\n");
                            sb.append("2. Update a Vehicle\n");
                            System.out.println(sb.toString());
                            String entry01 = reader.readLine();
                            switch (entry01) {
                                case "1":
                                    System.out.println("Enter details regarding the new vehicle.");
                                    System.out.println("\nEnter vehicle license plate value:");
                                    license_plate = reader.readLine();
                                    System.out.println("\nEnter vehicle manufacturer name:");
                                    manf_name = reader.readLine();
                                    System.out.println("\nEnter vehicle model name:");
                                    model = reader.readLine();
                                    System.out.println("\nEnter vehicle make year:");
                                    make_year = reader.readLine();
                                    System.out.println("\nEnter vehicle color:");
                                    color = reader.readLine();
                                    
                                    try{
                                        /* disable the auto commit*/
                                        conn.setAutoCommit(false);
                                        /* Seting the transaction Managment variables to capture the failure*/
                                        boolean trans1 = false,trans2 = false;

                                        sql = "INSERT INTO VEHICLE VALUES(?, ?, ?, ?, ?, ?)";
                                        try {
                                            ps = conn.prepareStatement(sql);
                                            ps.setString(1, license_plate);
                                            ps.setString(2, manf_name);
                                            ps.setString(3, model);
                                            ps.setString(4, make_year);
                                            ps.setString(5, color);
                                            ps.setString(6, permitNum);
                                        
                                            rs_new_vehicle = ps.executeQuery();
                                            if (rs_new_vehicle != null) {
                                                System.out.println("New Vehicle added successfully.");
                                                trans1=true;
                                            } else {
                                                System.out.println("Failed to add new vehicle.");
                                                trans1=false;
                                            }
                                    
                                        } catch (SQLException e) {
                                            System.out.println("Caught SQL Exception!" + e.getErrorCode() + "/" + e.getSQLState() + " " + e.getMessage());
                                            e.printStackTrace();
                                            conn.rollback();
                                            trans1=false;
                                            return;
                                        } 

                                        // insert assign single association, that was deleted due to cascade
                                        sql = "INSERT INTO ASSIGNMULTIPLE VALUES(?, ?, ?, ? ,?, ?)";
                                        try {
                                            ps = conn.prepareStatement(sql);
                                            ps.setString(1, empid);
                                            ps.setString(2, permitNum);
                                            ps.setString(3, license_plate);
                                            ps.setString(4, null);
                                            ps.setString(5, null);
                                            ps.setString(6, null);

                                            rs_new_assignmultiple = ps.executeQuery();

                                            if (rs_new_assignmultiple != null) {
                                                System.out.println("New Vehicle associated to permit successfully.");
                                                trans2=true;
                                            } else {
                                                System.out.println("Failed to associate new vehicle with permit.");
                                                trans2=false;
                                            }
                                        } catch (SQLException e) {
                                            System.out.println("Caught SQL Exception!" + e.getErrorCode() + "/" + e.getSQLState() + " " + e.getMessage());
                                            e.printStackTrace();
                                            conn.rollback();
                                            trans2=false;
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

                                    break;
                                case "2":

                                    /* disable the auto commit */
                                    conn.setAutoCommit(false);
                                    /* Seting the transaction Managment variables to capture the failure */
                                    boolean trans1 = false, trans2 = false, trans3 = false, trans4 = false;


                                    System.out.println("Enter the Vehicle Number of Vehicle to be updated:");
                                    vehicle_to_change = reader.readLine();
                                    if (!vehicles_set.contains(vehicle_to_change)) {
                                        System.out.println("You do not have permission to Update this vehicle.");
                                        return;
                                    }

                                    sql = "SELECT * FROM ASSIGNMULTIPLE WHERE VEHICLENO=\'" + vehicle_to_change + "\'";
                                    rs_for_time = st.executeQuery(sql);
                                    rs_for_time.next();
                                    if (rs_for_time.getTimestamp("PARKEDAT") != null) {
                                        System.out.println("Cannot update a vehicle that is currently parked.");
                                        return;
                                    }

                                    // store old vehicle details for fututre updates
                                    sql = "SELECT * FROM VEHICLE WHERE LICENSEPLATE = \'" + vehicle_to_change + "\'";
                                    rs_old_vehicle = st.executeQuery(sql);

                                    System.out.println("Enter values to be updated for the vehicle.");
                                    System.out.println("\nEnter vehicle license plate value:");
                                    license_plate = reader.readLine();
                                    System.out.println("\nEnter vehicle manufacturer Name:");
                                    manf_name = reader.readLine();
                                    System.out.println("\nEnter vehicle model name:");
                                    model = reader.readLine();
                                    System.out.println("\nEnter vehicle make year:");
                                    make_year = reader.readLine();
                                    System.out.println("\nEnter vehicle color:");
                                    color = reader.readLine();

                                    if (license_plate != null && license_plate.strip() != "") {
                                        values_to_update += "LICENSEPLATE=\'" + license_plate + "\',";
                                    }
                                    if (manf_name != null && manf_name.strip() != "") {
                                        values_to_update += "MANUFACTURER=\'" + manf_name + "\',";
                                    }
                                    if (model != null && model.strip() != "") {
                                        values_to_update += "MODEL=\'" + model + "\',";
                                    }
                                    if (make_year != null && make_year.strip() != "") {
                                        values_to_update += "YEAR=\'" + make_year + "\',";
                                    }
                                    if (color != null && color.strip() != "") {
                                        values_to_update += "COLOR=\'" + color + "\',";
                                    }

                                    if (values_to_update == "") {
                                        System.out.println("No new values were obtained to update.");
                                        return;
                                    }

                                    // delete vehicle from vehicle list, expect cascade delete in assignsingle
                                    sql = "DELETE FROM VEHICLE WHERE PERMITNO = \'" + permitNum + "\'";
                                    try {
                                        rs_temp = st.executeUpdate(sql);
                                        if (rs_temp != 1) {
                                            System.out.println("Vehicle was not deleted successfully.");
                                            trans1=false;
                                        } else {
                                            System.out.println("Vehicle was deleted successfully!");
                                            trans1=true;
                                        }
                                    } catch (SQLException e) {
                                        System.out.println("Caught SQL Exception!" + e.getErrorCode() + "/" + e.getSQLState() + " " + e.getMessage());
                                        e.printStackTrace();
                                        conn.rollback();
                                        trans1=false;
                                        return;
                                    }

                                    // update value of primary vehicle number for permit
                                    sql = "UPDATE PERMIT SET PRIMARYVEHICLENO = \'" + license_plate + "\' where PERMITNO = \'" + permitNum + "\'";
                                    try {
                                        rs_temp = st.executeUpdate(sql);
                                        if (rs_temp != 1) {
                                            System.out.println("Permit was not updated with Primary Vehicle Number successfully.");
                                            trans2=false;
                                        } else {
                                            System.out.println("Permit updated successfully!");
                                            trans2=true;
                                        }
                                    } catch (SQLException e) {
                                        System.out.println("Caught SQL Exception!" + e.getErrorCode() + "/" + e.getSQLState() + " " + e.getMessage());
                                        e.printStackTrace();
                                        conn.rollback();
                                        trans2=false;
                                        return;
                                    }

                                    // insert new vehicle values
                                    sql = "INSERT INTO VEHICLE VALUES(?, ?, ?, ?, ?, ?)";
                                    try {
                                        ps = conn.prepareStatement(sql);
                                        ps.setString(1, license_plate);
                                        if (manf_name != null && manf_name.strip() != "") {
                                            ps.setString(2, manf_name);
                                        } else {
                                            ps.setString(2, rs_old_vehicle.getString("MANUFACTURER"));
                                        }

                                        if (model != null && model.strip() != "") {
                                            ps.setString(3, model);
                                        } else {
                                            ps.setString(3, rs_old_vehicle.getString("MODEL"));
                                        }

                                        if (make_year != null && make_year.strip() != "") {
                                            ps.setString(4, make_year);
                                        } else {
                                            ps.setString(4, rs_old_vehicle.getString("YEAR"));
                                        }

                                        if (color != null && color.strip() != "") {
                                            ps.setString(5, manf_name);
                                        } else {
                                            ps.setString(5, rs_old_vehicle.getString("COLOR"));
                                        }
                                        ps.setString(6, permitNum);

                                        rs_new_vehicle = ps.executeQuery();

                                        if (rs_new_vehicle != null) {
                                            System.out.println("Vehicle added successfully!");
                                            trans3=true;

                                        } else {
                                            System.out.println("Failed to add vehicle.");
                                            trans3=false;
                                        }
                                    } catch (SQLException e) {
                                        System.out.println("Caught SQL Exception!" + e.getErrorCode() + "/" + e.getSQLState() + " " + e.getMessage());
                                        e.printStackTrace();
                                        conn.rollback();
                                        trans3=false;
                                        return;
                                    }

                                    // insert assign single association, that was deleted due to cascade
                                    sql = "INSERT INTO ASSIGNMULTIPLE VALUES(?, ?, ?, ? ,?, ?)";
                                    try {
                                        ps = conn.prepareStatement(sql);
                                        ps.setString(1, empid);
                                        ps.setString(2, permitNum);
                                        ps.setString(3, license_plate);
                                        ps.setString(4, null);
                                        ps.setString(5, null);
                                        ps.setString(6, null);

                                        rs_new_assignmultiple = ps.executeQuery();

                                        if (rs_new_assignmultiple != null) {
                                            System.out.println("Vehicle associated to permit successfully.");
                                            trans4=true;
                                        } else {
                                            System.out.println("Failed to associate vehicle with permit.");
                                            trans4=false;
                                        }
                                    } catch (SQLException e) {
                                        System.out.println("Caught SQL Exception!" + e.getErrorCode() + "/" + e.getSQLState() + " " + e.getMessage());
                                        e.printStackTrace();
                                        conn.rollback();
                                        trans4=false;
                                        return;
                                    }

                                    /* Transaction management check */
                                    if (trans1 && trans2 && trans3 && trans4) {
                                        conn.commit();
                                        System.out.println("Transaction Successful!");
                                    } else {
                                        conn.rollback();
                                        System.out.println("Transaction Failed");
                                    }
                                    conn.setAutoCommit(true);

                                    break;

                                default:
                                    return;
                            }

                        } else {
                            System.out.println("You have exceeded the limit of Vehicle associated with permit. Contact UPS Admin!");
                            return;
                        }


                        sql = "SELECT PRIMARYVEHICLENO FROM PERMIT WHERE PERMITNO = \'" + permitNum + "\'";
                        rs_primary_vehicle = st.executeQuery(sql);
                        rs_primary_vehicle.next();
                        primary_vehicle_num = rs_primary_vehicle.getString("PRIMARYVEHICLENO");
                        System.out.println("Primary vehicle number associated with account is:"
                                + primary_vehicle_num);
                        
                    }
                } else{
                    System.out.println("Incorrect permit ID entered.");
                    return;
                }
            }
        } catch (SQLException e) {
            System.out.println("Caught SQL Exception!" + e.getErrorCode() + "/" + e.getSQLState() + " " + e.getMessage());
            e.printStackTrace();
            conn.rollback();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (conn != null)
                conn.setAutoCommit(true);
            InitializeConnection.close(rs);
            InitializeConnection.close(st);
            System.out.println("Vehicles associated with permit are :");
            emp_ViewVehicleInfo.viewvehicleinfo(reader, conn, empid);
        }
    }
}
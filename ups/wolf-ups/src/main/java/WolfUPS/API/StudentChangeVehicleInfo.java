package WolfUPS.API;

import java.io.BufferedReader;
import java.io.IOException;
import java.sql.SQLException;
import WolfUPS.connection.InitializeConnection;
import java.sql.*;

public class StudentChangeVehicleInfo {
    public static void changeVehicle(BufferedReader reader, Connection conn, String univid)
            throws NumberFormatException, IOException, SQLException {
        Statement st = conn.createStatement();
        ResultSet rs = null, rs_assign_single = null, rs_old_vehicle = null, rs_new_vehicle=null, rs_new_assignsingle=null;
        PreparedStatement ps = null;
        String enteredPermit;
        String license_plate, manf_name, model, make_year, color;
        String values_to_update="";
        try {
            // get permits
            String getsql = "SELECT * FROM NONVISITORPERMIT WHERE UNIVID=\'" + univid +"\'";
            rs = st.executeQuery(getsql);
            //if no permits
            if (!rs.next()) {
                System.out.println("You do not have any associated permits. Request UPS Admin for one.");
            } else {
                String permitNum = rs.getString("PERMITNO");
                System.out.println("Enter Permit ID to verify:\n");
                enteredPermit = reader.readLine();
                // System.out.println(permitNum + "     " + enteredPermit);
                // if permit id entered by Student is correct, then proceed
                if (permitNum.equals(enteredPermit)){
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

                    // String values = "";
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

                    // Look for entry in assign single table
                    String SQL_AssignRelation_Record = "SELECT * FROM ASSIGNSINGLE WHERE PERMITNO=\'" + permitNum + "\'";
                    rs_assign_single = st.executeQuery(SQL_AssignRelation_Record);
                    if (!rs_assign_single.next()){
                        System.out.println("Value not found for Permit in Assign Table.");
                        return;
                    } else {
                        Timestamp parkedAt = rs_assign_single.getTimestamp("PARKEDAT");
                        // Not parked and there are values to update
                        if (parkedAt == null && values_to_update != ""){
                            values_to_update = values_to_update.replaceAll(",$", "");

                            // License was updated
                            if (license_plate != null && license_plate.strip() != "" && !license_plate.strip().equals(rs_assign_single.getString("VEHICLENO"))){
                        
                                /* disable the auto commit */
                                conn.setAutoCommit(false);
                                /* Seting the transaction Managment variables to capture the failure */
                                boolean trans1 = false, trans2 = false, trans3 = false, trans4 = false;
                                int rs_temp;

                                // store old vehicle details for fututre updates
                                String SQL_get_vehicle = "SELECT * FROM VEHICLE WHERE PERMITNO = \'" + permitNum + "\'";
                                rs_old_vehicle = st.executeQuery(SQL_get_vehicle);
                                rs_old_vehicle.next();


                                // delete vehicle from vehicle list, expect cascade delete in assignsingle
                                String SQL_delete_vehicle = "DELETE FROM VEHICLE WHERE PERMITNO = \'" + permitNum + "\'";
                                try {
                                    rs_temp = st.executeUpdate(SQL_delete_vehicle);
                                    if (rs_temp != 1){
                                        System.out.println("Vehicle was not deleted successfully.");
                                        trans1=false;
                                    } else {
                                        System.out.println("Vehicle was deleted successfully!");
                                        trans1 = true;
                                    }
                                } catch (SQLException e) {
                                    System.out.println("Caught SQL Exception!" + e.getErrorCode() + "/" + e.getSQLState() + " " + e.getMessage());
                                    e.printStackTrace();
                                    conn.rollback();
                                    trans1 = false;
                                    return;
                                }

                                // update value of primary vehicle number for permit
                                String SQL_update_permit = "UPDATE PERMIT SET PRIMARYVEHICLENO = \'" + license_plate + "\' where PERMITNO = \'" + permitNum + "\'";
                                try { 
                                    rs_temp = st.executeUpdate(SQL_update_permit); 
                                    if (rs_temp != 1) {
                                        System.out.println("Permit was not updated with Primary Vehicle Number successfully.");
                                        trans2 = false;
                                    } else {
                                        System.out.println("Permit updated successfully!");
                                        trans2 = true;
                                    }
                                }
                                catch (SQLException e) {
                                    System.out.println("Caught SQL Exception!" + e.getErrorCode() + "/" + e.getSQLState() + " " + e.getMessage());
                                    e.printStackTrace();
                                    conn.rollback();
                                    trans2 = false;
                                    return;
                                }
                                
                                // insert new vehicle values
                                String SQL_insert_vehicle = "INSERT INTO VEHICLE VALUES(?, ?, ?, ?, ?, ?)";
                                try {
                                    ps = conn.prepareStatement(SQL_insert_vehicle);
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

                                    if (rs_new_vehicle != null){
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
                                    trans3 = false;
                                    return;
                                }
                                
                                // insert assign single association, that was deleted due to cascade
                                String SQL_insert_assignsingle = "INSERT INTO ASSIGNSINGLE VALUES(?, ?, ?, ? ,?, ?)";
                                try {
                                    ps = conn.prepareStatement(SQL_insert_assignsingle);
                                    ps.setString(1, univid);
                                    ps.setString(2, permitNum);
                                    ps.setString(3 , license_plate);
                                    ps.setString(4, null);
                                    ps.setString(5, null);
                                    ps.setString(6, null);

                                    rs_new_assignsingle = ps.executeQuery();

                                    if (rs_new_assignsingle != null){
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
                                    trans4 = false;
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

                            } else {
                                String SQL_updt_vehicle = "UPDATE VEHICLE SET " + values_to_update + " WHERE PERMITNO=\'" + permitNum + "\'";
                                st.executeUpdate(SQL_updt_vehicle);
                                System.out.println("Vehicle Update Complete.\n");
                                
                            }
                        }
                    }
                    
                } else {
                    System.out.println("\nIncorrect Permit ID was entered");
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
            System.out.println("\nUpdated Vehicle details:");
            StudentViewVehicleInfo.viewVehicleInfo(reader, conn, univid);
        }
    }
}

package WolfUPS.UserInterface;

import java.io.BufferedReader;
import java.io.IOException;
import java.sql.SQLException;

import WolfUPS.connection.*;
import WolfUPS.API.*;

import java.sql.*;

public class EmployeeUI {
    public static void employeeUI(BufferedReader reader) throws NumberFormatException, IOException, SQLException{
        Connection conn = InitializeConnection.InitConn();
        Statement st = conn.createStatement();
        StringBuilder sb = new StringBuilder();

        // Prompt to enter the Employee University ID, authenticate the user as Employee.
        System.out.println("Please enter the Employee University ID:");
        String emp_id = reader.readLine();
        ResultSet rs = st.executeQuery("Select * from EMPLOYEE where UNIVID = \'" + emp_id + "\'");
        
        if(!rs.next()){
            System.out.println("Not a valid Employee University ID.\nSwitching back to MAIN MENU");
            return ;
        }    
        StringBuilder sb1 = null;
        sb1 = new StringBuilder();
            
        System.out.println("Employee entry found");
        sb1.append("Please select from the EMPLOYEE MENU options:\n");
        sb1.append("1. Enter lot\n");
        sb1.append("2. Exit lot\n");
        sb1.append("3. View Permits\n");
        sb1.append("4. Change Vehicle Info\n");
        sb1.append("5. Pay Citation\n");
        sb1.append("Otherwise back to Main Menu\n");
        System.out.println(sb1.toString());


        try {
            String entry01 = reader.readLine();
            switch (entry01) {
                case "1":
                    emp_EnterLot.enterlot(reader,conn,emp_id);
                    break;
                case "2":
                    emp_ExitLot.exitlot(reader,conn,emp_id);
                    break;
                case "3":
                    emp_ViewVehicleInfo.viewvehicleinfo(reader,conn, emp_id);
                    break;
                case "4":
                    emp_ChangeVehicleInfo.changevehicleinfo(reader,conn, emp_id);
                    break;
                case "5":
                    PayCitation.paycitation(reader,conn);
                    break;
                default:
                    return ;
            }
        }
        catch (IOException e){
            System.out.println("IO Exception occurred.");
            e.printStackTrace();
        } catch (Exception e){
            e.printStackTrace();
        }
        finally {
            InitializeConnection.close(rs);;
            InitializeConnection.close(st);
            InitializeConnection.close(conn);;
        } 
        
    }

}

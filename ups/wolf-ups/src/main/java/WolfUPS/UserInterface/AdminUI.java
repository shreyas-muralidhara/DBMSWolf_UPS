package WolfUPS.UserInterface;

import java.io.BufferedReader;
import java.io.IOException;
import java.sql.SQLException;

import WolfUPS.connection.*;
import WolfUPS.API.*;

import java.sql.*;

public class AdminUI {
    public static void adminUI(BufferedReader reader) throws NumberFormatException, IOException, SQLException{
        Connection conn = InitializeConnection.InitConn();
        Statement st = conn.createStatement();
        StringBuilder sb = new StringBuilder();

        // Prompt to enter the Admin University ID, authenticate the user as Admin.
        System.out.println("Please enter the Admin University ID:");
        String admin_check = "Select * from EMPLOYEE where UNIVID = \'" + reader.readLine() + "\' AND ISADMIN=1";
        ResultSet rs = st.executeQuery(admin_check);
        
        if(!rs.next()){
            
            System.out.println("Not a valid Admin Univid\n");

            sb.append("Would you like to try again:\n");
            sb.append("1. Re-Enter Admin univid\n");
            sb.append("otherwise back to Main Menu.");
            System.out.println(sb.toString());

            try {
                String choice = reader.readLine();
                switch (Integer.parseInt(choice)) {
                    case 1:
                        System.out.println("Please enter the Admin University ID:");
                        admin_check = "Select * from EMPLOYEE where UNIVID = \'" + reader.readLine() + "\' AND ISADMIN=1";
                        rs = st.executeQuery(admin_check);

                        if(!rs.next()){
                            System.out.println("Not a valid Admin Univid.\nSwitching back to MAIN MENU");
                            return ;
                        }    
                        break;
                    default:
                        return ;
                }
            }
            catch (Exception e){
                e.printStackTrace();
            }
        }
        StringBuilder sb1 = null;
        sb1 = new StringBuilder();
            
        System.out.println("Admin entry found");
        sb1.append("Please select from the ADMIN MENU options:\n");
        sb1.append("1. Add lot\n");
        sb1.append("2. Assign Zone to a lot\n");
        sb1.append("3. Assign Type to space\n");
        sb1.append("4. Assign Permit\n");
        sb1.append("5. Check Visitor Valid parking\n");
        sb1.append("6. Check Non Vistor Valid parking\n");
        sb1.append("otherwise back to Main Menu\n");
        System.out.println(sb1.toString());


        try {
            String entry01 = reader.readLine();
            switch (Integer.parseInt(entry01)) {
                case 1:
                    AddLot.addlot(reader,conn);
                    break;
                case 2:
                    //AssignZoneToLot.assignzonetolot(reader);
                    AssignZoneToLot.assignzonetolot(reader,conn);
                    break;
                case 3:
                    //AssignTypeToSpace.assigntypetospace(reader);
                    System.out.println("Assign Type To Space");
                    AssignTypeToSpace.assigntypetospace(reader,conn);
                    break;
                case 4:
                    //CheckVValidParking.checkvvalidparking(reader);
                    System.out.println("Check Visitor Valid Parking");
                    break;
                case 5:
                    //CheckNVValidParking.checknvvalidparking(reader);
                    System.out.println("Check Non-Visitor Valid Parking");
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

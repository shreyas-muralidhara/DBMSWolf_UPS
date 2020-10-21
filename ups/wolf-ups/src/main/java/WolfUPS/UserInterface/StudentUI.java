package WolfUPS.UserInterface;

import java.io.BufferedReader;
import java.io.IOException;
import java.sql.SQLException;
import java.sql.*;
import WolfUPS.connection.InitializeConnection;
import WolfUPS.API.*;

public class StudentUI {

    static String UNIV_ID;
    
    public static void studentUI(BufferedReader reader) throws NumberFormatException, IOException, SQLException{
        Connection conn = InitializeConnection.InitConn();
        Statement st = conn.createStatement();
        StringBuilder sb = new StringBuilder();

        // Prompt to enter the Student University ID, authenticate the user as a Student.
        System.out.println("Enter Student's University ID:");
        UNIV_ID = reader.readLine();
        String student_check = "Select * from STUDENT where UNIVID = \'" + UNIV_ID + "\'";
        ResultSet rs = st.executeQuery(student_check);
        
        if(!rs.next()){
            System.out.println("Not a valid Admin Univid.\nSwitching back to MAIN MENU");
            return ;
        }    
        
        StringBuilder sb1 = null;
        sb1 = new StringBuilder();
            
        System.out.println("Student entry found!\n");
        sb1.append("Please select from the Student MENU options:\n");
        sb1.append("1. Allocate Lot\n");
        sb1.append("2. Exit Lot\n");
        sb1.append("3. View Vehicle Info\n");
        sb1.append("4. Change Vehicle\n");
        sb1.append("5. Pay Existing Citations\n");
        sb1.append("Otherwise back to Main Menu\n");
        System.out.println(sb1.toString());

        try {
            String entry01 = reader.readLine();
            switch (entry01) {
                case "1":
                    StudentEnterLot.enterLot(reader, conn, UNIV_ID);
                    break;
                case "2":
                    StudentExitLot.exitlot(reader, conn, UNIV_ID);
                    break;
                case "3":
                    StudentViewVehicleInfo.viewVehicleInfo(reader, conn, UNIV_ID);
                    break;
                case "4":
                    StudentChangeVehicleInfo.changeVehicle(reader, conn, UNIV_ID);
                    break;
                case "5":
                    PayCitation.paycitation(reader, conn);
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

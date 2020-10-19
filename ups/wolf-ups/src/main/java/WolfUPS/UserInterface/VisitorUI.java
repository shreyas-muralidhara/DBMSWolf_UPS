package WolfUPS.UserInterface;

import java.io.BufferedReader;
import java.io.IOException;
import java.sql.SQLException;

import WolfUPS.connection.*;
import WolfUPS.API.*;

import java.sql.*;

public class VisitorUI {
    public static void visitorui(BufferedReader reader) throws NumberFormatException, IOException, SQLException{
        Connection conn = InitializeConnection.InitConn();
        Statement st = conn.createStatement();
        StringBuilder sb = new StringBuilder();

        
        StringBuilder sb1 = null;
        sb1 = new StringBuilder();
            
        sb1.append("Please select from the VISITOR MENU options:\n");
        sb1.append("1. Get Permit\n");
        sb1.append("2. Exit Lot\n");
        sb1.append("3. Pay Citation\n");
        System.out.println(sb1.toString());


        try {
            String entry01 = reader.readLine();
            switch (Integer.parseInt(entry01)) {
                case 1:
                    System.out.println("Get Permit");
                    GetVisitorPermit.getvisitorpermit(reader,conn);
                    break;
                case 2:
                    //AssignZoneToLot.assignzonetolot(reader);
                    //AssignZoneToLot.assignzonetolot(reader,conn);
                    break;
                case 3:
                    //AssignTypeToSpace.assigntypetospace(reader);
                    //System.out.println("Assign Type To Space");
                    //AssignTypeToSpace.assigntypetospace(reader,conn);
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
            InitializeConnection.close(st);
            InitializeConnection.close(conn);;
        } 
        
    }
    
}

package WolfUPS.UserInterface;

import java.io.BufferedReader;
import java.io.IOException;
import java.sql.SQLException;

import WolfUPS.connection.*;

import java.sql.*;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DemoQueries {
    public static void demoqueries(BufferedReader reader) throws NumberFormatException, IOException, SQLException{
        Connection conn = InitializeConnection.InitConn();
        Statement st = conn.createStatement();
        PreparedStatement ps = null;
        ResultSet rs = null;
        //String Licence, Lotname, Space_id;
        //Timestamp Expire_time, exitTime;
        // StringBuilder sb = new StringBuilder();
       
        StringBuilder sb1 = null;
        sb1 = new StringBuilder();
            
        System.out.println("Demo Queries");
        sb1.append("Please select from the options below:\n");
        sb1.append("1. Show the list of zones for each lot as tuple pairs (lot, zone)\n");
        sb1.append("2. Get permit information for a given employee with UnivID: 1006020\n");
        sb1.append("3. Get vehicle information for a particular UnivID: 1006003\n");
        sb1.append("4. Find an available space# for Visitor for an electric vehicle in a specific parking lot: Justice Lot\n");
        sb1.append("5. Find any cars that are currently in violation\n");
        sb1.append("6. How many employees have permits for parking zone D\n");
        sb1.append("\n");
        sb1.append("Report Queries\n");
        sb1.append("7. For each lot, generate the total number of citations given in all zones in the lot for a three month period (07/01/2020 - 09/30/2020)\n");
        sb1.append("8. For Justice Lot , generate the number of visitor permits in a date range: 08/12/2020 - 08/20/2020 , grouped by permit type e.g. regular, electric, handicapped\n");
        sb1.append("9. For each visitor’s parking zone, show the total amount of revenue generated (including pending citation fines) for each day in August 2020\n");

        System.out.println(sb1.toString());


        try {
            String entry01 = reader.readLine();
            switch (entry01) {
                case "1":
                    String sql = "Select NAME, ZONEID from REL_ALLOCATED GROUP BY NAME,ZONEID ORDER BY NAME,ZONEID";
                    rs = st.executeQuery(sql);
                    if (!rs.isBeforeFirst()) {
                        System.out.println("Query returns no rows!");
                        break;
                    }

                    System.out.println("LOT NAME    Zone");
				    while (rs.next()) {
					    System.out.println(rs.getString(1) + "	" + rs.getString(2));
                    }
                    break;

                case "2":
                    sql = "Select NV.UNIVID, NV.PERMITNO, P.STARTDATE, NV.EXPIRETIME, P.ZONEID, P.PRIMARYVEHICLENO, P.SPACETYPE, V.LICENSEPLATE from NONVISITORPERMIT NV, PERMIT P, VEHICLE V WHERE NV.PERMITNO=P.PERMITNO AND P.PERMITNO = V.PERMITNO AND NV.UNIVID = \'EM004\'";
                    rs = st.executeQuery(sql);
                    if (!rs.isBeforeFirst()) {
                        System.out.println("Query returns no rows!");
                        break;
                    }

                    System.out.println("Univid	Permit_num  Start_date	            Expire_time	Zone_ID     Primary_Vehicle     Space_type     List_of_vehicles_on_the_permit");
                    while (rs.next()) {
                        System.out.println(rs.getString(1) + "	" + rs.getString(2) + "     " + rs.getString(3) + "     " + rs.getString(4) + "     " + rs.getString(5) + " " + rs.getString(6) + "	    " + rs.getString(7) + "	    " + rs.getString(8));
                    }
                    break;

                case "3":
                    sql = "Select NV.UNIVID, V.LICENSEPLATE, V.MANUFACTURER, V.MODEL, V.YEAR, V.COLOR, V.PERMITNO from NONVISITORPERMIT NV, VEHICLE V WHERE NV.PERMITNO=V.PERMITNO AND NV.UNIVID = \'EM004\'";
                    rs = st.executeQuery(sql);
                    if (!rs.isBeforeFirst()) {
                        System.out.println("Query returns no rows!");
                        break;
                    }

                    System.out.println("Univid	License_Plate  Manufacturer Model     Year     Color     Permit_num");
                    while (rs.next()) {
                        System.out.println(rs.getString(1) + "	" + rs.getString(2) + "         " + rs.getString(3) + "         " + rs.getString(4) + "     " + rs.getString(5) + "     " + rs.getString(6) + "	    " + rs.getString(7));
                    }
                    break;
                 
                case "4":
                    sql = "Select MAX(SPACEID) AS SPC_ID FROM SPACE WHERE ISVISITOR = '1' AND SPACETYPE = 'electric' AND LOTNAME = 'Justice Lot' AND ISAVAILABLE = '1'";
                    rs = st.executeQuery(sql);
                    if (!rs.isBeforeFirst()) {
                        System.out.println("Query returns no rows!");
                        break;
                    }

                    System.out.println("Univid	License_Plate  Manufacturer Model     Year     Color     Permit_num");
                    while (rs.next()) {
                        System.out.println(rs.getString("SPC_ID") + " space is available");
                    }
                    break;
                    
                case "5":
                    sql = "SELECT CARLICENSENO, MODEL, COLOR, VIOLATIONCATEGORY FROM CITATION WHERE STATUS = 'Unpaid'";
                    rs = st.executeQuery(sql);
                    if (!rs.isBeforeFirst()) {
                        System.out.println("Query returns no rows!");
                        break;
                    }

                    System.out.println("Car_License	Model     Color     Permit_num  Category");
                    while (rs.next()) {
                        System.out.println(rs.getString(1) + "	" + rs.getString(2) + "	" + rs.getString(3) + "	" + rs.getString(4));
                    }
                    break;
                    
                case "6":
                    sql = "SELECT COUNT(*) AS COUNT_EMPLOYEE FROM PERMIT P, NONVISITORPERMIT NV, EMPLOYEE E WHERE P.ZONEID = 'D' AND P.PERMITNO = NV.PERMITNO AND NV.UNIVID=E.UNIVID";
                    rs = st.executeQuery(sql);
                    if (!rs.isBeforeFirst()) {
                        System.out.println("Query returns no rows!");
                        break;
                    }

                    
                    while (rs.next()) {
                        System.out.println(rs.getString("COUNT_EMPLOYEE") + " employees have permits for parking in Zone D");
                    }
                    break;
                
                case "7":
                    sql = "SELECT LOT, COUNT(*) FROM CITATION WHERE ISSUEDATE BETWEEN TO_DATE('07/01/2020','MM/DD/YYYY') AND TO_dATE('09/30/2020','MM/DD/YYYY') GROUP BY LOT";
                    rs = st.executeQuery(sql);
                    if (!rs.isBeforeFirst()) {
                        System.out.println("Query returns no rows!");
                        break;
                    }

                    System.out.println("Lotname     Number_of_citations");
                    while (rs.next()) {
                        System.out.println(rs.getString(1) + "	" + rs.getString(2));
                    }
                    break;

                case "8":
                    sql = "SELECT SPACETYPE, COUNT(*) FROM VISITORPERMIT V, PERMIT P WHERE V.PERMITNO = P.PERMITNO AND V.LOTNAME = 'Justice Lot' AND P.STARTDATE BETWEEN TO_DATE('07/01/2020','MM/DD/YYYY') AND TO_dATE('09/30/2020','MM/DD/YYYY') GROUP BY P.SPACETYPE";
                    rs = st.executeQuery(sql);
                    if (!rs.isBeforeFirst()) {
                        System.out.println("Query returns no rows!");
                        break;
                    }

                    System.out.println("Space_type     No_of_permits");
                    while (rs.next()) {
                        System.out.println(rs.getString(1) + "	" + rs.getString(2));
                    }
                    break;

                case "9":
                    sql = "SELECT LOT, ISSUEDATE, SUM(VIOLATIONFEE) FROM CITATION WHERE ISSUEDATE BETWEEN TO_DATE('08/01/2020','MM/DD/YYYY') AND TO_dATE('10/31/2020','MM/DD/YYYY')  GROUP BY LOT, ISSUEDATE ORDER BY LOT, ISSUEDATE";
                    rs = st.executeQuery(sql);
                    if (!rs.isBeforeFirst()) {
                        System.out.println("Query returns no rows!");
                        break;
                    }

                    
                    System.out.println("Lotname     Issue_date    Tota_revenue");
                    while (rs.next()) {
                        System.out.println(rs.getString(1) + "	" + rs.getString(2) + "	" + rs.getString(3));
                    }
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

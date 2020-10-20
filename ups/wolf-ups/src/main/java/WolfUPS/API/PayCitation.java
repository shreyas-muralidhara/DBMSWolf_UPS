package WolfUPS.API;

import java.io.BufferedReader;
import java.io.IOException;
import java.sql.SQLException;

import WolfUPS.connection.*;

import java.sql.*;

public class PayCitation {
    public static void paycitation(BufferedReader reader,Connection conn) throws NumberFormatException, IOException, SQLException{
        Statement st = conn.createStatement();
        StringBuilder sb = new StringBuilder();
        PreparedStatement ps = null;
        ResultSet rs = null;
        Integer citation_num;

        // Prompt to enter the Citation number.
        System.out.println("Please enter the citation number");
        citation_num = Integer.parseInt(reader.readLine());
        
        String sql = "SELECT * FROM CITATION WHERE CITATIONNO = " + citation_num;
        
        rs = st.executeQuery(sql);
        if (rs != null) {
            while (rs.next()) {
                Integer s = rs.getInt("VIOLATIONFEE");
                String n = rs.getString("STATUS");
                
                
                if (n.toLowerCase().substring(0, 4).equalsIgnoreCase("paid")) {
                    System.out.println("Citation fees of " + s + "$ already paid");
                    return;
                }
                else{
                    System.out.println("The unpaid citation fees is " + s + "$");
                }
                
            }
            
        } else {
            System.out.println("No citations present with this citation number");
            return;
        }

        //Prompt to the user to ask whether to pay the citation fees.

        StringBuilder sb1 = null;
        sb1 = new StringBuilder();

        
        sb1.append("Please select from the options below:\n");
        sb1.append("1. Pay Citation Fee\n");
        sb1.append("2. Return without paying\n");


        try {
            /*disable the auto commit*/
            conn.setAutoCommit(false);
            /* Seting the transaction Managment variables to capture the failure*/
            boolean trans1 = false;
            String choice = reader.readLine();

            if (choice.equalsIgnoreCase("1")){
                String sql1 = "UPDATE CITATION SET STATUS = \'paid\' WHERE CITATIONNO = " + citation_num;
                rs = st.executeQuery(sql1);
                if (rs != null) {
                    System.out.println("Citation fees paid successfully");
                    trans1 = true;
                } else {
                    System.out.println("Citation fee payment failed");
                    trans1 = false;
                }
            }
            else{
                //EmployeeUI.employeeUI(reader);
                System.out.println("Returned without paying");
                return;
            }

        }
        catch (IOException e){
            System.out.println("IO Exception occurred.");
            e.printStackTrace();
            conn.rollback();
        }
        finally {
            if(conn!=null)
                conn.setAutoCommit(true);
            InitializeConnection.close(rs);;
            //InitializeConnection.close(stmt);
            InitializeConnection.close(conn);;
        } 
    }

    
    
}

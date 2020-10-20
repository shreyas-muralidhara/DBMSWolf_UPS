package WolfUPS.API;

import java.io.IOException;
import java.sql.SQLException;

import java.sql.*;

public class IssueCitation {
    public static void issuecitation(Connection conn, String uniqueid, String LicenseNo, String Lot, String Model, String Color, String ViolationCat, Integer ViolationFee, String type) throws NumberFormatException, IOException, SQLException
    {
        Statement st = conn.createStatement();
        StringBuilder sb = new StringBuilder();
        PreparedStatement ps = null;
        ResultSet rs = null;

        String issueDate,issueTime,PaymentDue,sql;
        Integer CitationNo;

        rs = st.executeQuery("select to_char(sysdate,\'YYYY-MM-DD\') as CreateDate, to_char(current_timestamp,\'YYYY-MM-DD hh24:mi:ss\') as Timestamp, to_char(sysdate+30,\'YYYY-MM-DD\') as DueDate from dual");
        rs.next();
        issueDate = rs.getString("CreateDate");
        issueTime = rs.getString("Timestamp");
        PaymentDue = rs.getString("DueDate");
            
        rs = st.executeQuery("Select Max(CITATIONNO) from CITATION");

        if(!rs.next())
            CitationNo = 10001;
        else
            CitationNo = rs.getInt(1) + 1;

        try{
            /* disable the auto commit*/
            conn.setAutoCommit(false);
            /* Seting the transaction Managment variables to capture the failure*/
            boolean trans1 = false,trans2 = false;

            /*Insert Citation into the Citation table*/
            try {
                sql = "INSERT INTO CITATION VALUES(?, ?, ?, ?, TO_DATE(\'" + issueDate +"00:00:00\', \'YYYY-MM-DD hh24:mi:ss\'), ?, ?, ?, TO_TIMESTAMP(\'" + issueTime +"\', \'YYYY-MM-DD hh24:mi:ss\'), ?, TO_DATE(\'" + PaymentDue +"00:00:00\', \'YYYY-MM-DD hh24:mi:ss\'), ?)";
                ps = conn.prepareStatement(sql);
                ps.setInt(1, CitationNo);
                ps.setString(2, LicenseNo);
                ps.setString(3, Model);
                ps.setString(4, Color);
                ps.setString(5, "Unpaid");
                ps.setString(6, type);
                ps.setString(7, Lot);
                ps.setString(8, ViolationCat);
                ps.setInt(9, ViolationFee);
                rs = ps.executeQuery();

                if (rs != null) {
                    System.out.println("Citation "+ CitationNo +" entry created successfully");
                    trans1 = true;
                } else {
                    System.out.println("Unable to create the Citation");
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


            /*Insert into the Notification Non visitor/Visitor table*/
            try {
                if (type.equalsIgnoreCase("VISITOR"))
                    sql = "INSERT INTO NOTIFICATIONVISITOR VALUES(?, ?)";
                else
                    sql = "INSERT INTO NOTIFICATIONNONVISITOR VALUES(?, ?)";
                
                ps = conn.prepareStatement(sql);
                ps.setString(1, uniqueid);
                ps.setInt(2, CitationNo);
                
                rs = ps.executeQuery();

                if (rs != null) {
                    System.out.println("Notification for "+ ViolationCat +" citation sent successfully");
                    trans2 = true;
                } else {
                    System.out.println("Unable to send notification to " + type);
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
        return ;
    }
}

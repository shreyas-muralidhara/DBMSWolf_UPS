package wolfUPS.UserInterface;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Main {
    public static void main(String[] args) throws Exception {
        StringBuilder sb = null;
        System.out.println("Wolf University Parking Services Application");

        while(true){
            sb = new StringBuilder();
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            
            sb.append("Please enter the number from the below menu options:\n");
            sb.append("1. UPS Admin\n");
            sb.append("2. Employee\n");
            sb.append("3. Student\n");
            sb.append("4. Visitor\n");
            sb.append("5. Demo/Preassigned Queries\n");
            sb.append("otherwise Exit\n");
            System.out.println(sb.toString());

            try {
                String choice = reader.readLine();
                switch (Integer.parseInt(choice)) {
                    case 1:
                        //AdminUI.adminUI(reader);
                        System.out.println("Admin Menu");
                        break;
                    case 2:
                        //EmployeeUI.employeeUI(reader);
                        System.out.println("Employee Menu");
                        break;
                    case 3:
                        //StudentUI.studentUI(reader);
                        System.out.println("Student Menu");
                        break;
                    case 4:
                        //VisitorUI.visitorUI(reader);
                        System.out.println("Visitor Menu");
                        break;
                    case 5:
                        //DemoQueryUI.demoqueryUI(reader);
                        System.out.println("List of demo queries");
                        break;
                    default:
                        System.exit(0);
                        break;
                }
            }
            catch (IOException e) {
                System.out.println("IO Exception occurred.");
                e.printStackTrace();
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
        
    }
}

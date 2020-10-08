### DMBSWolfpub_UPS
## Project Description
*The goal of this project is for you to develop a database application to help University Parking Services (UPS) manage the campus parking lots and its users*. 
* The UPS issues parking permits to employees, students and visitors and there are different eligibility constraints for parking permits in the different lots as well as time restrictions for eligibility. 
* In addition to the permits, UPS issues tickets/citations for parking violations and collects fees for them. University students and employees all have a univid (integer) which is a unique identifier for identifying them and linking them to their vehicles as well as an attribute status that is either ‘S’ or ‘E’ or ‘A’, depending on whether a student or an employee or administrator (who is also an employee but works with UPS).

## User Interfaces 
* We assume that our system has a main entry screen where users can select an option of what role they want to play as either - admin, university user, visitor. 
* Then, for each role, the list of functions that they can perform will be listed and a user will select which function they want to perform. 
* University users should have a login step before their functions are displayed. (Visitors need not log in, because they dont have univids. Their functions should be available once they choose that role). 
* Example of menu list - numbered list of options and users selecting the corresponding number for the option they want, should suffice:
one the main screen you may present options like:
```   
1. UPS Admin Role
2. Employee Role
3. Student Role
4. Select and Execue preassigned queries
...
Please enter the number for the menu option desired
```

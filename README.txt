KEC R2024 Student Academic & Continuous Evaluation System (JDBC)

REQUIREMENTS
- JDK 17 or later
- MySQL Server
- MySQL Connector/J JAR
- VS Code with Java Extension Pack, or IntelliJ IDEA

SETUP
1. Start MySQL.
2. Open MySQL Workbench or MySQL command line.
3. Run kec_schema.sql.
4. Open src/DBConnection.java and change PASSWORD to your MySQL root password.
5. Add MySQL Connector/J to the project's classpath. Put the JAR in lib/ if desired.

COMPILE/RUN (Windows CMD; run from project root)
javac -cp ".;lib\mysql-connector-j.jar" -d out src\*.java
java -cp ".;out;lib\mysql-connector-j.jar" MainApp

If your JAR has a different filename, replace it in the commands. In VS Code, you can also add the connector JAR under Java Projects > Referenced Libraries.

DEMO ORDER
1. Add Student: 24ITR001, Demo Student, IT, 3
2. Add Course: 24ITR31, Java Programming, 4, THEORY
3. Enroll: 24ITR001, 24ITR31, 3
4. Update Attendance: held=40, attended=36, condonation=n
5. Enter marks: CIA1=80, CIA2=85, Assignment=90, ESE=75
6. Generate Mark Sheet for 24ITR001, semester 3

INCREMENTAL ENTRY
On later entries, leave any mark blank to retain the previously saved value. Marking ESE absence sets the ESE mark to AB.

IMPORTANT IMPLEMENTATION NOTES
- CIA raw score is currently calculated as the average of the available CIA1, CIA2 and assignment/lab marks; CIA weightage is then applied to that average.
- Attendance from 65% to below 75% is treated as pending condonation unless approval is recorded.
- The code implements the grading and thresholds supplied in the project brief. Confirm exact CIA calculation, grade bands, and rounding rules with the official KEC R2024 regulations before official academic use.
- This quick version includes student/course entry, enrollment, attendance, incremental marks, mark sheet/SGPA, and attendance defaulters. Department-wise class average SGPA/pass-fail summary is not yet included.

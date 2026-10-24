import java.sql.Connection;
import java.sql.SQLException;
import java.util.Scanner;

public class MainApp {
    private static final Scanner sc = new Scanner(System.in);
    private static final StudentDAO students = new StudentDAO();
    private static final AcademicDAO academic = new AcademicDAO();

    public static void main(String[] args) {
        try (Connection c = DBConnection.getConnection()) {
            System.out.println("Database connection successful.");
        } catch (SQLException e) {
            System.out.println("Database connection failed: " + e.getMessage());
            System.out.println("Check MySQL is running and update DBConnection.java password.");
            return;
        }

        while (true) {
            System.out.println("\n=== KEC STUDENT ACADEMIC & CONTINUOUS EVALUATION SYSTEM ===");
            System.out.println("1. Add Student");
            System.out.println("2. Add Course");
            System.out.println("3. Enroll Student in Course");
            System.out.println("4. Update Attendance");
            System.out.println("5. Enter/Update Incremental Marks");
            System.out.println("6. Generate Semester Mark Sheet");
            System.out.println("7. Attendance Defaulter Report");
            System.out.println("0. Exit");
            int choice = readInt("Choose: ");
            try {
                switch (choice) {
                    case 1:
                        students.addStudent(read("Roll number: "), read("Student name: "),
                            read("Department (CSE/ECE/IT/MECH etc.): "), readInt("Current semester: "));
                        break;
                    case 2:
                        academic.addCourse(read("Course code: "), read("Course title: "),
                            readDouble("Credits: "), read("Course type (THEORY/PRACTICAL/EMBEDDED): "));
                        break;
                    case 3:
                        academic.enroll(read("Roll number: "), read("Course code: "), readInt("Semester: "));
                        break;
                    case 4:
                        String ar = read("Roll number: "), ac = read("Course code: ");
                        int asem = readInt("Semester: "), held = readInt("Classes held: "), attended = readInt("Classes attended: ");
                        boolean cond = read("Medical condonation approved? (y/n): ").equalsIgnoreCase("y");
                        academic.updateAttendance(ar, ac, asem, held, attended, cond);
                        break;
                    case 5:
                        String roll = read("Roll number: "), code = read("Course code: ");
                        int sem = readInt("Semester: ");
                        System.out.println("Enter a mark from 0-100, or press Enter to leave it unchanged.");
                        Double cia1 = readOptionalMark("CIA Test 1: ");
                        Double cia2 = readOptionalMark("CIA Test 2: ");
                        Double assignment = readOptionalMark("Assignment/Quiz/Lab evaluation: ");
                        String absentAnswer = read("Was the student absent for ESE? (y/n): ");
                        boolean absent = absentAnswer.equalsIgnoreCase("y");
                        Double ese = absent ? null : readOptionalMark("ESE mark: ");
                        academic.updateMarks(roll, code, sem, cia1, cia2, assignment, ese, absent);
                        break;
                    case 6:
                        academic.printMarkSheet(read("Roll number: "), readInt("Semester: "));
                        break;
                    case 7:
                        academic.attendanceDefaulters();
                        break;
                    case 0:
                        System.out.println("Exiting."); return;
                    default:
                        System.out.println("Invalid option.");
                }
            } catch (Exception e) {
                System.out.println("Operation failed: " + e.getMessage());
            }
        }
    }

    private static String read(String prompt) {
        System.out.print(prompt); return sc.nextLine().trim();
    }
    private static int readInt(String prompt) {
        while (true) try { return Integer.parseInt(read(prompt)); }
        catch (NumberFormatException e) { System.out.println("Enter a valid integer."); }
    }
    private static double readDouble(String prompt) {
        while (true) try { return Double.parseDouble(read(prompt)); }
        catch (NumberFormatException e) { System.out.println("Enter a valid number."); }
    }
    private static Double readOptionalMark(String prompt) {
        while (true) {
            String s = read(prompt);
            if (s.isEmpty()) return null;
            try {
                double d = Double.parseDouble(s);
                if (d >= 0 && d <= 100) return d;
            } catch (NumberFormatException ignored) {}
            System.out.println("Enter a number from 0 to 100, or press Enter.");
        }
    }
}

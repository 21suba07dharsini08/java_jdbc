import java.sql.*;
import java.util.Locale;

public class AcademicDAO {
    public void addCourse(String code, String title, double credits, String type) throws SQLException {
        String sql = "INSERT INTO courses(course_code,title,credits,course_type) VALUES(?,?,?,?)";
        try (Connection c = DBConnection.getConnection(); PreparedStatement p = c.prepareStatement(sql)) {
            p.setString(1, code); p.setString(2, title); p.setDouble(3, credits);
            p.setString(4, type.toUpperCase(Locale.ROOT)); p.executeUpdate();
            System.out.println("Course added successfully.");
        }
    }

    public void enroll(String roll, String code, int semester) throws SQLException {
        String sql = "INSERT INTO enrollments(roll_no,course_code,semester) VALUES(?,?,?)";
        try (Connection c = DBConnection.getConnection(); PreparedStatement p = c.prepareStatement(sql)) {
            p.setString(1, roll); p.setString(2, code); p.setInt(3, semester); p.executeUpdate();
            System.out.println("Student enrolled successfully.");
        }
    }

    private int enrollmentId(Connection c, String roll, String code, int semester) throws SQLException {
        String sql = "SELECT enrollment_id FROM enrollments WHERE roll_no=? AND course_code=? AND semester=?";
        try (PreparedStatement p = c.prepareStatement(sql)) {
            p.setString(1, roll); p.setString(2, code); p.setInt(3, semester);
            try (ResultSet r = p.executeQuery()) {
                if (r.next()) return r.getInt(1);
                throw new SQLException("Enrollment not found. Enroll the student first.");
            }
        }
    }

    public void updateAttendance(String roll, String code, int semester, int held, int attended, boolean condonation) throws SQLException {
        if (held < 0 || attended < 0 || attended > held) throw new IllegalArgumentException("Attendance values are invalid.");
        String sql = "UPDATE enrollments SET classes_held=?,classes_attended=?,medical_condonation=? WHERE roll_no=? AND course_code=? AND semester=?";
        try (Connection c = DBConnection.getConnection(); PreparedStatement p = c.prepareStatement(sql)) {
            p.setInt(1, held); p.setInt(2, attended); p.setBoolean(3, condonation);
            p.setString(4, roll); p.setString(5, code); p.setInt(6, semester);
            if (p.executeUpdate() == 0) throw new SQLException("Enrollment not found.");
            System.out.println("Attendance updated.");
        }
    }

    public void updateMarks(String roll, String code, int semester, Double cia1, Double cia2, Double assignment, Double ese, boolean absent) throws SQLException {
        try (Connection c = DBConnection.getConnection()) {
            int id = enrollmentId(c, roll, code, semester);
            String sql = "INSERT INTO assessment_marks(enrollment_id,cia1,cia2,assignment_mark,ese_mark,ese_absent) " +
                "VALUES(?,?,?,?,?,?) ON DUPLICATE KEY UPDATE " +
                "cia1=COALESCE(VALUES(cia1),cia1),cia2=COALESCE(VALUES(cia2),cia2)," +
                "assignment_mark=COALESCE(VALUES(assignment_mark),assignment_mark)," +
                "ese_mark=IF(VALUES(ese_absent)=TRUE,NULL,COALESCE(VALUES(ese_mark),ese_mark)), ese_absent=VALUES(ese_absent)";
            try (PreparedStatement p = c.prepareStatement(sql)) {
                p.setInt(1, id);
                if (cia1 == null) p.setNull(2, Types.DECIMAL); else p.setDouble(2, cia1);
                if (cia2 == null) p.setNull(3, Types.DECIMAL); else p.setDouble(3, cia2);
                if (assignment == null) p.setNull(4, Types.DECIMAL); else p.setDouble(4, assignment);
                if (ese == null) p.setNull(5, Types.DECIMAL); else p.setDouble(5, ese);
                p.setBoolean(6, absent);
                p.executeUpdate();
                System.out.println("Marks saved/updated. Leave a field blank to keep its previous value.");
            }
        }
    }

    private String grade(double total) {
        if (total >= 91) return "O";
        if (total >= 81) return "A+";
        if (total >= 71) return "A";
        if (total >= 61) return "B+";
        if (total >= 50) return "B";
        return "RA";
    }

    public void printMarkSheet(String roll, int semester) throws SQLException {
        String sql = "SELECT s.student_name,s.department,e.course_code,c.title,c.credits,c.course_type," +
            "e.classes_held,e.classes_attended,e.medical_condonation,m.cia1,m.cia2,m.assignment_mark,m.ese_mark,m.ese_absent " +
            "FROM students s JOIN enrollments e ON s.roll_no=e.roll_no JOIN courses c ON c.course_code=e.course_code " +
            "LEFT JOIN assessment_marks m ON m.enrollment_id=e.enrollment_id WHERE s.roll_no=? AND e.semester=? ORDER BY e.course_code";
        double creditSum = 0, weightedPoints = 0;
        try (Connection c = DBConnection.getConnection(); PreparedStatement p = c.prepareStatement(sql)) {
            p.setString(1, roll); p.setInt(2, semester);
            try (ResultSet r = p.executeQuery()) {
                System.out.println("\n=== KONGU ENGINEERING COLLEGE - SEMESTER MARK SHEET ===");
                System.out.println("Roll No: " + roll);
                boolean found = false;
                while (r.next()) {
                    found = true;
                    String name = r.getString("student_name"), dept = r.getString("department");
                    String code = r.getString("course_code"), title = r.getString("title");
                    double credits = r.getDouble("credits");
                    String type = r.getString("course_type");
                    int held = r.getInt("classes_held"), attended = r.getInt("classes_attended");
                    double attendance = held == 0 ? 0 : attended * 100.0 / held;
                    boolean condonation = r.getBoolean("medical_condonation");
                    Double a = nullableDouble(r, "cia1"), b = nullableDouble(r, "cia2"), as = nullableDouble(r, "assignment_mark"), ese = nullableDouble(r, "ese_mark");
                    boolean absent = r.getBoolean("ese_absent");
                    double cia = (a == null ? 0 : a) + (b == null ? 0 : b) + (as == null ? 0 : as);
                    int ciaCount = (a != null ? 1 : 0) + (b != null ? 1 : 0) + (as != null ? 1 : 0);
                    double ciaRaw = ciaCount == 0 ? 0 : cia / ciaCount;
                    double ciaWeight = type.equals("THEORY") ? 0.40 : type.equals("PRACTICAL") ? 0.60 : 0.50;
                    double eseWeight = 1 - ciaWeight;
                    double total = ciaRaw * ciaWeight + (ese == null ? 0 : ese) * eseWeight;
                    double eseMinRaw = type.equals("THEORY") ? 45 : type.equals("PRACTICAL") ? 50 : 45;
                    double eseWeightedMin = type.equals("THEORY") ? 27 : type.equals("PRACTICAL") ? 20 : 22.5;
                    String status, g;
                    if (attendance < 65) { status = "SA - Barred"; g = "SA"; total = 0; }
                    else if (attendance < 75 && !condonation) { status = "Pending condonation"; g = "SA"; total = 0; }
                    else if (absent) { status = "AB - Absent"; g = "AB"; total = 0; }
                    else if (ese == null) { status = "ESE pending"; g = "Pending"; }
                    else if (ese < eseMinRaw || ese * eseWeight < eseWeightedMin || total < 50) { status = "FAIL"; g = "RA"; }
                    else { status = "PASS"; g = grade(total); }
                    int gp = g.equals("O") ? 10 : g.equals("A+") ? 9 : g.equals("A") ? 8 : g.equals("B+") ? 7 : g.equals("B") ? 6 : 0;
                    if (gp > 0 || g.equals("RA") || g.equals("AB") || g.equals("SA")) {
                        creditSum += credits;
                        weightedPoints += credits * gp;
                    }
                    System.out.printf("%nStudent: %s | Department: %s%n", name, dept);
                    System.out.printf("Course: %s - %s (%s, %.1f credits)%n", code, title, type, credits);
                    System.out.printf("Attendance: %.1f%% | CIA1: %s | CIA2: %s | Assignment: %s | ESE: %s%n",
                        attendance, val(a), val(b), val(as), absent ? "AB" : val(ese));
                    System.out.printf("Weighted Total: %.2f | Grade: %s | Result: %s%n", total, g, status);
                }
                if (!found) { System.out.println("No enrollment found for this roll number and semester."); return; }
            }
        }
        double sgpa = creditSum == 0 ? 0 : weightedPoints / creditSum;
        System.out.printf("%nSGPA: %.2f | Equivalent Percentage (SGPA x 10): %.2f%%%n", sgpa, sgpa * 10);
        System.out.println("Note: This is a project implementation of the rules supplied in the project brief; verify institutional regulations before official use.");
    }

    private Double nullableDouble(ResultSet r, String col) throws SQLException {
        double v = r.getDouble(col);
        return r.wasNull() ? null : v;
    }
    private String val(Double d) { return d == null ? "Pending" : String.format("%.2f", d); }

    public void attendanceDefaulters() throws SQLException {
        String sql = "SELECT s.roll_no,s.student_name,e.course_code,e.classes_held,e.classes_attended " +
            "FROM students s JOIN enrollments e ON s.roll_no=e.roll_no WHERE e.classes_held>0 AND e.classes_attended * 1.0/e.classes_held < 0.65";
        try (Connection c = DBConnection.getConnection(); Statement st = c.createStatement(); ResultSet r = st.executeQuery(sql)) {
            System.out.println("\n=== ATTENDANCE DEFAULTERS (<65%) ===");
            while (r.next()) System.out.printf("%s | %s | %s | Attendance: %.1f%% | Grade: SA%n",
                r.getString("roll_no"), r.getString("student_name"), r.getString("course_code"),
                r.getInt("classes_attended") * 100.0 / r.getInt("classes_held"));
        }
    }
}

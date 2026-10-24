import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class StudentDAO {
    public void addStudent(String roll, String name, String dept, int semester) throws SQLException {
        String sql = "INSERT INTO students(roll_no,student_name,department,current_semester) VALUES(?,?,?,?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, roll);
            ps.setString(2, name);
            ps.setString(3, dept.toUpperCase());
            ps.setInt(4, semester);
            ps.executeUpdate();
            System.out.println("Student added successfully.");
        }
    }
}

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Vector;
import javax.swing.table.DefaultTableModel;

public class UniversityManagementSystem extends JFrame {
    private JTextField nameField, emailField, courseNameField, creditsField;
    private JButton addButton, fetchButton;
    private JTable studentTable, courseTable;

    public UniversityManagementSystem() {
        setTitle("University Management System");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Students", createStudentPanel());
        tabbedPane.addTab("Courses", createCoursePanel());

        add(tabbedPane, BorderLayout.CENTER);
    }

    private JPanel createStudentPanel() {
        JPanel studentPanel = new JPanel(new BorderLayout());

        JPanel inputPanel = new JPanel(new GridLayout(3, 2));
        inputPanel.add(new JLabel("Name:"));
        nameField = new JTextField();
        inputPanel.add(nameField);
        inputPanel.add(new JLabel("Email:"));
        emailField = new JTextField();
        inputPanel.add(emailField);

        addButton = new JButton("Add Student");
        fetchButton = new JButton("Fetch Students");

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(addButton);
        buttonPanel.add(fetchButton);

        studentPanel.add(inputPanel, BorderLayout.NORTH);
        studentPanel.add(buttonPanel, BorderLayout.CENTER);

        studentTable = new JTable();
        studentPanel.add(new JScrollPane(studentTable), BorderLayout.SOUTH);

        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addStudent();
            }
        });

        fetchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                fetchStudents();
            }
        });

        return studentPanel;
    }

    private JPanel createCoursePanel() {
        JPanel coursePanel = new JPanel(new BorderLayout());

        JPanel inputPanel = new JPanel(new GridLayout(3, 2));
        inputPanel.add(new JLabel("Course Name:"));
        courseNameField = new JTextField();
        inputPanel.add(courseNameField);
        inputPanel.add(new JLabel("Credits:"));
        creditsField = new JTextField();
        inputPanel.add(creditsField);

        addButton = new JButton("Add Course");
        fetchButton = new JButton("Fetch Courses");

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(addButton);
        buttonPanel.add(fetchButton);

        coursePanel.add(inputPanel, BorderLayout.NORTH);
        coursePanel.add(buttonPanel, BorderLayout.CENTER);

        courseTable = new JTable();
        coursePanel.add(new JScrollPane(courseTable), BorderLayout.SOUTH);

        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addCourse();
            }
        });

        fetchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                fetchCourses();
            }
        });

        return coursePanel;
    }

    private void addStudent() {
        String name = nameField.getText();
        String email = emailField.getText();

        try (Connection connection = getConnection()) {
            String query = "INSERT INTO students (name, email) VALUES (?, ?)";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, name);
            statement.setString(2, email);
            statement.executeUpdate();
            JOptionPane.showMessageDialog(this, "Student added successfully");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void fetchStudents() {
        try (Connection connection = getConnection()) {
            String query = "SELECT * FROM students";
            PreparedStatement statement = connection.prepareStatement(query, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            ResultSet resultSet = statement.executeQuery();
            studentTable.setModel(buildTableModel(resultSet));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void addCourse() {
        String courseName = courseNameField.getText();
        String credits = creditsField.getText();

        try (Connection connection = getConnection()) {
            String query = "INSERT INTO courses (course_name, credits) VALUES (?, ?)";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, courseName);
            statement.setString(2, credits);
            statement.executeUpdate();
            JOptionPane.showMessageDialog(this, "Course added successfully");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void fetchCourses() {
        try (Connection connection = getConnection()) {
            String query = "SELECT * FROM courses";
            PreparedStatement statement = connection.prepareStatement(query, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            ResultSet resultSet = statement.executeQuery();
            courseTable.setModel(buildTableModel(resultSet));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private Connection getConnection() {
        try {
            return DriverManager.getConnection("jdbc:mysql://localhost:3306/university", "root", "1");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            UniversityManagementSystem frame = new UniversityManagementSystem();
            frame.setVisible(true);
        });
    }

    private DefaultTableModel buildTableModel(ResultSet resultSet) throws SQLException {
        ResultSetMetaData metaData = resultSet.getMetaData();

        // Names of columns
        Vector<String> columnNames = new Vector<>();
        int columnCount = metaData.getColumnCount();
        for (int column = 1; column <= columnCount; column++) {
            columnNames.add(metaData.getColumnName(column));
        }

        // Data of the table
        Vector<Vector<Object>> data = new Vector<>();
        while (resultSet.next()) {
            Vector<Object> vector = new Vector<>();
            for (int columnIndex = 1; columnIndex <= columnCount; columnIndex++) {
                vector.add(resultSet.getObject(columnIndex));
            }
            data.add(vector);
        }

        return new DefaultTableModel(data, columnNames);
    }
}
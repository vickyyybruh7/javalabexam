import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
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
    private JTextField studentIdField, courseIdField;
    private JButton addButton, fetchButton, updateButton, deleteButton, clearButton;
    private JTable studentTable, courseTable;
    private boolean isEditModeStudent = false;
    private boolean isEditModeCourse = false;

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

        JPanel inputPanel = new JPanel(new GridLayout(4, 2));
        inputPanel.add(new JLabel("Student ID:"));
        studentIdField = new JTextField();
        studentIdField.setEditable(false); // Make ID field non-editable by default
        inputPanel.add(studentIdField);
        inputPanel.add(new JLabel("Name:"));
        nameField = new JTextField();
        inputPanel.add(nameField);
        inputPanel.add(new JLabel("Email:"));
        emailField = new JTextField();
        inputPanel.add(emailField);

        addButton = new JButton("Add Student");
        fetchButton = new JButton("Fetch Students");
        updateButton = new JButton("Update Student");
        deleteButton = new JButton("Delete Student");
        clearButton = new JButton("Clear Fields");

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(addButton);
        buttonPanel.add(fetchButton);
        buttonPanel.add(updateButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(clearButton);

        studentPanel.add(inputPanel, BorderLayout.NORTH);
        studentPanel.add(buttonPanel, BorderLayout.CENTER);

        studentTable = new JTable();
        studentPanel.add(new JScrollPane(studentTable), BorderLayout.SOUTH);

        // Add event listeners for student operations
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
        
        updateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateStudent();
            }
        });
        
        deleteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                deleteStudent();
            }
        });
        
        clearButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                clearStudentFields();
            }
        });
        
        // Add mouse listener for table selection
        studentTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int selectedRow = studentTable.getSelectedRow();
                if (selectedRow != -1) {
                    isEditModeStudent = true;
                    studentIdField.setText(studentTable.getValueAt(selectedRow, 0).toString());
                    nameField.setText(studentTable.getValueAt(selectedRow, 1).toString());
                    emailField.setText(studentTable.getValueAt(selectedRow, 2).toString());
                }
            }
        });

        return studentPanel;
    }

    private JPanel createCoursePanel() {
        JPanel coursePanel = new JPanel(new BorderLayout());

        JPanel inputPanel = new JPanel(new GridLayout(4, 2));
        inputPanel.add(new JLabel("Course ID:"));
        courseIdField = new JTextField();
        courseIdField.setEditable(false); // Make ID field non-editable by default
        inputPanel.add(courseIdField);
        inputPanel.add(new JLabel("Course Name:"));
        courseNameField = new JTextField();
        inputPanel.add(courseNameField);
        inputPanel.add(new JLabel("Credits:"));
        creditsField = new JTextField();
        inputPanel.add(creditsField);

        JButton addCourseButton = new JButton("Add Course");
        JButton fetchCoursesButton = new JButton("Fetch Courses");
        JButton updateCourseButton = new JButton("Update Course");
        JButton deleteCourseButton = new JButton("Delete Course");
        JButton clearCourseButton = new JButton("Clear Fields");

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(addCourseButton);
        buttonPanel.add(fetchCoursesButton);
        buttonPanel.add(updateCourseButton);
        buttonPanel.add(deleteCourseButton);
        buttonPanel.add(clearCourseButton);

        coursePanel.add(inputPanel, BorderLayout.NORTH);
        coursePanel.add(buttonPanel, BorderLayout.CENTER);

        courseTable = new JTable();
        coursePanel.add(new JScrollPane(courseTable), BorderLayout.SOUTH);

        // Add event listeners for course operations
        addCourseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addCourse();
            }
        });

        fetchCoursesButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                fetchCourses();
            }
        });
        
        updateCourseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateCourse();
            }
        });
        
        deleteCourseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                deleteCourse();
            }
        });
        
        clearCourseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                clearCourseFields();
            }
        });
        
        // Add mouse listener for table selection
        courseTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int selectedRow = courseTable.getSelectedRow();
                if (selectedRow != -1) {
                    isEditModeCourse = true;
                    courseIdField.setText(courseTable.getValueAt(selectedRow, 0).toString());
                    courseNameField.setText(courseTable.getValueAt(selectedRow, 1).toString());
                    creditsField.setText(courseTable.getValueAt(selectedRow, 2).toString());
                }
            }
        });

        return coursePanel;
    }

    private void addStudent() {
        String name = nameField.getText();
        String email = emailField.getText();
        
        if (name.isEmpty() || email.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill all fields", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try (Connection connection = getConnection()) {
            String query = "INSERT INTO students (name, email) VALUES (?, ?)";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, name);
            statement.setString(2, email);
            statement.executeUpdate();
            JOptionPane.showMessageDialog(this, "Student added successfully");
            clearStudentFields();
            fetchStudents();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
    
    private void updateStudent() {
        if (!isEditModeStudent) {
            JOptionPane.showMessageDialog(this, "Please select a student to update", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        String studentId = studentIdField.getText();
        String name = nameField.getText();
        String email = emailField.getText();
        
        if (studentId.isEmpty() || name.isEmpty() || email.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill all fields", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try (Connection connection = getConnection()) {
            String query = "UPDATE students SET name = ?, email = ? WHERE student_id = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, name);
            statement.setString(2, email);
            statement.setInt(3, Integer.parseInt(studentId));
            int rowsUpdated = statement.executeUpdate();
            
            if (rowsUpdated > 0) {
                JOptionPane.showMessageDialog(this, "Student updated successfully");
                clearStudentFields();
                fetchStudents();
            } else {
                JOptionPane.showMessageDialog(this, "No student found with ID: " + studentId);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
    
    private void deleteStudent() {
        if (!isEditModeStudent) {
            JOptionPane.showMessageDialog(this, "Please select a student to delete", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        String studentId = studentIdField.getText();
        
        if (studentId.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No student selected", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        int confirmResult = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this student?", 
                                                         "Confirm Deletion", JOptionPane.YES_NO_OPTION);
        if (confirmResult != JOptionPane.YES_OPTION) {
            return;
        }

        try (Connection connection = getConnection()) {
            // First, delete any enrollments for this student
            String deleteEnrollmentsQuery = "DELETE FROM enrollments WHERE student_id = ?";
            PreparedStatement enrollmentStatement = connection.prepareStatement(deleteEnrollmentsQuery);
            enrollmentStatement.setInt(1, Integer.parseInt(studentId));
            enrollmentStatement.executeUpdate();
            
            // Then delete the student
            String query = "DELETE FROM students WHERE student_id = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, Integer.parseInt(studentId));
            int rowsDeleted = statement.executeUpdate();
            
            if (rowsDeleted > 0) {
                JOptionPane.showMessageDialog(this, "Student deleted successfully");
                clearStudentFields();
                fetchStudents();
            } else {
                JOptionPane.showMessageDialog(this, "No student found with ID: " + studentId);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
    
    private void clearStudentFields() {
        studentIdField.setText("");
        nameField.setText("");
        emailField.setText("");
        isEditModeStudent = false;
    }

    private void fetchStudents() {
        try (Connection connection = getConnection()) {
            String query = "SELECT * FROM students";
            PreparedStatement statement = connection.prepareStatement(query, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            ResultSet resultSet = statement.executeQuery();
            studentTable.setModel(buildTableModel(resultSet));
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void addCourse() {
        String courseName = courseNameField.getText();
        String credits = creditsField.getText();
        
        if (courseName.isEmpty() || credits.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill all fields", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try (Connection connection = getConnection()) {
            String query = "INSERT INTO courses (course_name, credits) VALUES (?, ?)";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, courseName);
            statement.setString(2, credits);
            statement.executeUpdate();
            JOptionPane.showMessageDialog(this, "Course added successfully");
            clearCourseFields();
            fetchCourses();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
    
    private void updateCourse() {
        if (!isEditModeCourse) {
            JOptionPane.showMessageDialog(this, "Please select a course to update", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        String courseId = courseIdField.getText();
        String courseName = courseNameField.getText();
        String credits = creditsField.getText();
        
        if (courseId.isEmpty() || courseName.isEmpty() || credits.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill all fields", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try (Connection connection = getConnection()) {
            String query = "UPDATE courses SET course_name = ?, credits = ? WHERE course_id = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, courseName);
            statement.setString(2, credits);
            statement.setInt(3, Integer.parseInt(courseId));
            int rowsUpdated = statement.executeUpdate();
            
            if (rowsUpdated > 0) {
                JOptionPane.showMessageDialog(this, "Course updated successfully");
                clearCourseFields();
                fetchCourses();
            } else {
                JOptionPane.showMessageDialog(this, "No course found with ID: " + courseId);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
    
    private void deleteCourse() {
        if (!isEditModeCourse) {
            JOptionPane.showMessageDialog(this, "Please select a course to delete", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        String courseId = courseIdField.getText();
        
        if (courseId.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No course selected", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
                int confirmResult = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this course?", 
                                                         "Confirm Deletion", JOptionPane.YES_NO_OPTION);
        if (confirmResult != JOptionPane.YES_OPTION) {
            return;
        }

        try (Connection connection = getConnection()) {
            // First, delete any enrollments for this course
            String deleteEnrollmentsQuery = "DELETE FROM enrollments WHERE course_id = ?";
            PreparedStatement enrollmentStatement = connection.prepareStatement(deleteEnrollmentsQuery);
            enrollmentStatement.setInt(1, Integer.parseInt(courseId));
            enrollmentStatement.executeUpdate();
            
            // Then delete the course
            String query = "DELETE FROM courses WHERE course_id = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, Integer.parseInt(courseId));
            int rowsDeleted = statement.executeUpdate();
            
            if (rowsDeleted > 0) {
                JOptionPane.showMessageDialog(this, "Course deleted successfully");
                clearCourseFields();
                fetchCourses();
            } else {
                JOptionPane.showMessageDialog(this, "No course found with ID: " + courseId);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
    
    private void clearCourseFields() {
        courseIdField.setText("");
        courseNameField.setText("");
        creditsField.setText("");
        isEditModeCourse = false;
    }

    private void fetchCourses() {
        try (Connection connection = getConnection()) {
            String query = "SELECT * FROM courses";
            PreparedStatement statement = connection.prepareStatement(query, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            ResultSet resultSet = statement.executeQuery();
            courseTable.setModel(buildTableModel(resultSet));
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
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
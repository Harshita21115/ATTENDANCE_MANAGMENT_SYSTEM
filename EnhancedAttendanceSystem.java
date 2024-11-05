import java.awt.*;
import java.util.Calendar;
import javax.swing.*;
import java.sql.*;

public class EnhancedAttendanceSystem extends JFrame {

    private static final String DB_URL = "jdbc:mysql://localhost:3306/attendance_db?useSSL=false&serverTimezone=UTC";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "Minyoongi@9";

    private JPanel dashboardPanel, attendancePanel, calendarPanel, profilePanel, statisticsPanel, historyPanel, reportPanel;
    private JSplitPane splitPane;
    private String[] classNames = {"A", "B", "C", "D", "E"};
    private String[][] studentNames;
    private JButton[] dashboardButtons;
    private boolean isDarkMode = false;

    private Connection connection;

    public EnhancedAttendanceSystem() {
        setTitle("Attendance System");
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());


        initializeDatabaseConnection();

        showLoginScreen();

        initializeStudentNames();

        dashboardPanel = createDashboardPanel();
        attendancePanel = createAttendancePanel();
        calendarPanel = createFullScreenCalendarPanel();
        profilePanel = createProfilePanel();
        statisticsPanel = createStatisticsPanel();
        historyPanel = createHistoryPanel();
        reportPanel = createReportPanel();

        JPanel leftPanel = createLeftPanel();

        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, dashboardPanel);
        splitPane.setDividerLocation(250);
        add(splitPane, BorderLayout.CENTER);

        JPanel footerPanel = new JPanel(new BorderLayout());
        JLabel statusLabel = new JLabel("Welcome to the Attendance System");
        footerPanel.add(statusLabel, BorderLayout.WEST);
        add(footerPanel, BorderLayout.SOUTH);

        setVisible(true);
    }


    private void initializeDatabaseConnection() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            System.out.println("Connected to MySQL database successfully.");
        } catch (ClassNotFoundException e) {
            JOptionPane.showMessageDialog(this, "MySQL JDBC Driver not found.", "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
            System.exit(1);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Failed to connect to MySQL database.", "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
            System.exit(1);
        }
    }


    private void showLoginScreen() {
        JPanel loginPanel = new JPanel(new GridLayout(3, 2));
        JTextField usernameField = new JTextField();
        JPasswordField passwordField = new JPasswordField();

        loginPanel.add(new JLabel("Username:"));
        loginPanel.add(usernameField);
        loginPanel.add(new JLabel("Password:"));
        loginPanel.add(passwordField);

        int result = JOptionPane.showConfirmDialog(this, loginPanel, "Login", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            String username = usernameField.getText();
            String password = new String(passwordField.getPassword());
            if (authenticateUser(username, password)) {
                JOptionPane.showMessageDialog(this, "Login successful!", "Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Invalid username or password. Please try again.", "Error", JOptionPane.ERROR_MESSAGE);
                showLoginScreen();
            }
        } else {
            System.exit(0);
        }
    }


    private boolean authenticateUser(String username, String password) {
        String query = "SELECT password FROM users WHERE username = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                String storedPassword = rs.getString("password");
                return storedPassword.equals(password);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error during authentication.", "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
        return false;
    }

    private void initializeStudentNames() {
        studentNames = new String[5][30];
        for (int i = 0; i < classNames.length; i++) {
            for (int j = 0; j < 30; j++) {
                studentNames[i][j] = "Student " + (j + 1) + " Class " + classNames[i];
            }
        }
    }

    private JPanel createLeftPanel() {
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setBackground(new Color(50, 50, 50));

        JButton dashboardButton = createDashboardButton("Dashboard");
        dashboardButton.addActionListener(e -> showDashboard());
        leftPanel.add(dashboardButton, BorderLayout.NORTH);

        return leftPanel;
    }

    private JButton createDashboardButton(String title) {
        JButton button = new JButton(title);
        button.setFont(new Font("Arial", Font.BOLD, 18));
        button.setBackground(Color.DARK_GRAY);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(true);
        button.setBorderPainted(false);
        button.setPreferredSize(new Dimension(250, 50));
        return button;
    }

    private JPanel createDashboardPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        JLabel dashboardLabel = new JLabel("Dashboard", JLabel.CENTER);
        dashboardLabel.setFont(new Font("Arial", Font.BOLD, 24));
        panel.add(dashboardLabel, BorderLayout.NORTH);

        JPanel buttonsPanel = new JPanel(new GridLayout(7, 1, 10, 10));
        String[] options = {"Attendance", "Calendar", "Profile", "Statistics", "History", "Report"};
        dashboardButtons = new JButton[options.length];

        for (int i = 0; i < options.length; i++) {
            dashboardButtons[i] = createDashboardOptionButton(options[i]);
            int finalI = i;
            dashboardButtons[i].setForeground(Color.BLUE);
            dashboardButtons[i].setOpaque(true);
            dashboardButtons[i].addActionListener(e -> navigateTo(finalI));
            buttonsPanel.add(dashboardButtons[i]);
        }

        JButton darkModeButton = new JButton("Toggle Dark Mode");
        darkModeButton.addActionListener(e -> toggleDarkMode());
        buttonsPanel.add(darkModeButton);
        panel.add(buttonsPanel, BorderLayout.CENTER);
        return panel;
    }

    private JButton createDashboardOptionButton(String title) {
        JButton button = new JButton(title);
        button.setFont(new Font("TIMES ROMAN", Font.PLAIN, 28));
        button.setBackground(new Color(170, 216, 230));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        return button;
    }

    private void showDashboard() {
        splitPane.setRightComponent(dashboardPanel);
    }

    private JPanel createAttendancePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        JLabel attendanceLabel = new JLabel("Attendance Management", JLabel.CENTER);
        attendanceLabel.setFont(new Font("Arial", Font.BOLD, 24));
        panel.add(attendanceLabel, BorderLayout.NORTH);

        JButton attendanceButton = new JButton("Mark Attendance");
        attendanceButton.setBackground(new Color(255, 240, 163));
        attendanceButton.setForeground(new Color(95, 110, 41));
        attendanceButton.addActionListener(e -> selectClassForAttendance());
        attendanceButton.setBorderPainted(false);
        panel.add(attendanceButton, BorderLayout.CENTER);
        attendanceButton.setBorderPainted(false);

        return panel;
    }

    private JPanel createFullScreenCalendarPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);

        JLabel calendarLabel = new JLabel("Calendar", JLabel.CENTER);
        calendarLabel.setFont(new Font("Arial", Font.BOLD, 40));
        calendarLabel.setForeground(Color.BLACK);
        panel.add(calendarLabel, BorderLayout.NORTH);

        JComboBox<String> monthBox = new JComboBox<>(new String[]{"January", "February", "March", "April", "May", "June",
                "July", "August", "September", "October", "November", "December"});
        JComboBox<Integer> yearBox = new JComboBox<>(new Integer[]{2023, 2024, 2025, 2026});
        JPanel dateSelector = new JPanel();
        dateSelector.add(new JLabel("Month:"));
        dateSelector.add(monthBox);
        dateSelector.add(new JLabel("Year:"));
        dateSelector.add(yearBox);

        panel.add(dateSelector, BorderLayout.NORTH);

        JPanel calendarDays = new JPanel(new GridLayout(6, 7));
        updateCalendar(calendarDays, monthBox, yearBox);
        panel.add(calendarDays, BorderLayout.CENTER);

        monthBox.addActionListener(e -> updateCalendar(calendarDays, monthBox, yearBox));
        yearBox.addActionListener(e -> updateCalendar(calendarDays, monthBox, yearBox));

        return panel;
    }

    private void updateCalendar(JPanel calendarDays, JComboBox<String> monthBox, JComboBox<Integer> yearBox) {
        calendarDays.removeAll();
        int month = monthBox.getSelectedIndex();
        int year = (int) yearBox.getSelectedItem();

        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, 1);

        int daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        for (int i = 1; i <= daysInMonth; i++) {
            JButton dayButton = new JButton(String.valueOf(i));
            dayButton.addActionListener(e -> addNoteToDate(dayButton.getText(), monthBox.getSelectedItem().toString(), year));
            calendarDays.add(dayButton);
        }

        calendarDays.revalidate();
        calendarDays.repaint();
    }

    private void addNoteToDate(String day, String month, int year) {
        String key = day + "-" + month + "-" + year;
        String existingNote = getNoteFromDatabase(day, month, year);
        String note = JOptionPane.showInputDialog(this, "Add note for " + key, existingNote);

        if (note != null) {
            saveOrUpdateNoteInDatabase(day, month, year, note);
        }
    }


    private String getNoteFromDatabase(String day, String month, int year) {
        String note = "";
        String query = "SELECT note FROM notes WHERE day = ? AND month = ? AND year = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, day);
            pstmt.setString(2, month);
            pstmt.setInt(3, year);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                note = rs.getString("note");
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error retrieving note.", "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
        return note;
    }


    private void saveOrUpdateNoteInDatabase(String day, String month, int year, String note) {
        String checkQuery = "SELECT id FROM notes WHERE day = ? AND month = ? AND year = ?";
        String insertQuery = "INSERT INTO notes (day, month, year, note) VALUES (?, ?, ?, ?)";
        String updateQuery = "UPDATE notes SET note = ? WHERE day = ? AND month = ? AND year = ?";
        try (PreparedStatement checkStmt = connection.prepareStatement(checkQuery)) {
            checkStmt.setString(1, day);
            checkStmt.setString(2, month);
            checkStmt.setInt(3, year);
            ResultSet rs = checkStmt.executeQuery();
            if (rs.next()) {
                try (PreparedStatement updateStmt = connection.prepareStatement(updateQuery)) {
                    updateStmt.setString(1, note);
                    updateStmt.setString(2, day);
                    updateStmt.setString(3, month);
                    updateStmt.setInt(4, year);
                    updateStmt.executeUpdate();
                }
            } else {
                try (PreparedStatement insertStmt = connection.prepareStatement(insertQuery)) {
                    insertStmt.setString(1, day);
                    insertStmt.setString(2, month);
                    insertStmt.setInt(3, year);
                    insertStmt.setString(4, note);
                    insertStmt.executeUpdate();
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error saving note.", "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private JPanel createProfilePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        JLabel profileLabel = new JLabel("Teacher Profile: Harshita", JLabel.CENTER);
        profileLabel.setFont(new Font("Arial", Font.BOLD, 24));
        panel.add(profileLabel, BorderLayout.NORTH);


        JLabel photoLabel = new JLabel(new ImageIcon("path/to/photo.jpg"));
        panel.add(photoLabel, BorderLayout.CENTER);

        JTextArea profileText = new JTextArea();
        profileText.setFont(new Font("Arial", Font.PLAIN, 16));
        profileText.setEditable(false);
        profileText.setText(
                "Position: Senior Teacher\n" +
                        "Subjects Taught: Mathematics, Computer Science, Physics\n" +
                        "Experience: Over 12 years in education\n\n" +
                        "Qualifications:\n" +
                        "   - M.Sc. in Mathematics\n" +
                        "   - B.Ed.\n" +
                        "   - Certification in STEM Education\n" +
                        "   - Certification in Educational Technology\n\n" +
                        "Professional Experience:\n" +
                        "I currently serve as a Senior Teacher at Holy Cross High School, a role I've embraced since 2016. " +
                        "Here, I've had the privilege of guiding students through complex mathematical concepts and igniting their passion for technology. " +
                        "Before this, I taught Mathematics at Universal Academy from 2012 to 2016 and shared my love for Physics at IIT Bombay from 2010 to 2012.\n\n" +
                        "Projects I've Led:\n" +
                        "   - Math in Real Life: I initiated this project to integrate real-world applications into our curriculum, " +
                        "helping students see the relevance of mathematics in their everyday lives.\n" +
                        "   - Annual Science Fair: I've organized this event at Holy Cross High School, fostering a spirit of innovation and encouraging students to showcase their creativity.\n\n" +
                        "Workshops Attended:\n" +
                        "   - National Conference on Mathematics Education (2022)\n" +
                        "   - Innovative Teaching Strategies workshop (2023)\n\n" +
                        "Community Involvement:\n" +
                        "Outside the classroom, I volunteer as a tutor at a local community center, providing support to underprivileged students. " +
                        "Additionally, I organize summer STEM camps to engage middle school students and spark their interest in science and technology.\n\n" +
                        "Teaching Philosophy:\n" +
                        "I believe in fostering a student-centered learning environment that cultivates curiosity and critical thinking. " +
                        "My aim is to engage students through interactive lessons that connect theoretical concepts to real-world applications.\n\n" +
                        "Awards:\n" +
                        "   - Best Teacher Award (2023)\n" +
                        "   - Excellence in STEM Education (2022)\n\n" +
                        "Hobbies:\n" +
                        "Reading, painting, developing educational resources, and hiking.\n\n" +
                        "Contact Information:\n" +
                        "   - Email: harshita@example.com\n" +
                        "   - Phone: +91-9167047003\n" +
                        "   - Office Hours: Monday to Friday, 9 AM - 3 PM\n\n" +
                        "Additional Skills:\n" +
                        "   - AI in Education\n" +
                        "   - Curriculum Development\n" +
                        "   - Student Engagement Strategies\n" +
                        "   - Proficient in various educational technologies (Google Classroom, Zoom, etc.)\n\n" +
                        "Testimonials:\n" +
                        "   - 'Harshita Maam has a unique gift for making complex concepts understandable.' - Former Student\n" +
                        "   - 'Her passion for teaching inspires everyone around her.' - Colleague\n" +
                        "   - 'An innovative educator who truly cares about her students.' - Parent\n"
        );

        profileText.setBackground(new Color(240, 248, 255));
        profileText.setForeground(new Color(25, 25, 112));

        JScrollPane scrollPane = new JScrollPane(profileText);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createStatisticsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        JLabel statsLabel = new JLabel("Class Attendance Statistics", JLabel.CENTER);
        statsLabel.setFont(new Font("Arial", Font.BOLD, 24));
        panel.add(statsLabel, BorderLayout.NORTH);

        JPanel statsContent = new JPanel(new GridLayout(5, 2, 10, 10));

        for (int i = 0; i < classNames.length; i++) {
            int classAttendance = calculateClassAttendance(classNames[i]);
            JLabel classLabel = new JLabel("Class " + classNames[i] + ": " + classAttendance + "% attendance");
            classLabel.setFont(new Font("Arial", Font.PLAIN, 18));
            statsContent.add(classLabel);
        }

        panel.add(statsContent, BorderLayout.CENTER);
        return panel;
    }


    private int calculateClassAttendance(String className) {
        String queryTotal = "SELECT COUNT(*) AS total FROM attendance WHERE class_name = ?";
        String queryPresent = "SELECT COUNT(*) AS present FROM attendance WHERE class_name = ? AND present = TRUE";
        int totalStudents = 0;
        int presentCount = 0;

        try (PreparedStatement pstmtTotal = connection.prepareStatement(queryTotal)) {
            pstmtTotal.setString(1, className);
            ResultSet rsTotal = pstmtTotal.executeQuery();
            if (rsTotal.next()) {
                totalStudents = rsTotal.getInt("total");
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error calculating total students.", "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }

        try (PreparedStatement pstmtPresent = connection.prepareStatement(queryPresent)) {
            pstmtPresent.setString(1, className);
            ResultSet rsPresent = pstmtPresent.executeQuery();
            if (rsPresent.next()) {
                presentCount = rsPresent.getInt("present");
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error calculating present students.", "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }

        if (totalStudents == 0) return 0;
        return (int) ((presentCount / (double) totalStudents) * 100);
    }

    private JPanel createHistoryPanel() {
        JPanel panel = new JPanel();
        panel.add(new JLabel("History of Attendance - Coming Soon!"));
        return panel;
    }

    private JPanel createReportPanel() {
        JPanel panel = new JPanel();
        panel.add(new JLabel("Attendance Reports - Coming Soon!"));
        return panel;
    }

    private void navigateTo(int option) {
        switch (option) {
            case 0 -> splitPane.setRightComponent(attendancePanel);
            case 1 -> splitPane.setRightComponent(calendarPanel);
            case 2 -> splitPane.setRightComponent(profilePanel);
            case 3 -> splitPane.setRightComponent(statisticsPanel);
            case 4 -> splitPane.setRightComponent(historyPanel);
            case 5 -> splitPane.setRightComponent(reportPanel);
            default -> showDashboard();
        }
    }

    private void selectClassForAttendance() {
        String selectedClass = (String) JOptionPane.showInputDialog(
                this,
                "Select Class for Attendance",
                "Class Selection",
                JOptionPane.PLAIN_MESSAGE,
                null,
                classNames,
                classNames[0]);

        if (selectedClass != null) {
            selectDateAndShowClassAttendance(selectedClass);
        }
    }

    private void selectDateAndShowClassAttendance(String className) {
        String date = JOptionPane.showInputDialog(this, "Enter date for attendance (yyyy-mm-dd):");
        if (date != null && date.matches("\\d{4}-\\d{2}-\\d{2}")) {
            showClassAttendance(className, date);
        } else {
            JOptionPane.showMessageDialog(this, "Invalid date format. Please enter date in yyyy-mm-dd format.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void refreshStatisticsPanel() {
        statisticsPanel.removeAll();
        statisticsPanel.add(createStatisticsPanel());
        statisticsPanel.revalidate();
        statisticsPanel.repaint();
    }


    private void showClassAttendance(String className, String date) {
        JPanel attendancePanelUI = new JPanel(new GridLayout(31, 1)); // Adjusted to include "Check All"
        JCheckBox checkAllBox = new JCheckBox("Check All");
        JCheckBox[] checkBoxes = new JCheckBox[30];

        String fetchStudentsQuery = "SELECT student_name, present FROM attendance WHERE class_name = ? AND date = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(fetchStudentsQuery)) {
            pstmt.setString(1, className);
            pstmt.setString(2, date);
            ResultSet rs = pstmt.executeQuery();
            int index = 0;

            // Fetch existing attendance records
            while (rs.next() && index < 30) {
                String studentName = rs.getString("student_name");
                boolean isPresent = rs.getBoolean("present");
                checkBoxes[index] = new JCheckBox(studentName);
                checkBoxes[index].setSelected(isPresent);
                attendancePanelUI.add(checkBoxes[index]);
                index++;
            }

            // Fill in remaining checkboxes with default student names
            while (index < 30) {
                checkBoxes[index] = new JCheckBox(studentNames[getClassIndex(className)][index]);
                attendancePanelUI.add(checkBoxes[index]);
                index++;
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error fetching students.", "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
            return;
        }

        // Add "Check All" checkbox and action listener
        checkAllBox.addActionListener(e -> {
            boolean selected = checkAllBox.isSelected();
            for (JCheckBox checkBox : checkBoxes) {
                checkBox.setSelected(selected);
            }
        });

        // Add "Check All" checkbox to the panel
        attendancePanelUI.add(checkAllBox);

        // Show dialog for marking attendance
        int result = JOptionPane.showConfirmDialog(this, attendancePanelUI,
                "Mark Attendance for Class " + className + " on " + date, JOptionPane.OK_CANCEL_OPTION);

        if (result == JOptionPane.OK_OPTION) {
            int presentCount = 0;
            for (int i = 0; i < 30; i++) {
                boolean isPresent = checkBoxes[i].isSelected();
                presentCount += isPresent ? 1 : 0;
                updateAttendanceDatabase(className, date, studentNames[getClassIndex(className)][i], isPresent);
            }

            int totalStudents = 30;
            int absentCount = totalStudents - presentCount;

            String summary = "Attendance Summary for Class " + className + " on " + date + ":\n" +
                    "Total Students: " + totalStudents + "\n" +
                    "Present: " + presentCount + "\n" +
                    "Absent: " + absentCount;

            JOptionPane.showMessageDialog(this, summary, "Attendance Summary", JOptionPane.INFORMATION_MESSAGE);

            // Refresh statistics panel if needed
            refreshStatisticsPanel();
        }
    }


    private void updateAttendanceDatabase(String className, String date, String studentName, boolean isPresent) {
        String checkQuery = "SELECT id FROM attendance WHERE class_name = ? AND date = ? AND student_name = ?";
        String insertQuery = "INSERT INTO attendance (class_name, date, student_name, present) VALUES (?, ?, ?, ?)";
        String updateQuery = "UPDATE attendance SET present = ? WHERE id = ?";

        try (PreparedStatement checkStmt = connection.prepareStatement(checkQuery)) {
            checkStmt.setString(1, className);
            checkStmt.setString(2, date);
            checkStmt.setString(3, studentName);
            ResultSet rs = checkStmt.executeQuery();
            if (rs.next()) {

                int id = rs.getInt("id");
                try (PreparedStatement updateStmt = connection.prepareStatement(updateQuery)) {
                    updateStmt.setBoolean(1, isPresent);
                    updateStmt.setInt(2, id);
                    updateStmt.executeUpdate();
                }
            } else {

                try (PreparedStatement insertStmt = connection.prepareStatement(insertQuery)) {
                    insertStmt.setString(1, className);
                    insertStmt.setString(2, date);
                    insertStmt.setString(3, studentName);
                    insertStmt.setBoolean(4, isPresent);
                    insertStmt.executeUpdate();
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error updating attendance.", "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }


    private int getClassIndex(String className) {
        for (int i = 0; i < classNames.length; i++) {
            if (classNames[i].equals(className)) {
                return i;
            }
        }
        return -1;
    }

    private void toggleDarkMode() {
        isDarkMode = !isDarkMode;
        Color backgroundColor = isDarkMode ? Color.DARK_GRAY : Color.LIGHT_GRAY;
        Color foregroundColor = isDarkMode ? Color.WHITE : Color.BLACK;

        dashboardPanel.setBackground(backgroundColor);
        for (JButton button : dashboardButtons) {
            button.setBackground(isDarkMode ? Color.GRAY : Color.LIGHT_GRAY);
            button.setForeground(foregroundColor);
        }

        attendancePanel.setBackground(backgroundColor);
        calendarPanel.setBackground(backgroundColor);
        profilePanel.setBackground(backgroundColor);
        statisticsPanel.setBackground(backgroundColor);
        historyPanel.setBackground(backgroundColor);
        reportPanel.setBackground(backgroundColor);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(EnhancedAttendanceSystem::new);
    }
}
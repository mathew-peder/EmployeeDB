package employeeDB;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Main {

    private final String userName = "root";
    private final String password = "";
    private final String serverName = "localhost";
    private final int portNumber = 3306;
    private final String dbName = "test";
    //private final String tableName = "employee";

    private static JLabel labelEmployeeInfo = new JLabel("Employee Information");

    //Text fields and labels of each area of the GUI
    private static JButton buttonAdd = new JButton("Add");
    private static JLabel employeeFirstName = new JLabel("First Name");
    private static JTextField txtFirstName = new JTextField();
    private static JLabel employeeLastName = new JLabel("Last Name");
    private static JTextField txtLastName = new JTextField();
    private static JLabel employeeSSN = new JLabel("SSN");
    private static JTextField txtSSN = new JTextField();
    private static JLabel employeeGender = new JLabel("Gender");
    private static String[] genders = {"Male", "Female", "Other"};
    private static JComboBox boxGender = new JComboBox(genders);
    private static JLabel employeeDOB = new JLabel("Date of Birth");
    private static JTextField txtDOB = new JTextField();
    private static JLabel employeeSalary = new JLabel("Salary");
    private static JTextField txtSalary = new JTextField();

    //update button
    private static JButton buttonUpdate = new JButton("Update");

    //delete button
    private static JButton buttonDelete = new JButton("Delete");

    //search and views buttons
    private static JButton buttonSearch = new JButton("Search");
    private static JButton buttonPrevious = new JButton("Previous");
    private static JButton buttonNext = new JButton("Next");
    private static JButton buttonClear = new JButton("Clear");

    //text for errors or information
    private static JLabel errorText = new JLabel();

    private static Connection connect;
    private static ResultSet employeeResults;


    public static void main(String[] args) {

        Main main = new Main();
        connect = main.mysqlConnect();
        main.menuGui();
        main.mysqlConnect();
        employeeResults = results();
    }

    
    //Connects to MySQL using the predefined username, password, and database name and prints whether the connection is
    // successful or not
    public Connection mysqlConnect() {

        Connection connect = null;
        Properties connectionProps = new Properties();
        connectionProps.put("user", this.userName);
        connectionProps.put("password", this.password);
        try {
            connect = DriverManager.getConnection("jdbc:mysql://"
                            + this.serverName + ":" + this.portNumber + "/" + this.dbName,
                    connectionProps);
            if (!connect.isClosed())
                System.out.println("Connection Successful!");
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
        return connect;
    }

    //Method to create a new employee in the DB by taking in the text a user adds to each text field then using the
    //addEmployee method to add the information
    public void createEmployee() {

        String employeeFirst = txtFirstName.getText();
        String employeeLast = txtLastName.getText();
        String employeeSsn = txtSSN.getText();
        String employeeDob = txtDOB.getText();
        String employeeSalary = txtSalary.getText();
        String employeeGender = boxGender.getSelectedItem().toString();

        //Checks for any blank or null fields in the GUI as well as proper lengths of strings
        if (employeeFirst == null) {
            errorText.setText("First Name cannot be empty");
            System.out.println("No first name");
        } else if (employeeLast == null) {
            errorText.setText("Last name cannot be empty");
        } else if (employeeSsn == null) {
            errorText.setText("SSN cannot be empty");
        } else if (employeeDob == null) {
            errorText.setText("Date of Birth cannot be empty");
        } else if (employeeSalary == null) {
            errorText.setText("Salary cannot be empty");
        } else if (employeeGender == null) {
            errorText.setText("Gender cannot be empty");
        } else if (employeeFirst.length() < 2 || employeeLast.length() < 2) {
            errorText.setText("Not enough Characters in First/Last Name");
        } else if (employeeFirst.length() > 25 || employeeLast.length() > 25) {
            errorText.setText("Too many characters in First/Last name");
        } else if (employeeSsn.length() != 10) {
            errorText.setText("SSN must be 10 characters");
        }

        // Setting the salary input as an int
        else {
            int salary = 0;
            try {
                salary = Integer.parseInt(employeeSalary);
            } catch (Exception e) {
                System.out.println(e);
                errorText.setText("Salary must be yearly and greater than 1 (i.e. 20000)");
                return;
            }

            //Takes the date added and converts it to the correct date format if input correctly
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-mm-dd");
            Date doB = null;
            try {
                doB = dateFormat.parse(employeeDob);
            } catch (Exception e) {
                System.out.println(e);
                errorText.setText("Please use yyyy-mm-dd for date format");
                return;
            }

            //Adds the information from the text fields to the DB by calling the addEmployee method
            try {
                this.addEmployee(employeeFirst, employeeLast, employeeSsn, doB, salary, employeeGender);
            } catch (Exception e) {
                System.out.println(e);
                errorText.setText("Could not add Employee");
            }
        }
    }

    //Method to add the employee information to the DB Using a PreparedStatement and the correct formats for the data
    public void addEmployee(String employeeFirst, String employeeLast, String employeeSsn, Date doB, int salary, String employeeGender) {

        try {
            PreparedStatement prep = connect.prepareStatement("INSERT INTO employee VALUES (?,?,?,?,?,?)");
            prep.setString (1, employeeFirst);
            prep.setString(2, employeeLast);
            prep.setString(3, employeeSsn);
            prep.setDate(4, new java.sql.Date(doB.getTime()));
            prep.setInt(5, salary);
            prep.setString(6, employeeGender);
            prep.executeUpdate();
            errorText.setText("Successfully added Employee");
            results();
        } catch (SQLException e) {
            System.out.println(e + "Could not add to DB");
            errorText.setText("Failed adding new Employee");
        }
    }

    //Method to search the DB for a specific employee using the last name
    public void searchEmployee() {

        ResultSet results = null;
        String employeeLast = txtLastName.getText();
        try {
            PreparedStatement prep = connect.prepareStatement("SELECT * FROM employee WHERE  employeeLast =?");
            prep.setString(1, employeeLast);
            results = prep.executeQuery();

        if (results != null) {
            employeeResults = results;
            employeeResults.first();
            displayEmployee();
        } else {
            errorText.setText("Could not find results based on your search");
        }
        } catch (Exception e) {
            System.out.println(e);
            errorText.setText("Could not find results based on your search request");
        }
    }

    //Method to Update an employee using the SSN to verify the correct Employee is updated
    public void updateEmployee() {

        try {
            PreparedStatement prep = connect.prepareStatement("UPDATE employee SET employeeFirst = ?, employeeLast = ?, doB = ?, salary = ?, employeeGender = ? WHERE employeeSsn = ?");
            prep.setString (1, txtFirstName.getText());
            prep.setString(2, txtLastName.getText());
            prep.setString(6, txtSSN.getText());
            prep.setDate(3, java.sql.Date.valueOf(txtDOB.getText()));
            prep.setInt(4, Integer.parseInt(txtSalary.getText()));
            prep.setString(5, boxGender.getSelectedItem().toString());
            prep.executeUpdate();
            errorText.setText("Successfully updated Employee");
            results();
        } catch (Exception e) {
            System.out.println(e);
            errorText.setText("Failed to Update");
        }
    }

    //Method to delete an employee from the DB using the SSN to verify it's the correct Employee
    public void deleteEmployee() {
        String employeeSsn = txtSSN.getText();
        try {
            PreparedStatement prep = connect.prepareStatement("DELETE FROM employee WHERE employeeSsn = ?");
            prep.setString(1, employeeSsn);
            prep.executeUpdate();
            errorText.setText("User deleted successfully");
        }catch (Exception e){
            System.out.println(e);
            errorText.setText("Could not delete User. Please try again.");
        }
    }

    //Method that retrieves a result set from the DB
    public static ResultSet results() {
        ResultSet results = null;
        try {
            Statement state = connect.createStatement();
            state.executeQuery("SELECT * FROM employee");
            results = state.getResultSet();
        }catch (Exception e) {
            System.out.println(e);
        }
        return results;
    }

    //Method Using option to iterate through the results of the DB using previous and next.
    // I was unable to get this working completely, So I did ask for assistance
    public void iterateEmployee(String option) {
        Boolean result = true;
        if (option.equals("previous"))
        {
            try {
                employeeResults.previous();
            } catch (Exception e) {
                result = false;
            }
        } else if (option.equals("next")) {
            try {
                employeeResults.next();
            } catch (Exception e) {
                result = false;
            }
        } else {
            try {
                employeeResults.first();
            } catch (Exception e) {
                System.out.println(e);
                errorText.setText("");
            }
        }
        displayEmployee();
    }

    //Method to display the employee information in the text fields after a search is successful
    public void displayEmployee() {
        try {
            txtFirstName.setText(employeeResults.getString("employeeFirst"));
            txtLastName.setText(employeeResults.getString("employeeLast"));
            txtSSN.setText(employeeResults.getString("employeeSsn"));
            txtDOB.setText(employeeResults.getString("doB"));
            txtSalary.setText(employeeResults.getString("salary"));
            if (employeeResults.getString("employeeGender").equals("Male")) {
                boxGender.setSelectedIndex(0);
            } else if (employeeResults.getString("employeeGender").equals("Female")) {
                boxGender.setSelectedIndex(1);
            } else {
                boxGender.setSelectedItem(2);
            }
        }catch (Exception e) {
            System.out.println(e);
            errorText.setText("Could not show data");
        }
    }

    //Method to clear all information from the GUI by setting the text fields to empty values
    public void clearAll() {
        txtFirstName.setText("");
        txtLastName.setText("");
        txtSSN.setText("");
        txtDOB.setText("");
        txtSalary.setText("");
        boxGender.setSelectedItem(null);
        errorText.setText(null);
    }

    //Method to create the GUI and att the fields and buttons as well as implement
    // the actionListeners for each button
    public void menuGui () {
            JFrame frame = new JFrame("Employee Info");
            JPanel addPanel = new JPanel();
            JPanel addUpDelPanel = new JPanel();
            JPanel buttonPanel = new JPanel();
            addPanel.setLayout(new BoxLayout(addPanel, BoxLayout.PAGE_AXIS));
            addUpDelPanel.setLayout(new BoxLayout(addUpDelPanel, BoxLayout.LINE_AXIS));
            addUpDelPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 70, 225));
            addUpDelPanel.add(Box.createHorizontalGlue());
            addUpDelPanel.add(Box.createRigidArea(new Dimension(10, 10)));
            //Components added to the GUI


            addPanel.add(employeeFirstName);
            addPanel.add(txtFirstName);
            employeeFirstName.setAlignmentX(Component.CENTER_ALIGNMENT);
            txtFirstName.setPreferredSize(new Dimension(200, 25));
            txtFirstName.setMaximumSize(txtFirstName.getPreferredSize());

            addPanel.add(employeeLastName);
            addPanel.add(txtLastName);
            employeeLastName.setAlignmentX(Component.CENTER_ALIGNMENT);
            employeeLastName.setPreferredSize(new Dimension(200, 25));
            txtLastName.setMaximumSize(employeeLastName.getPreferredSize());

            addPanel.add(employeeSSN);
            addPanel.add(txtSSN);
            employeeSSN.setAlignmentX(Component.CENTER_ALIGNMENT);
            employeeSSN.setPreferredSize(new Dimension(150, 25));
            txtSSN.setMaximumSize(employeeSSN.getPreferredSize());


            addPanel.add(employeeDOB);
            addPanel.add(txtDOB);
            employeeDOB.setAlignmentX(Component.CENTER_ALIGNMENT);
            employeeDOB.setPreferredSize(new Dimension(150, 25));
            txtDOB.setMaximumSize(employeeDOB.getPreferredSize());

            addPanel.add(employeeSalary);
            addPanel.add(txtSalary);
            employeeSalary.setAlignmentX(Component.CENTER_ALIGNMENT);
            employeeSalary.setPreferredSize(new Dimension(150, 25));
            txtSalary.setMaximumSize(employeeSalary.getPreferredSize());

            addPanel.add(employeeGender);
            addPanel.add(boxGender);
            employeeGender.setAlignmentX(Component.CENTER_ALIGNMENT);
            employeeGender.setPreferredSize(new Dimension(100, 25));
            boxGender.setMaximumSize(employeeGender.getPreferredSize());

            addUpDelPanel.add(buttonAdd);
            buttonAdd.setAlignmentX(Component.CENTER_ALIGNMENT);
            buttonAdd.setPreferredSize(new Dimension(80, 20));
            buttonAdd.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    createEmployee();;
                }
            });

            addUpDelPanel.add(buttonUpdate);
            buttonUpdate.setAlignmentX(Component.CENTER_ALIGNMENT);
            buttonUpdate.setPreferredSize(new Dimension(80, 20));
            buttonUpdate.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    updateEmployee();
                }
            });

            addUpDelPanel.add(buttonDelete);
            buttonDelete.setAlignmentX(Component.CENTER_ALIGNMENT);
            buttonDelete.setPreferredSize(new Dimension(80, 20));
            buttonDelete.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    deleteEmployee();
                }
            });

            buttonPanel.add(buttonPrevious);
            buttonPrevious.setAlignmentX(Component.CENTER_ALIGNMENT);
            buttonPrevious.setPreferredSize(new Dimension(100, 20));
            buttonPrevious.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    iterateEmployee("previous");
                }
            });

            buttonPanel.add(buttonSearch);
            buttonSearch.setAlignmentX(Component.CENTER_ALIGNMENT);
            buttonSearch.setPreferredSize(new Dimension(100, 20));
            buttonSearch.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    searchEmployee();
                }
            });

            buttonPanel.add(buttonNext);
            buttonNext.setAlignmentX(Component.CENTER_ALIGNMENT);
            buttonNext.setPreferredSize(new Dimension(100, 20));
            buttonNext.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    iterateEmployee("next");
                }
            });

            frame.add(buttonClear);
            buttonClear.setBounds(305, 300, 80, 20);
            buttonClear.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    clearAll();
                }
            });


        errorText.setBounds(10, 330, 400, 20);


            frame.add(errorText);
            frame.add(addPanel, BorderLayout.CENTER);
            frame.add(addUpDelPanel, BorderLayout.SOUTH);
            frame.add(buttonPanel, BorderLayout.NORTH);
            frame.pack();
            frame.setSize(700, 400);
            frame.setLocationRelativeTo(null);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setVisible(true);
            frame.setResizable(false);

        }

}


import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

public class CustomerAccountGUI extends JFrame {
    
    // UI Components
    private JTextField txtFirstName, txtLastName, txtEmail, txtPhone, txtAccountType, txtBalance, txtSearch;
    private JButton btnAdd, btnUpdate, btnDelete, btnSearch, btnRefresh;
    private JTable accountTable;
    private DefaultTableModel tableModel;
    
    private int selectedAccountId = -1;
    private int selectedCustomerId = -1;

    public CustomerAccountGUI() {
        setTitle("Customer & Account Management");
        setSize(850, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        // --- TOP WRAPPER PANEL (Holds Search and Form) ---
        JPanel topWrapperPanel = new JPanel(new BorderLayout());

        // 1. The Search Bar Panel
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.setBorder(BorderFactory.createTitledBorder("Search Accounts"));
        searchPanel.add(new JLabel("Search (Name or Account ID):"));
        txtSearch = new JTextField(20);
        btnSearch = new JButton("Search");
        btnRefresh = new JButton("Refresh/Clear");
        
        searchPanel.add(txtSearch);
        searchPanel.add(btnSearch);
        searchPanel.add(btnRefresh);

        // 2. The Input Form Panel
        JPanel formPanel = new JPanel(new GridLayout(7, 2, 5, 5)); 
        formPanel.setBorder(BorderFactory.createTitledBorder("Customer & Account Details"));

        formPanel.add(new JLabel("First Name:"));
        txtFirstName = new JTextField();
        formPanel.add(txtFirstName);

        formPanel.add(new JLabel("Last Name:"));
        txtLastName = new JTextField();
        formPanel.add(txtLastName);

        formPanel.add(new JLabel("Email:"));
        txtEmail = new JTextField();
        formPanel.add(txtEmail);

        formPanel.add(new JLabel("Phone Number:"));
        txtPhone = new JTextField();
        formPanel.add(txtPhone);

        formPanel.add(new JLabel("Account Type (Savings/Current):"));
        txtAccountType = new JTextField();
        formPanel.add(txtAccountType);

        formPanel.add(new JLabel("Initial Balance:"));
        txtBalance = new JTextField();
        formPanel.add(txtBalance);

        // CRUD Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout());
        btnAdd = new JButton("Add");
        btnUpdate = new JButton("Update");
        btnDelete = new JButton("Delete");
        
        buttonPanel.add(btnAdd);
        buttonPanel.add(btnUpdate);
        buttonPanel.add(btnDelete);
        
        formPanel.add(new JLabel("Actions:")); 
        formPanel.add(buttonPanel);

        // Add Search and Form to the Top Wrapper
        topWrapperPanel.add(searchPanel, BorderLayout.NORTH);
        topWrapperPanel.add(formPanel, BorderLayout.CENTER);

        add(topWrapperPanel, BorderLayout.NORTH);

        // --- BOTTOM PART: The Table ---
        String[] columns = {"Account ID", "Customer ID", "First Name", "Last Name", "Email", "Phone", "Type", "Balance"};
        tableModel = new DefaultTableModel(columns, 0);
        accountTable = new JTable(tableModel);
        
        JScrollPane scrollPane = new JScrollPane(accountTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Database Records"));
        add(scrollPane, BorderLayout.CENTER);

        loadTableData();

        // --- CLICK LISTENERS ---
        accountTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int selectedRow = accountTable.getSelectedRow();
                if (selectedRow >= 0) {
                    selectedAccountId = (int) tableModel.getValueAt(selectedRow, 0);
                    selectedCustomerId = (int) tableModel.getValueAt(selectedRow, 1);
                    txtFirstName.setText(tableModel.getValueAt(selectedRow, 2).toString());
                    txtLastName.setText(tableModel.getValueAt(selectedRow, 3).toString());
                    txtEmail.setText(tableModel.getValueAt(selectedRow, 4).toString());
                    txtPhone.setText(tableModel.getValueAt(selectedRow, 5).toString());
                    txtAccountType.setText(tableModel.getValueAt(selectedRow, 6).toString());
                    txtBalance.setText(tableModel.getValueAt(selectedRow, 7).toString());
                }
            }
        });

        btnAdd.addActionListener(e -> addCustomerAndAccount());
        btnUpdate.addActionListener(e -> updateCustomerAndAccount());
        btnDelete.addActionListener(e -> deleteAccount());
        btnSearch.addActionListener(e -> searchDatabase());
        btnRefresh.addActionListener(e -> {
            txtSearch.setText("");
            loadTableData();
            clearFields();
        });
    }

    // --- SEARCH METHOD ---
    private void searchDatabase() {
        String keyword = txtSearch.getText().trim();
        if (keyword.isEmpty()) {
            loadTableData();
            return;
        }

        tableModel.setRowCount(0); 
        try (Connection conn = DBConnection.getConnection()) {
            // Search by First Name OR Last Name OR Account ID
            String sql = "SELECT a.account_id, c.customer_id, c.first_name, c.last_name, c.email, c.phone_number, a.account_type, a.balance " +
                         "FROM Customer c JOIN Account a ON c.customer_id = a.customer_id " +
                         "WHERE c.first_name LIKE ? OR c.last_name LIKE ? OR a.account_id = ?";
                         
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, "%" + keyword + "%"); // % means it can match partial words
            pstmt.setString(2, "%" + keyword + "%");
            
            // Try to set it as an ID if they typed a number
            try {
                pstmt.setInt(3, Integer.parseInt(keyword));
            } catch (NumberFormatException e) {
                pstmt.setInt(3, -1); // If they typed letters, just search for -1 (which doesn't exist)
            }

            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Object[] rowData = {
                    rs.getInt("account_id"),
                    rs.getInt("customer_id"),
                    rs.getString("first_name"),
                    rs.getString("last_name"),
                    rs.getString("email"),
                    rs.getString("phone_number"),
                    rs.getString("account_type"),
                    rs.getDouble("balance")
                };
                tableModel.addRow(rowData);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error searching: " + ex.getMessage());
        }
    }

    // --- OTHER METHODS ---
    private void addCustomerAndAccount() {
        try (Connection conn = DBConnection.getConnection()) {
            String custSql = "INSERT INTO Customer (first_name, last_name, email, phone_number) VALUES (?, ?, ?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(custSql, Statement.RETURN_GENERATED_KEYS);
            pstmt.setString(1, txtFirstName.getText());
            pstmt.setString(2, txtLastName.getText());
            pstmt.setString(3, txtEmail.getText());
            pstmt.setString(4, txtPhone.getText());
            pstmt.executeUpdate();

            ResultSet rs = pstmt.getGeneratedKeys();
            int newCustomerId = 0;
            if (rs.next()) newCustomerId = rs.getInt(1);

            String accSql = "INSERT INTO Account (customer_id, account_type, balance) VALUES (?, ?, ?)";
            PreparedStatement accStmt = conn.prepareStatement(accSql);
            accStmt.setInt(1, newCustomerId);
            accStmt.setString(2, txtAccountType.getText());
            accStmt.setDouble(3, Double.parseDouble(txtBalance.getText()));
            accStmt.executeUpdate();

            JOptionPane.showMessageDialog(this, "Added Successfully!");
            clearFields();
            loadTableData();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }

    private void updateCustomerAndAccount() {
        if (selectedAccountId == -1) {
            JOptionPane.showMessageDialog(this, "Please click an account in the table first!");
            return;
        }
        try (Connection conn = DBConnection.getConnection()) {
            String custSql = "UPDATE Customer SET first_name=?, last_name=?, email=?, phone_number=? WHERE customer_id=?";
            PreparedStatement pstmt = conn.prepareStatement(custSql);
            pstmt.setString(1, txtFirstName.getText());
            pstmt.setString(2, txtLastName.getText());
            pstmt.setString(3, txtEmail.getText());
            pstmt.setString(4, txtPhone.getText());
            pstmt.setInt(5, selectedCustomerId);
            pstmt.executeUpdate();

            String accSql = "UPDATE Account SET account_type=? WHERE account_id=?";
            PreparedStatement accStmt = conn.prepareStatement(accSql);
            accStmt.setString(1, txtAccountType.getText());
            accStmt.setInt(2, selectedAccountId);
            accStmt.executeUpdate();

            JOptionPane.showMessageDialog(this, "Updated Successfully!");
            clearFields();
            loadTableData();
            selectedAccountId = -1;
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }

    private void deleteAccount() {
        if (selectedAccountId == -1) {
            JOptionPane.showMessageDialog(this, "Please click an account in the table first!");
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this account?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try (Connection conn = DBConnection.getConnection()) {
                String sql = "DELETE FROM Customer WHERE customer_id=?";
                PreparedStatement pstmt = conn.prepareStatement(sql);
                pstmt.setInt(1, selectedCustomerId);
                pstmt.executeUpdate();

                JOptionPane.showMessageDialog(this, "Deleted Successfully!");
                clearFields();
                loadTableData();
                selectedAccountId = -1; 
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
            }
        }
    }

    private void loadTableData() {
        tableModel.setRowCount(0); 
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT a.account_id, c.customer_id, c.first_name, c.last_name, c.email, c.phone_number, a.account_type, a.balance " +
                         "FROM Customer c JOIN Account a ON c.customer_id = a.customer_id";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {
                Object[] rowData = {
                    rs.getInt("account_id"),
                    rs.getInt("customer_id"),
                    rs.getString("first_name"),
                    rs.getString("last_name"),
                    rs.getString("email"),
                    rs.getString("phone_number"),
                    rs.getString("account_type"),
                    rs.getDouble("balance")
                };
                tableModel.addRow(rowData);
            }
        } catch (Exception ex) {
            System.out.println("Error loading table data: " + ex.getMessage());
        }
    }

    private void clearFields() {
        txtFirstName.setText(""); txtLastName.setText(""); txtEmail.setText("");
        txtPhone.setText(""); txtAccountType.setText(""); txtBalance.setText("");
        selectedAccountId = -1;
        selectedCustomerId = -1;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new CustomerAccountGUI().setVisible(true));
    }
}
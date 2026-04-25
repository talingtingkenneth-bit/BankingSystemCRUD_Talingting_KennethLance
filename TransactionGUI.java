import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class TransactionGUI extends JFrame {

    private JTextField txtAccountId, txtAmount;
    private JButton btnDeposit, btnWithdraw;

    public TransactionGUI() {
        setTitle("Transaction Management");
        setSize(400, 250);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // Only closes this window, not the whole app
        setLayout(new BorderLayout(10, 10));

        // Create the form
        JPanel formPanel = new JPanel(new GridLayout(3, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        formPanel.add(new JLabel("Account ID:"));
        txtAccountId = new JTextField();
        formPanel.add(txtAccountId);

        formPanel.add(new JLabel("Amount:"));
        txtAmount = new JTextField();
        formPanel.add(txtAmount);

        // Create the buttons
        btnDeposit = new JButton("Deposit");
        btnWithdraw = new JButton("Withdraw");

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(btnDeposit);
        buttonPanel.add(btnWithdraw);

        add(formPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        // Button Actions
        btnDeposit.addActionListener(e -> processTransaction("Deposit"));
        btnWithdraw.addActionListener(e -> processTransaction("Withdraw"));
    }

    private void processTransaction(String transactionType) {
        String accountIdStr = txtAccountId.getText().trim();
        String amountStr = txtAmount.getText().trim();

        if (accountIdStr.isEmpty() || amountStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter both Account ID and Amount.");
            return;
        }

        try {
            int accountId = Integer.parseInt(accountIdStr);
            double amount = Double.parseDouble(amountStr);

            if (amount <= 0) {
                JOptionPane.showMessageDialog(this, "Amount must be greater than zero.");
                return;
            }

            try (Connection conn = DBConnection.getConnection()) {
                // 1. Check if the account exists and get current balance
                String checkSql = "SELECT balance FROM Account WHERE account_id = ?";
                PreparedStatement checkStmt = conn.prepareStatement(checkSql);
                checkStmt.setInt(1, accountId);
                ResultSet rs = checkStmt.executeQuery();

                if (rs.next()) {
                    double currentBalance = rs.getDouble("balance");

                    // 2. Withdrawal Logic (Check if they have enough money!)
                    if (transactionType.equals("Withdraw") && currentBalance < amount) {
                        JOptionPane.showMessageDialog(this, "Insufficient balance! Current balance is: " + currentBalance);
                        return;
                    }

                    // 3. Calculate new balance
                    double newBalance = transactionType.equals("Deposit") ? (currentBalance + amount) : (currentBalance - amount);

                    // 4. Update the Account balance in the database
                    String updateSql = "UPDATE Account SET balance = ? WHERE account_id = ?";
                    PreparedStatement updateStmt = conn.prepareStatement(updateSql);
                    updateStmt.setDouble(1, newBalance);
                    updateStmt.setInt(2, accountId);
                    updateStmt.executeUpdate();

                    // 5. Save the record in the TransactionLog table
                    String logSql = "INSERT INTO TransactionLog (account_id, transaction_type, amount) VALUES (?, ?, ?)";
                    PreparedStatement logStmt = conn.prepareStatement(logSql);
                    logStmt.setInt(1, accountId);
                    logStmt.setString(2, transactionType);
                    logStmt.setDouble(3, amount);
                    logStmt.executeUpdate();

                    JOptionPane.showMessageDialog(this, transactionType + " successful! New Balance: " + newBalance);
                    txtAccountId.setText("");
                    txtAmount.setText("");

                } else {
                    JOptionPane.showMessageDialog(this, "Account ID not found in database.");
                }
            }

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Please enter valid numbers for ID and Amount.");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Database Error: " + ex.getMessage());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new TransactionGUI().setVisible(true));
    }
}
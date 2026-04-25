import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public class TransactionHistoryGUI extends JFrame {

    private JTable historyTable;
    private DefaultTableModel tableModel;
    private JButton btnRefresh;

    public TransactionHistoryGUI() {
        setTitle("Transaction History Logs");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // Only close this window
        setLayout(new BorderLayout(10, 10));

        // Create the Table
        String[] columns = {"Transaction ID", "Account ID", "Type", "Amount", "Date & Time"};
        tableModel = new DefaultTableModel(columns, 0);
        historyTable = new JTable(tableModel);

        JScrollPane scrollPane = new JScrollPane(historyTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder("All Transactions"));
        add(scrollPane, BorderLayout.CENTER);

        // Create a Refresh Button
        JPanel bottomPanel = new JPanel();
        btnRefresh = new JButton("Refresh Logs");
        bottomPanel.add(btnRefresh);
        add(bottomPanel, BorderLayout.SOUTH);

        // Load data immediately
        loadHistoryData();

        // Button Action
        btnRefresh.addActionListener(e -> loadHistoryData());
    }

    private void loadHistoryData() {
        tableModel.setRowCount(0); // Clear table before loading
        try (Connection conn = DBConnection.getConnection()) {
            // Fetch everything from TransactionLog, showing the newest ones first
            String sql = "SELECT * FROM TransactionLog ORDER BY transaction_date DESC";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {
                Object[] rowData = {
                    rs.getInt("transaction_id"),
                    rs.getInt("account_id"),
                    rs.getString("transaction_type"),
                    rs.getDouble("amount"),
                    rs.getTimestamp("transaction_date")
                };
                tableModel.addRow(rowData);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error loading logs: " + ex.getMessage());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new TransactionHistoryGUI().setVisible(true));
    }
}
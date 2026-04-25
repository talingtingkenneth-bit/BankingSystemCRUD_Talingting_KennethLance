
# Simple Banking System with CRUD Operations
**Author:** Kenneth Lance Talingting  
**Course:** [Insert Course Name Here]

## System Description
This project is a Simple Banking Application built using Java Swing and MySQL. It manages Customer Accounts, Account Records, and Financial Transactions. The system allows users to perform CRUD operations (Create, Read, Update, Delete) on bank accounts, execute Deposit and Withdraw transactions, view real-time balances, and search for accounts using a built-in search feature.

## ERD Explanation
The database is structured around three main tables with relational mapping:
1. **Customer Table:** Stores the primary customer details (First Name, Last Name, Email, Phone). The `customer_id` acts as the Primary Key.
2. **Account Table:** Linked to the Customer table via a Foreign Key (`customer_id`). It stores the `account_type` (Savings/Current) and the current `balance`. This enforces a One-to-Many relationship (One Customer -> Many Accounts).
3. **Transaction Table (TransactionLog):** Linked to the Account table via a Foreign Key (`account_id`). It logs every Deposit and Withdraw action alongside a timestamp. This enforces a One-to-Many relationship (One Account -> Many Transactions).

## How to Run the Program
1. **Database Setup:** - Open MySQL Workbench or XAMPP (phpMyAdmin).
   - Create a database named `BankingSystem`.
   - Import the provided `BankingSystem.sql` file to automatically generate the tables.
2. **Project Setup:**
   - Open the project folder in Apache NetBeans (or your preferred Java IDE).
   - Ensure the `mysql-connector-j-x.x.x.jar` (JDBC Driver) is added to the project Libraries.
   - Update the `DBConnection.java` file with your local MySQL username and password (if applicable).
3. **Execution:**
   - Run the main class `BankingSystemCRUD_Talingting_Kennethlacce.java` to launch the application.

## Functionalities Included
- Add, Update, View, and Delete Customer Accounts.
- Search Accounts by Name or ID.
- Deposit and Withdraw funds (with insufficient balance validation).
- View a complete Log of all Transactions.

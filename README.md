# 🩸 Blood Bank Project 

A robust, terminal-based Blood Bank Management system designed to manage blood donation and distribution operations. This project focuses on backend logic, secure role-based access, and persistent data storage using a relational database.

---

## ✨ Key Features

- **Role-Based Access Control:** Separate functionalities for Admin and Staff users to ensure system security.
- **Blood Inventory Management:** Full CRUD operations for blood groups, including stock updates and type tracking.
- **Donor & Recipient Processing:** Real-time donor registration and blood request generation linked to patient data.
- **Database Persistence:** Integrated with MySQL via JDBC for reliable data management.

---

## 🛠️ Technical Stack

| Layer | Technology |
|-------|------------|
| Language | Java |
| Database | MySQL |
| Connector | MySQL Connector/J 9.5.0 |

---

## 🚀 Getting Started

### Prerequisites

1. Install [Java JDK](https://www.oracle.com/java/technologies/downloads/)
2. Install [MySQL Server](https://dev.mysql.com/downloads/mysql/)

### Setup

1. Create a database named `blood_bank` in your MySQL server.
2. Update the credentials in `DatabaseManager.java` with your local DB username and password.
3. Add the `mysql-connector-j-9.5.0.jar` from the `/libs` folder to your project's build path.
4. Run `Main.java` to start the application.

---

## 📁 Project Structure

```
📂 blood-bank-system/
├── /code    ← All Java source files
└── /libs    ← MySQL database driver (.jar)
```

---

## 📄 License

MIT — free to use, modify, share.

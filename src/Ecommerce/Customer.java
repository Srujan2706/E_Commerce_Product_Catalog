package Ecommerce;

import java.sql.*;
import java.util.Scanner;

public class Customer {
    private Connection connection;
    private Scanner scanner;

    public Customer(Connection connection, Scanner scanner) {
        this.connection = connection;
        this.scanner = scanner;
    }
    //Register Func for Customer
    public String register() throws SQLException {
        scanner.nextLine();
        System.out.print("Full Name: ");
        String name = scanner.nextLine();
        System.out.print("Email: ");
        String gmail_c = scanner.nextLine();
        System.out.print("Password: ");
        String c_pass = scanner.nextLine();
        System.out.print("Address: ");
        String address = scanner.nextLine();
        System.out.print("Phone Number: ");
        String phone = scanner.nextLine();

        if (userExists(gmail_c)) {
            System.out.println("User Already Exists for this Email Address!!");
            return name;
        }

        String customerId = generateCustomerId();

        String registerQuery = "INSERT INTO Customer(Customer_id, Name, gmail_c, c_pass, Address, Phone_number) VALUES(?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(registerQuery)) {
            stmt.setString(1, customerId);
            stmt.setString(2, name);
            stmt.setString(3, gmail_c);
            stmt.setString(4, c_pass);
            stmt.setString(5, address);
            stmt.setString(6, phone);
            int rows = stmt.executeUpdate();
            if (rows > 0) {
                System.out.println("Registration Successful! Your Customer ID is: " + customerId);
            } else {
                System.out.println("Registration Failed.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return customerId;
    }
    //Login Func for customer
    public String login() {
        scanner.nextLine();
        System.out.print("Email: ");
        String gmail_c = scanner.nextLine();
        System.out.print("Password: ");
        String c_pass = scanner.nextLine();

        String loginQuery = "SELECT * FROM Customer WHERE gmail_c = ? AND c_pass = ?";
        try (PreparedStatement stmt = connection.prepareStatement(loginQuery)) {
            stmt.setString(1, gmail_c);
            stmt.setString(2, c_pass);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                System.out.println("Login successful. Welcome, " + rs.getString("Name") + "!");
                return rs.getString("Customer_id"); // ✅
            } else {
                System.out.println("Invalid credentials.");
                return null;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    //userExists func to get whether the customer exists or not
    private boolean userExists(String gmail_c) throws SQLException {
        String query = "SELECT * FROM Customer WHERE gmail_c = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, gmail_c);
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    //generateCustomerId func to generate customer id when registered
    private String generateCustomerId() {
        String prefix = "CUS";
        String query = "SELECT Customer_id FROM Customer ORDER BY Customer_id DESC LIMIT 1";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            if (rs.next()) {
                String lastId = rs.getString("Customer_id"); // e.g., "CUS007"
                int num = Integer.parseInt(lastId.substring(3));
                num++;
                return String.format("%s%03d", prefix, num);
            } else {
                return "CUS001";
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "CUS001";
    }
}

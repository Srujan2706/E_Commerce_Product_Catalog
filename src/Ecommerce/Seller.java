package Ecommerce;

import java.sql.*;
import java.util.Scanner;

public class Seller {
    private Connection connection;
    private Scanner scanner;

    public Seller(Connection connection, Scanner scanner) {
        this.connection = connection;
        this.scanner = scanner;
    }
    //Register func for seller
    public void register() {
        scanner.nextLine();
        System.out.print("Full Name: ");
        String sname = scanner.nextLine();
        System.out.print("Email: ");
        String gmail_s = scanner.nextLine();
        System.out.print("Password: ");
        String s_pass = scanner.nextLine();

        if (userExists(gmail_s)) {
            System.out.println("User Already Exists for this Email Address!!");
            return;
        }

        String sellerId = generateSellerId();

        String registerQuery = "INSERT INTO Seller(Seller_id, sname, gmail_s, s_pass) VALUES(?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(registerQuery)) {
            stmt.setString(1, sellerId);
            stmt.setString(2, sname);
            stmt.setString(3, gmail_s);
            stmt.setString(4, s_pass);
            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                System.out.println("Registration Successful! Your Seller ID is: " + sellerId);
            } else {
                System.out.println("Registration Failed!");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    //login func for seller
    public String login() {
        scanner.nextLine();
        System.out.print("Email: ");
        String gmail_s = scanner.nextLine();
        System.out.print("Password: ");
        String s_pass = scanner.nextLine();

        String loginQuery = "SELECT * FROM Seller WHERE gmail_s = ? AND s_pass = ?";
        try (PreparedStatement stmt = connection.prepareStatement(loginQuery)) {
            stmt.setString(1, gmail_s);
            stmt.setString(2, s_pass);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                System.out.println("Login successful. Welcome, " + rs.getString("sname") + "!");
                return rs.getString("Seller_id");
            } else {
                System.out.println("Invalid credentials.");
                return null;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    //userExists func to get whether the seller exists or not
    private boolean userExists(String email) {
        String query = "SELECT * FROM Seller WHERE gmail_s = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    //generateCustomerId func to seller customer id when registered
    private String generateSellerId() {
        String prefix = "SEL";
        String query = "SELECT Seller_id FROM Seller ORDER BY Seller_id DESC LIMIT 1";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            if (rs.next()) {
                String lastId = rs.getString("Seller_id"); // e.g., "SEL005"
                int num = Integer.parseInt(lastId.substring(3));
                num++;
                return String.format("%s%03d", prefix, num);
            } else {
                return "SEL001";
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "SEL001";
    }
}

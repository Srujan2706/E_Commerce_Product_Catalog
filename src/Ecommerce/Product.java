package Ecommerce;
import java.sql.*;
import java.util.Scanner;

public class Product {
    private Connection connection;
    private Scanner scanner;

    public Product(Connection connection, Scanner scanner) {
        this.connection = connection;
        this.scanner = scanner;
    }
    //addProduct func to add product to cart
    public void addProduct(String sellerId) {
        scanner.nextLine();

        System.out.print("Product Name: ");
        String name = scanner.nextLine();
        System.out.print("Type (e.g., Shoes, Shirt): ");
        String type = scanner.nextLine();
        System.out.print("Color: ");
        String color = scanner.nextLine();
        System.out.print("Size: ");
        String size = scanner.nextLine();
        System.out.print("Cost: ");
        double cost = scanner.nextDouble();
        System.out.print("Quantity: ");
        int quantity = scanner.nextInt();

        String productId = generateProductId();

        String insertQuery = "INSERT INTO Product(Product_id, Name, Type, Color, Size, Cost, Quantity, Seller_id) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(insertQuery)) {
            stmt.setString(1, productId);
            stmt.setString(2, name);
            stmt.setString(3, type);
            stmt.setString(4, color);
            stmt.setString(5, size);
            stmt.setDouble(6, cost);
            stmt.setInt(7, quantity);
            stmt.setString(8, sellerId);  // Set the sellerId

            int rows = stmt.executeUpdate();
            if (rows > 0) {
                System.out.println("Product added successfully! Product ID: " + productId);
            } else {
                System.out.println("Product addition failed.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    //generateProductId for the products added by the seller.
    private String generateProductId() {
        String prefix = "PRO";
        String query = "SELECT Product_id FROM Product ORDER BY Product_id DESC LIMIT 1";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            if (rs.next()) {
                String lastId = rs.getString("Product_id"); // e.g., "PRO007"
                int num = Integer.parseInt(lastId.substring(3));
                num++;
                return String.format("%s%03d", prefix, num); // PRO008
            } else {
                return "PRO001";
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "PRO001";
    }
}

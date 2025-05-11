package Ecommerce;

import java.sql.*;
import java.time.LocalDate;
import java.util.UUID;

public class Payment {
    private Connection connection;

    public Payment(Connection connection) {
        this.connection = connection;
    }

    // Calculate total amount for customer's cart
    public double calculateTotalAmount(String customerId) throws SQLException {
        String sql = """
            SELECT SUM(p.Cost * ci.Quantity) AS TotalAmount
            FROM Cart_Item ci
            JOIN Product p ON ci.Product_id = p.Product_id
            JOIN Cart c ON ci.Cart_id = c.Cart_id
            WHERE c.Customer_id = ? AND c.Status = 'Active';
        """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, customerId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getDouble("TotalAmount");
            }
        }
        return 0.0;
    }

    // Get Cart ID for customer
    public String getCartId(String customerId) throws SQLException {
        String sql = "SELECT Cart_id FROM Cart WHERE Customer_id = ? AND Status = 'Active'";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, customerId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("Cart_id");
            }
        }
        return null;
    }

    // Process the payment with transaction handling
    public void processPayment(String customerId) {
        try {
            String cartId = getCartId(customerId);
            if (cartId == null) {
                System.out.println("No pending cart found.");
                return;
            }

            double amount = calculateTotalAmount(customerId);
            if (amount == 0.0) {
                System.out.println("Cart is empty. Nothing to pay.");
                return;
            }

            // Create a new payment ID
            String paymentId = "PAY" + UUID.randomUUID().toString().substring(0, 5).toUpperCase();

            // Insert into Payment table
            String insertPayment = "INSERT INTO Payment (Payment_id, Customer_id, Amount, Status, Payment_date) VALUES (?, ?, ?, 'Success', ?)";
            try (PreparedStatement stmt = connection.prepareStatement(insertPayment)) {
                stmt.setString(1, paymentId);
                stmt.setString(2, customerId);
                stmt.setDouble(3, amount);
                stmt.setDate(4, Date.valueOf(LocalDate.now()));
                stmt.executeUpdate();
            }

            // Update Cart status and link payment
            String updateCart = "UPDATE Cart SET Status = 'Paid', Payment_id = ? WHERE Cart_id = ?";
            try (PreparedStatement stmt = connection.prepareStatement(updateCart)) {
                stmt.setString(1, paymentId);
                stmt.setString(2, cartId);
                stmt.executeUpdate();
            }

            System.out.println("Payment successful. Amount paid: $" + amount);
            System.out.println("Payment ID: " + paymentId);

            // Create a new active cart after payment
            String newCartId = generateNewCartId();
            String insertNewCart = "INSERT INTO Cart (Cart_id, Customer_id, Status) VALUES (?, ?, 'Active')";
            try (PreparedStatement stmt = connection.prepareStatement(insertNewCart)) {
                stmt.setString(1, newCartId);
                stmt.setString(2, customerId);
                stmt.executeUpdate();
                System.out.println("A new active cart has been created for you. Cart ID: " + newCartId);
            }

        } catch (SQLException e) {
            System.out.println("Payment failed: " + e.getMessage());
        }
    }

    // Generate a new unique cart ID
    private String generateNewCartId() throws SQLException {
        String prefix = "CART";
        String query = "SELECT Cart_id FROM Cart ORDER BY Cart_id DESC LIMIT 1";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            if (rs.next()) {
                String lastId = rs.getString("Cart_id");
                int num = Integer.parseInt(lastId.substring(4));
                num++;
                return String.format("%s%03d", prefix, num);
            } else {
                return "CART001";
            }
        }
    }
}

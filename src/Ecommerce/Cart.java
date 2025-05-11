package Ecommerce;

import java.sql.*;
import java.util.Scanner;

public class Cart {
    private Connection connection;
    private Scanner scanner;

    public Cart(Connection connection, Scanner scanner) {
        this.connection = connection;
        this.scanner = scanner;
    }
    //createCartForCustomer func to create a cart for customer
    public void createCartForCustomer(String customerId) {
        if (cartExists(customerId)) {
            System.out.println("Cart already exists for this customer.");
            return;
        }

        String cartId = generateCartId();

        String insertQuery = "INSERT INTO Cart(Cart_id, Customer_id, Status) VALUES (?, ?, 'Active')";
        try (PreparedStatement stmt = connection.prepareStatement(insertQuery)) {
            stmt.setString(1, cartId);
            stmt.setString(2, customerId);
            int rows = stmt.executeUpdate();
            if (rows > 0) {
                System.out.println("Cart created successfully! Cart ID: " + cartId);
            } else {
                System.out.println("Cart creation failed.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    //cartExists func to check whether the cart exists for a customer or not
    private boolean cartExists(String customerId) {
        String query = "SELECT * FROM Cart WHERE Customer_id = ? AND Status = 'Active'";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, customerId);
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    //generateCartId func to generate cart id for a customer
    private String generateCartId() {
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
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "CART001";
    }
    //It fetches the Cart_id for a specific customer whose cart is currently active.
    private String getCartIdForCustomer(String customerId) {
        String cartId = null;
        String query = "SELECT Cart_id FROM Cart WHERE Customer_id = ? AND Status = 'Active'";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, customerId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                cartId = rs.getString("Cart_id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return cartId;
    }
    //addProductToCart func add product to cart
    public void addProductToCart(String customerId) {
        try {
            // Retrieve the cart ID for the customer
            String cartId = getCartIdForCustomer(customerId);
            if (cartId == null) {
                // Create a new cart if none exists
                createCartForCustomer(customerId);
                cartId = getCartIdForCustomer(customerId);
                if (cartId == null) {
                    System.out.println("Failed to create new cart.");
                    return;
                } else {
                    System.out.println("A new active cart has been created for you. Cart ID: " + cartId);
                }
            }

            // Check if the cart is still active
            String checkCartStatusQuery = "SELECT Status FROM Cart WHERE Cart_id = ?";
            try (PreparedStatement checkStatusStmt = connection.prepareStatement(checkCartStatusQuery)) {
                checkStatusStmt.setString(1, cartId);
                ResultSet statusRs = checkStatusStmt.executeQuery();
                if (statusRs.next()) {
                    String cartStatus = statusRs.getString("Status");
                    if (!cartStatus.equals("Active")) {
                        System.out.println("Cannot modify cart. Cart is " + cartStatus + ".");
                        return;
                    }
                }
            }
            //Continues adding product to cart until its true
            boolean continueAdding = true;
            while (continueAdding) {
                // Show available products
                String fetchProductsQuery = "SELECT Product_id, Name, Cost FROM Product";
                try (PreparedStatement stmt = connection.prepareStatement(fetchProductsQuery);
                     ResultSet rs = stmt.executeQuery()) {

                    System.out.println("\nAvailable Products:");
                    while (rs.next()) {
                        String productId = rs.getString("Product_id");
                        String productName = rs.getString("Name");
                        double cost = rs.getDouble("Cost");
                        System.out.println(productId + ": " + productName + " - $" + cost);
                    }
                }

                // Ask customer for product and quantity
                System.out.print("\nEnter the product ID you want to add to your cart: ");
                String productId = scanner.nextLine().trim();
                System.out.print("Enter the quantity you want to add: ");
                int quantity = scanner.nextInt();
                scanner.nextLine();

                // Check if product exists
                String checkProductQuery = "SELECT * FROM Product WHERE Product_id = ?";
                try (PreparedStatement checkStmt = connection.prepareStatement(checkProductQuery)) {
                    checkStmt.setString(1, productId);
                    try (ResultSet checkRs = checkStmt.executeQuery()) {
                        if (!checkRs.next()) {
                            System.out.println("Invalid product ID.");
                            continue;
                        }

                        String name = checkRs.getString("Name");

                        // Check if product already in cart
                        String checkItemQuery = "SELECT Quantity FROM Cart_Item WHERE Cart_id = ? AND Product_id = ?";
                        try (PreparedStatement checkItemStmt = connection.prepareStatement(checkItemQuery)) {
                            checkItemStmt.setString(1, cartId);
                            checkItemStmt.setString(2, productId);
                            ResultSet itemRs = checkItemStmt.executeQuery();

                            if (itemRs.next()) {
                                // Product exists, update quantity
                                int currentQty = itemRs.getInt("Quantity");
                                int newQty = currentQty + quantity;
                                String updateQtyQuery = "UPDATE Cart_Item SET Quantity = ? WHERE Cart_id = ? AND Product_id = ?";
                                try (PreparedStatement updateStmt = connection.prepareStatement(updateQtyQuery)) {
                                    updateStmt.setInt(1, newQty);
                                    updateStmt.setString(2, cartId);
                                    updateStmt.setString(3, productId);
                                    updateStmt.executeUpdate();
                                    System.out.println("Updated quantity for product: " + name + " to " + newQty);
                                }
                            } else {
                                // New entry
                                String insertProductToCart = "INSERT INTO Cart_Item (Cart_id, Product_id, Quantity) VALUES (?, ?, ?)";
                                try (PreparedStatement addProductStmt = connection.prepareStatement(insertProductToCart)) {
                                    addProductStmt.setString(1, cartId);
                                    addProductStmt.setString(2, productId);
                                    addProductStmt.setInt(3, quantity);
                                    addProductStmt.executeUpdate();
                                    System.out.println("Product added to your cart: " + name + " with quantity: " + quantity);
                                }
                            }
                        }
                    }
                }

                // Ask if they want to add another product
                System.out.print("Do you want to add another product? (yes/no): ");
                String response = scanner.nextLine().trim().toLowerCase();
                continueAdding = response.equals("yes");
            }

        } catch (SQLException e) {
            System.out.println("Error adding product to cart: " + e.getMessage());
        }
    }

}

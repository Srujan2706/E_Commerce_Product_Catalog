package Ecommerce;

import java.sql.*;
import java.util.Scanner;

public class MAIN {
    private static final String jdbcURL = "jdbc:mysql://localhost:3306/ecommerce";
    private static final String dbUser = "root";
    private static final String dbPassword = "Srujan@2706";

    public static void main(String[] args) {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            System.out.println("Driver loaded successfully.");
        } catch (ClassNotFoundException e) {
            System.out.println("JDBC Driver not found: " + e.getMessage());
            return;
        }

        try (Connection connection = DriverManager.getConnection(jdbcURL, dbUser, dbPassword);
             Scanner scanner = new Scanner(System.in)) {

            System.out.println("Connected to the database.");

            while (true) {
                System.out.println("\n===== E-Commerce Console App =====");
                System.out.println("1. Register as Seller");
                System.out.println("2. Login as Seller");
                System.out.println("3. Register as Customer");
                System.out.println("4. Login as Customer");
                System.out.println("5. Exit");
                System.out.print("Select option: ");
                int choice = scanner.nextInt();
                //register as seller
                if (choice == 1) {
                    Seller seller = new Seller(connection, scanner);
                    seller.register();
                //login as seller
                } else if (choice == 2) {
                    Seller seller = new Seller(connection, scanner);
                    String sellerId = seller.login();
                    if (sellerId != null) {
                        System.out.println("Logged in successfully as seller: " + sellerId);
                        Product product = new Product(connection, scanner);
                        System.out.println("Add Product:");
                        product.addProduct(sellerId);
                    } else {
                        System.out.println("Invalid login.");
                    }
                //register as customer
                } else if (choice == 3) {
                    Customer customer = new Customer(connection, scanner);
                    String customerId = customer.register();
                    if (customerId != null) {
                        Cart cart = new Cart(connection, scanner);
                        cart.createCartForCustomer(customerId);
                    }
                //login as customer
                } else if (choice == 4) {
                    Customer customer = new Customer(connection, scanner);
                    String customerId = customer.login();
                    if (customerId != null) {
                        System.out.println("Logged in successfully as customer: " + customerId);
                        Cart cart = new Cart(connection, scanner);
                        cart.createCartForCustomer(customerId);

                        cart.addProductToCart(customerId);
                        scanner.nextLine();

                        // Ask if customer wants to proceed to payment
                        System.out.print("Do you want to proceed to payment? (yes/no): ");
                        String proceed = scanner.nextLine().trim().toLowerCase();

                        if (proceed.equals("yes")) {
                            Payment payment = new Payment(connection);
                            payment.processPayment(customerId);
                            System.out.println("Payment successful.\n Thank you for coming!!!");
                        }else{
                            System.out.println("Thank you for being here, comeback when your ready for payment.");
                        }
                    } else {
                        System.out.println("Invalid login.");
                    }
                //Exit
                } else if (choice == 5) {
                    System.out.println("Exiting...");
                    break;

                } else {
                    System.out.println("Invalid option.");
                }
            }

        } catch (SQLException e) {
            System.out.println("Database connection error: " + e.getMessage());
        }
    }
}

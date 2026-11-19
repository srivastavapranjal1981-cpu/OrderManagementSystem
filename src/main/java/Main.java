
import java.sql.*;
import java.util.Scanner;

public class Main {

    static final String URL = "jdbc:mysql://localhost:3306/ecommerce_db";
    static final String USER = "root";
    static final String PASS = "root";

    static Scanner sc = new Scanner(System.in);

    public static void main(String[] args) {

        try (Connection con = DriverManager.getConnection(URL, USER, PASS)) {

            System.out.println("Database Connected Successfully!");

            int ch;

            do {
                System.out.println("\n===== ORDER MANAGEMENT SYSTEM =====");
                System.out.println("1. Insert 3 Orders");
                System.out.println("2. Search Orders by Status");
                System.out.println("3. Increase Order Quantity");
                System.out.println("4. Update Order Status");
                System.out.println("5. Calculate Total Order Value");
                System.out.println("6. Delete Cancelled Order");
                System.out.println("7. Exit");

                System.out.print("Enter your choice: ");
                ch = sc.nextInt();
                sc.nextLine();

                switch (ch) {

                    case 1:
                        insertOrders(con);
                        break;

                    case 2:
                        searchOrders(con);
                        break;

                    case 3:
                        increaseQuantity(con);
                        break;

                    case 4:
                        updateStatus(con);
                        break;

                    case 5:
                        totalValue(con);
                        break;

                    case 6:
                        deleteOrder(con);
                        break;

                    case 7:
                        System.out.println("Thank you!");
                        break;

                    default:
                        System.out.println("Invalid Choice!");
                }

            } while (ch != 7);

        } catch (SQLException e) {
            System.out.println("Database Error: " + e.getMessage());
        }
    }

    // Q1(a) Insert at least 3 orders
    static void insertOrders(Connection con) {

        String sql = "INSERT INTO Orders VALUES(?,?,?,?,?,?)";

        try (PreparedStatement ps = con.prepareStatement(sql)) {

            for (int i = 1; i <= 3; i++) {

                System.out.println("\nEnter Order " + i + " Details:");

                System.out.print("Order ID: ");
                int id = sc.nextInt();
                sc.nextLine();

                System.out.print("Customer Name: ");
                String name = sc.nextLine();

                System.out.print("Product Name: ");
                String product = sc.nextLine();

                System.out.print("Quantity: ");
                int qty = sc.nextInt();

                System.out.print("Unit Price: ");
                double price = sc.nextDouble();
                sc.nextLine();

                System.out.print("Status: ");
                String status = sc.nextLine();

                ps.setInt(1, id);
                ps.setString(2, name);
                ps.setString(3, product);
                ps.setInt(4, qty);
                ps.setDouble(5, price);
                ps.setString(6, status);

                try {
                    ps.executeUpdate();
                    System.out.println("Order inserted successfully!");
                } catch (SQLIntegrityConstraintViolationException e) {
                    System.out.println("Duplicate Order ID! Try another ID.");
                }
            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    // Q1(b) Search orders by status and quantity > 2

    static void searchOrders(Connection con) {

        System.out.print("Enter Order Status: ");
        String status = sc.nextLine();

        System.out.print("Enter Minimum Quantity: ");
        int min = sc.nextInt();
        sc.nextLine();

        String sql = "SELECT * FROM Orders WHERE status=? AND quantity>? ORDER BY unit_price DESC";

        try (PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, status);
            ps.setInt(2, min);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                System.out.println(
                        rs.getInt("order_id") + " " +
                                rs.getString("customer_name") + " " +
                                rs.getInt("quantity") + " " +
                                rs.getDouble("unit_price")
                );
            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }


    // Q1(c) Increase order quantity
    static void increaseQuantity(Connection con) {

        System.out.print("Enter Order ID: ");
        int id = sc.nextInt();

        System.out.print("Enter Additional Quantity: ");
        int qty = sc.nextInt();
        sc.nextLine();

        if (qty <= 0) {
            System.out.println("Quantity must be positive.");
            return;
        }

        String sql = "UPDATE Orders SET quantity=quantity+? WHERE order_id=?";

        try (PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, qty);
            ps.setInt(2, id);

            int rows = ps.executeUpdate();

            if (rows > 0)
                System.out.println("Quantity updated successfully!");
            else
                System.out.println("Order does not exist.");

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    // Q1(d) Update order status
    static void updateStatus(Connection con) {

        System.out.print("Enter Order ID: ");
        int id = sc.nextInt();
        sc.nextLine();

        System.out.print("Enter New Status: ");
        String status = sc.nextLine();

        if (!(status.equalsIgnoreCase("Pending") ||
                status.equalsIgnoreCase("Shipped") ||
                status.equalsIgnoreCase("Delivered"))) {

            System.out.println("Invalid Status!");
            return;
        }

        String sql = "UPDATE Orders SET status=? WHERE order_id=?";

        try (PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, status);
            ps.setInt(2, id);

            int rows = ps.executeUpdate();

            if (rows > 0) {

                System.out.println("Status updated successfully!");

                String query = "SELECT * FROM Orders WHERE order_id=?";

                try (PreparedStatement p = con.prepareStatement(query)) {
                    p.setInt(1, id);

                    ResultSet rs = p.executeQuery();

                    if (rs.next()) {
                        System.out.println(
                                "ID: " + rs.getInt("order_id") +
                                        " | Customer: " + rs.getString("customer_name") +
                                        " | Product: " + rs.getString("product_name") +
                                        " | Quantity: " + rs.getInt("quantity") +
                                        " | Price: " + rs.getDouble("unit_price") +
                                        " | Status: " + rs.getString("status")
                        );
                    }
                }

            } else {
                System.out.println("Order does not exist.");
            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    // Q1(e) Calculate total order value
    static void totalValue(Connection con) {

        String sql = "SELECT SUM(quantity * unit_price) AS total FROM Orders";

        try (Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            if (rs.next()) {
                System.out.println("Total Order Value: " +
                        rs.getDouble("total"));
            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    // Q1(e) Delete only Cancelled orders
    static void deleteOrder(Connection con) {

        System.out.print("Enter Order ID to Delete: ");
        int id = sc.nextInt();
        sc.nextLine();

        String sql = "DELETE FROM Orders WHERE order_id=? AND status='Cancelled'";

        try (PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);

            int rows = ps.executeUpdate();

            if (rows > 0) {
                System.out.println("Cancelled order deleted successfully!");
            } else {

                String check = "SELECT status FROM Orders WHERE order_id=?";

                try (PreparedStatement p = con.prepareStatement(check)) {
                    p.setInt(1, id);

                    ResultSet rs = p.executeQuery();

                    if (rs.next()) {
                        System.out.println("Cannot delete! Order is not Cancelled.");
                    } else {
                        System.out.println("Order does not exist.");
                    }
                }
            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
}

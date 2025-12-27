package dao;

import database.DatabaseConnection;
import model.Product;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ProductDAO
{

    /**
     * Reduces the stock of a specific product after a sale.
     * This is critical for the 'Complete Order' process.
     * * @param productId The ID of the product to update.
     * @param quantitySold The amount to subtract from stock.
     * @return true if update is successful.
     */
    public boolean reduceStock(int productId, double quantitySold)
    {
        // SQL query to decrease the stock value
        String sql = "UPDATE productinfo SET stock = stock - ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql))
        {

            if (conn == null) return false;

            // Set parameters
            stmt.setDouble(1, quantitySold);
            stmt.setInt(2, productId);

            // Execute UPDATE
            int rowsUpdated = stmt.executeUpdate();
            return rowsUpdated > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Fetches all products from the database.
     * Useful for refreshing the table after an order.
     * * @return A List of Product objects.
     */
    public List<Product> getAllProducts()
    {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT * FROM productinfo";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery())
        {

            while (rs.next())
            {
                Product p = new Product(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("type"), // enum as string
                        rs.getDouble("price"),
                        rs.getDouble("stock"),
                        rs.getString("imagelocation"),
                        rs.getDouble("threshold")
                );
                products.add(p);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return products;
    }

    /**
     * Adds a new product to the database.
     * This will be used in the Owner (Patron) screen.
     * * @param product The product object to add.
     * @return true if added successfully.
     */
    public boolean addProduct(Product product)
    {
        String sql = "INSERT INTO productinfo (name, type, price, stock, imagelocation, threshold) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql))
        {

            stmt.setString(1, product.getName());
            stmt.setString(2, product.getType());
            stmt.setDouble(3, product.getPrice());
            stmt.setDouble(4, product.getStock());
            stmt.setString(5, product.getImageLocation());
            stmt.setDouble(6, product.getThreshold());

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
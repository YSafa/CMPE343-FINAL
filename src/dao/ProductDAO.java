package dao;

import database.DatabaseConnection;
import model.Product;
import util.Alertutil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for products.
 * It manages product data in the database.
 */
public class ProductDAO {

    /**
     * Reduces product stock after a sale.
     *
     * @param productId product ID
     * @param quantitySold sold quantity
     * @return true if update is successful
     */
    public boolean reduceStock(int productId, double quantitySold) {
        String sql = "UPDATE productinfo SET stock = stock - ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            if (conn == null) return false;

            stmt.setDouble(1, quantitySold);
            stmt.setInt(2, productId);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            Alertutil.showErrorMessage("Database error while reducing stock:\n" + e.getMessage());
            return false;
        }
    }

    /**
     * Gets all products from the database.
     *
     * @return list of products
     */
    public List<Product> getAllProducts() {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT * FROM productinfo";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Product p = new Product(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("type"),
                        rs.getDouble("price"),
                        rs.getDouble("stock"),
                        rs.getBytes("image"),
                        rs.getDouble("threshold")
                );
                products.add(p);
            }

        } catch (SQLException e) {
            Alertutil.showErrorMessage("Error while fetching products:\n" + e.getMessage());
        }

        return products;
    }

    /**
     * Adds a new product to the database.
     *
     * @param product product to add
     * @return true if product is added
     */
    public boolean addProduct(Product product) {
        String sql = "INSERT INTO productinfo (name, type, price, stock, image, threshold) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, product.getName());
            stmt.setString(2, product.getType());
            stmt.setDouble(3, product.getPrice());
            stmt.setDouble(4, product.getStock());
            stmt.setBytes(5, product.getImage());
            stmt.setDouble(6, product.getThreshold());

            stmt.executeUpdate();
            return true;

        } catch (SQLIntegrityConstraintViolationException e) {
            // Duplicate product name error
            Alertutil.showErrorMessage("A product with this name already exists!");
            return false;

        } catch (SQLException e) {
            Alertutil.showErrorMessage("Database error while adding product:\n" + e.getMessage());
            return false;
        }
    }

    /**
     * Updates an existing product.
     *
     * @param product product to update
     * @return true if update is successful
     */

    public boolean updateProduct(Product product) {

        boolean hasImage = product.getImage() != null;

        String sqlWithImage =
                "UPDATE productinfo SET name=?, type=?, price=?, stock=?, image=?, threshold=? WHERE id=?";

        String sqlWithoutImage =
                "UPDATE productinfo SET name=?, type=?, price=?, stock=?, threshold=? WHERE id=?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     hasImage ? sqlWithImage : sqlWithoutImage
             )) {

            stmt.setString(1, product.getName());
            stmt.setString(2, product.getType());
            stmt.setDouble(3, product.getPrice());
            stmt.setDouble(4, product.getStock());

            if (hasImage) {
                stmt.setBytes(5, product.getImage());
                stmt.setDouble(6, product.getThreshold());
                stmt.setInt(7, product.getId());
            } else {
                stmt.setDouble(5, product.getThreshold());
                stmt.setInt(6, product.getId());
            }

            stmt.executeUpdate();
            return true;

        } catch (SQLIntegrityConstraintViolationException e) {
            Alertutil.showErrorMessage("Cannot update — a product with this name already exists!");
            return false;

        } catch (SQLException e) {
            Alertutil.showErrorMessage("Database error while updating product:\n" + e.getMessage());
            return false;
        }
    }


    /**
     * Deletes a product by ID.
     *
     * @param id product ID
     * @return true if delete is successful
     */
    public boolean deleteProduct(int id) {
        String sql = "DELETE FROM productinfo WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            stmt.executeUpdate();
            return true;

        } catch (SQLException e) {
            Alertutil.showErrorMessage("Database error while deleting product:\n" + e.getMessage());
            return false;
        }
    }
}

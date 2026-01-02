package model;

/**
 * Represents one item in the shopping cart.
 * It stores a product and its quantity.
 */
public class CartItem
{
    private Product product;
    private double quantity;


    // Constructor
    /**
     * Creates a cart item.
     *
     * @param product product in the cart
     * @param quantity product quantity
     */
    public CartItem(Product product, double quantity)
    {
        this.product = product;
        this.quantity = quantity;
    }

    // Getters and Setters
    /**
     * Gets the product.
     *
     * @return product
     */
    public Product getProduct() {
        return product;
    }

    /**
     * Gets the quantity.
     *
     * @return quantity
     */
    public double getQuantity() {
        return quantity;
    }

    /**
     * Sets the quantity.
     *
     * @param quantity new quantity
     */
    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }

    /**
     * Calculates total price of this item.
     * (price * quantity)
     *
     * @return total item price
     */
    public double getTotalItemPrice() {
        return product.getPrice() * quantity;
    }
}
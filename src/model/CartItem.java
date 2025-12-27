package model;

public class CartItem
{
    private Product product;
    private double quantity;

    // Constructor
    public CartItem(Product product, double quantity)
    {
        this.product = product;
        this.quantity = quantity;
    }

    // Getters and Setters
    public Product getProduct() {return product;}

    public double getQuantity() { return quantity;}

    public void setQuantity(double quantity) {this.quantity = quantity;}

    // Calculates the total price for this item (Price * Quantity)
    public double getTotalItemPrice() {return product.getPrice() * quantity;}
}
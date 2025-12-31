package model;

import java.util.ArrayList;
import java.util.List;

public class Cart
{
    // Using ArrayList instead of HashMap as per course material
    private List<CartItem> items;

    public Cart()
    {
        this.items = new ArrayList<>();
    }

    /**
     * Adds a product to the cart.
     * If the product already exists, updates its quantity.
     * @param product The product to add.
     * @param quantity The amount to add.
     */
    public void addItem(Product product, double quantity)
    {
        boolean isFound = false;

        // Iterate through the list to check if product exists
        for (CartItem item : items)
        {
            // Check by ID to be safe
            if (item.getProduct().getId() == product.getId())
            {
                // Product found, update the quantity
                double newQuantity = item.getQuantity() + quantity;
                item.setQuantity(newQuantity);
                isFound = true;
                break;
            }
        }

        // If product was not found in the loop, add it as a new item
        if (!isFound)
        {
            items.add(new CartItem(product, quantity));
        }
    }
    private static final double VAT_RATE = 0.18;

    /**
     * Calculates total price INCLUDING VAT.
     * @return total price with VAT
     */
    public double getTotalPriceWithVAT() {
        return getTotalPrice() * (1 + VAT_RATE);
    }

    /**
     * Removes a product from the cart completely.
     */
    public void removeItem(Product product)
    {
        // Remove based on ID matching
        items.removeIf(item -> item.getProduct().getId() == product.getId());
    }

    /**
     * Calculates the total price of all items in the cart.
     * @return Total cost.
     */
    public double getTotalPrice()
    {
        double total = 0.0;
        for (CartItem item : items)
        {
            total += item.getTotalItemPrice();
        }
        return total;
    }

    /**
     * Clears all items from the cart.
     */
    public void clear()
    {
        items.clear();
    }

    /**
     * Returns the list of items (useful for TableView display).
     */
    public List<CartItem> getItems()
    {
        return items;
    }

    /**
     * Converts cart contents to a String format for database storage.
     * Format: "Apple x 2.0; Banana x 1.5;"
     */
    public String getCartContentAsString()
    {
        StringBuilder sb = new StringBuilder();
        for (CartItem item : items)
        {
            sb.append(item.getProduct().getName())
                    .append(" x ")
                    .append(item.getQuantity())
                    .append("; ");
        }
        return sb.toString();
    }
}
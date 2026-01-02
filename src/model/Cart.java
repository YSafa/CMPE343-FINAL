package model;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a shopping cart that stores CartItems.
 * It supports adding/removing items, calculating totals, and storing applied coupons.
 */
public class Cart {

    private List<CartItem> items;
    private Coupon appliedCoupon; // Kupon bilgisi

    /** VAT rate (18%). */
    private static final double VAT_RATE = 0.18;

    /**
     * Creates an empty cart.
     */
    public Cart() {
        this.items = new ArrayList<>();
    }

    /**
     * Adds a product to the cart.
     * If product exists, quantity is increased.
     *
     * @param product product to add
     * @param quantity product quantity
     */
    public void addItem(Product product, double quantity) {
        boolean isFound = false;
        for (CartItem item : items) {
            if (item.getProduct().getId() == product.getId()) {
                item.setQuantity(item.getQuantity() + quantity);
                isFound = true;
                break;
            }
        }
        if (!isFound) {
            items.add(new CartItem(product, quantity));
        }
    }

    /**
     * Removes a product from the cart.
     *
     * @param product product to remove
     */
    public void removeItem(Product product) {
        items.removeIf(item -> item.getProduct().getId() == product.getId());
    }

    /** Clears all items from the cart. */
    public void clear() {
        items.clear();
        appliedCoupon = null; // 🧾 Kuponu da sıfırla
    }

    /**
     * Gets all cart items.
     *
     * @return list of cart items
     */
    public List<CartItem> getItems() {
        return items;
    }

    /** Calculates total price (without VAT). */
    public double getTotalPrice() {
        double total = 0.0;
        for (CartItem item : items) {
            total += item.getTotalItemPrice();
        }
        return total;
    }

    /**
     * Calculates total price without VAT.
     *
     * @return total price
     */
    public double getTotalPriceWithVAT() {
        return getTotalPrice() * (1 + VAT_RATE);
    }

    /**
     * Converts cart items to a single string.
     * This is used for saving to database.
     *
     * @return cart content as string
     */
    public String getCartContentAsString() {
        StringBuilder sb = new StringBuilder();
        for (CartItem item : items) {
            sb.append(item.getProduct().getName())
                    .append(" x ")
                    .append(item.getQuantity())
                    .append("; ");
        }
        return sb.toString();
    }

    /**
     * Gets the applied coupon.
     *
     * @return applied coupon
     */
    public Coupon getAppliedCoupon() {
        return appliedCoupon;
    }

    /**
     * Sets a coupon for the cart.
     *
     * @param coupon coupon to apply
     */
    public void setAppliedCoupon(Coupon coupon) {
        this.appliedCoupon = coupon;
    }
}

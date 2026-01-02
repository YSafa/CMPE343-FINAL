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

    private static final double VAT_RATE = 0.18;

    public Cart() {
        this.items = new ArrayList<>();
    }

    /** Adds a product to the cart or increases quantity if it already exists. */
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

    /** Removes a product completely from the cart. */
    public void removeItem(Product product) {
        items.removeIf(item -> item.getProduct().getId() == product.getId());
    }

    /** Clears all items from the cart. */
    public void clear() {
        items.clear();
        appliedCoupon = null; // 🧾 Kuponu da sıfırla
    }

    /** Returns all cart items. */
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

    /** Calculates total price including VAT. */
    public double getTotalPriceWithVAT() {
        return getTotalPrice() * (1 + VAT_RATE);
    }

    /** Converts cart contents into a string for saving in database. */
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

    // Kupon get/set metotları
    public Coupon getAppliedCoupon() {
        return appliedCoupon;
    }

    public void setAppliedCoupon(Coupon coupon) {
        this.appliedCoupon = coupon;
    }
}

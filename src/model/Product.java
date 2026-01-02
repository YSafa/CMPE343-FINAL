package model;

/**
 * Represents a product in the system.
 * It stores product details like price, stock, and type.
 */
public class Product
{
    private int id;
    private String name;
    private String type;
    private double price;
    private double stock;
    private byte[] image;
    private double threshold;

    private byte[] imageBytes;


    /**
     * Creates a product object.
     *
     * @param id product ID
     * @param name product name
     * @param type product type (fruit or vegetable)
     * @param price base price
     * @param stock current stock amount
     * @param image product image
     * @param threshold low stock threshold
     */
    public Product(int id, String name, String type, double price, double stock, byte[] image, double threshold)
    {
        this.id = id;
        this.name = name;
        this.type = type;
        this.price = price;
        this.stock = stock;
        this.image = image;
        this.threshold = threshold;
    }

    /** @return product ID */
    public int getId() {
        return id;
    }

    /** @return product name */
    public String getName() {
        return name;
    }

    /** @param name new product name */
    public void setName(String name) {
        this.name = name;
    }

    /** @return product type */
    public String getType() {
        return type;
    }

    /** @param type product type */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * Gets the product price.
     * If stock is below threshold, price is doubled.
     *
     * @return product price
     */
    public double getPrice() {
        if (this.stock <= this.threshold) {
            return this.price * 2;
        }
        return this.price;
    }

    /** @param price base price */
    public void setPrice(double price) {
        this.price = price;
    }

    /** @return stock amount */
    public double getStock() {
        return stock;
    }

    /** @param stock new stock amount */
    public void setStock(double stock) {
        this.stock = stock;
    }

    /** @return product image */
    public byte[] getImage() {
        return image;
    }

    /** @param image product image */
    public void setImage(byte[] image) {
        this.image = image;
    }

    /** @return stock threshold */
    public double getThreshold() {
        return threshold;
    }

    /** @param threshold new threshold value */
    public void setThreshold(double threshold) {
        this.threshold = threshold;
    }
}
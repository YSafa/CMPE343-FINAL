package model;

public class Product
{
    private int id;
    private String name;
    private String type; // enum('fruit','vegetable')
    private double price;
    private double stock; // Defined as double in DB
    private String imageLocation;
    private double threshold;

    // Constructor matching database columns
    public Product(int id, String name, String type, double price, double stock, String imageLocation, double threshold)
    {
        this.id = id;
        this.name = name;
        this.type = type;
        this.price = price;
        this.stock = stock;
        this.imageLocation = imageLocation;
        this.threshold = threshold;
    }

    // Getters
    public int getId() { return id; }
    public String getName() { return name; }
    public String getType() { return type; }
    public double getPrice() { return price; }
    public double getStock() { return stock; }
    public String getImageLocation() { return imageLocation; }
    public double getThreshold() { return threshold; }

    // Setter for stock (needed when updating inventory)
    public void setStock(double stock) { this.stock = stock; }
}
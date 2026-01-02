package model;

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

    public int getId() { return id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public byte[] getImageBytes() { return imageBytes; }
    public void setImageBytes(byte[] imageBytes) { this.imageBytes = imageBytes; }

    public double getPrice() {
        if (this.stock <= this.threshold) {
            return this.price * 2;
        }
        return this.price;
    }
    public void setPrice(double price) { this.price = price; }

    public double getStock() { return stock; }
    public void setStock(double stock) { this.stock = stock; }

    public byte[] getImage() { return image; }
    public void setImage(byte[] image) { this.image = image; }

    public double getThreshold() { return threshold; }
    public void setThreshold(double threshold) { this.threshold = threshold; }
}
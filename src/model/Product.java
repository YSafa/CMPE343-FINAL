package model;

public class Product
{
    private int id;
    private String name;
    private String type; // enum('fruit','vegetable')
    private double price;
    private double stock; // Defined as double in DB
    private byte[] image; // image stored as binary data (BLOB)
    private double threshold;

    // Constructor matching database columns
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

    // Getters
    public int getId() { return id; }
    public String getName() { return name; }
    public String getType() { return type; }
    // Product.java sınıfının içinde
    public double getPrice()
    {
        // Proje Kuralı: Stok, eşik değerine (threshold) eşit veya altındaysa fiyat 2 katına çıkar.
        if (this.stock <= this.threshold) {
            return this.price * 2;
        }
        // Stok yeterliyse normal fiyatı döndür.
        return this.price;
    }
    public double getStock() { return stock; }
    public byte[] getImage() { return image; }
    public double getThreshold() { return threshold; }

    // Setter for stock (needed when updating inventory)
    public void setStock(double stock) { this.stock = stock; }
    public void setImage(byte[] image) { this.image = image; }
}
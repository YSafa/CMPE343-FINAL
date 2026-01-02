package model;

public class User
{
    private int id;
    private String username;
    private String password;
    private String role; // enum('customer','carrier','owner')
    private String address; // New field from your table

    // Updated Constructor
    public User(int id, String username, String password, String role, String address)
    {
        this.id = id;
        this.username = username;
        this.password = password;
        this.role = role;
        this.address = address;
    }
    private double averageRating;
    private int ratingCount;

    public double getAverageRating() {
        return averageRating;
    }

    public void setAverageRating(double averageRating) {
        this.averageRating = averageRating;
    }

    public int getRatingCount() {
        return ratingCount;
    }

    public void setRatingCount(int ratingCount) {
        this.ratingCount = ratingCount;
    }

    // Getters
    public int getId() { return id; }
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public String getRole() { return role; }
    public String getAddress() { return address; }

    // Setter for address (if user wants to update it later)
    public void setAddress(String address) { this.address = address; }
}
package model;

/**
 * Represents a user in the system.
 * A user can be a customer, carrier, or owner.
 */
public class User
{
    private int id;
    private String username;
    private String password;
    private String role; // enum('customer','carrier','owner')
    private String address; // New field from your table

    // Updated Constructor
    /**
     * Creates a user object.
     *
     * @param id user ID
     * @param username username
     * @param password password
     * @param role user role
     * @param address user address
     */
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

    /** @param averageRating average rating value */
    public void setAverageRating(double averageRating) {
        this.averageRating = averageRating;
    }

    /** @return rating count */
    public int getRatingCount() {
        return ratingCount;
    }

    /** @param ratingCount number of ratings */
    public void setRatingCount(int ratingCount) {
        this.ratingCount = ratingCount;
    }

    // Getters
    /** @return user ID */
    public int getId() {
        return id;
    }

    /** @return username */
    public String getUsername() {
        return username;
    }

    /** @return password */
    public String getPassword() {
        return password;
    }

    /** @return user role */
    public String getRole() {
        return role;
    }

    /** @return user address */
    public String getAddress() {
        return address;
    }

    // Setter for address (if user wants to update it later)
    /**
     * Updates the user address.
     *
     * @param address new address
     */
    public void setAddress(String address) { this.address = address; }
}
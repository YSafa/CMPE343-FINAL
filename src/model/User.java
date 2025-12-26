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

    // Getters
    public int getId() { return id; }
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public String getRole() { return role; }
    public String getAddress() { return address; }

    // Setter for address (if user wants to update it later)
    public void setAddress(String address) { this.address = address; }
}
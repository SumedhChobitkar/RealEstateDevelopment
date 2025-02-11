package com.RealEstateDevelopment.Entity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "REVIEWS")
@Data
@AllArgsConstructor
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;
    private int cleanlinessRating;
    private int locationRating;
    private int valueForMoneyRating;
    private String comment;

    public Review() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public int getCleanlinessRating() { return cleanlinessRating; }
    public void setCleanlinessRating(int cleanlinessRating) { this.cleanlinessRating = cleanlinessRating; }

    public int getLocationRating() { return locationRating; }
    public void setLocationRating(int locationRating) { this.locationRating = locationRating; }

    public int getValueForMoneyRating() { return valueForMoneyRating; }
    public void setValueForMoneyRating(int valueForMoneyRating) { this.valueForMoneyRating = valueForMoneyRating; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
}

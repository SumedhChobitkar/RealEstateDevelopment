package com.RealEstateDevelopment.Entity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "CONTACTS")
@Data
@AllArgsConstructor
public class Contact {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String mobileNo;
    private String email;
    private String description;

    public Contact() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getMobileNo() {return mobileNo;}

    public void setMobileNo(String mobileNo) {this.mobileNo = mobileNo;}

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}

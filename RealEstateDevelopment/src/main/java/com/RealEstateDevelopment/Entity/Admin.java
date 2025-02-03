package com.RealEstateDevelopment.Entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({"agent", "admin", "properties", "temporaryProperties"})
public class Admin {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long adminId;

    private String username;

    private String fullname;

    @Column(unique = true, nullable = false)
    private String email;

    private String password;

    @Column(unique = true, nullable = false)
    private String mobileNo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = true) // Optional: Role can be null
    private Role role; // Enum for role assignment
    @Enumerated(EnumType.STRING)
    private Status status;

    @Lob
    @Column(name = "profile_picture", columnDefinition = "LONGBLOB")
    @Basic(fetch = FetchType.LAZY)
    private byte[] profilePicture;

    @Column(nullable = false, updatable = false)
    private Timestamp createdAt;

    @Column(nullable = false)
    private Timestamp updatedAt;

    @PrePersist
    protected void onCreate() {
        Timestamp now = Timestamp.from(Instant.now());
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Timestamp.from(Instant.now());
    }

//    @OneToMany(mappedBy = "admin") // One admin can manage many properties
//    @JsonIgnore
//    private List<TemporaryProperty> temporaryProperties;
//
//    @OneToMany(mappedBy = "admin")
//    @JsonIgnore
//    private List<Property> properties;

//    @OneToMany(mappedBy = "admin")
//    @JsonIgnore // Prevent serialization of properties field in Admin
//    private List<TemporaryProperty> temporaryProperties;
//
//    @OneToMany(mappedBy = "admin")
//    @JsonIgnore // Prevent serialization of properties field in Admin
//    private List<Property> properties;
}

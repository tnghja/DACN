package com.ecommerce.user.model.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.DynamicInsert;

import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@DynamicInsert
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String userId;

    @Column(nullable = false)
    private String userName;
    @Column(nullable = false, unique = true)
    private String email;

   private String password;

    private String avtUrl;

    private String publicAvtId;

    private String fullName;
    private String gender;
    @Column(nullable = true, unique = true)
    private String phoneNumber;

    private LocalDate dateOfBirth;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Address> addresses;
}

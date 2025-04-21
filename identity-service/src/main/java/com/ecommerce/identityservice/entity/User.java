package com.ecommerce.identityservice.entity;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import jakarta.persistence.*;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "app_user")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String userId;

//    @Column(nullable = false)
    private String userName;
    @Column(nullable = false, unique = true, name = "user_email",length = 255)
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

    @ManyToMany
    Set<Role> roles;
}
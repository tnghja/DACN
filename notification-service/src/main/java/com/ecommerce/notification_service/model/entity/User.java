package com.ecommerce.notification_service.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "users")
public class User {
    @Id
    @Size(max = 255)
    @Column(name = "user_id", nullable = false)
    private String userId;

    @Size(max = 255)
    @Column(name = "avt_url")
    private String avtUrl;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Size(max = 255)
    @NotNull
    @Column(name = "email", nullable = false)
    private String email;

    @Size(max = 255)
    @Column(name = "full_name")
    private String fullName;

    @Size(max = 255)
    @Column(name = "gender")
    private String gender;

    @Size(max = 255)
    @Column(name = "password")
    private String password;

    @Size(max = 255)
    @Column(name = "phone_number")
    private String phoneNumber;

    @Size(max = 255)
    @Column(name = "public_avt_id")
    private String publicAvtId;

    @Size(max = 255)
    @NotNull
    @Column(name = "user_name", nullable = false)
    private String userName;

}
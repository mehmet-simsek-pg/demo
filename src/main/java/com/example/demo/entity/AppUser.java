package com.example.demo.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

@Entity
@Table(name = "users")
public class AppUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @Column(unique = true, nullable = false)
    @NotBlank(message = "username must not be blank")
    private String username;

    @Column(nullable = false)
    @NotBlank(message = "password must not be blank")
    private String password;// Demo: plain text

    private String fullName;

    public AppUser() {
    }

    public AppUser(Long id, String username, String password, String fullName) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.fullName = fullName;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }
}

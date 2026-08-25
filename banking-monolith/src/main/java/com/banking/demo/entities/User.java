package com.banking.demo.entities;

import com.banking.demo.enums.Role;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name="users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String username;
    private String password;
    private String email;
    @Enumerated(EnumType.STRING)
    private Role role;
    private Boolean enabled = true;
    private LocalDateTime createdAt = LocalDateTime.now();
    @OneToOne
    @JoinColumn(name = "customer_id")
    private Customer  customer;
}

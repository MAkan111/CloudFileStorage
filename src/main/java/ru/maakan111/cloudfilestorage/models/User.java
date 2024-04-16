package ru.maakan111.cloudfilestorage.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    @Column(name = "username", unique = true)
    @Size(min = 2, max = 50, message = "Username length must be in between 2 and 50 symbols")
    private String username;
    @Column(name = "password")
    @Size(min = 5, max = 100, message = "Password length must be in between 5 and 100 symbols")
    private String password;

    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }
}

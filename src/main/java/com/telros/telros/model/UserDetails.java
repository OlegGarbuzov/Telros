package com.telros.telros.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Модель для хранения детальной информации о пользователе
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "user_details")
public class UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 50)
    @Column(name = "last_name")
    private String lastName; // Фамилия

    @NotBlank
    @Size(max = 50)
    @Column(name = "first_name")
    private String firstName; // Имя

    @Size(max = 50)
    @Column(name = "middle_name")
    private String middleName; // Отчество

    @Column(name = "birth_date")
    private LocalDate birthDate; // Дата рождения

    @Size(max = 20)
    @Column(name = "phone_number")
    private String phoneNumber; // Номер телефона

    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;

    @OneToOne(mappedBy = "userDetails", cascade = CascadeType.ALL, orphanRemoval = true)
    private UserPhoto userPhoto;
}
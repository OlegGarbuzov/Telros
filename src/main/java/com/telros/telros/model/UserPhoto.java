package com.telros.telros.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

/**
 * Модель для хранения фотографии пользователя
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "user_photos")
public class UserPhoto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Lob
    @Column(name = "data")
    private byte[] data;

    @Column(name = "file_name")
    private String fileName;

    @Column(name = "file_size", nullable = false)
    private Long fileSize;

    @Column(name = "file_type")
    private String fileType;

    @Column(name = "upload_date", nullable = false)
    private Timestamp uploadDate = new Timestamp(System.currentTimeMillis());

    @OneToOne
    @JoinColumn(name = "user_details_id")
    private UserDetails userDetails;

    public UserPhoto(String fileName, String fileType, byte[] data, Long fileSize, UserDetails userDetails) {
        this.fileName = fileName;
        this.fileType = fileType;
        this.data = data;
        this.fileSize = fileSize;
        this.userDetails = userDetails;
    }
}
package com.dataportal.datastorage.entity;

import lombok.Data;

import javax.persistence.*;
import java.time.Instant;

@Data
@Entity
@Table(name = "tags")
public class Tag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String uid;

    @Column
    private String title;

    @Column
    private Instant createdAt;

    @Column
    private Instant lastModified;
}

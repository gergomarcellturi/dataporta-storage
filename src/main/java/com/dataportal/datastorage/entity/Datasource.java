package com.dataportal.datastorage.entity;

import lombok.Data;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "datasource")
@Data
public class Datasource {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String uid;

    @OneToOne
    @JoinColumn(name = "metadata_uid")
    private Metadata metadata;

    @Lob
    @Type(type = "org.hibernate.type.BinaryType")
    private byte[] data;

    @Column
    private Instant createdAt;

    @Column
    private Instant lastModified;


}

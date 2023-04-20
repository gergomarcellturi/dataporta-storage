package com.dataportal.datastorage.entity;


import com.dataportal.datastorage.model.DataAccess;
import com.dataportal.datastorage.model.DataDownloadAccess;
import lombok.Data;
import com.dataportal.datastorage.model.DatasourceStatus;
import com.dataportal.datastorage.model.FileType;

import javax.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "metadata")
@Data
public class Metadata {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String uid;

    @Column
    private String userUid;

    @Column
    private String filename;

    @Column
    private Integer size;

    @Column
    @Enumerated(EnumType.STRING)
    private FileType type;

    @Column
    private Instant createdAt;

    @Column
    private Instant lastModified;

    @Column
    @Enumerated(EnumType.STRING)
    private DatasourceStatus status;

    @Column
    @Enumerated(EnumType.STRING)
    private DataAccess dataAccess;

    @Column
    @Enumerated(EnumType.STRING)
    private DataDownloadAccess dataDownloadAccess;

    @Column
    private Instant datePublished;

    @Column
    private Instant dateDeleted;
}

package com.dataportal.datastorage.entity;

import lombok.Data;

import javax.persistence.*;
import java.time.Instant;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "datasource_details")
@Data
public class DatasourceDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String uid;

    @Column
    private String title;

    @Column
    private String summary;

    @Column
    private String description;

    @OneToOne()
    @JoinColumn(name = "metadata_uid")
    private Metadata metadata;

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(name = "datasource_details_tags",
            joinColumns = @JoinColumn(name = "datasource_details_uid"),
            inverseJoinColumns = @JoinColumn(name = "tag_uid"))
    private List<Tag> tags;

    @Column
    private Instant lastModified;

    @Column
    private Instant createdAt;

}

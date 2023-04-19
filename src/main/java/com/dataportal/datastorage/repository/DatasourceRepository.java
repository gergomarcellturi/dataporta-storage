package com.dataportal.datastorage.repository;

import com.dataportal.datastorage.entity.Datasource;
import com.dataportal.datastorage.entity.Metadata;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DatasourceRepository extends JpaRepository<Datasource, String> {

    Optional<Datasource> findByMetadata(Metadata metadata);
}

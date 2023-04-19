package com.dataportal.datastorage.repository;

import com.dataportal.datastorage.entity.DatasourceDetails;
import com.dataportal.datastorage.entity.Metadata;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DatasourceDetailsRepository extends JpaRepository<DatasourceDetails, String> {

    Optional<DatasourceDetails> findByMetadata(Metadata metadata);
}

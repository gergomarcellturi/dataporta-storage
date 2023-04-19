package com.dataportal.datastorage.repository;

import com.dataportal.datastorage.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TagsRepository extends JpaRepository<Tag, String> {

    Optional<Tag> findByTitle(String title);

}

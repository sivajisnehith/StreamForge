package com.Opsfusionn.StreamForge.repository;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.Opsfusionn.StreamForge.model.Video;
import com.Opsfusionn.StreamForge.model.User;

public interface VideoRepository extends JpaRepository<Video, UUID>{
    Page<Video> findByTitleContainingIgnoreCase(
            String title,
            Pageable pageable);

    long countByUser(User user);
}

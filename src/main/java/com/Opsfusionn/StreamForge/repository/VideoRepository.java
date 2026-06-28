package com.Opsfusionn.StreamForge.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.Opsfusionn.StreamForge.model.Video;

public interface VideoRepository extends JpaRepository<Video, UUID>{
    
}

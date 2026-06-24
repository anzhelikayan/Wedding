package com.example.wedding;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GuestResponseRepository extends JpaRepository<GuestResponse, Long> {
    List<GuestResponse> findAllByOrderByCreatedAtDesc();
}

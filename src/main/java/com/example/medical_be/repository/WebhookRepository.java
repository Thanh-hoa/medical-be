package com.example.medical_be.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.medical_be.entity.Webhook;

import java.util.List;

public interface WebhookRepository extends JpaRepository<Webhook, Long> {

    List<Webhook> findAllByIsActiveTrue();
}

package com.example.demo.repository;

import com.example.demo.entity.Snippet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * SnippetRepository - Spring Data JPA repository for Snippet entity persistence.
 *
 * Conforms to Experiment 4 (Spring Data JPA) and Experiment 5 (REST CRUD persistence):
 * - Extends JpaRepository<Snippet, Long> providing out-of-the-box CRUD, pagination, and sorting
 * - Primary Key: Long (maps to auto-increment identity column 'id')
 * - Automatically registered as a Spring bean via @Repository
 */
@Repository
public interface SnippetRepository extends JpaRepository<Snippet, Long> {
}

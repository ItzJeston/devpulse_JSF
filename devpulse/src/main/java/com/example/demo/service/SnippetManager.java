package com.example.demo.service;

import com.example.demo.entity.Snippet;
import com.example.demo.repository.SnippetRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * SnippetManager - Business service layer for Snippet persistence and lifecycle operations.
 *
 * Conforms to Experiment 4 & 5 architecture:
 * - Encapsulates SnippetRepository calls with transactional boundaries
 * - Provides non-null collections for view rendering
 * - Throws NoSuchElementException on missing entities for REST 404 translation
 * - Handles identity generation and field updates via saveSnippet
 */
@Service
@Transactional
public class SnippetManager {

    private final SnippetRepository snippetRepository;

    @Autowired
    public SnippetManager(SnippetRepository snippetRepository) {
        this.snippetRepository = snippetRepository;
    }

    /**
     * Retrieves all code snippets from the database.
     * Guaranteed to return a non-null list (returns empty list if no snippets exist).
     *
     * @return non-null list of all Snippet entities
     */
    @Transactional(readOnly = true)
    public List<Snippet> getAllSnippets() {
        List<Snippet> list = snippetRepository.findAll();
        return list != null ? list : Collections.emptyList();
    }

    /**
     * Retrieves a single snippet by its primary key ID.
     * Throws NoSuchElementException when the entity does not exist or id is null,
     * which ApiController translates to HTTP 404 NOT FOUND.
     *
     * @param id snippet primary key ID
     * @return matching Snippet entity
     * @throws NoSuchElementException if no entity exists with the specified id or id is null
     */
    @Transactional(readOnly = true)
    public Snippet getSnippetById(Long id) {
        if (id == null) {
            throw new NoSuchElementException("Snippet ID must not be null");
        }
        return snippetRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Snippet not found with ID: " + id));
    }

    /**
     * Saves or updates a snippet entity.
     * Assigns the auto-generated identity ID on initial insert.
     *
     * @param snippet the snippet entity to persist
     * @return the persisted snippet entity with assigned ID
     * @throws IllegalArgumentException if snippet is null
     */
    public Snippet saveSnippet(Snippet snippet) {
        if (snippet == null) {
            throw new IllegalArgumentException("Snippet entity must not be null");
        }
        return snippetRepository.save(snippet);
    }

    /**
     * Deletes a snippet by its primary key ID.
     * Safely handles null or non-existent IDs without throwing exceptions.
     *
     * @param id snippet primary key ID
     */
    public void deleteSnippet(Long id) {
        if (id != null && snippetRepository.existsById(id)) {
            snippetRepository.deleteById(id);
        }
    }

    /**
     * Checks if a snippet exists with the given ID.
     * Returns false if ID is null, negative, or not present in the database.
     *
     * @param id snippet primary key ID
     * @return true if snippet exists, false otherwise
     */
    @Transactional(readOnly = true)
    public boolean exists(Long id) {
        if (id == null) {
            return false;
        }
        return snippetRepository.existsById(id);
    }
}

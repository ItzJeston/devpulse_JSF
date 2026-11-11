package com.example.demo.controller;

import com.example.demo.entity.Snippet;
import com.example.demo.service.SnippetManager;
import com.example.demo.util.SanitizationUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;

/**
 * RESTful API Controller (Experiment 5) providing JSON endpoints
 * for CRUD access to code snippets.
 */
@RestController
@RequestMapping("/api/snippets")
public class ApiController {

    private final SnippetManager snippetManager;

    @Autowired
    public ApiController(SnippetManager snippetManager) {
        this.snippetManager = snippetManager;
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Snippet>> getAllSnippets() {
        return ResponseEntity.ok(snippetManager.getAllSnippets());
    }

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Snippet> getSnippetById(@PathVariable("id") Long id) {
        if (id == null || id <= 0) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        try {
            Snippet snippet = snippetManager.getSnippetById(id);
            return ResponseEntity.ok(snippet);
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Snippet> createSnippet(@RequestBody Snippet snippet) {
        if (snippet == null) {
            return ResponseEntity.badRequest().build();
        }
        if (snippet.getStatus() == null || snippet.getStatus().trim().isEmpty()) {
            snippet.setStatus("PENDING");
        }
        if (snippet.getRawCode() != null) {
            snippet.setSanitizedCode(SanitizationUtil.sanitizeHtml(snippet.getRawCode()));
        }
        Snippet saved = snippetManager.saveSnippet(snippet);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Snippet> updateSnippet(@PathVariable("id") Long id, @RequestBody Snippet snippet) {
        if (id == null || id <= 0 || !snippetManager.exists(id)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        try {
            Snippet existing = snippetManager.getSnippetById(id);
            if (snippet.getUsn() != null) existing.setUsn(snippet.getUsn());
            if (snippet.getStudentName() != null) existing.setStudentName(snippet.getStudentName());
            if (snippet.getEmail() != null) existing.setEmail(snippet.getEmail());
            if (snippet.getLabExpNo() > 0) existing.setLabExpNo(snippet.getLabExpNo());
            if (snippet.getLanguage() != null) existing.setLanguage(snippet.getLanguage());
            if (snippet.getTitle() != null) existing.setTitle(snippet.getTitle());
            if (snippet.getRawCode() != null) {
                existing.setRawCode(snippet.getRawCode());
                existing.setSanitizedCode(SanitizationUtil.sanitizeHtml(snippet.getRawCode()));
            }
            if (snippet.getStatus() != null) existing.setStatus(snippet.getStatus());
            existing.setMarks(snippet.getMarks());
            if (snippet.getReviewerComments() != null) existing.setReviewerComments(snippet.getReviewerComments());

            Snippet updated = snippetManager.saveSnippet(existing);
            return ResponseEntity.ok(updated);
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSnippet(@PathVariable("id") Long id) {
        if (id == null || id <= 0 || !snippetManager.exists(id)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        snippetManager.deleteSnippet(id);
        return ResponseEntity.ok().build();
    }
}

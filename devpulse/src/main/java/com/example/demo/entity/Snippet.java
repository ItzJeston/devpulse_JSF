package com.example.demo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;

import java.io.Serializable;
import java.util.Objects;

/**
 * Snippet JPA Entity.
 * Represents a student lab code snippet submission, its sanitized counterpart,
 * and faculty review evaluation.
 * Conforms to Experiments 4 & 5 (Spring Data JPA Persistence Layer).
 */
@Entity
@Table(name = "snippet")
public class Snippet implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "usn", length = 20)
    private String usn;

    @Column(name = "student_name", length = 100)
    private String studentName;

    @Column(name = "email", length = 100)
    private String email;

    @Column(name = "lab_exp_no")
    private int labExpNo;

    @Column(name = "language", length = 30)
    private String language;

    @Column(name = "title", length = 100)
    private String title;

    @Lob
    @Column(name = "raw_code", columnDefinition = "TEXT")
    private String rawCode;

    @Lob
    @Column(name = "sanitized_code", columnDefinition = "TEXT")
    private String sanitizedCode;

    @Column(name = "status", length = 30)
    private String status = "PENDING";

    @Column(name = "reviewer_comments", length = 500)
    private String reviewerComments;

    @Column(name = "marks")
    private int marks = 0;

    /**
     * Default no-arg constructor required by JPA and Jackson.
     */
    public Snippet() {
    }

    /**
     * Full parameterized constructor without id (for new snippet creation).
     */
    public Snippet(String usn, String studentName, String email, int labExpNo,
                   String language, String title, String rawCode, String sanitizedCode,
                   String status, String reviewerComments, int marks) {
        this.usn = usn;
        this.studentName = studentName;
        this.email = email;
        this.labExpNo = labExpNo;
        this.language = language;
        this.title = title;
        this.rawCode = rawCode;
        this.sanitizedCode = sanitizedCode;
        this.status = status;
        this.reviewerComments = reviewerComments;
        this.marks = marks;
    }

    /**
     * Complete parameterized constructor including id.
     */
    public Snippet(Long id, String usn, String studentName, String email, int labExpNo,
                   String language, String title, String rawCode, String sanitizedCode,
                   String status, String reviewerComments, int marks) {
        this(usn, studentName, email, labExpNo, language, title, rawCode, sanitizedCode, status, reviewerComments, marks);
        this.id = id;
    }

    // =========================================================================
    // Getters and Setters
    // =========================================================================

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsn() {
        return usn;
    }

    public void setUsn(String usn) {
        this.usn = usn;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getLabExpNo() {
        return labExpNo;
    }

    public void setLabExpNo(int labExpNo) {
        this.labExpNo = labExpNo;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getRawCode() {
        return rawCode;
    }

    public void setRawCode(String rawCode) {
        this.rawCode = rawCode;
    }

    public String getSanitizedCode() {
        return sanitizedCode;
    }

    public void setSanitizedCode(String sanitizedCode) {
        this.sanitizedCode = sanitizedCode;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getReviewerComments() {
        return reviewerComments;
    }

    public void setReviewerComments(String reviewerComments) {
        this.reviewerComments = reviewerComments;
    }

    public int getMarks() {
        return marks;
    }

    public void setMarks(int marks) {
        this.marks = marks;
    }

    // =========================================================================
    // Object Identity & Diagnostics
    // =========================================================================

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Snippet snippet = (Snippet) o;
        return Objects.equals(id, snippet.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Snippet{" +
                "id=" + id +
                ", usn='" + usn + '\'' +
                ", studentName='" + studentName + '\'' +
                ", email='" + email + '\'' +
                ", labExpNo=" + labExpNo +
                ", language='" + language + '\'' +
                ", title='" + title + '\'' +
                ", status='" + status + '\'' +
                ", reviewerComments='" + reviewerComments + '\'' +
                ", marks=" + marks +
                '}';
    }
}

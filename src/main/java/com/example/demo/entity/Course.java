package com.example.demo.entity;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

@Entity
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "code must not be blank")
    private String code;

    @NotBlank(message = "title must not be blank")
    private String title;

    private String description;

    @Positive(message = "credit must be positive")
    private Integer credit;

    public Course() {
    }

    public Course(Long id, String code, String title, String description, Integer credit) {
        this.id = id;
        this.code = code;
        this.title = title;
        this.description = description;
        this.credit = credit;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getCredit() {
        return credit;
    }

    public void setCredit(Integer credit) {
        this.credit = credit;
    }
}

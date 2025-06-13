package com.bezkoder.springjwt.models;

import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "reports")
public class Report {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Size(max = 1000)
    private String chat;

    @OneToMany(mappedBy = "report")
    private Set<Case> cases = new HashSet<>();

    private Integer total;

    @Size(max = 100)
    private String engineerName;

    public Report() {
    }

    public Report(String chat, Integer total, String engineerName) {
        this.chat = chat;
        this.total = total;
        this.engineerName = engineerName;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getChat() {
        return chat;
    }

    public void setChat(String chat) {
        this.chat = chat;
    }

    public Set<Case> getCases() {
        return cases;
    }

    public void setCases(Set<Case> cases) {
        this.cases = cases;
    }

    public Integer getTotal() {
        return total;
    }

    public void setTotal(Integer total) {
        this.total = total;
    }

    public String getEngineerName() {
        return engineerName;
    }

    public void setEngineerName(String engineerName) {
        this.engineerName = engineerName;
    }
} 
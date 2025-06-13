package com.bezkoder.springjwt.models;

import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import com.fasterxml.jackson.annotation.JsonIgnore;
@Entity
@Table(name = "engineers")
public class Engineer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 100)
    private String fullName;

    @Size(max = 20)
    private String phoneNumber;

    @Email
    @Size(max = 50)
    private String email;

    @Size(max = 10)
    private String gender;

    @NotBlank
    @Size(max = 50)
    private String manager;

    @ManyToOne
    @JoinColumn(name = "team_id")
    private Team team;

    @JsonIgnore
    @OneToMany(mappedBy = "engineer")
    private Set<Case> assignedCases = new HashSet<>();

    public Engineer() {
    }

    public Engineer(String fullName, String phoneNumber, String email, String gender, String manager) {
        this.fullName = fullName;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.gender = gender;
        this.manager = manager;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getManager() {
        return manager;
    }

    public void setManager(String manager) {
        this.manager = manager;
    }

    public Team getTeam() {
        return team;
    }

    public void setTeam(Team team) {
        this.team = team;
    }

    public Set<Case> getAssignedCases() {
        return assignedCases;
    }

    public void setAssignedCases(Set<Case> assignedCases) {
        this.assignedCases = assignedCases;
    }
} 
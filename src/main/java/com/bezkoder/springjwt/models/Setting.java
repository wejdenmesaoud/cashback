package com.bezkoder.springjwt.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "settings")
public class Setting {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 100)
    @Column(name = "setting_key")
    private String settingKey;

    @NotNull
    @DecimalMin(value = "0.0")
    private Double caseCoefficient;

    @NotNull
    @DecimalMin(value = "0.0")
    private Double chatCoefficient;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    public Setting() {
    }

    public Setting(String settingKey, Double caseCoefficient, Double chatCoefficient, User user) {
        this.settingKey = settingKey;
        this.caseCoefficient = caseCoefficient;
        this.chatCoefficient = chatCoefficient;
        this.user = user;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getSettingKey() {
        return settingKey;
    }

    public void setSettingKey(String settingKey) {
        this.settingKey = settingKey;
    }

    public Double getCaseCoefficient() {
        return caseCoefficient;
    }

    public void setCaseCoefficient(Double caseCoefficient) {
        this.caseCoefficient = caseCoefficient;
    }

    public Double getChatCoefficient() {
        return chatCoefficient;
    }

    public void setChatCoefficient(Double chatCoefficient) {
        this.chatCoefficient = chatCoefficient;
    }
}
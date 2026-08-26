package com.smartparking.userservice.dto;

import jakarta.validation.constraints.NotBlank;

public class ProfileCreateRequest {

    @NotBlank
    private String fullName;

    private String phone;

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}

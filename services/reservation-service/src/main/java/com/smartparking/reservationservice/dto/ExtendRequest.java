package com.smartparking.reservationservice.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class ExtendRequest {

    @NotNull
    @Min(1)
    @Max(120)
    private Integer additionalMinutes;

    public Integer getAdditionalMinutes() {
        return additionalMinutes;
    }

    public void setAdditionalMinutes(Integer additionalMinutes) {
        this.additionalMinutes = additionalMinutes;
    }
}

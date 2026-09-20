package com.elms.backend.performance.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateReviewPeriodRequest {

    @NotBlank(message = "Nama periode evaluasi wajib diisi")
    private String name;

    @NotNull(message = "Tanggal mulai periode wajib diisi")
    private LocalDate startDate;

    @NotNull(message = "Tanggal selesai periode wajib diisi")
    private LocalDate endDate;
}

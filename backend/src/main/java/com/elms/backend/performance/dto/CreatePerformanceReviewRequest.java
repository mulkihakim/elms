package com.elms.backend.performance.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreatePerformanceReviewRequest {

    @NotNull(message = "Periode evaluasi wajib dipilih")
    private Long periodId;

    @NotNull(message = "Karyawan yang dinilai wajib dipilih")
    private UUID employeeId;

    @NotNull(message = "Nilai kemampuan teknis wajib diisi")
    @Min(value = 1, message = "Nilai minimal adalah 1")
    @Max(value = 5, message = "Nilai maksimal adalah 5")
    private Integer technicalSkill;

    @NotNull(message = "Nilai komunikasi wajib diisi")
    @Min(value = 1, message = "Nilai minimal adalah 1")
    @Max(value = 5, message = "Nilai maksimal adalah 5")
    private Integer communication;

    @NotNull(message = "Nilai kerjasama tim wajib diisi")
    @Min(value = 1, message = "Nilai minimal adalah 1")
    @Max(value = 5, message = "Nilai maksimal adalah 5")
    private Integer teamwork;

    @NotNull(message = "Nilai pemecahan masalah wajib diisi")
    @Min(value = 1, message = "Nilai minimal adalah 1")
    @Max(value = 5, message = "Nilai maksimal adalah 5")
    private Integer problemSolving;

    private String comments;
}

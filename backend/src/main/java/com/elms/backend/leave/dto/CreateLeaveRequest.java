package com.elms.backend.leave.dto;

import com.elms.backend.leave.LeaveType;
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
public class CreateLeaveRequest {

    @NotNull(message = "Tanggal mulai wajib diisi")
    private LocalDate startDate;

    @NotNull(message = "Tanggal selesai wajib diisi")
    private LocalDate endDate;

    @NotNull(message = "Jenis cuti wajib dipilih")
    private LeaveType leaveType;

    @NotBlank(message = "Alasan cuti wajib diisi")
    private String reason;
}

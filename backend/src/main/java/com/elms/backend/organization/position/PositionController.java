package com.elms.backend.organization.position;

import com.elms.backend.common.response.ApiResponse;
import com.elms.backend.organization.position.dto.PositionRequest;
import com.elms.backend.organization.position.dto.PositionResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/positions")
@RequiredArgsConstructor
public class PositionController {

    private final PositionService positionService;

    @PostMapping
    public ResponseEntity<ApiResponse<PositionResponse>> createPosition(
            @Valid @RequestBody PositionRequest request) {
        PositionResponse response = positionService.createPosition(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Position created successfully"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<PositionResponse>>> getAllPositions(
            @RequestParam(required = false) Long departmentId,
            @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.ASC) Pageable pageable) {
        Page<PositionResponse> response = positionService.getAllPositions(departmentId, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PositionResponse>> getPositionById(@PathVariable Long id) {
        PositionResponse response = positionService.getPositionById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<PositionResponse>> updatePosition(
            @PathVariable Long id,
            @Valid @RequestBody PositionRequest request) {
        PositionResponse response = positionService.updatePosition(id, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Position updated successfully"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deletePosition(@PathVariable Long id) {
        positionService.deletePosition(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Position deleted successfully"));
    }
}

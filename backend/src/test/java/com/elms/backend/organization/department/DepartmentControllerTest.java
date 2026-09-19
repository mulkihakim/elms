package com.elms.backend.organization.department;

import com.elms.backend.common.exception.DuplicateResourceException;
import com.elms.backend.common.exception.GlobalExceptionHandler;
import com.elms.backend.common.exception.ResourceNotFoundException;
import com.elms.backend.organization.department.dto.DepartmentRequest;
import com.elms.backend.organization.department.dto.DepartmentResponse;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = {DepartmentController.class, GlobalExceptionHandler.class})
@AutoConfigureMockMvc(addFilters = false)
class DepartmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private DepartmentService departmentService;

    private DepartmentResponse sampleResponse;

    @BeforeEach
    void setUp() {
        sampleResponse = DepartmentResponse.builder()
                .id(1L)
                .name("Human Resources")
                .build();
    }

    @Test
    void createDepartment_Success() throws Exception {
        DepartmentRequest request = new DepartmentRequest("Human Resources");

        when(departmentService.createDepartment(any(DepartmentRequest.class))).thenReturn(sampleResponse);

        mockMvc.perform(post("/api/v1/departments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.name").value("Human Resources"))
                .andExpect(jsonPath("$.message").value("Department created successfully"));
    }

    @Test
    void createDepartment_ValidationError_BlankName() throws Exception {
        DepartmentRequest request = new DepartmentRequest("");

        mockMvc.perform(post("/api/v1/departments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));
    }

    @Test
    void createDepartment_DuplicateName() throws Exception {
        DepartmentRequest request = new DepartmentRequest("Human Resources");

        when(departmentService.createDepartment(any(DepartmentRequest.class)))
                .thenThrow(new DuplicateResourceException("Department with name 'Human Resources' already exists"));

        mockMvc.perform(post("/api/v1/departments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("RESOURCE_ALREADY_EXISTS"));
    }

    @Test
    void getAllDepartments_Success() throws Exception {
        when(departmentService.getAllDepartments(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(sampleResponse)));

        mockMvc.perform(get("/api/v1/departments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].id").value(1))
                .andExpect(jsonPath("$.data.content[0].name").value("Human Resources"));
    }

    @Test
    void getDepartmentById_Success() throws Exception {
        when(departmentService.getDepartmentById(1L)).thenReturn(sampleResponse);

        mockMvc.perform(get("/api/v1/departments/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.name").value("Human Resources"));
    }

    @Test
    void getDepartmentById_NotFound() throws Exception {
        when(departmentService.getDepartmentById(99L))
                .thenThrow(new ResourceNotFoundException("Department not found with id: 99"));

        mockMvc.perform(get("/api/v1/departments/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("RESOURCE_NOT_FOUND"));
    }

    @Test
    void updateDepartment_Success() throws Exception {
        DepartmentRequest request = new DepartmentRequest("People Operations");
        DepartmentResponse updatedResponse = DepartmentResponse.builder().id(1L).name("People Operations").build();

        when(departmentService.updateDepartment(eq(1L), any(DepartmentRequest.class))).thenReturn(updatedResponse);

        mockMvc.perform(put("/api/v1/departments/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("People Operations"))
                .andExpect(jsonPath("$.message").value("Department updated successfully"));
    }

    @Test
    void deleteDepartment_Success() throws Exception {
        doNothing().when(departmentService).deleteDepartment(1L);

        mockMvc.perform(delete("/api/v1/departments/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Department deleted successfully"));
    }

    @Test
    void deleteDepartment_NotFound() throws Exception {
        doThrow(new ResourceNotFoundException("Department not found with id: 99"))
                .when(departmentService).deleteDepartment(99L);

        mockMvc.perform(delete("/api/v1/departments/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("RESOURCE_NOT_FOUND"));
    }
}

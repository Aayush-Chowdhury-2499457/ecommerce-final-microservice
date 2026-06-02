package com.cts.userservice.controller;

import com.cts.userservice.dto.AddressDTO;
import com.cts.userservice.dto.AddressResponseDTO;
import com.cts.userservice.exception.GlobalExceptionHandler;
import com.cts.userservice.exception.custom.ResourceNotFoundException;
import com.cts.userservice.service.AddressService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Standalone MockMvc tests for {@link AddressController} covering routing and authorization.
 */
@Slf4j
@ExtendWith(MockitoExtension.class)
class AddressControllerTest {

    @Mock AddressService addressService;
    @InjectMocks AddressController controller;

    MockMvc mvc;
    final ObjectMapper mapper = new ObjectMapper();

    private AddressDTO dto;
    private AddressResponseDTO resp;

    /** Sets up MockMvc with the controller advice plus sample request/response data. */
    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        dto = new AddressDTO();
        dto.setHouseNo("12");
        dto.setArea("Main");
        dto.setCity("NYC");
        dto.setState("NY");
        dto.setCountry("US");
        dto.setPincode("12345");

        resp = AddressResponseDTO.builder()
                .addressId(5L).userId(1L).houseNo("12").area("Main")
                .city("NYC").state("NY").country("US").pincode("12345").build();
    }

    /** Owner adding an address receives HTTP 201. */
    @Test
    void add_self_returns201() throws Exception {
        when(addressService.add(eq(1L), any())).thenReturn(resp);

        mvc.perform(post("/api/users/1/addresses")
                        .header("X-User-Id", "1").header("X-User-Role", "CUSTOMER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.addressId").value(5));
    }

    /** Non-owner adding an address receives HTTP 403. */
    @Test
    void add_notSelf_returns403() throws Exception {
        mvc.perform(post("/api/users/1/addresses")
                        .header("X-User-Id", "2").header("X-User-Role", "CUSTOMER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden());
    }

    /** Invalid request body receives HTTP 400. */
    @Test
    void add_invalidBody_returns400() throws Exception {
        dto.setCity("");
        dto.setPincode("xx");

        mvc.perform(post("/api/users/1/addresses")
                        .header("X-User-Id", "1").header("X-User-Role", "CUSTOMER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    /** Owner listing addresses receives HTTP 200. */
    @Test
    void list_self_returns200() throws Exception {
        when(addressService.listForUser(1L)).thenReturn(List.of(resp));

        mvc.perform(get("/api/users/1/addresses")
                        .header("X-User-Id", "1").header("X-User-Role", "CUSTOMER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].city").value("NYC"));
    }

    /** Owner fetching one address receives HTTP 200. */
    @Test
    void getOne_withCaller_returns200() throws Exception {
        when(addressService.getOne(1L, 5L)).thenReturn(resp);

        mvc.perform(get("/api/users/1/addresses/5")
                        .header("X-User-Id", "1").header("X-User-Role", "CUSTOMER"))
                .andExpect(status().isOk());
    }

    /** Internal call without a caller id skips auth and receives HTTP 200. */
    @Test
    void getOne_internalNoCaller_returns200() throws Exception {
        when(addressService.getOne(1L, 5L)).thenReturn(resp);

        mvc.perform(get("/api/users/1/addresses/5"))
                .andExpect(status().isOk());
    }

    /** Missing address receives HTTP 404. */
    @Test
    void getOne_notFound_returns404() throws Exception {
        when(addressService.getOne(1L, 5L))
                .thenThrow(new ResourceNotFoundException("Address not found: 5"));

        mvc.perform(get("/api/users/1/addresses/5")
                        .header("X-User-Id", "1").header("X-User-Role", "ADMIN"))
                .andExpect(status().isNotFound());
    }

    /** Owner updating an address receives HTTP 200. */
    @Test
    void update_self_returns200() throws Exception {
        when(addressService.update(eq(1L), eq(5L), any())).thenReturn(resp);

        mvc.perform(put("/api/users/1/addresses/5")
                        .header("X-User-Id", "1").header("X-User-Role", "CUSTOMER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(dto)))
                .andExpect(status().isOk());
    }

    /** Owner deleting an address receives HTTP 204. */
    @Test
    void delete_self_returns204() throws Exception {
        mvc.perform(delete("/api/users/1/addresses/5")
                        .header("X-User-Id", "1").header("X-User-Role", "CUSTOMER"))
                .andExpect(status().isNoContent());
        verify(addressService).delete(1L, 5L);
    }

    /** Non-owner deleting an address receives HTTP 403. */
    @Test
    void delete_notSelf_returns403() throws Exception {
        mvc.perform(delete("/api/users/1/addresses/5")
                        .header("X-User-Id", "2").header("X-User-Role", "CUSTOMER"))
                .andExpect(status().isForbidden());
        verify(addressService, never()).delete(any(), any());
    }
}
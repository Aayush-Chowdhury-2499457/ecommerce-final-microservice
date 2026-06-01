package com.cts.userservice.controller;

import com.cts.userservice.dto.AddressDTO;
import com.cts.userservice.dto.AddressResponseDTO;
import com.cts.userservice.exception.GlobalExceptionHandler;
import com.cts.userservice.exception.custom.ResourceNotFoundException;
import com.cts.userservice.service.AddressService;
import com.fasterxml.jackson.databind.ObjectMapper;
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

@ExtendWith(MockitoExtension.class)
class AddressControllerTest {

    @Mock AddressService addressService;
    @InjectMocks AddressController controller;

    MockMvc mvc;
    final ObjectMapper mapper = new ObjectMapper();

    private AddressDTO dto;
    private AddressResponseDTO resp;

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

    @Test
    void add_notSelf_returns403() throws Exception {
        mvc.perform(post("/api/users/1/addresses")
                        .header("X-User-Id", "2").header("X-User-Role", "CUSTOMER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden());
    }

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

    @Test
    void list_self_returns200() throws Exception {
        when(addressService.listForUser(1L)).thenReturn(List.of(resp));

        mvc.perform(get("/api/users/1/addresses")
                        .header("X-User-Id", "1").header("X-User-Role", "CUSTOMER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].city").value("NYC"));
    }

    @Test
    void getOne_withCaller_returns200() throws Exception {
        when(addressService.getOne(1L, 5L)).thenReturn(resp);

        mvc.perform(get("/api/users/1/addresses/5")
                        .header("X-User-Id", "1").header("X-User-Role", "CUSTOMER"))
                .andExpect(status().isOk());
    }

    @Test
    void getOne_internalNoCaller_returns200() throws Exception {
        when(addressService.getOne(1L, 5L)).thenReturn(resp);

        mvc.perform(get("/api/users/1/addresses/5"))
                .andExpect(status().isOk());
    }

    @Test
    void getOne_notFound_returns404() throws Exception {
        when(addressService.getOne(1L, 5L))
                .thenThrow(new ResourceNotFoundException("Address not found: 5"));

        mvc.perform(get("/api/users/1/addresses/5")
                        .header("X-User-Id", "1").header("X-User-Role", "ADMIN"))
                .andExpect(status().isNotFound());
    }

    @Test
    void update_self_returns200() throws Exception {
        when(addressService.update(eq(1L), eq(5L), any())).thenReturn(resp);

        mvc.perform(put("/api/users/1/addresses/5")
                        .header("X-User-Id", "1").header("X-User-Role", "CUSTOMER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(dto)))
                .andExpect(status().isOk());
    }

    @Test
    void delete_self_returns204() throws Exception {
        mvc.perform(delete("/api/users/1/addresses/5")
                        .header("X-User-Id", "1").header("X-User-Role", "CUSTOMER"))
                .andExpect(status().isNoContent());
        verify(addressService).delete(1L, 5L);
    }

    @Test
    void delete_notSelf_returns403() throws Exception {
        mvc.perform(delete("/api/users/1/addresses/5")
                        .header("X-User-Id", "2").header("X-User-Role", "CUSTOMER"))
                .andExpect(status().isForbidden());
        verify(addressService, never()).delete(any(), any());
    }
}
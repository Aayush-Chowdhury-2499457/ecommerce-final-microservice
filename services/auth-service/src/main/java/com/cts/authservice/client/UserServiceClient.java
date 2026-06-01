package com.cts.authservice.client;

import com.cts.authservice.config.FeignConfig;
import com.cts.authservice.dto.request.CreateUserRequestDTO;
import com.cts.authservice.dto.response.RegisterResponseDTO;
import com.cts.authservice.dto.response.UserDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "user-service", configuration = FeignConfig.class)
public interface UserServiceClient {

    @PostMapping("/api/users")
    ResponseEntity<RegisterResponseDTO> createUser(@RequestBody CreateUserRequestDTO registerRequestDTO);

    @GetMapping("/api/users/username/{username}")
    ResponseEntity<UserDTO> getUserByUsername(@PathVariable String username);

    @GetMapping("/api/users/email/{email}")
    ResponseEntity<UserDTO> getUserByEmail(@PathVariable String email);

}

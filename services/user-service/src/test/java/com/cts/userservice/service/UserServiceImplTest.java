package com.cts.userservice.service;

import com.cts.userservice.dto.CreateUserDTO;
import com.cts.userservice.dto.UpdateUserDTO;
import com.cts.userservice.dto.UserResponseDTO;
import com.cts.userservice.entity.Role;
import com.cts.userservice.entity.User;
import com.cts.userservice.exception.custom.DuplicateResourceException;
import com.cts.userservice.exception.custom.ResourceNotFoundException;
import com.cts.userservice.repository.UserRepository;
import com.cts.userservice.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock UserRepository userRepository;
    @InjectMocks UserServiceImpl service;

    private User user;
    private CreateUserDTO createDto;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .userId(1L).name("Bob").username("bob")
                .email("bob@x.com").phoneNumber("1234567890")
                .dateOfBirth(LocalDate.of(1990, 1, 1)).role(Role.CUSTOMER)
                .build();

        createDto = new CreateUserDTO();
        createDto.setName("Bob");
        createDto.setUsername("bob");
        createDto.setEmail("bob@x.com");
        createDto.setPhoneNumber("1234567890");
        createDto.setDateOfBirth(LocalDate.of(1990, 1, 1));
    }

    @Test
    void create_success() {
        when(userRepository.existsByUsername("bob")).thenReturn(false);
        when(userRepository.existsByEmail("bob@x.com")).thenReturn(false);
        when(userRepository.existsByPhoneNumber("1234567890")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(user);

        UserResponseDTO out = service.create(createDto);

        assertThat(out.getUsername()).isEqualTo("bob");
        assertThat(out.getRole()).isEqualTo(Role.CUSTOMER);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void create_duplicateUsername_throws() {
        when(userRepository.existsByUsername("bob")).thenReturn(true);

        assertThatThrownBy(() -> service.create(createDto))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessage("Username already taken");
        verify(userRepository, never()).save(any());
    }

    @Test
    void create_duplicateEmail_throws() {
        when(userRepository.existsByUsername("bob")).thenReturn(false);
        when(userRepository.existsByEmail("bob@x.com")).thenReturn(true);

        assertThatThrownBy(() -> service.create(createDto))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessage("Email already in use");
    }

    @Test
    void create_duplicatePhone_throws() {
        when(userRepository.existsByUsername("bob")).thenReturn(false);
        when(userRepository.existsByEmail("bob@x.com")).thenReturn(false);
        when(userRepository.existsByPhoneNumber("1234567890")).thenReturn(true);

        assertThatThrownBy(() -> service.create(createDto))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessage("Phone number already in use");
    }

    @Test
    void findAll_returnsMappedList() {
        when(userRepository.findAll()).thenReturn(List.of(user));

        List<UserResponseDTO> out = service.findAll();

        assertThat(out).hasSize(1);
        assertThat(out.get(0).getUsername()).isEqualTo("bob");
    }

    @Test
    void findById_success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        assertThat(service.findById(1L).getUserId()).isEqualTo(1L);
    }

    @Test
    void findById_notFound_throws() {
        when(userRepository.findById(9L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(9L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    void findByUsername_success() {
        when(userRepository.findByUsername("bob")).thenReturn(Optional.of(user));

        assertThat(service.findByUsername("bob").getEmail()).isEqualTo("bob@x.com");
    }

    @Test
    void findByUsername_notFound_throws() {
        when(userRepository.findByUsername("x")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findByUsername("x"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void findByEmail_success() {
        when(userRepository.findByEmail("bob@x.com")).thenReturn(Optional.of(user));

        assertThat(service.findByEmail("bob@x.com").getUsername()).isEqualTo("bob");
    }

    @Test
    void findByEmail_notFound_throws() {
        when(userRepository.findByEmail("x@x.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findByEmail("x@x.com"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void update_allFields_success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.existsByEmail("new@x.com")).thenReturn(false);
        when(userRepository.existsByPhoneNumber("9999999999")).thenReturn(false);

        UpdateUserDTO dto = new UpdateUserDTO();
        dto.setName("New");
        dto.setEmail("new@x.com");
        dto.setPhoneNumber("9999999999");
        dto.setDateOfBirth(LocalDate.of(2000, 5, 5));

        UserResponseDTO out = service.update(1L, dto);

        assertThat(out.getName()).isEqualTo("New");
        assertThat(out.getEmail()).isEqualTo("new@x.com");
        assertThat(out.getPhoneNumber()).isEqualTo("9999999999");
        assertThat(out.getDateOfBirth()).isEqualTo(LocalDate.of(2000, 5, 5));
    }

    @Test
    void update_notFound_throws() {
        when(userRepository.findById(9L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update(9L, new UpdateUserDTO()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void update_emailDuplicate_throws() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.existsByEmail("new@x.com")).thenReturn(true);

        UpdateUserDTO dto = new UpdateUserDTO();
        dto.setEmail("new@x.com");

        assertThatThrownBy(() -> service.update(1L, dto))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessage("Email already in use");
    }

    @Test
    void update_phoneDuplicate_throws() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.existsByPhoneNumber("9999999999")).thenReturn(true);

        UpdateUserDTO dto = new UpdateUserDTO();
        dto.setPhoneNumber("9999999999");

        assertThatThrownBy(() -> service.update(1L, dto))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessage("Phone number already in use");
    }

    @Test
    void update_sameValues_skipsDuplicateChecks() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        UpdateUserDTO dto = new UpdateUserDTO();
        dto.setEmail("bob@x.com");          // unchanged
        dto.setPhoneNumber("1234567890");   // unchanged

        UserResponseDTO out = service.update(1L, dto);

        assertThat(out.getEmail()).isEqualTo("bob@x.com");
        verify(userRepository, never()).existsByEmail(any());
        verify(userRepository, never()).existsByPhoneNumber(any());
    }

    @Test
    void update_nullFields_keepsExisting() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        UserResponseDTO out = service.update(1L, new UpdateUserDTO());

        assertThat(out.getName()).isEqualTo("Bob");
    }

    @Test
    void delete_success() {
        when(userRepository.existsById(1L)).thenReturn(true);

        service.delete(1L);

        verify(userRepository).deleteById(1L);
    }

    @Test
    void delete_notFound_throws() {
        when(userRepository.existsById(9L)).thenReturn(false);

        assertThatThrownBy(() -> service.delete(9L))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(userRepository, never()).deleteById(any());
    }
}
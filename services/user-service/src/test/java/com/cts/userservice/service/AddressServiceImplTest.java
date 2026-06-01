package com.cts.userservice.service;

import com.cts.userservice.dto.AddressDTO;
import com.cts.userservice.dto.AddressResponseDTO;
import com.cts.userservice.entity.Address;
import com.cts.userservice.entity.User;
import com.cts.userservice.exception.custom.ResourceNotFoundException;
import com.cts.userservice.repository.AddressRepository;
import com.cts.userservice.repository.UserRepository;
import com.cts.userservice.service.impl.AddressServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AddressServiceImplTest {

    @Mock AddressRepository addressRepository;
    @Mock UserRepository userRepository;
    @InjectMocks AddressServiceImpl service;

    private AddressDTO dto;

    @BeforeEach
    void setUp() {
        dto = new AddressDTO();
        dto.setHouseNo("12");
        dto.setArea("Main");
        dto.setCity("NYC");
        dto.setState("NY");
        dto.setCountry("US");
        dto.setPincode("12345");
    }

    private User user(long id) {
        return User.builder().userId(id).username("bob").build();
    }

    private Address address(long id, long ownerId) {
        return Address.builder()
                .addressId(id).user(user(ownerId))
                .houseNo("12").area("Main").city("NYC").state("NY")
                .country("US").pincode("12345").build();
    }

    @Test
    void add_success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user(1L)));
        when(addressRepository.save(any(Address.class))).thenReturn(address(5L, 1L));

        AddressResponseDTO out = service.add(1L, dto);

        assertThat(out.getAddressId()).isEqualTo(5L);
        assertThat(out.getUserId()).isEqualTo(1L);
        assertThat(out.getCity()).isEqualTo("NYC");
    }

    @Test
    void add_userNotFound_throws() {
        when(userRepository.findById(9L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.add(9L, dto))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(addressRepository, never()).save(any());
    }

    @Test
    void listForUser_success() {
        when(userRepository.existsById(1L)).thenReturn(true);
        when(addressRepository.findByUser_UserId(1L)).thenReturn(List.of(address(5L, 1L)));

        assertThat(service.listForUser(1L)).hasSize(1);
    }

    @Test
    void listForUser_userNotFound_throws() {
        when(userRepository.existsById(9L)).thenReturn(false);

        assertThatThrownBy(() -> service.listForUser(9L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getOne_success() {
        when(addressRepository.findById(5L)).thenReturn(Optional.of(address(5L, 1L)));

        assertThat(service.getOne(1L, 5L).getCity()).isEqualTo("NYC");
    }

    @Test
    void getOne_addressNotFound_throws() {
        when(addressRepository.findById(5L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getOne(1L, 5L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Address not found");
    }

    @Test
    void getOne_belongsToOtherUser_throws() {
        when(addressRepository.findById(5L)).thenReturn(Optional.of(address(5L, 2L)));

        assertThatThrownBy(() -> service.getOne(1L, 5L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void update_success() {
        when(addressRepository.findById(5L)).thenReturn(Optional.of(address(5L, 1L)));

        dto.setCity("LA");
        assertThat(service.update(1L, 5L, dto).getCity()).isEqualTo("LA");
    }

    @Test
    void update_notFound_throws() {
        when(addressRepository.findById(5L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update(1L, 5L, dto))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void delete_success() {
        Address a = address(5L, 1L);
        when(addressRepository.findById(5L)).thenReturn(Optional.of(a));

        service.delete(1L, 5L);

        verify(addressRepository).delete(a);
    }

    @Test
    void delete_notFound_throws() {
        when(addressRepository.findById(5L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.delete(1L, 5L))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(addressRepository, never()).delete(any());
    }
}
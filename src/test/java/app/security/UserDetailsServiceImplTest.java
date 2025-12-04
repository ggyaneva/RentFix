package app.security;

import app.model.User;
import app.model.enums.Role;
import app.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserDetailsServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserDetailsServiceImpl userDetailsService;

    @Test
    void loadUserByUsername_returnsUserData_whenUserExists() {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setUsername("testUser");
        user.setPassword("encodedPass");
        user.setRole(Role.TENANT);

        when(userRepository.findByUsername("testUser"))
                .thenReturn(Optional.of(user));

        UserDetails result = userDetailsService.loadUserByUsername("testUser");

        assertTrue(result instanceof UserData);
        UserData userData = (UserData) result;

        assertEquals(user.getId(), userData.getUserId());
        assertEquals("testUser", userData.getUsername());
        assertEquals("encodedPass", userData.getPassword());
        assertEquals(Role.TENANT, userData.getRole());

        verify(userRepository).findByUsername("testUser");
    }

    @Test
    void loadUserByUsername_throwsException_whenUserMissing() {
        when(userRepository.findByUsername("missing"))
                .thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class,
                () -> userDetailsService.loadUserByUsername("missing"));

        verify(userRepository).findByUsername("missing");
    }
}
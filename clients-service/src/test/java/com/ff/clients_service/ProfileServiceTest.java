package com.ff.clients_service;

import com.ff.clients_service.dto.ProfileResponse;
import com.ff.clients_service.dto.ProfileUpdateRequest;
import com.ff.clients_service.entity.Profile;
import com.ff.clients_service.entity.User;
import com.ff.clients_service.entity.UserRole;
import com.ff.clients_service.repository.ProfileRepository;
import com.ff.clients_service.repository.UserRepository;
import com.ff.clients_service.service.ProfileService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class ProfileServiceTest {

    private ProfileService profileService;
    private ProfileRepository profileRepository;
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        profileRepository = mock(ProfileRepository.class);
        userRepository = mock(UserRepository.class);
        profileService = new ProfileService(profileRepository, userRepository);
    }

    @Test
    void testSaveProfile_Success() {
        // GIVEN
        String email = "test@example.com";
        User user = User.builder().id(1L).email(email).role(UserRole.USER).build(); // Add role for complete ProfileResponse
        ProfileUpdateRequest request = new ProfileUpdateRequest();
        request.setUsername("jdoe");
        request.setFirstName("John");
        request.setLastName("Doe");

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        // Mock the save method to return the passed profile, including its generated ID if any.
        // For unit tests, we're mostly concerned that the save method was called with the correct data.
        when(profileRepository.save(any(Profile.class))).thenAnswer(invocation -> {
            Profile profileToSave = invocation.getArgument(0);
            profileToSave.setId(1L); // Simulate ID generation upon saving
            profileToSave.setCreatedAt(LocalDateTime.now()); // Simulate createdAt being set
            return profileToSave;
        });

        // WHEN
        ProfileResponse savedProfileResponse = profileService.saveProfile(email, request);

        // THEN
        assertThat(savedProfileResponse).isNotNull();
        assertThat(savedProfileResponse.getId()).isEqualTo(1L);
        assertThat(savedProfileResponse.getFirstName()).isEqualTo("John");
        assertThat(savedProfileResponse.getLastName()).isEqualTo("Doe");
        assertThat(savedProfileResponse.getUsername()).isEqualTo("jdoe");
        assertThat(savedProfileResponse.getEmail()).isEqualTo(email);
        assertThat(savedProfileResponse.getRole()).isEqualTo(user.getRole().name());
        assertThat(savedProfileResponse.getCreatedAt()).isNotNull();
        verify(userRepository).findByEmail(email);
        verify(profileRepository).save(any(Profile.class));
    }

    @Test
    void testSaveProfile_UserNotFound() {
        // GIVEN
        String email = "notfound@example.com";
        ProfileUpdateRequest request = new ProfileUpdateRequest();

        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        // WHEN & THEN
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            profileService.saveProfile(email, request);
        });
        assertThat(exception.getMessage()).isEqualTo("Utilisateur non trouvé");
        verify(userRepository).findByEmail(email);
        verifyNoInteractions(profileRepository); // Ensure profileRepository is not called
    }

    @Test
    void testUpdateProfile_Success() {
        // GIVEN
        String email = "test@example.com";
        User user = User.builder().id(1L).email(email).role(UserRole.USER).build();
        Profile existingProfile = Profile.builder()
                .id(1L)
                .user(user)
                .firstName("OldFirst")
                .lastName("OldLast")
                .username("olduser")
                .createdAt(LocalDateTime.now().minusDays(1))
                .build();

        ProfileUpdateRequest request = new ProfileUpdateRequest();
        request.setUsername("newuser");
        request.setFirstName("Jane");
        request.setLastName("Doe");

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(profileRepository.findByUser(user)).thenReturn(Optional.of(existingProfile));
        // Mock save to return the modified existingProfile
        when(profileRepository.save(any(Profile.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // WHEN
        ProfileResponse updatedProfileResponse = profileService.updateProfile(email, request);

        // THEN
        assertThat(updatedProfileResponse).isNotNull();
        assertThat(updatedProfileResponse.getId()).isEqualTo(existingProfile.getId());
        assertThat(updatedProfileResponse.getFirstName()).isEqualTo("Jane");
        assertThat(updatedProfileResponse.getLastName()).isEqualTo("Doe");
        assertThat(updatedProfileResponse.getUsername()).isEqualTo("newuser");
        assertThat(updatedProfileResponse.getEmail()).isEqualTo(email);
        assertThat(updatedProfileResponse.getRole()).isEqualTo(user.getRole().name());
        assertThat(updatedProfileResponse.getCreatedAt()).isEqualTo(existingProfile.getCreatedAt().toString());
        assertThat(updatedProfileResponse.getUpdatedAt()).isNotNull(); // Should be updated

        verify(userRepository).findByEmail(email);
        verify(profileRepository).findByUser(user);
        verify(profileRepository).save(existingProfile); // Verify that the existing profile object was saved
    }

    @Test
    void testUpdateProfile_UserNotFound() {
        // GIVEN
        String email = "notfound@example.com";
        ProfileUpdateRequest request = new ProfileUpdateRequest();

        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        // WHEN & THEN
        UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class, () -> {
            profileService.updateProfile(email, request);
        });
        assertThat(exception.getMessage()).isEqualTo("Utilisateur non trouvé");
        verify(userRepository).findByEmail(email);
        verifyNoInteractions(profileRepository);
    }

    @Test
    void testUpdateProfile_ProfileNotFound() {
        // GIVEN
        String email = "test@example.com";
        User user = User.builder().id(1L).email(email).build();
        ProfileUpdateRequest request = new ProfileUpdateRequest();

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(profileRepository.findByUser(user)).thenReturn(Optional.empty());

        // WHEN & THEN
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            profileService.updateProfile(email, request);
        });
        assertThat(exception.getMessage()).isEqualTo("Profile non trouvé");
        verify(userRepository).findByEmail(email);
        verify(profileRepository).findByUser(user);
        verifyNoMoreInteractions(profileRepository); // No save should happen
    }

    @Test
    void testGetProfile_Success() {
        // GIVEN
        String email = "test@example.com";
        User user = User.builder().id(1L).email(email).role(UserRole.USER).build();
        Profile profile = Profile.builder()
                .id(1L)
                .user(user)
                .firstName("John")
                .lastName("Doe")
                .username("johndoe")
                .createdAt(LocalDateTime.now().minusMonths(1))
                .updatedAt(LocalDateTime.now().minusDays(5))
                .build();

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(profileRepository.findByUser(user)).thenReturn(Optional.of(profile));

        // WHEN
        ProfileResponse result = profileService.getProfile(email);

        // THEN
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(profile.getId());
        assertThat(result.getFirstName()).isEqualTo(profile.getFirstName());
        assertThat(result.getLastName()).isEqualTo(profile.getLastName());
        assertThat(result.getUsername()).isEqualTo(profile.getUsername());
        assertThat(result.getEmail()).isEqualTo(user.getEmail());
        assertThat(result.getRole()).isEqualTo(user.getRole().name());
        assertThat(result.getCreatedAt()).isEqualTo(profile.getCreatedAt().toString());
        assertThat(result.getUpdatedAt()).isEqualTo(profile.getUpdatedAt().toString());

        verify(userRepository).findByEmail(email);
        verify(profileRepository).findByUser(user);
    }

    @Test
    void testGetProfile_UserNotFound() {
        // GIVEN
        String email = "notfound@example.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        // WHEN & THEN
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            profileService.getProfile(email);
        });
        assertThat(exception.getMessage()).isEqualTo("Utilisateur non trouver"); // Typo in message
        verify(userRepository).findByEmail(email);
        verifyNoInteractions(profileRepository);
    }

    @Test
    void testGetProfile_ProfileNotFound() {
        // GIVEN
        String email = "test@example.com";
        User user = User.builder().id(1L).email(email).build();

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(profileRepository.findByUser(user)).thenReturn(Optional.empty());

        // WHEN & THEN
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            profileService.getProfile(email);
        });
        assertThat(exception.getMessage()).isEqualTo("Profile non trouver"); // Typo in message
        verify(userRepository).findByEmail(email);
        verify(profileRepository).findByUser(user);
    }
}
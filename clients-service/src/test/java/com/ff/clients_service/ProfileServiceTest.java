package com.ff.clients_service;


import com.ff.clients_service.dto.ProfileUpdateRequest;
import com.ff.clients_service.entity.Profile;
import com.ff.clients_service.entity.User;
import com.ff.clients_service.repository.ProfileRepository;
import com.ff.clients_service.repository.UserRepository;
import com.ff.clients_service.service.ProfileService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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
        User user = User.builder().id(1L).email(email).build();
        ProfileUpdateRequest request = new ProfileUpdateRequest();
        request.setUsername("jdoe");
        request.setFirstName("John");
        request.setLastName("Doe");

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(profileRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        // WHEN
        Profile saved = profileService.saveProfile(email, request);

        // THEN
        assertThat(saved.getUsername()).isEqualTo("jdoe");
        assertThat(saved.getUser()).isEqualTo(user);
        verify(profileRepository).save(any(Profile.class));
    }

    @Test
    void testUpdateProfile_Success() {
        String email = "test@example.com";
        User user = User.builder().id(1L).email(email).build();
        Profile existing = Profile.builder().id(1L).user(user).build();
        ProfileUpdateRequest request = new ProfileUpdateRequest();
        request.setUsername("newuser");
        request.setFirstName("Jane");
        request.setLastName("Doe");

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(profileRepository.findByUser(user)).thenReturn(Optional.of(existing));
        when(profileRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        Profile updated = profileService.updateProfile(email, request);

        assertThat(updated.getUsername()).isEqualTo("newuser");
        assertThat(updated.getFirstName()).isEqualTo("Jane");
    }

    @Test
    void testGetProfile_Success() {
        String email = "test@example.com";
        User user = User.builder().id(1L).email(email).build();
        Profile profile = Profile.builder().user(user).username("user").build();

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(profileRepository.findByUser(user)).thenReturn(Optional.of(profile));

        Profile result = profileService.getProfile(email);

        assertThat(result.getUsername()).isEqualTo("user");
    }

    @Test
    void testSaveProfile_UserNotFound() {
        when(userRepository.findByEmail("notfound@example.com")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> {
            profileService.saveProfile("notfound@example.com", new ProfileUpdateRequest());
        });
    }

    @Test
    void testUpdateProfile_ProfileNotFound() {
        User user = User.builder().id(1L).email("a@b.c").build();
        when(userRepository.findByEmail("a@b.c")).thenReturn(Optional.of(user));
        when(profileRepository.findByUser(user)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> {
            profileService.updateProfile("a@b.c", new ProfileUpdateRequest());
        });
    }
}

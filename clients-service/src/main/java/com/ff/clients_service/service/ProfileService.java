package com.ff.clients_service.service;

import com.ff.clients_service.dto.ProfileResponse;
import com.ff.clients_service.dto.ProfileUpdateRequest;
import com.ff.clients_service.entity.Profile;
import com.ff.clients_service.entity.User;
import com.ff.clients_service.repository.ProfileRepository;
import com.ff.clients_service.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@AllArgsConstructor
public class ProfileService {
    private final ProfileRepository profileRepository;
    private final UserRepository userRepository;

    public ProfileResponse saveProfile(String email, ProfileUpdateRequest request){
        var user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
        var profile = Profile.builder()
                .user(user)
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .username(request.getUsername())
                .createdAt(LocalDateTime.now())
                .build();

        profileRepository.save(profile);

        return ProfileResponse.builder()
                .id(profile.getId())
                .firstName(profile.getFirstName())
                .lastName(profile.getLastName())
                .username(profile.getUsername())
                .email(user.getEmail())
                .role(user.getRole().name())
                .createdAt(profile.getCreatedAt().toString())
                .build();
    }

    public ProfileResponse updateProfile(String email, ProfileUpdateRequest request){
        User user = userRepository.findByEmail(email).orElseThrow(()-> new UsernameNotFoundException("Utilisateur non trouvé"));

        Profile profile = profileRepository.findByUser(user).orElseThrow(()-> new RuntimeException("Profile non trouvé"));

        profile.setFirstName(request.getFirstName());
        profile.setLastName(request.getLastName());
        profile.setUsername(request.getUsername());
        profile.setUpdatedAt(LocalDateTime.now());
         profileRepository.save(profile);
         return ProfileResponse.builder()
                .id(profile.getId())
                .firstName(profile.getFirstName())
                .lastName(profile.getLastName())
                .username(profile.getUsername())
                .email(user.getEmail())
                .role(user.getRole().name())
                .createdAt(profile.getCreatedAt().toString())
                .updatedAt(profile.getUpdatedAt() != null ? profile.getUpdatedAt().toString() : null)
                 .build();
    }

    public ProfileResponse getProfile(String email) {
        User user = userRepository.findByEmail(email).orElseThrow(()-> new RuntimeException("Utilisateur non trouver"));

        Profile profile = profileRepository.findByUser(user).orElseThrow(()-> new RuntimeException("Profile non trouver"));
        return  ProfileResponse.builder()
                .id(profile.getId())
                .firstName(profile.getFirstName())
                .lastName(profile.getLastName())
                .username(profile.getUsername())
                .email(user.getEmail())
                .role(user.getRole().name())
                .createdAt(profile.getCreatedAt().toString())
                .updatedAt(profile.getUpdatedAt() != null ? profile.getUpdatedAt().toString() : null)
                .build();
    }
}

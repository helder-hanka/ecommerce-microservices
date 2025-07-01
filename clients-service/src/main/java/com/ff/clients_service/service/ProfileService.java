package com.ff.clients_service.service;

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

    public Profile saveProfile(String email, ProfileUpdateRequest request){
        var user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
        var profile = Profile.builder()
                .user(user)
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .username(request.getUsername())
                .createdAt(LocalDateTime.now())
                .build();

        return profileRepository.save(profile);
    }

    public Profile updateProfile(String email, ProfileUpdateRequest request){
        User user = userRepository.findByEmail(email).orElseThrow(()-> new UsernameNotFoundException("Utilisateur non trouvé"));

        Profile profile = profileRepository.findByUser(user).orElseThrow(()-> new RuntimeException("Profile non trouvé"));

        profile.setFirstName(request.getFirstName());
        profile.setLastName(request.getLastName());
        profile.setUsername(request.getUsername());
        profile.setUpdatedAt(LocalDateTime.now());
        return profileRepository.save(profile);
    }

    public Profile getProfile(String email) {
        User user = userRepository.findByEmail(email).orElseThrow(()-> new RuntimeException("Utilisateur non trouver"));

        return profileRepository.findByUser(user).orElseThrow(()-> new RuntimeException("Profile non trouver"));
    }
}

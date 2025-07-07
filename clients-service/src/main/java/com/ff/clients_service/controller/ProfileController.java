package com.ff.clients_service.controller;

import com.ff.clients_service.dto.ProfileResponse;
import com.ff.clients_service.dto.ProfileUpdateRequest;
import com.ff.clients_service.security.JwtService;
import com.ff.clients_service.service.ProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/clients/profile")
@RequiredArgsConstructor
public class ProfileController {
    private final ProfileService profileService;
    private final JwtService jwtService;

    @PostMapping
    public ResponseEntity<ProfileResponse> createProfile(@RequestBody ProfileUpdateRequest request, Principal principal){
        if (principal == null || principal.getName() == null) {
            throw new RuntimeException("Utilisateur non trouvé");
        }
        String email = principal.getName();
        ProfileResponse createProfile = profileService.saveProfile(email, request);
        return ResponseEntity.ok(createProfile);
    }

    @PutMapping
    public ResponseEntity<ProfileResponse> UpdateProfile(@RequestBody ProfileUpdateRequest request, Principal principal){
        String email = principal.getName();
        ProfileResponse updateProf = profileService.updateProfile(email, request);
        return ResponseEntity.ok(updateProf);
    }

    @GetMapping
    public ResponseEntity<ProfileResponse> getProfile(Principal principal) {
        if (principal == null || principal.getName() == null) {
            throw new RuntimeException("Utilisateur non trouvé");
        }
        String email = principal.getName();
        return ResponseEntity.ok(profileService.getProfile(email));
    }


}

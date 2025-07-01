package com.ff.clients_service.controller;

import com.ff.clients_service.dto.ProfileUpdateRequest;
import com.ff.clients_service.entity.Profile;
import com.ff.clients_service.service.ProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/client/profile")
@RequiredArgsConstructor
public class ProfileController {
    private final ProfileService profileService;

    @PostMapping
    public ResponseEntity<Profile> createProfile(@RequestBody ProfileUpdateRequest request, Principal principal){
        if (principal == null || principal.getName() == null) {
            throw new RuntimeException("Utilisateur non trouvé");
        }
        String email = principal.getName();
        Profile createProfile = profileService.saveProfile(email, request);
        return ResponseEntity.ok(createProfile);
    }

    @PutMapping
    public ResponseEntity<Profile>UpdateProfile(@RequestBody ProfileUpdateRequest request, Principal principal){
        String email = principal.getName();
        Profile updateProf = profileService.updateProfile(email, request);
        return ResponseEntity.ok(updateProf);
    }

    @GetMapping
    public ResponseEntity<Profile> getProfile(Principal principal){
        String email = principal.getName();
        return ResponseEntity.ok(profileService.getProfile(email));
    }


}

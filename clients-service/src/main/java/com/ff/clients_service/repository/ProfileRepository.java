package com.ff.clients_service.repository;

import com.ff.clients_service.entity.Profile;
import com.ff.clients_service.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProfileRepository extends JpaRepository<Profile, Long>{
    Optional<Profile>findByUser(User user);
}

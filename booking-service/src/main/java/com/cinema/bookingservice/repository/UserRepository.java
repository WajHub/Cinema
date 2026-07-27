package com.cinema.bookingservice.repository;

import java.util.UUID;
import com.cinema.bookingservice.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<UserEntity, UUID> {
}
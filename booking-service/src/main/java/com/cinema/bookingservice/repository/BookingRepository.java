package com.cinema.bookingservice.repository;

import java.util.UUID;
import com.cinema.bookingservice.entity.BookingEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookingRepository extends JpaRepository<BookingEntity, UUID> {
}
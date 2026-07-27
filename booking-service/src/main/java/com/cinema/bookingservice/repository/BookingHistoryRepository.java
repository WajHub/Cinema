package com.cinema.bookingservice.repository;

import java.util.UUID;
import com.cinema.bookingservice.entity.BookingHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookingHistoryRepository extends JpaRepository<BookingHistoryEntity, UUID> {
}
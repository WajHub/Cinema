package com.cinema.bookingservice.repository;

import com.cinema.bookingservice.entity.BookingEntity;
import com.cinema.bookingservice.entity.BookingStatus;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookingRepository extends JpaRepository<BookingEntity, UUID> {

  List<BookingEntity> findAllByUser_IdAndStatus(UUID userId, BookingStatus status);
}

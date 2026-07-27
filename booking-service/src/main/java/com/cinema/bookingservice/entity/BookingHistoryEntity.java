package com.cinema.bookingservice.entity;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "booking_history")
public class BookingHistoryEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(name = "original_booking_id", nullable = false)
  private UUID originalBookingId;

  @Column(name = "user_id")
  private UUID userId;

  @Column(name = "movie_title", nullable = false)
  private String movieTitle;

  @Column(name = "total_price", nullable = false, precision = 12, scale = 2)
  private BigDecimal totalPrice;

  @CreationTimestamp
  @Column(name = "archived_at", nullable = false, updatable = false)
  private OffsetDateTime archivedAt;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(name = "details", columnDefinition = "jsonb")
  private String details;

  @Column(name = "session_movie_history")
  private UUID sessionMovieHistory;
}
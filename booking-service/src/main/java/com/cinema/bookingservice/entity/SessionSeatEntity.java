package com.cinema.bookingservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "session_seat")
public class SessionSeatEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "movie_session_id", nullable = false)
  private MovieSessionEntity movieSession;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "booking_id")
  private BookingEntity booking;

  @Column(name = "catalog_seat_id", nullable = false)
  private UUID catalogSeatId;

  @Column(name = "row_label", nullable = false)
  private String rowLabel;

  @Column(name = "seat_number", nullable = false)
  private Integer seatNumber;

  @Column(name = "final_price", nullable = false, precision = 12, scale = 2)
  private BigDecimal finalPrice;

  @Column(name = "status", nullable = false, length = 32)
  @Enumerated(EnumType.STRING)
  private SeatReservationStatus status;

  @Version
  @Column(name = "version")
  private Long version;
}

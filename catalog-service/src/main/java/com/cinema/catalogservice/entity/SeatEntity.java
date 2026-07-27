package com.cinema.catalogservice.entity;

import java.math.BigDecimal;
import java.util.UUID;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "seat")
public class SeatEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "auditory_id", nullable = false)
  private AuditoryEntity auditory;

  @Column(name = "row_label", nullable = false)
  private String rowLabel;

  @Column(name = "seat_number", nullable = false)
  private Integer seatNumber;

  @Column(name = "seat_price", nullable = false, precision = 12, scale = 2)
  private BigDecimal seatPrice;

  @Column(name = "seat_type", nullable = false)
  private String seatType;
}
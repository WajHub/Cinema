package com.cinema.catalogservice.entity;

import java.time.LocalDate;
import java.util.UUID;
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
@Table(name = "movie")
public class MovieEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(nullable = false)
  private String title;

  @Column(nullable = false, length = 4000)
  private String description;

  @Column(name = "poster_url", nullable = false)
  private String posterUrl;

  @JdbcTypeCode(SqlTypes.ARRAY)
  @Column(nullable = false, columnDefinition = "text[]")
  private String[] genres;

  @Column(name = "duration_minutes", nullable = false)
  private Integer durationMinutes;

  @Column(nullable = false)
  private String language;

  @Column(name = "age_rating", nullable = false)
  private String ageRating;

  @Column(name = "release_date", nullable = false)
  private LocalDate releaseDate;

  @Column(name = "is_active", nullable = false)
  private boolean active;
}
package com.cinema.catalogservice.entity;

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
@Table(name = "session_movie_history")
public class SessionMovieHistoryEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(name = "cinema_name", nullable = false)
  private String cinemaName;

  @Column(name = "cinema_city", nullable = false)
  private String cinemaCity;

  @Column(name = "auditory_name", nullable = false)
  private String auditoryName;

  @Column(name = "movie_title", nullable = false)
  private String movieTitle;

  @Column(name = "movie_description", nullable = false, length = 4000)
  private String movieDescription;

  @Column(name = "movie_poster_url", nullable = false)
  private String moviePosterUrl;

  @JdbcTypeCode(SqlTypes.ARRAY)
  @Column(name = "movie_genres", nullable = false, columnDefinition = "text[]")
  private String[] movieGenres;

  @Column(name = "movie_duration_minutes", nullable = false)
  private Integer movieDurationMinutes;

  @Column(name = "movie_language", nullable = false)
  private String movieLanguage;

  @Column(name = "movie_age_rating", nullable = false)
  private String movieAgeRating;

  @Column(name = "starts_at", nullable = false)
  private OffsetDateTime startsAt;

  @Column(name = "ends_at", nullable = false)
  private OffsetDateTime endsAt;

  @Column(name = "base_price", nullable = false, precision = 12, scale = 2)
  private BigDecimal basePrice;

  @Column(name = "session_status", nullable = false)
  private String sessionStatus;

  @CreationTimestamp
  @Column(name = "recorded_at", nullable = false, updatable = false)
  private OffsetDateTime recordedAt;

  @Column(name = "archived_reason", length = 1000)
  private String archivedReason;
}
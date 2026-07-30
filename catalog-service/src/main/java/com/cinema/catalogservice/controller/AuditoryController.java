package com.cinema.catalogservice.controller;

import com.cinema.catalogservice.dto.AuditoryRequest;
import com.cinema.catalogservice.dto.AuditoryResponse;
import com.cinema.catalogservice.service.AuditoryService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auditoriums")
public class AuditoryController {

  private final AuditoryService auditoryService;

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public AuditoryResponse create(@Valid @RequestBody AuditoryRequest request) {
    return auditoryService.create(request);
  }

  @GetMapping
  public List<AuditoryResponse> list() {
    return auditoryService.findAll();
  }

  @GetMapping("/{id}")
  public AuditoryResponse get(@PathVariable UUID id) {
    return auditoryService.findById(id);
  }

  @PutMapping("/{id}")
  public AuditoryResponse update(@PathVariable UUID id, @Valid @RequestBody AuditoryRequest request) {
    return auditoryService.update(id, request);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable UUID id) {
    auditoryService.delete(id);
  }
}

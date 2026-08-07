package com.cinema.catalogservice.kafka;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class CustomMessage {
  String message;
}

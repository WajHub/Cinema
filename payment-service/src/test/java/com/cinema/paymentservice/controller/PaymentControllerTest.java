package com.cinema.paymentservice.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.cinema.paymentservice.dto.StripeConnectionStatusResponse;
import com.cinema.paymentservice.service.PaymentService;
import com.cinema.paymentservice.service.StripeHealthCheckService;
import com.cinema.paymentservice.service.StripeWebhookService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class PaymentControllerTest {

  private MockMvc mockMvc;

  @Mock
  private PaymentService paymentService;

  @Mock
  private StripeHealthCheckService stripeHealthCheckService;

  @Mock
  private StripeWebhookService stripeWebhookService;

  @InjectMocks
  private PaymentController paymentController;

  @BeforeEach
  void setUp() {
    mockMvc = MockMvcBuilders.standaloneSetup(paymentController).build();
  }

  @Test
  void getStripeStatus_returnsConnectedStatus() throws Exception {
    StripeConnectionStatusResponse expectedResponse = new StripeConnectionStatusResponse(
        true,
        "CONNECTED",
        "acct_test123",
        "Cinema Test",
        "Connected"
    );

    when(stripeHealthCheckService.checkConnection()).thenReturn(expectedResponse);

    mockMvc.perform(get("/api/v1/payments/stripe/status"))
        .andExpect(status().isOk());
  }

  @Test
  void paymentSuccess_returnsSuccessText() throws Exception {
    mockMvc.perform(get("/api/v1/payments/success"))
        .andExpect(status().isOk())
        .andExpect(content().string("SUCCESS"));
  }

  @Test
  void paymentCancel_returnsCancelledText() throws Exception {
    mockMvc.perform(get("/api/v1/payments/cancel"))
        .andExpect(status().isOk())
        .andExpect(content().string("CANCELLED"));
  }
}

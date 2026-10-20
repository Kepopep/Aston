package com.example.notification;

import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
class NotificationApiIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void sendCreated_shouldSendCreatedEmail() throws Exception {
        mockMvc.perform(post("/api/notifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"operation": "CREATED", "email": "api-new@mail.ru"}
                                """))
                .andExpect(status().isOk());

        MimeMessage message = awaitSingleMessage();
        assertEquals("api-new@mail.ru", message.getAllRecipients()[0].toString());
        assertEquals(CREATED_TEXT, textOf(message));
    }

    @Test
    void sendDeleted_shouldSendDeletedEmail() throws Exception {
        mockMvc.perform(post("/api/notifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"operation": "DELETED", "email": "api-old@mail.ru"}
                                """))
                .andExpect(status().isOk());

        MimeMessage message = awaitSingleMessage();
        assertEquals("api-old@mail.ru", message.getAllRecipients()[0].toString());
        assertEquals(DELETED_TEXT, textOf(message));
    }

    @Test
    void invalidEmail_shouldReturnBadRequestAndNotSendEmail() throws Exception {
        mockMvc.perform(post("/api/notifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"operation": "CREATED", "email": "not-an-email"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").isNotEmpty());

        assertEquals(0, greenMail.getReceivedMessages().length);
    }

    @Test
    void missingOperation_shouldReturnBadRequestAndNotSendEmail() throws Exception {
        mockMvc.perform(post("/api/notifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email": "user@mail.ru"}
                                """))
                .andExpect(status().isBadRequest());

        assertEquals(0, greenMail.getReceivedMessages().length);
    }
}

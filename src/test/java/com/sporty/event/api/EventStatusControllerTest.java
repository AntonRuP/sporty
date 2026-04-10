package com.sporty.event.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.sporty.controller.EventStatusController;
import com.sporty.controller.model.EventStatus;
import com.sporty.controller.model.EventStatusResponse;
import com.sporty.service.EventLifecycleService;
import com.sporty.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.TestConstructor;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(EventStatusController.class)
@Import(GlobalExceptionHandler.class)
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class EventStatusControllerTest {

    private final MockMvc mockMvc;

    @MockitoBean
    private EventLifecycleService eventLifecycleService;

    EventStatusControllerTest(MockMvc mockMvc) {
        this.mockMvc = mockMvc;
    }

    @Test
    void shouldAcceptValidStatusUpdate() throws Exception {
        when(eventLifecycleService.updateStatus(any()))
                .thenReturn(new EventStatusResponse("1234", EventStatus.LIVE, true));

        mockMvc.perform(post("/events/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "eventId": "1234",
                                  "status": "LIVE"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.eventId").value("1234"))
                .andExpect(jsonPath("$.status").value("LIVE"))
                .andExpect(jsonPath("$.pollingActive").value(true));
    }

    @Test
    void shouldRejectInvalidPayload() throws Exception {
        mockMvc.perform(post("/events/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "eventId": "",
                                  "status": "UNKNOWN"
                                }
                                """))
                .andExpect(status().isBadRequest());
    }
}

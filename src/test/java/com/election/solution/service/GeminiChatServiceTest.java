package com.election.solution.service;

import com.election.solution.ai.GeminiAssistant;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

class GeminiChatServiceTest {

    @SuppressWarnings("unchecked")
    private final ObjectProvider<GeminiAssistant> provider = mock(ObjectProvider.class);
    private final GeminiAssistant assistant = mock(GeminiAssistant.class);
    private final GeminiChatService service = new GeminiChatService(provider);

    @Test
    void sendsTrimmedQuestionToGemini() {
        given(provider.getIfAvailable()).willReturn(assistant);
        given(assistant.chat("s1", "What is AI?")).willReturn("Machines that learn.");

        assertThat(service.isEnabled()).isTrue();
        assertThat(service.ask("s1", "  What is AI?  ")).isEqualTo("Machines that learn.");
    }

    @Test
    void failsClearlyWhenNoKeyConfigured() {
        given(provider.getIfAvailable()).willReturn(null);

        assertThat(service.isEnabled()).isFalse();
        assertThatThrownBy(() -> service.ask("s1", "hi"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("GEMINI_API_KEY");
    }

    @Test
    void rejectsBlankQuestion() {
        given(provider.getIfAvailable()).willReturn(assistant);

        assertThatThrownBy(() -> service.ask("s1", "   ")).isInstanceOf(IllegalArgumentException.class);
        verifyNoInteractions(assistant);
    }

    @Test
    void wrapsGeminiFailuresInReadableMessage() {
        given(provider.getIfAvailable()).willReturn(assistant);
        given(assistant.chat("s1", "hi")).willThrow(new RuntimeException("API key not valid"));

        assertThatThrownBy(() -> service.ask("s1", "hi"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Gemini request failed: API key not valid");
    }
}

package com.election.solution.ai;

import dev.langchain4j.service.SystemMessage;

/**
 * LangChain4j AI service. The implementation is generated at runtime by
 * {@link dev.langchain4j.service.AiServices}, which wires in the chat model and tools.
 */
public interface ElectionAssistant {

    @SystemMessage("""
            You are a neutral, helpful assistant for the Election Solution application.
            Answer questions about the election using the available tools for facts
            such as candidates and the polling schedule. Never invent election data,
            and do not express support for any candidate or party.
            """)
    String chat(String question);
}

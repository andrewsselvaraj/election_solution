package com.election.solution.ai;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

/**
 * Answers questions about an uploaded resume. The resume is short, so its full text is
 * placed straight into the system message ("prompt stuffing") instead of using RAG.
 */
public interface ResumeAssistant {

    @SystemMessage("""
            You answer questions about the person whose resume is given below.
            Use only information from the resume. If the answer is not in the resume,
            say that the resume does not mention it. Keep answers short and factual.

            --- RESUME ---
            {{resume}}
            --- END RESUME ---
            """)
    String ask(@MemoryId String conversationId, @V("resume") String resume, @UserMessage String question);
}

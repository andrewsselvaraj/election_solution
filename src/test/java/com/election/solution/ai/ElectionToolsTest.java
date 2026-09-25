package com.election.solution.ai;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ElectionToolsTest {

    private final ElectionTools tools = new ElectionTools();

    @Test
    void filtersCandidatesByConstituencyIgnoringCase() {
        assertThat(tools.candidatesInConstituency(" north "))
                .extracting(ElectionTools.Candidate::name)
                .containsExactly("Asha Raman", "Daniel Joseph");
    }

    @Test
    void listsDistinctConstituencies() {
        assertThat(tools.constituencies()).containsExactly("North", "South");
    }
}

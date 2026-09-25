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

    @Test
    void returnsPollingBoothForConstituencyIgnoringCase() {
        assertThat(tools.pollingBooth(" South "))
                .isEqualTo("Community Hall, 45 Temple Street, South Ward");
    }

    @Test
    void explainsWhenConstituencyHasNoBooth() {
        assertThat(tools.pollingBooth("East"))
                .contains("No polling booth found for 'East'")
                .contains("North", "South");
    }
}

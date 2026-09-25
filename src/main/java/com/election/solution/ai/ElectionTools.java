package com.election.solution.ai;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Tools the LLM can call to look up election facts. Sample in-memory data;
 * replace with a repository once the domain model exists.
 */
@Component
public class ElectionTools {

    public record Candidate(String name, String party, String constituency) {
    }

    private static final List<Candidate> CANDIDATES = List.of(
            new Candidate("Asha Raman", "Green Future Party", "North"),
            new Candidate("Daniel Joseph", "People's Alliance", "North"),
            new Candidate("Meera Iyer", "Civic Reform Party", "South"),
            new Candidate("Karthik Das", "People's Alliance", "South"));

    @Tool("Lists the candidates standing in the given constituency (e.g. North, South)")
    public List<Candidate> candidatesInConstituency(@P("constituency name") String constituency) {
        return CANDIDATES.stream()
                .filter(c -> c.constituency().equalsIgnoreCase(constituency.trim()))
                .toList();
    }

    @Tool("Lists every constituency in the election")
    public List<String> constituencies() {
        return CANDIDATES.stream().map(Candidate::constituency).distinct().toList();
    }

    @Tool("Returns the polling date and voting hours")
    public String pollingSchedule() {
        return "Polling day is 2026-11-03; booths are open from 07:00 to 18:00 local time.";
    }
}

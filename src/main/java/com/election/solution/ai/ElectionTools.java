package com.election.solution.ai;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Tools the LLM can call to look up election facts. Sample in-memory data;
 * replace with a repository once the domain model exists.
 */
@Component
public class ElectionTools {

    public record Candidate(String name, String party, String constituency) {
    }

    public record PollingBooth(String constituency, String name, String address) {
    }

    private static final List<Candidate> CANDIDATES = List.of(
            new Candidate("Asha Raman", "Green Future Party", "North"),
            new Candidate("Daniel Joseph", "People's Alliance", "North"),
            new Candidate("Meera Iyer", "Civic Reform Party", "South"),
            new Candidate("Karthik Das", "People's Alliance", "South"));

    private static final Map<String, PollingBooth> BOOTHS = Map.of(
            "north", new PollingBooth("North", "Government Higher Secondary School", "12 Lake Road, North Ward"),
            "south", new PollingBooth("South", "Community Hall", "45 Temple Street, South Ward"));

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

    @Tool("Returns the polling booth name and address for a constituency. "
            + "Use it when the user asks where to vote.")
    public String pollingBooth(@P("constituency name, e.g. North or South") String constituency) {
        PollingBooth booth = BOOTHS.get(constituency.trim().toLowerCase());
        if (booth == null) {
            return "No polling booth found for '" + constituency + "'. Known constituencies: " + constituencies();
        }
        return booth.name() + ", " + booth.address();
    }
}

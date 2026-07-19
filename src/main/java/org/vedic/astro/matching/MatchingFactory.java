package org.vedic.astro.matching;

import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class MatchingFactory {

    private final Map<MatchingType, MatchingEngine> engines;

    public MatchingFactory(List<MatchingEngine> engineList) {
        this.engines = engineList.stream()
                .collect(Collectors.toMap(MatchingEngine::getType, Function.identity()));
    }

    public MatchingEngine getEngine(MatchingType type) {
        return Optional.ofNullable(engines.get(type))
                .orElseThrow(() -> new IllegalArgumentException("Unsupported matching system: " + type));
    }
}

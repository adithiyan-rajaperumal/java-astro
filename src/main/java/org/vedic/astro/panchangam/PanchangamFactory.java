package org.vedic.astro.panchangam;

import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class PanchangamFactory {

    private final Map<PanchangamType, PanchangamEngine> engines;

    public PanchangamFactory(List<PanchangamEngine> engineList) {
        this.engines = engineList.stream()
                .collect(Collectors.toMap(PanchangamEngine::getType, Function.identity()));
    }

    public PanchangamEngine getEngine(PanchangamType type) {
        if (type == null) return engines.get(PanchangamType.DRIK_TIRUKANITHAM);
        return Optional.ofNullable(engines.get(type))
                .orElseGet(() -> engines.get(PanchangamType.DRIK_TIRUKANITHAM));
    }
}

package nl.kabulter.threekids.application;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Stream;

/**
 * The people with a partner and three children who all have that partner as parent, sorted by the day their youngest
 * child turns 18 (ADR 0010). Who matches today is a range query, so the age is never stored as an outcome (ADR 0006).
 * Not thread-safe: the caller serializes access.
 */
class MatchCandidates {

    private final Map<Long, LocalDate> minorUntilById = new HashMap<>();
    private final NavigableMap<LocalDate, Set<Long>> byMinorUntil = new TreeMap<>();

    /** Records whether this person is a candidate, and until when it has a child under 18. */
    void update(long id, Optional<LocalDate> minorUntil) {
        LocalDate previous = minorUntilById.remove(id);
        if (previous != null) {
            Set<Long> candidates = byMinorUntil.get(previous);
            candidates.remove(id);
            if (candidates.isEmpty()) {
                byMinorUntil.remove(previous);
            }
        }
        minorUntil.ifPresent(until -> {
            minorUntilById.put(id, until);
            byMinorUntil.computeIfAbsent(until, unused -> new HashSet<>()).add(id);
        });
    }

    /** The candidates who still have a child under 18 on this day. */
    Stream<Long> matchingOn(LocalDate today) {
        return byMinorUntil.tailMap(today, false).values().stream().flatMap(Set::stream);
    }
}

package org.trivait.minigamesmod.leaderboard;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LeaderboardCache {

    public enum State { IDLE, LOADING, READY, ERROR }

    private static final long TTL_MS = 60_000;

    private static class Entry {
        List<LeaderboardEntry> data = Collections.emptyList();
        State state = State.IDLE;
        long fetchedAt = 0;
        String error = null;
    }

    private final Map<String, Entry> cache = new HashMap<>();

    private Entry entry(String game) {
        return cache.computeIfAbsent(game, g -> new Entry());
    }

    public State getState(String game) {
        return entry(game).state;
    }

    public List<LeaderboardEntry> getData(String game) {
        return entry(game).data;
    }

    public String getError(String game) {
        return entry(game).error;
    }

    public boolean needsRefresh(String game) {
        Entry e = entry(game);
        return e.state != State.LOADING
                && (e.state == State.IDLE || System.currentTimeMillis() - e.fetchedAt > TTL_MS);
    }

    public void refreshIfNeeded(String game, Runnable onDone) {
        Entry e = entry(game);

        if (!needsRefresh(game)) {
            onDone.run();
            return;
        }

        e.state = State.LOADING;

        SheetsApi.fetchAsync(game).whenComplete((data, err) -> {
            if (err != null) {
                e.state = State.ERROR;
                e.error = err.getCause() != null
                        ? err.getCause().getMessage()
                        : err.getMessage();
            } else {
                e.data = data;
                e.state = State.READY;
                e.fetchedAt = System.currentTimeMillis();
                e.error = null;
            }

            onDone.run();
        });
    }

    public void invalidate(String game) {
        Entry e = entry(game);
        e.fetchedAt = 0;
        e.state = State.IDLE;
    }

    public void invalidateAll() {
        cache.values().forEach(e -> {
            e.fetchedAt = 0;
            e.state = State.IDLE;
        });
    }
}
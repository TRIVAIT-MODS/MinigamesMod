package org.trivait.minigamesmod.minigame.minesweeper.leaderboard;

import org.trivait.minigamesmod.minigame.minesweeper.game.GameMode;

import java.util.Collections;
import java.util.EnumMap;
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

    private static final GameMode[] MODES = { GameMode.LEADERBOARD_TIME, GameMode.LEADERBOARD_WIN_COUNT };

    private final Map<GameMode, Entry> cache = new EnumMap<>(GameMode.class);

    public LeaderboardCache() {
        for (GameMode m : MODES) cache.put(m, new Entry());
    }

    public State getState(GameMode mode) {
        Entry e = cache.get(mode);
        return e != null ? e.state : State.ERROR;
    }

    public List<LeaderboardEntry> getData(GameMode mode) {
        Entry e = cache.get(mode);
        return e != null ? e.data : Collections.emptyList();
    }

    public String getError(GameMode mode) {
        Entry e = cache.get(mode);
        return e != null ? e.error : "unsupported mode";
    }

    public boolean needsRefresh(GameMode mode) {
        Entry e = cache.get(mode);
        if (e == null) return false;
        return e.state != State.LOADING
            && (e.state == State.IDLE || System.currentTimeMillis() - e.fetchedAt > TTL_MS);
    }

    public void refreshIfNeeded(GameMode mode, Runnable onDone) {
        Entry e = cache.get(mode);
        if (e == null) { onDone.run(); return; }
        if (!needsRefresh(mode)) { onDone.run(); return; }
        e.state = State.LOADING;
        SheetsApi.fetchAsync(mode).whenComplete((data, err) -> {
            if (err != null) {
                e.state = State.ERROR;
                e.error = err.getCause() != null ? err.getCause().getMessage() : err.getMessage();
            } else {
                e.data = data;
                e.state = State.READY;
                e.fetchedAt = System.currentTimeMillis();
                e.error = null;
            }
            onDone.run();
        });
    }

    public void invalidate(GameMode mode) {
        Entry e = cache.get(mode);
        if (e == null) return;
        e.fetchedAt = 0;
        e.state = State.IDLE;
    }
}

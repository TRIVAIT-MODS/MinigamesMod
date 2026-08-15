package org.trivait.minigamesmod.leaderboard;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class SheetsApi {
    public static final String SCOREBOARD_API_VERSION = "1.1";
    public static volatile boolean leaderboardsEnabled = true;
    private static final String CSV_BASE = "https://docs.google.com/spreadsheets/d/" + LeaderboardLink.SPREADSHEET_ID + "/gviz/tq?tqx=out:csv&sheet=";
    private static final String LEADERBOARDS_ENABLED_URL = "https://docs.google.com/spreadsheets/d/" + LeaderboardLink.SPREADSHEET_ID + "/gviz/tq?tqx=out:csv&sheet=DIno&range=P35";
    private static final long LEADERBOARDS_UPDATE_INTERVAL = 1L;
    private static String writeWebAppUrl = LeaderboardLink.link;
    private static volatile String scriptVersion = null;
    private static final HttpClient HTTP = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).followRedirects(HttpClient.Redirect.NORMAL).build();
    private static final Gson GSON = new Gson();
    private static final ScheduledExecutorService LEADERBOARDS_EXECUTOR = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread thread = new Thread(r, "SheetsApi-Leaderboards");
        thread.setDaemon(true);
        return thread;
    });

    static {
        LEADERBOARDS_EXECUTOR.scheduleAtFixedRate(SheetsApi::updateLeaderboardsEnabled, 0L, LEADERBOARDS_UPDATE_INTERVAL, TimeUnit.MINUTES);
    }

    public static void setWriteUrl(String url) {
        writeWebAppUrl = url;
    }

    public static boolean canWrite() {
        return writeWebAppUrl != null && !writeWebAppUrl.isBlank();
    }

    public static String getScriptVersion() {
        return scriptVersion;
    }

    public static boolean isVersionMismatch() {
        if (scriptVersion == null) {
            return false;
        }
        String[] apiParts = SCOREBOARD_API_VERSION.split("\\.");
        String[] scriptParts = scriptVersion.split("\\.");
        if (apiParts.length == 0 || scriptParts.length == 0) {
            return false;
        }
        return !apiParts[0].equals(scriptParts[0]);
    }

    public static CompletableFuture<Void> submitAsync(String game, String name, int value, boolean rewrite) {
        return CompletableFuture.runAsync(() -> submit(game, name, value, rewrite));
    }

    public static CompletableFuture<List<LeaderboardEntry>> fetchAsync(String game) {
        return CompletableFuture.supplyAsync(() -> fetch(game));
    }

    public static List<LeaderboardEntry> fetch(String game) {
        String url = CSV_BASE + game;
        try {
            HttpRequest req = HttpRequest.newBuilder(URI.create(url)).GET().timeout(Duration.ofSeconds(8)).build();
            HttpResponse<String> resp = HTTP.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() != 200) {
                throw new RuntimeException("HTTP " + resp.statusCode());
            }
            List<LeaderboardEntry> result = new ArrayList<>();
            String[] lines = resp.body().split("\\R");
            for (int i = 1; i < lines.length; i++) {
                String[] cols = splitCsvLine(lines[i]);
                if (cols.length < 2) {
                    continue;
                }
                String name = cols[0].trim();
                if (name.isEmpty()) {
                    continue;
                }
                try {
                    int value = Integer.parseInt(cols[1].trim());
                    result.add(new LeaderboardEntry(name, value));
                } catch (NumberFormatException ignored) {
                }
            }
            return result;
        } catch (Exception e) {
            throw new RuntimeException("Fetch failed", e);
        }
    }

    public static void updateLeaderboardsEnabled() {
        try {
            HttpRequest req = HttpRequest.newBuilder(URI.create(LEADERBOARDS_ENABLED_URL)).GET().timeout(Duration.ofSeconds(8)).build();
            HttpResponse<String> resp = HTTP.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() != 200 || resp.body() == null || resp.body().trim().isEmpty()) {
                return;
            }
            String value = extractBoolean(resp.body());
            if (value != null) {
                leaderboardsEnabled = !Boolean.parseBoolean(value);
            }
        } catch (Exception ignored) {
        }
    }

    private static String extractBoolean(String body) {
        String[] lines = body.split("\\R");
        for (String line : lines) {
            String[] fields = splitCsvLine(line);
            for (String field : fields) {
                String candidate = field.replace("\"", "").trim();
                if (candidate.equalsIgnoreCase("true")) {
                    return "true";
                }
                if (candidate.equalsIgnoreCase("false")) {
                    return "false";
                }
            }
        }
        return null;
    }

    public static CompletableFuture<Void> fetchScriptVersionAsync() {
        return CompletableFuture.runAsync(() -> {
            if (!canWrite()) {
                return;
            }
            try {
                String url = writeWebAppUrl;
                for (int redirects = 0; redirects < 5; redirects++) {
                    HttpRequest req = HttpRequest.newBuilder(URI.create(url)).GET().timeout(Duration.ofSeconds(5)).build();
                    HttpResponse<String> resp = HTTP.send(req, HttpResponse.BodyHandlers.ofString());
                    int status = resp.statusCode();
                    if (status == 301 || status == 302 || status == 303 || status == 307 || status == 308) {
                        url = resp.headers().firstValue("location").orElseThrow(() -> new RuntimeException("Redirect with no Location header"));
                    } else {
                        JsonObject json = GSON.fromJson(resp.body(), JsonObject.class);
                        scriptVersion = json.has("version") ? json.get("version").getAsString() : null;
                        return;
                    }
                }
            } catch (Exception e) {
                scriptVersion = null;
            }
        });
    }

    public static void submit(String game, String name, int value, boolean rewrite) {
        if (!canWrite()) {
            throw new IllegalStateException("writeWebAppUrl not set");
        }
        JsonObject body = new JsonObject();
        body.addProperty("game", game);
        body.addProperty("name", name);
        body.addProperty("value", value);
        body.addProperty("rewrite", rewrite);
        String json = GSON.toJson(body);
        try {
            String url = writeWebAppUrl;
            for (int redirects = 0; redirects < 5; redirects++) {
                HttpRequest req = HttpRequest.newBuilder(URI.create(url)).POST(HttpRequest.BodyPublishers.ofString(json)).header("Content-Type", "application/json").timeout(Duration.ofSeconds(8)).build();
                HttpResponse<String> resp = HTTP.send(req, HttpResponse.BodyHandlers.ofString());
                int status = resp.statusCode();
                if (status == 301 || status == 302 || status == 303 || status == 307 || status == 308) {
                    url = resp.headers().firstValue("location").orElseThrow(() -> new RuntimeException("Redirect with no Location header"));
                } else {
                    return;
                }
            }
            throw new RuntimeException("Too many redirects");
        } catch (Exception e) {
            throw new RuntimeException("Submit failed", e);
        }
    }

    private static String[] splitCsvLine(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    sb.append('"');
                    i++;
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (c == ',' && !inQuotes) {
                fields.add(sb.toString());
                sb.setLength(0);
            } else {
                sb.append(c);
            }
        }
        fields.add(sb.toString());
        return fields.toArray(new String[0]);
    }
}
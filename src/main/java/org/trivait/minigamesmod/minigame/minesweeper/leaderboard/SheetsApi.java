package org.trivait.minigamesmod.minigame.minesweeper.leaderboard;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.trivait.minigamesmod.minigame.minesweeper.game.GameMode;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class SheetsApi {

    public static final String SCOREBOARD_API_VERSION = "1.1";

    private static final String SPREADSHEET_ID = "1MontuwLcsr7T9EygHmWJ1ziVLi7oAywvdNOb6bmj7KM";
    private static final String CSV_BASE =
            "https://docs.google.com/spreadsheets/d/" + SPREADSHEET_ID + "/gviz/tq?tqx=out:csv&sheet=";

    private static String writeWebAppUrl = LeaderboardLink.link;
    private static String scriptVersion = null;

    private static final HttpClient HTTP = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .followRedirects(HttpClient.Redirect.NEVER)
            .build();
    private static final Gson GSON = new Gson();


    public static void setWriteUrl(String url) { writeWebAppUrl = url; }
    public static boolean canWrite() { return writeWebAppUrl != null && !writeWebAppUrl.isBlank(); }
    public static String getScriptVersion() { return scriptVersion; }
    public static boolean isVersionMismatch() {
        if (scriptVersion == null) return false;
        String[] apiParts = SCOREBOARD_API_VERSION.split("\\.");
        String[] scriptParts = scriptVersion.split("\\.");
        if (apiParts.length == 0 || scriptParts.length == 0) return false;
        return !apiParts[0].equals(scriptParts[0]);
    }
    public static String sheetName(GameMode mode) {
        return switch (mode) {
            case LEADERBOARD_TIME      -> "Time";
            case LEADERBOARD_WIN_COUNT -> "Score";
            default -> throw new IllegalArgumentException("GameMode " + mode + " has no leaderboard sheet");
        };
    }

    public static CompletableFuture<List<LeaderboardEntry>> fetchAsync(GameMode mode, String categoryFilter) {
        return CompletableFuture.supplyAsync(() -> fetch(mode, categoryFilter));
    }

    public static CompletableFuture<List<LeaderboardEntry>> fetchAsync(GameMode mode) {
        return fetchAsync(mode, null);
    }

    public static List<LeaderboardEntry> fetch(GameMode mode, String categoryFilter) {
        String url = CSV_BASE + sheetName(mode);
        try {
            HttpRequest req = HttpRequest.newBuilder(URI.create(url))
                    .GET().timeout(Duration.ofSeconds(8)).build();
            HttpResponse<String> resp = HTTP.send(req, HttpResponse.BodyHandlers.ofString());
            return parseAndAggregate(resp.body(), categoryFilter, mode);
        } catch (Exception e) {
            throw new RuntimeException("Fetch failed: " + e.getMessage(), e);
        }
    }

    public static CompletableFuture<Void> submitAsync(GameMode mode, String name, String value, BoardCategory category) {
        return CompletableFuture.runAsync(() -> submit(mode, name, value, category));
    }

    public static CompletableFuture<Void> submitTimeAsync(String name, double seconds, BoardCategory category) {
        String value = String.format(java.util.Locale.US, "%.2f", seconds);
        return submitAsync(GameMode.LEADERBOARD_TIME, name, value, category);
    }


    public static CompletableFuture<Void> submitScoreAsync(String name, int wins, BoardCategory category) {
        return submitAsync(GameMode.LEADERBOARD_WIN_COUNT, name, String.valueOf(wins), category);
    }

    public static CompletableFuture<Void> fetchScriptVersionAsync() {
        return CompletableFuture.runAsync(() -> {
            if (!canWrite()) return;
            try {
                String url = writeWebAppUrl + "?action=version";
                for (int redirects = 0; redirects < 5; redirects++) {
                    HttpRequest req = HttpRequest.newBuilder(URI.create(url))
                            .GET().timeout(Duration.ofSeconds(5)).build();
                    HttpResponse<String> resp = HTTP.send(req, HttpResponse.BodyHandlers.ofString());
                    int status = resp.statusCode();
                    if (status == 301 || status == 302 || status == 303 || status == 307 || status == 308) {
                        url = resp.headers().firstValue("location")
                                .orElseThrow(() -> new RuntimeException("Redirect with no Location header"));
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

    public static void submit(GameMode mode, String name, String value, BoardCategory category) {
        if (!canWrite())
            throw new IllegalStateException("writeWebAppUrl not set");

        JsonObject body = new JsonObject();
        body.addProperty("sheet", sheetName(mode));
        body.addProperty("name", name);
        body.addProperty("value", value);
        body.addProperty("category", category.label);
        body.addProperty("apiVersion", SCOREBOARD_API_VERSION);
        String json = GSON.toJson(body);

        try {
            String url = writeWebAppUrl;
            for (int redirects = 0; redirects < 5; redirects++) {
                HttpRequest req = HttpRequest.newBuilder(URI.create(url))
                        .POST(HttpRequest.BodyPublishers.ofString(json))
                        .header("Content-Type", "application/json")
                        .timeout(Duration.ofSeconds(8))
                        .build();
                HttpResponse<String> resp = HTTP.send(req, HttpResponse.BodyHandlers.ofString());
                int status = resp.statusCode();
                if (status == 301 || status == 302 || status == 303 || status == 307 || status == 308) {
                    url = resp.headers().firstValue("location")
                            .orElseThrow(() -> new RuntimeException("Redirect with no Location header"));
                } else {
                    return;
                }
            }
            throw new RuntimeException("Too many redirects");
        } catch (Exception e) {
            throw new RuntimeException("Submit failed: " + e.getMessage(), e);
        }
    }

    private static List<LeaderboardEntry> parseAndAggregate(String csv, String categoryFilter, GameMode mode) {
        java.util.LinkedHashMap<String, double[]> agg  = new java.util.LinkedHashMap<>();
        java.util.LinkedHashMap<String, String[]> meta = new java.util.LinkedHashMap<>();

        String[] lines = csv.split("\r?\n");
        for (int i = 1; i < lines.length; i++) {
            String[] cols = splitCsvLine(lines[i]);
            if (cols.length < 2) continue;
            String name     = cols[0].trim().replaceAll("[\\p{Cf}\\p{Co}\\p{Cn}]", "");
            String value    = cols[1].trim().replaceAll("[\\p{Cf}\\p{Co}\\p{Cn}]", "");
            String category = cols.length >= 3 ? cols[2].trim().replaceAll("[\\p{Cf}\\p{Co}\\p{Cn}]", "") : "";
            if (name.isEmpty()) continue;
            if (categoryFilter != null && !categoryFilter.equalsIgnoreCase(category)) continue;

            double numeric = parseSeconds(value);
            if (Double.isInfinite(numeric)) continue;
            String key = name.toLowerCase() + "\0" + category.toLowerCase();

            if (mode == GameMode.LEADERBOARD_TIME) {
                agg.merge(key, new double[]{numeric}, (a, b) -> new double[]{Math.min(a[0], b[0])});
            } else {
                agg.merge(key, new double[]{numeric}, (a, b) -> new double[]{a[0] + b[0]});
            }
            meta.putIfAbsent(key, new String[]{name, category});
        }

        List<String> catOrder = List.of("8x8", "16x16", "26x18");
        List<LeaderboardEntry> result = new ArrayList<>();
        for (var entry : agg.entrySet()) {
            String[] m = meta.get(entry.getKey());
            double v = entry.getValue()[0];
            String display;
            if (mode == GameMode.LEADERBOARD_TIME) {
                display = String.format("%.2f", v);
            } else {
                display = String.valueOf((int) v);
            }
            result.add(new LeaderboardEntry(m[0], display, m[1], v));
        }

        result.sort((a, b) -> {
            int ca = catOrder.indexOf(a.category()); if (ca == -1) ca = 999;
            int cb = catOrder.indexOf(b.category()); if (cb == -1) cb = 999;
            if (ca != cb) return Integer.compare(ca, cb);
            return mode == GameMode.LEADERBOARD_TIME
                ? Double.compare(a.numericValue(), b.numericValue())
                : Double.compare(b.numericValue(), a.numericValue());
        });

        return result;
    }

    private static double parseSeconds(String value) {
        if (value == null || value.isBlank()) return Double.POSITIVE_INFINITY;
        try {
            return Double.parseDouble(value.trim().replace(',', '.'));
        } catch (NumberFormatException e) {
            return Double.POSITIVE_INFINITY;
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
                    sb.append('"'); i++;
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (c == ',' && !inQuotes) {
                fields.add(sb.toString()); sb.setLength(0);
            } else {
                sb.append(c);
            }
        }
        fields.add(sb.toString());
        return fields.toArray(new String[0]);
    }
}

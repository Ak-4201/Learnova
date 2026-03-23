package com.learnova.b2c;

import java.net.URI;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Normalizes YouTube URLs for B2C playback (embed / iframe) and derives thumbnails from the primary video id.
 */
public final class YoutubeUrlUtil {

    private static final Pattern WATCH_V = Pattern.compile("[?&]v=([a-zA-Z0-9_-]{11})");
    private static final Pattern SHORT = Pattern.compile("youtu\\.be/([a-zA-Z0-9_-]{11})");

    private YoutubeUrlUtil() {
    }

    /**
     * Extracts the 11-character video id from a watch URL, short URL, or embed URL.
     */
    public static String extractVideoId(String url) {
        if (url == null || url.isBlank()) {
            return null;
        }
        String trimmed = url.trim();
        Matcher m = WATCH_V.matcher(trimmed);
        if (m.find()) {
            return m.group(1);
        }
        m = SHORT.matcher(trimmed);
        if (m.find()) {
            return m.group(1);
        }
        try {
            URI uri = URI.create(trimmed);
            String path = uri.getPath();
            if (path != null && path.contains("/embed/")) {
                String[] parts = path.split("/embed/");
                if (parts.length > 1) {
                    String id = parts[1].replaceAll("[?#].*", "");
                    if (id.length() >= 11) {
                        return id.substring(0, 11);
                    }
                }
            }
        } catch (Exception ignored) {
            // fall through
        }
        return null;
    }

    /**
     * YouTube CDN thumbnail for course cards (medium quality).
     */
    public static String toMqDefaultThumbnail(String watchOrAnyYoutubeUrl) {
        String id = extractVideoId(watchOrAnyYoutubeUrl);
        return id != null ? "https://img.youtube.com/vi/" + id + "/mqdefault.jpg" : null;
    }
}

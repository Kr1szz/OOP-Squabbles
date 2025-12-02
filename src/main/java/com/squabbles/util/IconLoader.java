package com.squabbles.util;

import javafx.scene.image.Image;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility class for loading and caching icon images.
 * This class implements the Singleton pattern and uses lazy loading with
 * caching.
 */
public class IconLoader {
    private static IconLoader instance;
    private final Map<Integer, Image> iconCache;

    // Brand‑new colorful emoji set for all 57 math icons
    // Index 0 is unused; IDs 1‑57 map directly into this array.
    private static final String[] EMOJI_FALLBACKS = {
            "?", // 0 (unused)
            // 1‑10
            "🔴", "🟠", "🟡", "🟢", "🔵", "🟣", "🟤", "⚪", "⚫", "🟥",
            // 11‑20
            "🟧", "🟨", "🟩", "🟦", "🟪", "⬛", "⬜", "🔺", "🔻", "🔷",
            // 21‑30
            "🔶", "🔸", "🔹", "⭐", "🌟", "✨", "💫", "💥", "🔥", "⚡",
            // 31‑40
            "🌈", "☄️", "🌙", "☀️", "🌎", "🌍", "🌏", "🪐", "🌌", "🌠",
            // 41‑50
            "🎯", "🎲", "🧩", "🎮", "🎵", "🎶", "🎧", "📀", "💎", "🔑",
            // 51‑57
            "💠", "🔰", "🌀", "♠️", "♥️", "♦️", "♣️"
    };

    private IconLoader() {
        iconCache = new HashMap<>();
    }

    /**
     * Gets the singleton instance of IconLoader.
     */
    public static synchronized IconLoader getInstance() {
        if (instance == null) {
            instance = new IconLoader();
        }
        return instance;
    }

    /**
     * Loads an icon image by ID.
     *
     * For this customized build we ignore PNG files completely so that
     * ALL visuals come from the new emoji set defined above.
     *
     * @param iconId The ID of the icon to load (1-57)
     * @return Always null, so callers fall back to emoji rendering.
     */
    public Image loadIcon(int iconId) {
        // We no longer use PNG resources; rely entirely on emoji fallbacks.
        return null;
    }

    /**
     * Gets the emoji fallback for a given icon ID.
     * 
     * @param iconId The ID of the icon
     * @return The emoji string
     */
    public static String getEmojiFallback(int iconId) {
        if (iconId > 0 && iconId < EMOJI_FALLBACKS.length) {
            return EMOJI_FALLBACKS[iconId];
        }
        return "?";
    }

    /**
     * Checks if an icon image file exists for the given ID.
     * 
     * @param iconId The ID to check
     * @return true if the icon file exists, false otherwise
     */
    public boolean hasIconFile(int iconId) {
        String iconPath = String.format("/icons/icon_%02d.png", iconId);
        InputStream stream = getClass().getResourceAsStream(iconPath);
        if (stream != null) {
            try {
                stream.close();
            } catch (Exception e) {
                // Ignore
            }
            return true;
        }
        return false;
    }

    /**
     * Preloads all available icons into the cache.
     * This can be called during application startup for better performance.
     */
    public void preloadIcons() {
        for (int i = 1; i <= 57; i++) {
            loadIcon(i);
        }
    }

    /**
     * Clears the icon cache to free memory.
     */
    public void clearCache() {
        iconCache.clear();
    }
}

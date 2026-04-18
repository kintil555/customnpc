package com.npcmod.client.entity;

import com.npcmod.NpcMod;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.util.Identifier;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public final class NpcSkinManager {

    private static final Identifier STEVE =
            Identifier.of("minecraft", "textures/entity/player/wide/steve.png");

    private static final Map<String, Identifier> CACHE = new ConcurrentHashMap<>();
    private static final Map<String, Boolean> LOADING = new ConcurrentHashMap<>();

    public static Identifier getSkin(String key) {
        if (key == null || key.isBlank()) return STEVE;
        Identifier cached = CACHE.get(key);
        if (cached != null) return cached;
        if (!LOADING.containsKey(key)) {
            LOADING.put(key, true);
            if (key.startsWith("http://") || key.startsWith("https://")) {
                scheduleUrlLoad(key, key);
            } else {
                scheduleMojangLookup(key);
            }
        }
        return STEVE;
    }

    private static void scheduleMojangLookup(String username) {
        CompletableFuture.runAsync(() -> {
            try {
                String uuidUrl = "https://api.mojang.com/users/profiles/minecraft/" + username;
                String uuidJson = fetchString(uuidUrl);
                if (uuidJson == null || !uuidJson.contains("\"id\"")) return;
                String uuid = extractJsonField(uuidJson, "id");
                if (uuid == null) return;
                String profileUrl = "https://sessionserver.mojang.com/session/minecraft/profile/"
                        + uuid + "?unsigned=false";
                String profileJson = fetchString(profileUrl);
                if (profileJson == null) return;
                String encoded = extractJsonField(profileJson, "value");
                if (encoded == null) return;
                String decoded = new String(java.util.Base64.getDecoder().decode(encoded));
                String textureUrl = extractNestedUrl(decoded);
                if (textureUrl != null) {
                    scheduleUrlLoad(username, textureUrl);
                }
            } catch (Exception e) {
                NpcMod.LOGGER.warn("Failed Mojang skin lookup for '{}': {}", username, e.getMessage());
                LOADING.remove(username);
            }
        });
    }

    private static void scheduleUrlLoad(String cacheKey, String url) {
        CompletableFuture.runAsync(() -> {
            try {
                HttpURLConnection conn = (HttpURLConnection) URI.create(url).toURL().openConnection();
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);
                conn.setRequestProperty("User-Agent", "NpcPlayerMod/1.0");
                try (InputStream in = conn.getInputStream()) {
                    NativeImage image = NativeImage.read(in);
                    String safe = cacheKey.replaceAll("[^a-zA-Z0-9_.-]", "_");
                    Identifier texId = Identifier.of(NpcMod.MOD_ID, "skins/" + safe);
                    MinecraftClient client = MinecraftClient.getInstance();
                    client.execute(() -> {
                        client.getTextureManager().registerTexture(
                                texId, new NativeImageBackedTexture(image));
                        CACHE.put(cacheKey, texId);
                    });
                }
            } catch (Exception e) {
                NpcMod.LOGGER.warn("Failed skin texture load for '{}': {}", cacheKey, e.getMessage());
                LOADING.remove(cacheKey);
            }
        });
    }

    private static String fetchString(String url) {
        try {
            HttpURLConnection conn = (HttpURLConnection) URI.create(url).toURL().openConnection();
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);
            conn.setRequestProperty("User-Agent", "NpcPlayerMod/1.0");
            try (InputStream in = conn.getInputStream()) {
                return new String(in.readAllBytes());
            }
        } catch (Exception e) {
            return null;
        }
    }

    private static String extractJsonField(String json, String field) {
        String search = "\"" + field + "\":\"";
        int start = json.indexOf(search);
        if (start < 0) return null;
        start += search.length();
        int end = json.indexOf("\"", start);
        if (end < 0) return null;
        return json.substring(start, end);
    }

    private static String extractNestedUrl(String decodedTexturesJson) {
        String search = "\"url\":\"";
        int start = decodedTexturesJson.indexOf(search);
        if (start < 0) return null;
        start += search.length();
        int end = decodedTexturesJson.indexOf("\"", start);
        if (end < 0) return null;
        return decodedTexturesJson.substring(start, end);
    }

    private NpcSkinManager() {}
}
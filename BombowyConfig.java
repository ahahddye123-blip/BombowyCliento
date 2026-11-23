package dev.bombowy.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import net.fabricmc.loader.api.FabricLoader;

public class BombowyConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private final Path filePath;

    public BombowyConfig() {
        this.filePath = FabricLoader.getInstance().getConfigDir().resolve("bombowyclient-v4.json");
    }

    public void load(Modules modules) {
        if (!Files.exists(filePath)) {
            save(modules);
            return;
        }
        try (Reader reader = Files.newBufferedReader(filePath, StandardCharsets.UTF_8)) {
            Map<String, SavedEntry> data = GSON.fromJson(reader, new TypeToken<Map<String, SavedEntry>>() {}.getType());
            if (data == null) return;
            for (Modules.ModuleEntry m : modules.getAll()) {
                SavedEntry s = data.get(m.id);
                if (s != null) {
                    m.enabled = s.enabled;
                    m.favorite = s.favorite;
                    for (Modules.SettingEntry setting : m.settings) {
                        if (s.values.containsKey(setting.key)) {
                            Object raw = s.values.get(setting.key);
                            if (setting.isHex && raw != null) {
                                setting.hexValue = String.valueOf(raw);
                            } else if (!setting.isHex && raw instanceof Boolean) {
                                setting.boolValue = (Boolean) raw;
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            BombowyClient.LOGGER.warn("Nie udało się wczytać konfiguracji: {}", e.getMessage());
        }
    }

    public void save(Modules modules) {
        Map<String, SavedEntry> out = new LinkedHashMap<>();
        for (Modules.ModuleEntry m : modules.getAll()) {
            SavedEntry entry = new SavedEntry();
            entry.enabled = m.enabled;
            entry.favorite = m.favorite;
            for (Modules.SettingEntry setting : m.settings) {
                entry.values.put(setting.key, setting.isHex ? setting.hexValue : setting.boolValue);
            }
            out.put(m.id, entry);
        }
        try {
            Files.createDirectories(filePath.getParent());
            try (Writer writer = Files.newBufferedWriter(filePath, StandardCharsets.UTF_8)) {
                GSON.toJson(out, writer);
            }
        } catch (IOException e) {
            BombowyClient.LOGGER.warn("Nie udało się zapisać konfiguracji: {}", e.getMessage());
        }
    }

    private static class SavedEntry {
        boolean enabled;
        boolean favorite;
        Map<String, Object> values = new LinkedHashMap<>();
    }
}

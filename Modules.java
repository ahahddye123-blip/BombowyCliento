package dev.bombowy.client;

import java.util.*;
import java.util.stream.Collectors;

public class Modules {
    public enum Category {
        ALL("Wszystkie"),
        ENABLED("Włączone"),
        COMBAT("Walka"),
        VISUAL("Wygląd"),
        HUD("HUD"),
        SOUND("Dźwięki"),
        TOOLS("Narzędzia");

        public final String label;
        Category(String label) { this.label = label; }
    }

    public static class SettingEntry {
        public final String key;
        public final String name;
        public final String description;
        public final boolean isHex;
        public boolean boolValue;
        public String hexValue;

        public SettingEntry(String key, String name, String description, boolean defaultBool) {
            this.key = key;
            this.name = name;
            this.description = description;
            this.isHex = false;
            this.boolValue = defaultBool;
            this.hexValue = null;
        }

        public SettingEntry(String key, String name, String description, String defaultHex) {
            this.key = key;
            this.name = name;
            this.description = description;
            this.isHex = true;
            this.boolValue = true;
            this.hexValue = defaultHex;
        }
    }

    public static class ModuleEntry {
        public final String id;
        public final String name;
        public final String description;
        public final Category category;
        public final String glyph;
        public boolean enabled;
        public boolean favorite;
        public final List<SettingEntry> settings = new ArrayList<>();

        public ModuleEntry(String id, String name, String description, Category category, String glyph, boolean enabled) {
            this.id = id;
            this.name = name;
            this.description = description;
            this.category = category;
            this.glyph = glyph;
            this.enabled = enabled;
        }

        public ModuleEntry addBool(String key, String name, String desc, boolean val) {
            settings.add(new SettingEntry(key, name, desc, val));
            return this;
        }

        public ModuleEntry addHex(String key, String name, String desc, String hex) {
            settings.add(new SettingEntry(key, name, desc, hex));
            return this;
        }
    }

    private final List<ModuleEntry> list = new ArrayList<>();
    private final Map<String, ModuleEntry> byId = new LinkedHashMap<>();

    public Modules() {
        register(new ModuleEntry(
                "bomb_nickname",
                "Nick (💣)",
                "Wyświetla nick na biało oraz czarną bombę w nawiasie () obok nicku.",
                Category.VISUAL,
                "💣",
                true
        ).addBool("show_black_bomb", "Czarna bomba w ()", "Wyświetla biały nick oraz czarną bombę w nawiasie (💣).", true));

        register(new ModuleEntry(
                "hitboxes",
                "Hitboxy",
                "Ramki graczy zmieniające kolor przy celowaniu w przeciwnika.",
                Category.COMBAT,
                "▣",
                true
        )
                .addBool("highlight_on_aim", "Podświetlanie przy celowaniu", "Zmienia kolor ramki na kolor celowania, gdy patrzysz w gracza.", true)
                .addHex("aim_color", "Kolor hitboxa przy celowaniu (HEX)", "#FF3B3B")
                .addHex("side_color", "Kolor hitboxa z boku (HEX)", "#FFFFFF"));

        register(new ModuleEntry("crosshair", "Celownik", "Własny celownik PvP z podglądem trafień.", Category.HUD, "⌖", false)
                .addBool("dynamic_hit", "Podświetlenie trafienia", "Podświetla celownik przy trafieniu przeciwnika.", true));

        register(new ModuleEntry("armor_status", "Status tarczy", "Wyświetla stan zbroi i tarczy na ekranie.", Category.HUD, "🛡", false)
                .addBool("show_durability", "Pokaż wytrzymałość", "Wyświetla pasek wytrzymałości elementów zbroi.", true));

        register(new ModuleEntry("hit_effects", "Efekty uderzenia", "Lekkie efekty wizualne przy trafieniu przeciwnika.", Category.VISUAL, "⚡", false)
                .addBool("crit_flash", "Błysk trafienia", "Subtelny efekt wizualny przy uderzeniu.", true));

        register(new ModuleEntry("hit_particles", "Cząsteczki uderzenia", "Subtelne cząsteczki trafienia zoptymalizowane pod FPS.", Category.VISUAL, "✦", false)
                .addBool("compact_particles", "Kompaktowe cząsteczki", "Ogranicza liczbę cząsteczek dla wyższego FPS.", true));

        register(new ModuleEntry("damage_tint", "Kolor obrażeń", "Dostosowuje kolor błysku przy otrzymaniu obrażeń.", Category.VISUAL, "◆", false)
                .addHex("tint_color", "Kolor błysku obrażeń (HEX)", "#FF3B3B"));

        register(new ModuleEntry("hit_streak", "Smuga ciosu", "Łagodna smuga animacji uderzenia.", Category.COMBAT, "⌒", true)
                .addBool("smooth_trail", "Płynna smuga", "Wygładza tor ruchu przy ciosie.", true));

        register(new ModuleEntry("swing_speed", "Prędkość machnięcia", "Dostosowuje płynność animacji machnięcia ręką.", Category.COMBAT, "»", false)
                .addBool("smooth_swing", "Płynna animacja", "Poprawia płynność machnięcia bronią.", true));

        register(new ModuleEntry("ender_pearl", "Ender Pearl", "Wskaźnik trajektorii i cooldownu perły.", Category.TOOLS, "◎", false)
                .addBool("show_cooldown", "Pokaż cooldown", "Wyświetla pozostały czas do kolejnego rzutu perłą.", true));

        register(new ModuleEntry("cape", "Peleryna Bombowy", "Włącza pelerynę klienta BombowyClient.", Category.VISUAL, "⛨", true)
                .addBool("wave_physics", "Fizyka peleryny", "Włącza płynny ruch peleryny podczas biegu.", true));

        register(new ModuleEntry("colors", "Kolory", "Paleta kolorów akcentów interfejsu.", Category.VISUAL, "◑", true)
                .addHex("accent_hex", "Kolor akcentu (HEX)", "#22C55E"));

        register(new ModuleEntry("motion_blur", "Rozmycie ruchu", "Płynny motion blur zoptymalizowany pod wysokie FPS.", Category.VISUAL, "≋", false)
                .addBool("subtle_blur", "Delikatne rozmycie", "Lekkie rozmycie ruchu bez spadku FPS.", true));

        register(new ModuleEntry("brightness", "Jasność", "Zwiększa czytelność ciemnych miejsc bez spadku FPS.", Category.VISUAL, "☀", false)
                .addBool("full_bright", "Pełna jasność", "Rozjaśnia ciemne obszary w grze.", true));

        register(new ModuleEntry("custom_sky", "Własne niebo", "Czyste, zoptymalizowane niebo bez ciężkich shaderów.", Category.VISUAL, "☁", false)
                .addBool("clear_horizon", "Czysty horyzont", "Poprawia przejrzystość nieba.", true));

        register(new ModuleEntry("aura", "Aura", "Lekka poświata wokół wybranego celu.", Category.COMBAT, "✦", false)
                .addHex("aura_hex", "Kolor poświaty (HEX)", "#60A5FA"));

        register(new ModuleEntry("player_trails", "Ślady za graczem", "Delikatne ślady ruchu za graczem.", Category.VISUAL, "••", false)
                .addBool("fade_trails", "Zanikające ślady", "Płynnie wygasza ślady za graczem.", true));

        register(new ModuleEntry("clean_view", "Czysty obraz", "Usuwa zbędne nakładki i rozproszenia z ekranu.", Category.VISUAL, "◎", true)
                .addBool("hide_overlays", "Ukryj nakładki", "Ogranicza zbędne efekty zasłaniające widok.", true));

        register(new ModuleEntry("no_block_particles", "Bez cząsteczek bloków", "Ukrywa nadmiarowe cząsteczki przy niszczeniu bloków.", Category.TOOLS, "◌", true)
                .addBool("skip_break_particles", "Pomijaj przy niszczeniu", "Redukuje cząsteczki podczas kopania bloków.", true));

        register(new ModuleEntry("fps_booster", "Optymalizacja FPS", "Ogranicza zbędne alokacje w każdej klatce.", Category.TOOLS, "⚙", true)
                .addBool("cache_buffers", "Buforowanie UI", "Utrzymuje lekki narzut pamięci przy renderowaniu.", true));
    }

    private void register(ModuleEntry entry) {
        list.add(entry);
        byId.put(entry.id, entry);
    }

    public List<ModuleEntry> getAll() {
        return Collections.unmodifiableList(list);
    }

    public ModuleEntry getById(String id) {
        return byId.get(id);
    }

    public int count(Category category) {
        if (category == Category.ALL) return list.size();
        if (category == Category.ENABLED) {
            int c = 0;
            for (ModuleEntry m : list) if (m.enabled) c++;
            return c;
        }
        int c = 0;
        for (ModuleEntry m : list) if (m.category == category) c++;
        return c;
    }

    public List<ModuleEntry> filter(Category category, String query, boolean favoritesOnly) {
        String q = query == null ? "" : query.trim().toLowerCase(Locale.ROOT);
        return list.stream()
                .filter(m -> {
                    if (favoritesOnly && !m.favorite) return false;
                    if (category == Category.ENABLED && !m.enabled) return false;
                    if (category != Category.ALL && category != Category.ENABLED && m.category != category) return false;
                    if (q.isEmpty()) return true;
                    return m.name.toLowerCase(Locale.ROOT).contains(q)
                            || m.description.toLowerCase(Locale.ROOT).contains(q);
                })
                .collect(Collectors.toList());
    }
}

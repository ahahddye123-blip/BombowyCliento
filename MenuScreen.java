package dev.bombowy.client;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public class MenuScreen extends Screen {
    private static class HitBoxRect {
        final int x, y, w, h;
        final Runnable action;

        HitBoxRect(int x, int y, int w, int h, Runnable action) {
            this.x = x;
            this.y = y;
            this.w = w;
            this.h = h;
            this.action = action;
        }

        boolean contains(double mx, double my) {
            return mx >= x && mx <= x + w && my >= y && my <= y + h;
        }
    }

    private final Screen parent;
    private Modules.Category selectedCategory = Modules.Category.ALL;
    private String search = "";
    private boolean favoritesOnly = false; // Działające serduszko (Polubione)
    private boolean compactRowsMode = false; // Działający przycisk 3 kresek (układ listy/kompaktowy)
    private int scrollY = 0;

    private final List<HitBoxRect> clickTargets = new ArrayList<>();

    public MenuScreen(Screen parent) {
        super(Text.literal("BombowyClient"));
        this.parent = parent;
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        // Celowo puste: wyłącza rozmycie (blur) oraz domyślne przyciemnianie tła w Minecraft 1.21.4
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        clickTargets.clear();
        Modules modules = BombowyClient.getInstance().getModules();

        // Główne ciemne tło (zgodne ze screenami)
        context.fill(0, 0, this.width, this.height, 0xFF14161A);

        int sidebarW = 196;
        // Lewy pasek boczny
        context.fill(0, 0, sidebarW, this.height, 0xFF0E1014);
        context.fill(sidebarW, 0, sidebarW + 1, this.height, 0xFF21252E);

        // Logo w lewym górnym rogu: ładna bomba + napis BOMBOWY (jak 2 ss)
        drawBombLogo(context, 16, 14);
        context.drawText(this.textRenderer, "BOMBOWY", 42, 20, 0xFFFFFFFF, true);

        // Kategorie po lewej
        int catY = 54;
        int catH = 26;
        for (Modules.Category cat : Modules.Category.values()) {
            boolean active = (cat == this.selectedCategory) && !this.favoritesOnly;
            boolean hovered = mouseX >= 8 && mouseX <= sidebarW - 8 && mouseY >= catY && mouseY <= catY + catH;

            if (active) {
                context.fill(8, catY, sidebarW - 8, catY + catH, 0xFF1E222C);
                context.fill(8, catY + 4, 11, catY + catH - 4, 0xFF22C55E);
            } else if (hovered) {
                context.fill(8, catY, sidebarW - 8, catY + catH, 0xFF161920);
            }

            int count = modules.count(cat);
            context.drawText(this.textRenderer, cat.label, 20, catY + 9, active ? 0xFFFFFFFF : 0xFFA3AAB8, active);
            String countStr = String.valueOf(count);
            context.drawText(this.textRenderer, countStr, sidebarW - 14 - this.textRenderer.getWidth(countStr), catY + 9, 0xFF6B7280, false);

            final Modules.Category targetCat = cat;
            clickTargets.add(new HitBoxRect(8, catY, sidebarW - 16, catH, () -> {
                this.favoritesOnly = false;
                this.selectedCategory = targetCat;
                this.scrollY = 0;
            }));

            catY += catH + 4;
        }

        // Dolny panel boczny (jak na 4 ss)
        int footY = this.height - 86;
        context.drawText(this.textRenderer, "Konfiguracja", 16, footY, 0xFF9CA3AF, false);
        context.drawText(this.textRenderer, "Ustawienia", 16, footY + 20, 0xFF9CA3AF, false);
        context.drawText(this.textRenderer, "Edytor HUD", 16, footY + 40, 0xFFFFFFFF, true);
        context.drawText(this.textRenderer, BombowyClient.VERSION + " · Right Shift", 16, footY + 60, 0xFF6B7280, false);

        // Górny pasek po prawej
        int contentX = sidebarW + 22;
        int contentW = this.width - contentX - 22;

        String headerTitle = this.favoritesOnly ? "Polubione" : "Moduły";
        String headerSub = this.favoritesOnly
                ? "Twoje ulubione moduły BombowyClient"
                : "Wszystkie moduły BombowyClient";

        context.drawText(this.textRenderer, headerTitle, contentX, 15, 0xFFFFFFFF, true);
        context.drawText(this.textRenderer, headerSub, contentX, 29, 0xFF8E96A6, false);

        // Przyciski w prawym górnym rogu: [Serce - polubione] oraz [3 kreski - układ]
        int iconBtnSize = 24;
        int iconGap = 6;
        int linesX = this.width - 22 - iconBtnSize;
        int linesY = 15;
        int heartTopX = linesX - iconGap - iconBtnSize;
        int heartTopY = 15;

        int searchW = 192;
        int searchH = 24;
        int searchX = heartTopX - 10 - searchW;
        int searchY = 15;

        // Pole wyszukiwania
        context.fill(searchX, searchY, searchX + searchW, searchY + searchH, 0xFF1A1D25);
        String searchText = this.search.isEmpty() ? "Szukaj modułu..." : this.search;
        context.drawText(this.textRenderer, searchText, searchX + 8, searchY + 8, this.search.isEmpty() ? 0xFF6B7280 : 0xFFFFFFFF, false);

        // Działający przycisk SERCA (Polubione)
        context.fill(heartTopX, heartTopY, heartTopX + iconBtnSize, heartTopY + iconBtnSize, this.favoritesOnly ? 0xFF2B1D24 : 0xFF1A1D25);
        context.drawText(this.textRenderer, this.favoritesOnly ? "♥" : "♡", heartTopX + 8, heartTopY + 8, this.favoritesOnly ? 0xFFF43F5E : 0xFFD0D5DD, false);
        clickTargets.add(new HitBoxRect(heartTopX, heartTopY, iconBtnSize, iconBtnSize, () -> {
            this.favoritesOnly = !this.favoritesOnly;
            this.scrollY = 0;
        }));

        // Działający przycisk 3 KRESEK (przełącza układ na listę/kompaktowy)
        context.fill(linesX, linesY, linesX + iconBtnSize, linesY + iconBtnSize, this.compactRowsMode ? 0xFF1E293B : 0xFF1A1D25);
        context.drawText(this.textRenderer, "☰", linesX + 8, linesY + 8, this.compactRowsMode ? 0xFF60A5FA : 0xFFD0D5DD, false);
        clickTargets.add(new HitBoxRect(linesX, linesY, iconBtnSize, iconBtnSize, () -> {
            this.compactRowsMode = !this.compactRowsMode;
            this.scrollY = 0;
        }));

        // Siatka / lista modułów
        List<Modules.ModuleEntry> visible = modules.filter(this.selectedCategory, this.search, this.favoritesOnly);
        int gridTop = 54;
        int cols = this.compactRowsMode ? 2 : 4;
        int gap = 12;
        int cardW = (contentW - gap * (cols - 1)) / cols;
        int cardH = this.compactRowsMode ? 52 : 98;

        for (int i = 0; i < visible.size(); i++) {
            Modules.ModuleEntry mod = visible.get(i);
            int col = i % cols;
            int row = i / cols;
            int x = contentX + col * (cardW + gap);
            int y = gridTop + row * (cardH + gap) - this.scrollY;

            if (y + cardH < gridTop - 8 || y > this.height) {
                continue;
            }

            context.fill(x, y, x + cardW, y + cardH, 0xFF191C23);
            context.drawText(this.textRenderer, mod.name, x + 10, y + 9, 0xFFFFFFFF, true);

            // Serduszko na karcie modułu (dodaje/usuwa z Polubionych)
            int cardHeartX = x + cardW - 18;
            int cardHeartY = y + 9;
            context.drawText(this.textRenderer, mod.favorite ? "♥" : "♡", cardHeartX, cardHeartY, mod.favorite ? 0xFFF43F5E : 0xFF7A8291, false);
            clickTargets.add(new HitBoxRect(cardHeartX - 4, cardHeartY - 4, 20, 20, () -> {
                mod.favorite = !mod.favorite;
                BombowyClient.getInstance().getConfig().save(modules);
            }));

            if (!this.compactRowsMode) {
                // Ikona na środku karty
                context.drawText(this.textRenderer, mod.glyph, x + cardW / 2 - 4, y + 35, 0xFFD1D5DB, true);

                // Dolny pasek: zębatka (ustawienia modułu) + przycisk Włączony / Wyłączony
                int barY = y + cardH - 26;
                int gearX = x + 10;
                int gearW = 24;
                int btnX = gearX + gearW + 8;
                int btnW = x + cardW - 10 - btnX;
                int btnH = 18;

                context.fill(gearX, barY, gearX + gearW, barY + btnH, 0xFF222630);
                context.drawText(this.textRenderer, "⚙", gearX + 8, barY + 5, 0xFFA1A8B6, false);
                clickTargets.add(new HitBoxRect(gearX, barY, gearW, btnH, () -> openModuleSettings(mod)));

                int bg = mod.enabled ? 0xFF22C55E : 0xFF252932;
                int fg = mod.enabled ? 0xFF061A0D : 0xFF9CA3AF;
                context.fill(btnX, barY, btnX + btnW, barY + btnH, bg);
                String statusLabel = mod.enabled ? "Włączony" : "Wyłączony";
                context.drawText(this.textRenderer, statusLabel, btnX + (btnW - this.textRenderer.getWidth(statusLabel)) / 2, barY + 5, fg, mod.enabled);
                clickTargets.add(new HitBoxRect(btnX, barY, btnW, btnH, () -> {
                    mod.enabled = !mod.enabled;
                    BombowyClient.getInstance().getConfig().save(modules);
                }));
            } else {
                // Tryb 3 kresek: kompaktowy wiersz z przełącznikiem i zębatką po prawej
                String statusLabel = mod.enabled ? "Włączony" : "Wyłączony";
                int stColor = mod.enabled ? 0xFF22C55E : 0xFF9CA3AF;
                context.drawText(this.textRenderer, statusLabel, x + 10, y + 29, stColor, false);
                clickTargets.add(new HitBoxRect(x + 6, y + 24, cardW - 38, 22, () -> {
                    mod.enabled = !mod.enabled;
                    BombowyClient.getInstance().getConfig().save(modules);
                }));

                int gearX = x + cardW - 24;
                int gearY = y + 26;
                context.drawText(this.textRenderer, "⚙", gearX, gearY, 0xFFA1A8B6, false);
                clickTargets.add(new HitBoxRect(gearX - 4, gearY - 4, 22, 20, () -> openModuleSettings(mod)));
            }
        }

        super.render(context, mouseX, mouseY, delta);
    }

    private void drawBombLogo(DrawContext ctx, int x, int y) {
        // Ładna ikona bomby (korpus, nakrętka, pomarańczowy lont)
        ctx.fill(x + 6, y + 2, x + 10, y + 5, 0xFF9CA3AF);
        ctx.fill(x + 10, y + 1, x + 14, y + 4, 0xFFF97316);
        ctx.fill(x + 3, y + 5, x + 15, y + 17, 0xFFFFFFFF);
        ctx.fill(x + 5, y + 4, x + 13, y + 18, 0xFFFFFFFF);
    }

    private void openModuleSettings(Modules.ModuleEntry mod) {
        if (this.client != null) {
            this.client.setScreen(new ModuleSettingsScreen(this, mod));
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            for (int i = clickTargets.size() - 1; i >= 0; i--) {
                HitBoxRect rect = clickTargets.get(i);
                if (rect.contains(mouseX, mouseY)) {
                    rect.action.run();
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        this.scrollY = Math.max(0, this.scrollY - (int) (verticalAmount * 24));
        return true;
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        if (!Character.isISOControl(chr)) {
            this.search += chr;
            this.scrollY = 0;
            return true;
        }
        return super.charTyped(chr, modifiers);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 259 && !this.search.isEmpty()) { // Backspace
            this.search = this.search.substring(0, this.search.length() - 1);
            this.scrollY = 0;
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
}

package dev.bombowy.client;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public class ModuleSettingsScreen extends Screen {
    private static class ClickArea {
        final int x, y, w, h;
        final Runnable action;

        ClickArea(int x, int y, int w, int h, Runnable action) {
            this.x = x;
            this.y = y;
            this.w = w;
            this.h = h;
            this.action = action;
        }

        boolean inside(double mx, double my) {
            return mx >= x && mx <= x + w && my >= y && my <= y + h;
        }
    }

    private final Screen parent;
    private final Modules.ModuleEntry module;
    private final List<ClickArea> areas = new ArrayList<>();

    public ModuleSettingsScreen(Screen parent, Modules.ModuleEntry module) {
        super(Text.literal(module.name));
        this.parent = parent;
        this.module = module;
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        // Celowo puste: wyłącza rozmycie (blur) oraz domyślne przyciemnianie tła w Minecraft 1.21.4
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        areas.clear();
        context.fill(0, 0, this.width, this.height, 0xFF14161A);

        int left = 36;
        int top = 28;

        // Działająca strzałka powrotu (<-) w lewym górnym rogu
        int backX = left;
        int backY = top;
        int backW = 24;
        int backH = 22;
        boolean hoverBack = mouseX >= backX && mouseX <= backX + backW && mouseY >= backY && mouseY <= backY + backH;

        if (hoverBack) {
            context.fill(backX - 2, backY - 2, backX + backW, backY + backH, 0xFF222630);
        }
        context.drawText(this.textRenderer, "←", backX + 4, backY + 5, hoverBack ? 0xFFFFFFFF : 0xFFD0D5DD, true);
        context.drawText(this.textRenderer, this.module.name, backX + 28, backY + 5, 0xFFFFFFFF, true);

        areas.add(new ClickArea(backX - 4, backY - 4, backW + 8, backH + 8, this::goBack));

        context.drawText(this.textRenderer, this.module.description, left, top + 24, 0xFF9AA2B1, false);

        int cardX = left;
        int cardY = top + 54;
        int cardW = Math.min(this.width - left * 2, 760);
        int rowH = 54;
        int gap = 12;

        // 1. Aktywność modułu
        drawSwitchCard(context, cardX, cardY, cardW, rowH,
                "Aktywność modułu",
                "Włącza lub wyłącza działanie modułu " + this.module.name + ".",
                this.module.enabled);
        areas.add(new ClickArea(cardX, cardY, cardW, rowH, () -> {
            this.module.enabled = !this.module.enabled;
            BombowyClient.getInstance().getConfig().save(BombowyClient.getInstance().getModules());
        }));

        int curY = cardY + rowH + gap;
        for (Modules.SettingEntry setting : this.module.settings) {
            if (!setting.isHex) {
                drawSwitchCard(context, cardX, curY, cardW, rowH, setting.name, setting.description, setting.boolValue);
                areas.add(new ClickArea(cardX, curY, cardW, rowH, () -> {
                    setting.boolValue = !setting.boolValue;
                    BombowyClient.getInstance().getConfig().save(BombowyClient.getInstance().getModules());
                }));
                curY += rowH + gap;
            } else {
                drawHexCard(context, cardX, curY, cardW, 74, setting);
                curY += 86;
            }
        }

        super.render(context, mouseX, mouseY, delta);
    }

    private void drawSwitchCard(DrawContext ctx, int x, int y, int w, int h, String title, String desc, boolean on) {
        ctx.fill(x, y, x + w, y + h, 0xFF1A1D24);
        ctx.drawText(this.textRenderer, title, x + 16, y + 10, 0xFFFFFFFF, true);
        ctx.drawText(this.textRenderer, desc, x + 16, y + 28, 0xFF8E96A6, false);

        int swW = 36;
        int swH = 18;
        int swX = x + w - swW - 18;
        int swY = y + (h - swH) / 2;

        ctx.fill(swX, swY, swX + swW, swY + swH, on ? 0xFF2563EB : 0xFF374151);
        int knobX = on ? (swX + swW - swH + 2) : (swX + 2);
        ctx.fill(knobX, swY + 2, knobX + swH - 4, swY + swH - 2, 0xFFFFFFFF);
    }

    private Modules.SettingEntry activeHexSetting = null;

    private void drawHexCard(DrawContext ctx, int x, int y, int w, int h, Modules.SettingEntry setting) {
        ctx.fill(x, y, x + w, y + h, 0xFF1A1D24);
        ctx.drawText(this.textRenderer, setting.name + ":", x + 16, y + 10, 0xFFD0D5DD, false);

        int boxX = x + 16;
        int boxY = y + 28;
        int boxW = 280;
        int boxH = 26;

        boolean focused = (this.activeHexSetting == setting);
        ctx.fill(boxX, boxY, boxX + boxW, boxY + boxH, focused ? 0xFF131822 : 0xFF0D0F13);
        if (focused) {
            ctx.fill(boxX, boxY, boxX + boxW, boxY + 1, 0xFF22C55E);
            ctx.fill(boxX, boxY + boxH - 1, boxX + boxW, boxY + boxH, 0xFF22C55E);
        }
        String displayHex = setting.hexValue + (focused ? "_" : "");
        ctx.drawText(this.textRenderer, displayHex, boxX + 10, boxY + 9, 0xFFFFFFFF, false);

        areas.add(new ClickArea(boxX, boxY, boxW, boxH, () -> this.activeHexSetting = setting));

        int color = parseHex(setting.hexValue, 0xFFFF3B3B);
        ctx.fill(boxX + boxW + 12, boxY, boxX + boxW + 48, boxY + boxH, color);
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        if (this.activeHexSetting != null && !Character.isISOControl(chr)) {
            String allowed = "#0123456789abcdefABCDEF";
            if (allowed.indexOf(chr) >= 0 && this.activeHexSetting.hexValue.length() < 7) {
                if (chr == '#' && !this.activeHexSetting.hexValue.isEmpty()) {
                    return true;
                }
                if (this.activeHexSetting.hexValue.isEmpty() && chr != '#') {
                    this.activeHexSetting.hexValue = "#" + chr;
                } else {
                    this.activeHexSetting.hexValue += chr;
                }
                BombowyClient.getInstance().getConfig().save(BombowyClient.getInstance().getModules());
                return true;
            }
        }
        return super.charTyped(chr, modifiers);
    }

    private int parseHex(String hex, int fallback) {
        try {
            String clean = hex.replace("#", "").trim();
            if (clean.length() == 6) {
                return 0xFF000000 | Integer.parseInt(clean, 16);
            }
        } catch (Exception ignored) {
        }
        return fallback;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            for (int i = areas.size() - 1; i >= 0; i--) {
                ClickArea a = areas.get(i);
                if (a.inside(mouseX, mouseY)) {
                    a.action.run();
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 259 && this.activeHexSetting != null && !this.activeHexSetting.hexValue.isEmpty()) {
            this.activeHexSetting.hexValue = this.activeHexSetting.hexValue.substring(0, this.activeHexSetting.hexValue.length() - 1);
            BombowyClient.getInstance().getConfig().save(BombowyClient.getInstance().getModules());
            return true;
        }
        if (keyCode == 256) { // ESC
            if (this.activeHexSetting != null) {
                this.activeHexSetting = null;
                return true;
            }
            goBack();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private void goBack() {
        if (this.client != null) {
            this.client.setScreen(this.parent != null ? this.parent : new MenuScreen(null));
        }
    }
}

package dev.bombowy.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BombowyClient implements ClientModInitializer {
    public static final String MOD_ID = "bombowyclient";
    public static final String MOD_NAME = "BombowyClient";
    public static final String VERSION = "v4.0";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private static BombowyClient instance;
    private BombowyConfig config;
    private Modules modules;
    private KeyBinding openGuiKey;

    @Override
    public void onInitializeClient() {
        instance = this;
        this.config = new BombowyConfig();
        this.modules = new Modules();
        this.config.load(this.modules);

        this.openGuiKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.bombowyclient.open_gui",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_RIGHT_SHIFT,
                "category.bombowyclient.main"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(this::onClientTick);
        LOGGER.info("{} {} (MC 1.21.4) gotowy!", MOD_NAME, VERSION);
    }

    private void onClientTick(MinecraftClient client) {
        if (client.player == null) return;
        if (this.openGuiKey.wasPressed() && !(client.currentScreen instanceof MenuScreen)) {
            client.setScreen(new MenuScreen(null));
        }
    }

    public static BombowyClient getInstance() {
        return instance;
    }

    public BombowyConfig getConfig() {
        return config;
    }

    public Modules getModules() {
        return modules;
    }
}

package crimsonfluff.simplemagnet.messages;

import crimsonfluff.simplemagnet.SimpleMagnet;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fmlclient.registry.ClientRegistry;
import org.lwjgl.glfw.GLFW;

public class KeybindHandler {
    private static KeyMapping keyMagnetToggle;
    private static KeyMapping keyMagnetMode;

    public KeybindHandler() {
        keyMagnetToggle = new KeyMapping("key." + SimpleMagnet.MOD_ID + ".magnet", GLFW.GLFW_KEY_V, "Crimson Simple Magnet");
        ClientRegistry.registerKeyBinding(keyMagnetToggle);

        keyMagnetMode = new KeyMapping("key." + SimpleMagnet.MOD_ID + ".magnetmode", GLFW.GLFW_KEY_B, "Crimson Simple Magnet");
        ClientRegistry.registerKeyBinding(keyMagnetMode);
    }

    @SubscribeEvent
    public void onKeyInput(InputEvent.KeyInputEvent event) {
        if (Minecraft.getInstance().player == null) return;

        // because this is client, send message to server and process MagnetToggle
        if (keyMagnetToggle.isDown()) NetworkHandler.CHANNEL.sendToServer(new MagnetToggle());
        if (keyMagnetMode.isDown()) NetworkHandler.CHANNEL.sendToServer(new MagnetMode());
    }
}

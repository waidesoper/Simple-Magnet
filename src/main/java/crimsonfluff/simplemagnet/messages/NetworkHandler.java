package crimsonfluff.simplemagnet.messages;

import crimsonfluff.simplemagnet.SimpleMagnet;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fmllegacy.network.NetworkRegistry;
import net.minecraftforge.fmllegacy.network.simple.SimpleChannel;

public class NetworkHandler {
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(new ResourceLocation(SimpleMagnet.MOD_ID, SimpleMagnet.MOD_ID), () -> "1.0", (s) -> true, (s) -> true);
    private static int id = 0;

    public static void onCommonSetup() {
        CHANNEL.registerMessage(id(), MagnetToggle.class, MagnetToggle::write, MagnetToggle::read, MagnetToggle::onMessage);
        CHANNEL.registerMessage(id(), MagnetMode.class, MagnetMode::write, MagnetMode::read, MagnetMode::onMessage);
    }

    private static int id() { return id++; }
}

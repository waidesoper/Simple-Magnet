package crimsonfluff.simplemagnet;

import crimsonfluff.simplemagnet.init.initConfigs;
import crimsonfluff.simplemagnet.init.initCurios;
import crimsonfluff.simplemagnet.init.initItems;
import crimsonfluff.simplemagnet.messages.KeybindHandler;
import crimsonfluff.simplemagnet.messages.NetworkHandler;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import top.theillusivec4.curios.api.SlotTypeMessage;
import top.theillusivec4.curios.api.SlotTypePreset;

@Mod(SimpleMagnet.MOD_ID)
public class SimpleMagnet {
    public static final String MOD_ID = "simplemagnet";
    public static final initConfigs CONFIGURATION = new initConfigs();
//    public static final Logger LOGGER = LogManager.getLogger(SimpleMagnet.MOD_ID);

    public SimpleMagnet() {
        IEventBus MOD_EVENTBUS = FMLJavaModLoadingContext.get().getModEventBus();
        MOD_EVENTBUS.addListener(this::enqueueIMC);
        MOD_EVENTBUS.addListener(this::onFMLCommonSetupEvent);
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> MOD_EVENTBUS.addListener(this::onFMLClientSetupEvent));
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> MOD_EVENTBUS.addListener(this::onTextureStitchEvent));

        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, CONFIGURATION.COMMON);
        initItems.ITEMS.register(MOD_EVENTBUS);

        MinecraftForge.EVENT_BUS.register(this);
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public void onTextureStitchEvent(TextureStitchEvent.Pre event) {
        if (event.getMap().location().equals(InventoryMenu.BLOCK_ATLAS))
            event.addSprite(new ResourceLocation(SimpleMagnet.MOD_ID, "item/empty_magnet_slot"));
    }

    private void enqueueIMC(final InterModEnqueueEvent event) {
        if (initCurios.isModLoaded()) {
            if (! SlotTypePreset.findPreset("magnet").isPresent()) {
                InterModComms.sendTo("curios", SlotTypeMessage.REGISTER_TYPE,
                    () -> new SlotTypeMessage
                        .Builder("magnet")
                        .icon(new ResourceLocation(SimpleMagnet.MOD_ID, "item/empty_magnet_slot"))
                        .size(1)
                        .build());
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    private void onFMLClientSetupEvent(final FMLClientSetupEvent event) {
        MinecraftForge.EVENT_BUS.register(new KeybindHandler());
    }

    private void onFMLCommonSetupEvent(final FMLCommonSetupEvent event) {
        event.enqueueWork(NetworkHandler::onCommonSetup);
    }
}

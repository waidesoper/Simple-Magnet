package crimsonfluff.simplemagnet.init;

import crimsonfluff.simplemagnet.SimpleMagnet;
import crimsonfluff.simplemagnet.items.MagnetItem;
import net.minecraft.item.Item;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class initItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, SimpleMagnet.MOD_ID);

    public static final RegistryObject<Item> SIMPLE_MAGNET = ITEMS.register("magnet", MagnetItem::new);
}

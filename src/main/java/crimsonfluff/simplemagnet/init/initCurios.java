package crimsonfluff.simplemagnet.init;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.ModList;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import top.theillusivec4.curios.api.CuriosApi;

public class initCurios {
    public static ItemStack findItem(Item item, LivingEntity entity) {
        return CuriosApi.getCuriosHelper().findEquippedCurio(item, entity)
            .map(ImmutableTriple::getRight)
            .orElse(ItemStack.EMPTY);
    }

    public static boolean isModLoaded() { return (ModList.get().getModContainerById("curios").isPresent()); }
}

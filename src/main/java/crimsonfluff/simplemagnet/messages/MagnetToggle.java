package crimsonfluff.simplemagnet.messages;

import crimsonfluff.simplemagnet.init.initCurios;
import crimsonfluff.simplemagnet.init.initItems;
import crimsonfluff.simplemagnet.items.MagnetItem;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class MagnetToggle {
    public static MagnetToggle read(PacketBuffer buffer) { return new MagnetToggle(); }

    public static void write(MagnetToggle message, PacketBuffer buffer) { }

    public static void onMessage(MagnetToggle message, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            ServerPlayerEntity player = context.get().getSender();

            if (player != null) {
                ItemStack itemStack = ItemStack.EMPTY;

                if (player.getMainHandItem().getItem() instanceof MagnetItem)
                    itemStack = player.getMainHandItem();

                else if (player.getOffhandItem().getItem() instanceof MagnetItem)
                    itemStack = player.getOffhandItem();

                else {
                    if (initCurios.isModLoaded())
                        itemStack = initCurios.findItem(initItems.SIMPLE_MAGNET.get(), player);

                    if (itemStack.isEmpty()) {
                        for (int a = 0; a < player.inventory.getContainerSize(); a++) {
                            if (player.inventory.getItem(a).getItem() instanceof MagnetItem) {
                                itemStack = player.inventory.getItem(a);
                                break;
                            }
                        }
                    }
                }

                if (! itemStack.isEmpty())
                    ((MagnetItem) itemStack.getItem()).changeMagnetToggle(itemStack.getOrCreateTag().getInt("CustomModelData"), player, itemStack);
            }
        });

        context.get().setPacketHandled(true);
    }
}

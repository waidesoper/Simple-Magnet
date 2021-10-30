package crimsonfluff.simplemagnet.items;

import crimsonfluff.simplemagnet.SimpleMagnet;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import java.util.ArrayList;
import java.util.List;

public class MagnetItem extends Item {
    public MagnetItem() {
        super(new Properties().tab(CreativeModeTab.TAB_TOOLS).stacksTo(1).durability(SimpleMagnet.CONFIGURATION.maxDamage.get()));
    }

    @Override
    public boolean isValidRepairItem(ItemStack toRepair, ItemStack repair) {
        return repair.sameItem(new ItemStack(Items.IRON_INGOT)) || super.isValidRepairItem(toRepair, repair);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand handIn) {
        ItemStack itemStack = playerIn.getItemInHand(handIn);
        if (worldIn.isClientSide) return InteractionResultHolder.consume(itemStack);

        // 0=off, 1=items, 2=xp (bitwise)
        int magnetMode = itemStack.getOrCreateTag().getInt("CustomModelData");
        if (playerIn.isShiftKeyDown())
            changeMagnetMode(magnetMode, playerIn, itemStack);
        else
            changeMagnetToggle(magnetMode, playerIn, itemStack);

        return InteractionResultHolder.success(itemStack);
    }

    public void changeMagnetMode(int magnetMode, Player player, ItemStack itemStack) {
        magnetMode++;
        if (magnetMode == 4) magnetMode = 1;    // dont start from off

        String modeString = switch (magnetMode) {
            default -> "tip.simplemagnet.magnet1";
            case 2 -> "tip.simplemagnet.magnet2";
            case 3 -> "tip.simplemagnet.magnet3";
        };

        player.displayClientMessage(new TranslatableComponent(modeString, itemStack.getHoverName()), true);

        player.level.playSound(null, player.blockPosition(), SoundEvents.NOTE_BLOCK_BELL, SoundSource.PLAYERS, 1f, 0.9f);

        itemStack.getTag().putInt("CustomModelData", magnetMode);
    }

    public void changeMagnetToggle(int magnetMode, Player player, ItemStack itemStack) {
        boolean active = ! itemStack.getOrCreateTag().getBoolean("active");
        itemStack.getTag().putBoolean("active", active);

        // set default to items
        if (active && magnetMode == 0) itemStack.getTag().putInt("CustomModelData", 1);

        if (active)
            player.displayClientMessage(new TranslatableComponent("tip." + SimpleMagnet.MOD_ID + ".active", itemStack.getHoverName()), true);
        else
            player.displayClientMessage(new TranslatableComponent("tip." + SimpleMagnet.MOD_ID + ".inactive", itemStack.getHoverName()), true);

        player.level.playSound(null, player.blockPosition(), SoundEvents.NOTE_BLOCK_BELL, SoundSource.PLAYERS, 1f, (active) ? 0.9f : 0.1f);
    }

    @Override
    public boolean isFoil(ItemStack stack) {return (stack.getOrCreateTag().getBoolean("active"));}

    @Override
    public void inventoryTick(ItemStack stack, Level worldIn, Entity entityIn, int itemSlot, boolean isSelected) {
        if (! worldIn.isClientSide) {
            if (! (entityIn instanceof Player)) return;        // stop zombies and the like using the magnet, lol

            if (stack.getOrCreateTag().getBoolean("active")) {
                Player playerIn = (Player) entityIn;

                double x = entityIn.getX();
                double y = entityIn.getY();
                double z = entityIn.getZ();
                boolean shouldBreak = false;
                int magnetMode = stack.getTag().getInt("CustomModelData");

                int r = (SimpleMagnet.CONFIGURATION.pullRadius.get());
                AABB area = new AABB(x - r, y - r, z - r, x + r, y + r, z + r);

                if ((magnetMode & 0b00000001) == 1) {
//                    List<ItemEntity> items = worldIn.getEntitiesOfClass(ItemEntity.class, area, itm -> { if (itm.getThrower() != playerIn.getUUID()) return;});
                    List<ItemEntity> items = worldIn.getEntitiesOfClass(ItemEntity.class, area);

                    if (items.size() != 0) {
                        for (ItemEntity itemIE : items) {
                            ((ServerLevel) worldIn).sendParticles(ParticleTypes.POOF, itemIE.getX(), itemIE.getY(),
                                itemIE.getZ(), 2, 0D, 0D, 0D, 0D);

                            //r = itemIE.getItem().getCount();
                            itemIE.setNoPickUpDelay();
                            //if (r != 0) {
                            if (itemIE.distanceToSqr(playerIn) > 1.5f) shouldBreak = true;
                            itemIE.setPos(x, y, z);
                            //}
                        }
                    }
                }

                // Handle the XP
                if ((magnetMode & 0b00000010) == 2) {
                    List<ExperienceOrb> orbs = worldIn.getEntitiesOfClass(ExperienceOrb.class, area);

                    if (orbs.size() != 0) {
                        shouldBreak = true;
                        worldIn.playSound(null, x, y, z, SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 1f, 1f);

                        ArrayList<ItemStack> MendingItems = new ArrayList<>();
                        ItemStack stacks;

                        // getRandomEquippedWithEnchantment only works with offhand, main hand, armor slots
                        // so make a list of valid items, add magnet to it, then randomly choose an item to repair
                        for (int a = 36; a < 41; a++) {
                            stacks = playerIn.getInventory().getItem(a);
                            if (EnchantmentHelper.getItemEnchantmentLevel(Enchantments.MENDING, stacks) > 0)
                                if (stacks.isDamaged()) MendingItems.add(stacks);
                        }

                        // if Magnet is MENDING then add to list
                        if (EnchantmentHelper.getItemEnchantmentLevel(Enchantments.MENDING, stack) > 0)
                            if (stack.isDamaged()) MendingItems.add(stack);

                        for (ExperienceOrb orb : orbs) {
                            ((ServerLevel) worldIn).sendParticles(ParticleTypes.POOF, orb.blockPosition().getX(), orb.blockPosition().getY(), orb.blockPosition().getZ(), 2, 0D, 0D, 0D, 0D);

                            // Choose random item from MendingItems list
                            if (MendingItems.size() > 0) {
                                r = worldIn.random.nextInt(MendingItems.size());
                                stacks = MendingItems.get(r);

                                int i = Math.min((int) (orb.value * stacks.getXpRepairRatio()), stacks.getDamageValue());
                                orb.value -= i / 2;     //orb.durabilityToXp(i);
                                stacks.setDamageValue(stacks.getDamageValue() - i);

                                if (stacks.getDamageValue() == 0) MendingItems.remove(r);
                            }

                            if (orb.value > 0) playerIn.giveExperiencePoints(orb.value);
                            orb.remove(Entity.RemovalReason.DISCARDED);
                        }
                    }
                }

                // NOTE: DamageItem checks Creative mode !
                if (shouldBreak) {
                    stack.hurtAndBreak(1, playerIn, plyr -> plyr.broadcastBreakEvent(EquipmentSlot.MAINHAND));
                    stack.setPopTime(5);
                }
            }
        }
    }
}

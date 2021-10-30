package crimsonfluff.simplemagnet.items;

import crimsonfluff.simplemagnet.SimpleMagnet;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ExperienceOrbEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

import java.util.ArrayList;
import java.util.List;

public class MagnetItem extends Item {
    public MagnetItem() {
        super(new Properties().tab(ItemGroup.TAB_TOOLS).stacksTo(1).durability(SimpleMagnet.CONFIGURATION.maxDamage.get()));
    }

    @Override
    public boolean isValidRepairItem(ItemStack toRepair, ItemStack repair) {
        return repair.sameItem(new ItemStack(Items.IRON_INGOT)) || super.isValidRepairItem(toRepair, repair);
    }

    @Override
    public ActionResult<ItemStack> use(World worldIn, PlayerEntity playerIn, Hand handIn) {
        ItemStack itemStack = playerIn.getItemInHand(handIn);
        if (worldIn.isClientSide) return ActionResult.consume(itemStack);

        // 0=off, 1=items, 2=xp (bitwise)
        int magnetMode = itemStack.getOrCreateTag().getInt("CustomModelData");
        if (playerIn.isShiftKeyDown())
            changeMagnetMode(magnetMode, playerIn, itemStack);
        else
            changeMagnetToggle(magnetMode, playerIn, itemStack);

        return ActionResult.success(itemStack);
    }

    public void changeMagnetMode(int magnetMode, PlayerEntity player, ItemStack itemStack) {
        magnetMode++;
        if (magnetMode == 4) magnetMode = 1;    // dont start from off

        String modeString;
        switch (magnetMode) {
            default:
            case 1:
                modeString = "tip.simplemagnet.magnet1";
                break;

            case 2:
                modeString = "tip.simplemagnet.magnet2";
                break;

            case 3:
                modeString = "tip.simplemagnet.magnet3";
                break;
        }

        player.displayClientMessage(new TranslationTextComponent(modeString, itemStack.getHoverName()), true);

        player.level.playSound(null, player.blockPosition(), SoundEvents.NOTE_BLOCK_BELL, SoundCategory.PLAYERS, 1f, 0.9f);

        itemStack.getTag().putInt("CustomModelData", magnetMode);
    }

    public void changeMagnetToggle(int magnetMode, PlayerEntity player, ItemStack itemStack) {
        boolean active = ! itemStack.getOrCreateTag().getBoolean("active");
        itemStack.getTag().putBoolean("active", active);

        // set default to items
        if (active && magnetMode == 0) itemStack.getTag().putInt("CustomModelData", 1);

        if (active)
            player.displayClientMessage(new TranslationTextComponent("tip." + SimpleMagnet.MOD_ID + ".active", itemStack.getHoverName()), true);
        else
            player.displayClientMessage(new TranslationTextComponent("tip." + SimpleMagnet.MOD_ID + ".inactive", itemStack.getHoverName()), true);

        player.level.playSound(null, player.blockPosition(), SoundEvents.NOTE_BLOCK_BELL, SoundCategory.PLAYERS, 1f, (active) ? 0.9f : 0.1f);
    }

    @Override
    public boolean isFoil(ItemStack stack) {return (stack.getOrCreateTag().getBoolean("active"));}

    @Override
    public void inventoryTick(ItemStack stack, World worldIn, Entity entityIn, int itemSlot, boolean isSelected) {
        if (! worldIn.isClientSide) {
            if (! (entityIn instanceof PlayerEntity)) return;        // stop zombies and the like using the magnet, lol

            if (stack.getOrCreateTag().getBoolean("active")) {
                PlayerEntity playerIn = (PlayerEntity) entityIn;

                double x = entityIn.getX();
                double y = entityIn.getY();
                double z = entityIn.getZ();
                boolean shouldBreak = false;
                int magnetMode = stack.getTag().getInt("CustomModelData");

                int r = (SimpleMagnet.CONFIGURATION.pullRadius.get());
                AxisAlignedBB area = new AxisAlignedBB(x - r, y - r, z - r, x + r, y + r, z + r);

                if ((magnetMode & 0b00000001) == 1) {
//                    List<ItemEntity> items = worldIn.getEntitiesOfClass(ItemEntity.class, area, itm -> { if (itm.getThrower() != playerIn.getUUID()) return;});
                    List<ItemEntity> items = worldIn.getEntitiesOfClass(ItemEntity.class, area);

                    if (items.size() != 0) {
                        for (ItemEntity itemIE : items) {
                            ((ServerWorld) worldIn).sendParticles(ParticleTypes.POOF, itemIE.getX(), itemIE.getY(),
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
                    List<ExperienceOrbEntity> orbs = worldIn.getEntitiesOfClass(ExperienceOrbEntity.class, area);

                    if (orbs.size() != 0) {
                        shouldBreak = true;
                        worldIn.playSound(null, x, y, z, SoundEvents.EXPERIENCE_ORB_PICKUP, SoundCategory.PLAYERS, 1f, 1f);

                        ArrayList<ItemStack> MendingItems = new ArrayList<>();
                        ItemStack stacks;

                        // getRandomEquippedWithEnchantment only works with offhand, main hand, armor slots
                        // so make a list of valid items, add magnet to it, then randomly choose an item to repair
                        for (int a = 36; a < 41; a++) {
                            stacks = playerIn.inventory.getItem(a);
                            if (EnchantmentHelper.getItemEnchantmentLevel(Enchantments.MENDING, stacks) > 0)
                                if (stacks.isDamaged()) MendingItems.add(stacks);
                        }

                        // if Magnet is MENDING then add to list
                        if (EnchantmentHelper.getItemEnchantmentLevel(Enchantments.MENDING, stack) > 0)
                            if (stack.isDamaged()) MendingItems.add(stack);

                        for (ExperienceOrbEntity orb : orbs) {
                            ((ServerWorld) worldIn).sendParticles(ParticleTypes.POOF, orb.blockPosition().getX(), orb.blockPosition().getY(), orb.blockPosition().getZ(), 2, 0D, 0D, 0D, 0D);

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
                            orb.remove();
                        }
                    }
                }

                // NOTE: DamageItem checks Creative mode !
                if (shouldBreak) {
                    stack.hurtAndBreak(1, playerIn, plyr -> plyr.broadcastBreakEvent(EquipmentSlotType.MAINHAND));
                    stack.setPopTime(5);
                }
            }
        }
    }
}

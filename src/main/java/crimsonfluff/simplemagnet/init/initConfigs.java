package crimsonfluff.simplemagnet.init;
import net.minecraftforge.common.ForgeConfigSpec;

public class initConfigs {
    public final ForgeConfigSpec COMMON;

    public ForgeConfigSpec.IntValue pullRadius;
    public ForgeConfigSpec.IntValue maxDamage;

    public initConfigs() {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        builder.comment("Simple Magnet");
        builder.push("Hello");

        pullRadius = builder
            .comment("Number of blocks radius to pull items?  Default: 16",
                     "Add Unbreaking enchantment for more durability")
            .defineInRange("pullRadius", 16, 2, 64);

        maxDamage = builder
            .comment("Amount of durability the magnet has?  Default: 256")
            .defineInRange("maxDamage", 256, 256, 2500);

        builder.pop();

        COMMON = builder.build();
    }
}

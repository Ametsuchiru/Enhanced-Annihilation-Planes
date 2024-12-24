package io.github.ametsuchiru.enhancedannihilationplanes;

import net.minecraftforge.common.config.Config;

@Config(modid = Tags.MOD_ID)
public class EAPConfig {

    @Config.RangeInt(min = 1)
    @Config.Name("Identity Annihilation Plane Enchantability")
    @Config.Comment("Sets the enchantability of the Identity Annihilation Plane. Default is 10 (Diamond Enchantability)")
    public static int enchantability = 10;

    @Config.RangeInt(min = 1)
    @Config.Name("Identity Annihilation Plane Unbreaking Chance")
    @Config.Comment("This value contributes to the chance of unbreaking happening, the formula: X/V, where X is the Unbreaking level, and V is this value. Default is X/100.")
    public static int unbreakingRollFrom = 100;

    @Config.RangeDouble(min = 0.0, max = 1.0)
    @Config.Name("Identity Annihilation Plane Efficiency Percentage")
    @Config.Comment("Sets the percentage of reduction in energy used per operation with each efficiency level. Default is 10%.")
    public static double efficiencyPercentage = 0.10;

    @Config.RangeInt(min = 1)
    @Config.Name("Identity Annihilation Plane Silk Touch Multiplier")
    @Config.Comment("Sets the multiplier for energy used per operation when silk touch is present. Default is 8x.")
    public static int silkTouchMultiplier = 8;

    @Config.RangeInt(min = 1)
    @Config.Name("Identity Annihilation Plane Fortune Multiplier")
    @Config.Comment("Sets the multiplier for energy used per operation when fortune is present. Default is 3x per fortune level.")
    public static int fortuneMultiplier = 3;

}

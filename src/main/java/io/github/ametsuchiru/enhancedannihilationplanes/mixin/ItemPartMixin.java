package io.github.ametsuchiru.enhancedannihilationplanes.mixin;

import appeng.items.AEBaseItem;
import appeng.items.parts.ItemPart;
import appeng.items.parts.PartType;
import io.github.ametsuchiru.enhancedannihilationplanes.EAPConfig;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnumEnchantmentType;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import javax.annotation.Nonnull;

@Mixin(ItemPart.class)
public abstract class ItemPartMixin extends AEBaseItem {

    @Shadow(remap = false) @Nonnull public abstract PartType getTypeByStack(ItemStack is);

    @Override
    public boolean isBookEnchantable(ItemStack itemstack1, ItemStack itemstack2) {
        return true;
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        if (this.getTypeByStack(stack) == PartType.IDENTITY_ANNIHILATION_PLANE) {
            return true;
        }
        return super.isEnchantable(stack);
    }

    @Override
    public int getItemEnchantability(ItemStack stack) {
        if (this.getTypeByStack(stack) == PartType.IDENTITY_ANNIHILATION_PLANE) {
            return EAPConfig.enchantability;
        }
        return super.getItemEnchantability();
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
        if (this.getTypeByStack(stack) == PartType.IDENTITY_ANNIHILATION_PLANE) {
            return enchantment.type == EnumEnchantmentType.DIGGER;
        }
        return super.canApplyAtEnchantingTable(stack, enchantment);
    }

}

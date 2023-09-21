package net.minecraft.block;

import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;

public class EAPBlockExpander extends Block {

    public static ItemStack eap$getSilkTouchDrop(IBlockState state) {
        return state.getBlock().getSilkTouchDrop(state);
    }

    private EAPBlockExpander() {
        super(null);
        throw new AssertionError();
    }

}

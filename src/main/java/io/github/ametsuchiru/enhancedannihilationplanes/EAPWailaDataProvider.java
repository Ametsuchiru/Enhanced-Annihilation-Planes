package io.github.ametsuchiru.enhancedannihilationplanes;

import appeng.api.parts.IPart;
import appeng.integration.modules.waila.part.BasePartWailaDataProvider;
import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

import java.util.List;

public class EAPWailaDataProvider extends BasePartWailaDataProvider {

    @Override
    public List<String> getWailaBody(IPart part, List<String> currentToolTip, IWailaDataAccessor accessor, IWailaConfigHandler config) {
        if (part instanceof EAPInterface) {
            NBTTagCompound tag = accessor.getNBTData().getCompoundTag("enchantments");
            if (!tag.isEmpty()) {
                currentToolTip.add("");
                for (String tagKey : tag.getKeySet()) {
                    Enchantment enchantment = Enchantment.getEnchantmentByID(Integer.parseInt(tagKey));
                    int level = tag.getInteger(tagKey);
                    currentToolTip.add(TextFormatting.AQUA + enchantment.getTranslatedName(level));
                }
            }
        }
        return currentToolTip;
    }

    @Override
    public NBTTagCompound getNBTData(EntityPlayerMP player, IPart part, TileEntity te, NBTTagCompound tag, World world, BlockPos pos) {
        NBTTagCompound returnTag = super.getNBTData(player, part, te, tag, world, pos);
        if (part instanceof EAPInterface) {
            NBTTagCompound enchantmentTag = ((EAPInterface) part).eap$writeEnchantments();
            if (!enchantmentTag.isEmpty()) {
                returnTag.setTag("enchantments", enchantmentTag);
            }
        }
        return returnTag;
    }
}

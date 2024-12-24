package io.github.ametsuchiru.enhancedannihilationplanes.mixin;

import appeng.api.AEApi;
import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.networking.IGridNode;
import appeng.api.networking.energy.IEnergyGrid;
import appeng.api.networking.storage.IStorageGrid;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.storage.channels.IItemStorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.core.AppEng;
import appeng.core.sync.packets.PacketTransitionEffect;
import appeng.hooks.TickHandler;
import appeng.me.GridAccessException;
import appeng.parts.automation.PartAnnihilationPlane;
import appeng.parts.automation.PartIdentityAnnihilationPlane;
import appeng.util.Platform;
import appeng.util.SettingsFrom;
import appeng.util.item.AEItemStack;
import io.github.ametsuchiru.enhancedannihilationplanes.EAPConfig;
import io.github.ametsuchiru.enhancedannihilationplanes.EAPInterface;
import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import net.minecraft.block.EAPBlockExpander;
import net.minecraft.block.state.IBlockState;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Enchantments;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.event.ForgeEventFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Mixin(value = PartIdentityAnnihilationPlane.class, remap = false)
public abstract class PartIdentityAnnihilationPlaneMixin extends PartAnnihilationPlane implements EAPInterface {

    @Unique private Map<Enchantment, Integer> eap$enchantments;
    @Unique private PartAnnihilationPlaneAccessor eap$accessor;
    @Unique private boolean eap$currentlyBreaking;

    protected PartIdentityAnnihilationPlaneMixin(ItemStack stack) {
        super(stack);
        throw new AssertionError();
    }

    @Override
    public NBTTagCompound eap$writeEnchantments() {
        NBTTagCompound tag = new NBTTagCompound();
        for (Map.Entry<Enchantment, Integer> entry : this.eap$enchantments.entrySet()) {
            tag.setInteger(String.valueOf(Enchantment.getEnchantmentID(entry.getKey())), entry.getValue());
        }
        return tag;
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    private void afterInit(ItemStack stack, CallbackInfo ci) {
        this.eap$enchantments = new Object2IntArrayMap<>(EnchantmentHelper.getEnchantments(stack));
        this.eap$accessor = (PartAnnihilationPlaneAccessor) this;
    }

    @Override
    protected List<ItemStack> obtainBlockDrops(WorldServer world, BlockPos pos) {
        FakePlayer fakePlayer = FakePlayerFactory.getMinecraft(world);
        IBlockState state = world.getBlockState(pos);
        ItemStack prevHeldItem = fakePlayer.getHeldItem(EnumHand.MAIN_HAND);
        ItemStack stack = this.getItemStack();
        fakePlayer.setHeldItem(EnumHand.MAIN_HAND, stack);
        if (state.getBlock().canSilkHarvest(world, pos, state, fakePlayer) && this.eap$enchantments.containsKey(Enchantments.SILK_TOUCH)) {
            ItemStack itemstack = EAPBlockExpander.eap$getSilkTouchDrop(state);
            if (!itemstack.isEmpty()) {
                List<ItemStack> items = new ArrayList<>();
                items.add(itemstack);
                ForgeEventFactory.fireBlockHarvesting(items, world, pos, state, 0, 1.0F, true, fakePlayer);
                fakePlayer.setHeldItem(EnumHand.MAIN_HAND, prevHeldItem);
                return items;
            }
            fakePlayer.setHeldItem(EnumHand.MAIN_HAND, prevHeldItem);
            return Collections.emptyList();
        }
        int fortune = this.eap$enchantments.getOrDefault(Enchantments.FORTUNE, 0);
        List<ItemStack> drops = state.getBlock().getDrops(world, pos, state, fortune);
        float chance = ForgeEventFactory.fireBlockHarvesting(drops, world, pos, state, fortune, 1.0F, false, fakePlayer);
        fakePlayer.setHeldItem(EnumHand.MAIN_HAND, prevHeldItem);
        if (chance == 1.0F) {
            return drops;
        }
        return drops.stream().filter($ -> world.rand.nextFloat() <= chance).collect(Collectors.toList());
    }


    // TODO: GrS support for other enchants multipliers?
    @Override
    protected float calculateEnergyUsage(WorldServer world, BlockPos pos, List<ItemStack> items) {
        float requiredEnergy = super.calculateEnergyUsage(world, pos, items);
        int unbreakingLevel = this.eap$enchantments.getOrDefault(Enchantments.UNBREAKING, 0);
        if (unbreakingLevel > 0 && world.rand.nextInt(EAPConfig.unbreakingRollFrom) < unbreakingLevel) {
            return 0F;
        }
        if (this.eap$enchantments.containsKey(Enchantments.SILK_TOUCH)) {
            requiredEnergy *= EAPConfig.silkTouchMultiplier;
        }
        int fortuneLevel = this.eap$enchantments.getOrDefault(Enchantments.FORTUNE, 0);
        if (fortuneLevel > 0) {
            requiredEnergy *= (fortuneLevel * EAPConfig.fortuneMultiplier);
        }
        int efficiencyLevel = this.eap$enchantments.getOrDefault(Enchantments.EFFICIENCY, 0);
        if (efficiencyLevel > 0) {
            requiredEnergy *= (float) (1.0F - (efficiencyLevel * EAPConfig.efficiencyPercentage));
        }
        return requiredEnergy;
    }

    @Override
    public TickRateModulation call(World world) {
        this.eap$currentlyBreaking = false;
        return this.eap$breakBlock(true);
    }

    @Override
    public TickRateModulation tickingRequest(IGridNode node, int ticksSinceLastCall) {
        if (this.eap$currentlyBreaking) {
            return TickRateModulation.URGENT;
        } else {
            this.eap$accessor.eap$accepting(true);
            return this.eap$breakBlock(false);
        }
    }

    @Override
    protected NBTTagCompound downloadSettings(SettingsFrom from) {
        NBTTagCompound tag = super.downloadSettings(from);
        if (from == SettingsFrom.DISMANTLE_ITEM) {
            tag.setTag("ench", this.getItemStack().getEnchantmentTagList());
        }
        return tag;
    }

    @Override
    public void uploadSettings(SettingsFrom from, NBTTagCompound output, EntityPlayer player) {
        super.uploadSettings(from, output, player);
        if (from == SettingsFrom.DISMANTLE_ITEM) {
            this.getItemStack().setTagInfo("ench", output.getTagList("ench", 10));
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        data.setTag("ench", this.getItemStack().getEnchantmentTagList());
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        this.getItemStack().setTagInfo("ench", data.getTagList("ench", 10));
    }

    @Unique
    private TickRateModulation eap$breakBlock(boolean modulate) {
        if (this.eap$accessor.eap$isAccepting() && this.getProxy().isActive()) {
            try {
                TileEntity tile = this.getTile();
                WorldServer world = (WorldServer) tile.getWorld();
                BlockPos pos = tile.getPos().offset(this.getSide().getFacing());
                IEnergyGrid energy = this.getProxy().getEnergy();
                if (this.eap$accessor.eap$canHandleBlock(world, pos)) {
                    List<ItemStack> drops = this.obtainBlockDrops(world, pos);
                    float requiredPower = this.calculateEnergyUsage(world, pos, drops);
                    boolean hasPower = energy.extractAEPower(requiredPower, Actionable.SIMULATE, PowerMultiplier.CONFIG) > (double) requiredPower - 0.1;
                    if (hasPower && this.eap$canStoreItemStacks(drops)) {
                        if (modulate) {
                            energy.extractAEPower(requiredPower, Actionable.MODULATE, PowerMultiplier.CONFIG);
                            this.eap$destroyAndStoreItems(world, pos, drops);
                            AppEng.proxy.sendToAllNearExcept(null, pos.getX(), pos.getY(), pos.getZ(), 64D, world,
                                    new PacketTransitionEffect(pos.getX(),pos.getY(), pos.getZ(), this.getSide(), true));
                        } else {
                            this.eap$currentlyBreaking = true;
                            TickHandler.INSTANCE.addCallable(this.getTile().getWorld(), this);
                        }
                        return TickRateModulation.URGENT;
                    }
                }
            } catch (GridAccessException ignored) { }
        }
        return TickRateModulation.IDLE;
    }

    @Unique
    private boolean eap$canStoreItemStacks(final List<ItemStack> stacks) {
        boolean canStore = stacks.isEmpty();
        try {
            final IStorageGrid storage = this.getProxy().getStorage();
            for (final ItemStack itemStack : stacks) {
                final IAEItemStack itemToTest = AEItemStack.fromItemStack(itemStack);
                final IAEItemStack overflow = storage.getInventory(AEApi.instance().storage().getStorageChannel(IItemStorageChannel.class))
                        .injectItems(itemToTest, Actionable.SIMULATE, this.eap$accessor.eap$mySrc());
                if (overflow == null || itemToTest.getStackSize() > overflow.getStackSize()) {
                    canStore = true;
                }
            }
        } catch (final GridAccessException ignored) { }
        this.eap$accessor.eap$accepting(canStore);
        return canStore;
    }

    @Unique
    private void eap$destroyAndStoreItems(World world, BlockPos pos, List<ItemStack> drops) {
        world.destroyBlock(pos, false);
        for (ItemStack drop : drops) {
            IAEItemStack overflow = this.eap$storeItemStack(drop);
            if (overflow != null) {
                eap$handleOverflow(drop, overflow);
            }
        }
    }

    @Unique
    private IAEItemStack eap$storeItemStack(ItemStack stack) {
        IAEItemStack itemToStore = AEItemStack.fromItemStack(stack);
        IAEItemStack overflow;
        try {
            IStorageGrid storage = this.getProxy().getStorage();
            IEnergyGrid energy = this.getProxy().getEnergy();
            overflow = Platform.poweredInsert(energy, storage.getInventory(AEApi.instance().storage()
                    .getStorageChannel(IItemStorageChannel.class)), itemToStore, this.eap$accessor.eap$mySrc());
            this.eap$accessor.eap$accepting(overflow == null);
            return overflow;
        } catch (GridAccessException ignored) { }
        return null;
    }

    @Unique
    private void eap$handleOverflow(ItemStack stack, IAEItemStack overflow) {
        if (overflow.getStackSize() != 0L) {
            stack.setCount((int) overflow.getStackSize());
        }
    }

}

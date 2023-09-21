package io.github.ametsuchiru.enhancedannihilationplanes.mixin;

import appeng.api.networking.security.IActionSource;
import appeng.parts.automation.PartAnnihilationPlane;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(value = PartAnnihilationPlane.class, remap = false)
public interface PartAnnihilationPlaneAccessor {

    @Accessor(value = "mySrc")
    IActionSource eap$mySrc();

    @Accessor(value = "isAccepting")
    boolean eap$isAccepting();

    @Accessor(value = "isAccepting")
    void eap$accepting(boolean isAccepting);

    @Invoker(value = "canHandleBlock")
    boolean eap$canHandleBlock(WorldServer world, BlockPos pos);

}

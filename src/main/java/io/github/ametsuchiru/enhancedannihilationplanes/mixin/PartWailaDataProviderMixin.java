package io.github.ametsuchiru.enhancedannihilationplanes.mixin;

import appeng.integration.modules.waila.PartWailaDataProvider;
import appeng.integration.modules.waila.part.IPartWailaDataProvider;
import io.github.ametsuchiru.enhancedannihilationplanes.EAPWailaDataProvider;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(value = PartWailaDataProvider.class, remap = false)
public class PartWailaDataProviderMixin {

    @Shadow @Final private List<IPartWailaDataProvider> providers;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void afterInit(CallbackInfo ci) {
        this.providers.add(new EAPWailaDataProvider());
    }

}

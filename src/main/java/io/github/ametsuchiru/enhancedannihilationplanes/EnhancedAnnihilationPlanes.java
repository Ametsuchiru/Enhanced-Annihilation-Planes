package io.github.ametsuchiru.enhancedannihilationplanes;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import zone.rong.mixinbooter.ILateMixinLoader;

import java.util.Collections;
import java.util.List;

@Mod(modid = Tags.MOD_ID, name = Tags.MOD_NAME, version = Tags.VERSION)
public class EnhancedAnnihilationPlanes {

    public static final Logger LOGGER = LogManager.getLogger(Tags.MOD_NAME);

    /**
     * <a href="https://wiki.cleanroommc.com/mod-development/event/overview/">
     *     Take a look at how many FMLStateEvents you can listen to via the @Mod.EventHandler annotation here
     * </a>
     */
    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        LOGGER.info("Initializing {}!", Tags.MOD_NAME);
    }

    public static class MixinLoader implements ILateMixinLoader {

        @Override
        public List<String> getMixinConfigs() {
            return Collections.singletonList(Tags.MIXIN_CONFIG);
        }

    }

}

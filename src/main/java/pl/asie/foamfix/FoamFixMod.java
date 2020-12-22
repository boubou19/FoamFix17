package pl.asie.foamfix;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.relauncher.Side;
import net.minecraftforge.common.MinecraftForge;
import org.apache.logging.log4j.Logger;
import pl.asie.foamfix.bugfixmod.coremod.BugfixModClassTransformer;
import pl.asie.foamfix.bugfixmod.mod.ToolDesyncFixEventHandler;
import pl.asie.foamfix.bugfixmod.mod.ArrowDingTweakEventHandler;
import pl.asie.foamfix.ghostbuster.CommandGhostBuster;

@Mod(name = "FoamFix", modid = "foamfix", version = "@VERSION@")
public class FoamFixMod {
    @Mod.Instance
    public static FoamFixMod instance;

    public static Logger logger;

    @Mod.EventHandler
    public void preinit(FMLPreInitializationEvent evt) {
        logger = evt.getModLog();

        if (BugfixModClassTransformer.instance.settings.ArrowDingTweakEnabled) {
            ArrowDingTweakEventHandler handler = new ArrowDingTweakEventHandler();
            FMLCommonHandler.instance().bus().register(handler);
            MinecraftForge.EVENT_BUS.register(handler);
        }

        if (evt.getSide() == Side.CLIENT) {
            if (BugfixModClassTransformer.instance.settings.ToolDesyncFixEnabled) {
                ToolDesyncFixEventHandler handler = new ToolDesyncFixEventHandler();
                FMLCommonHandler.instance().bus().register(handler);
                MinecraftForge.EVENT_BUS.register(handler);
            }
        }
    }

    @Mod.EventHandler
    public void serverStarting(FMLServerStartingEvent event) {
        if (BugfixModClassTransformer.instance.settings.gbEnableDebugger) {
            event.registerServerCommand(new CommandGhostBuster());
        }
    }
}

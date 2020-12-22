package pl.asie.foamfix.bugfixmod.mod;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.ReflectionHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraftforge.event.world.BlockEvent;
import pl.asie.foamfix.bugfixmod.coremod.BugfixModClassTransformer;

import java.lang.reflect.Method;

public class ToolDesyncFixEventHandler {
    private final Method syncItemMth;

    public ToolDesyncFixEventHandler() {
        syncItemMth = ReflectionHelper.findMethod(PlayerControllerMP.class, null, new String[] {"syncCurrentPlayItem", "func_78750_j"});
        if (syncItemMth != null) {
            syncItemMth.setAccessible(true);
        }
    }

    @SubscribeEvent
    public void blockBreak(BlockEvent.BreakEvent evt) {
        if (syncItemMth != null) {
            try {
                syncItemMth.invoke(Minecraft.getMinecraft().playerController);
            } catch (Exception ex) {
                BugfixModClassTransformer.instance.logger.warn("ToolDesyncFix failed to resync");
            }
        }
    }
}

package pl.asie.foamfix.bugfixmod.coremod;

import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraftforge.common.config.Configuration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import pl.asie.foamfix.bugfixmod.BugfixModSettings;
import pl.asie.foamfix.bugfixmod.coremod.patchers.BoatDesyncFixPatcher_Extra;
import pl.asie.foamfix.bugfixmod.coremod.patchers.ChickenLureTweakPatcher;
import pl.asie.foamfix.coremod.patchers.GhostBusterEarlyReturnPatcher;
import pl.asie.foamfix.coremod.patchers.GhostBusterHookPatcher;
import pl.asie.foamfix.bugfixmod.coremod.patchers.HeartBlinkFixPatcher;
import pl.asie.foamfix.bugfixmod.coremod.patchers.SnowballFixPatcher;
import pl.asie.foamfix.bugfixmod.coremod.patchers.AbstractPatcher;
import pl.asie.foamfix.bugfixmod.coremod.patchers.BoatDesyncFixPatcher_Main;
import pl.asie.foamfix.bugfixmod.coremod.patchers.HeartFlashFixPatcher;
import pl.asie.foamfix.bugfixmod.coremod.patchers.ItemHopperBounceFixPatcher;
import pl.asie.foamfix.bugfixmod.coremod.patchers.ItemStairBounceFixPatcher;
import pl.asie.foamfix.bugfixmod.coremod.patchers.VillageAnvilTweakPatcher;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;


/**
 * Created by Vincent on 3/10/14.
 */
public class BugfixModClassTransformer implements IClassTransformer {

    public static BugfixModClassTransformer instance;
    public File settingsFile;
    private boolean hasInit = false;
    public BugfixModSettings settings;
    private Map<String, AbstractPatcher> patchers;
    public Logger logger = LogManager.getLogger("foamfix");

    public BugfixModClassTransformer() {
        if (instance != null) {
            throw new RuntimeException("Only one transformer may exist!");
        } else {
            instance = this;
        }
    }

    public void initialize(Boolean isObf) {
        if (!hasInit) {
            Configuration config = new Configuration(settingsFile);
            config.load();
            settings = new BugfixModSettings();

            settings.gbEnableDebugger = config.get("ghostbuster", "enableDebugger", false,
                    "Enable the /ghostbuster command used for logging ghost chunkloading events.").getBoolean(false);
            settings.gbEnableFixes = config.get("ghostbuster", "enableFixes", true,
                    "Main toggle. If disabled, none of the ghost chunkloading fixes are applied.").getBoolean(true);
            settings.gbFixGrassVanilla = config.get("ghostbuster", "fixGrassVanilla", true,
                    "Fix ghost chunkloading caused by vanilla grass blocks.").getBoolean(true);
            settings.gbFixGrassBOP = config.get("ghostbuster", "fixGrassBop", true,
                    "Fix ghost chunkloading caused by Biomes O' Plenty grass blocks.").getBoolean(true);

            settings.ItemHopperBounceFixEnabled = config.get("bugfixes", "itemBounceHopperFix", false,
                    "Fix items bouncing around on locked hoppers. (from BugfixMod by williewillus)"
                    ).getBoolean(false);
            settings.ItemStairBounceFixEnabled = config.get("bugfixes", "itemBounceStairFix", false,
                    "Fix items bouncing around on stairs. (from BugfixMod by williewillus)"
                    ).getBoolean(false);
            settings.SnowballFixEnabled = config.get("bugfixes", "snowballFix", true,
                    "Fix projectiles that deal 0 damage not knocking back players. (from BugfixMod by williewillus)"
                    ).getBoolean(true);

            settings.ArrowDingTweakEnabled = config.get("tweaks", "arrowDing", false,
                    "Emits a 'ding' sound when mobs are hit by an arrow, not just players. (from BugfixMod by williewillus)"
                    ).getBoolean(false);
            settings.ChickenLureTweakEnabled = config.get("tweaks", "chickenLureFix", false,
                    "Adds AI tasks that make chickens attracted to all items they can breed with, not just seeds. (from BugfixMod by williewillus)"
                    ).getBoolean(false);
            settings.VillageAnvilTweakEnabled = config.get("tweaks", "villageAnvils", false,
                    "Blacksmith houses generate with an anvil where there was a double stone slab (which was supposed to be an \"anvil\" anyway). (from BugfixMod by williewillus)"
                    ).getBoolean(false);

            settings.BoatDesyncFixEnabled = config.get("bugfixes", "clientBoatDesyncFix", true,
                    "Reduce the amount of boat desynchronization between client and server. (from BugfixMod by williewillus)"
                    ).getBoolean(true);
            settings.HeartBlinkFixEnabled = config.get("bugfixes", "clientHeartBlinkFix", true,
                    "Restore rendering the client-side heart blink. (from BugfixMod by williewillus)"
                    ).getBoolean(true);
            settings.HeartFlashFixEnabled = config.get("bugfixes", "clientHeartFlashFix", true,
                    "Restore lost hearts flashing to indicate loss. (from BugfixMod by williewillus)"
                    ).getBoolean(true);
            settings.ToolDesyncFixEnabled = config.get("bugfixes", "clientToolDesyncFix", true,
                    "Fix client-side desynchronization of damaged tools related to Unbreaking enchantments. (from BugfixMod by williewillus)"
                    ).getBoolean(true);

            if (!Arrays.asList(new File(new File(settingsFile.getParent()).getParent()).list()).contains("saves")) {
                logger.info("You probably are on a dedicated server. Disabling client fixes");
                settings.BoatDesyncFixEnabled = false;
                settings.HeartBlinkFixEnabled = false;
                settings.HeartFlashFixEnabled = false;
            }

            config.save();
            MappingRegistry.init(isObf);
            setupPatchers();
            hasInit = true;
        }
    }


    public byte[] transform(String name, String transformedName, byte[] bytes) {
        if (hasInit) {
            AbstractPatcher p = patchers.get(transformedName);
            if (p != null) {
                bytes = p.patch(transformedName, bytes);
            }
        }
        return bytes;
    }


    private void setupPatchers() {
        if (patchers != null) {
            logger.warn("Patcher already initialized!!");
        } else {
            patchers = new HashMap<>();

            if (settings.BoatDesyncFixEnabled) {
                addPatcher(new BoatDesyncFixPatcher_Main(
                    "BoatDesyncFix",
                    "net/minecraft/entity/item/EntityBoat",
                    MappingRegistry.getMethodNameFor("EntityBoat.setBoatIsEmpty"),
                    "(Z)V"
                ));
                addPatcher(new BoatDesyncFixPatcher_Extra(
                    "BoatDesyncFix|Extra",
                    "net/minecraft/entity/item/EntityBoat",
                    MappingRegistry.getMethodNameFor("EntityBoat.setPositionAndRotation2"),
                    "(DDDFFI)V"
                ));
            }

            if (settings.ChickenLureTweakEnabled) {
                addPatcher(new ChickenLureTweakPatcher(
                        "ChickenLureTweak",
                        "net/minecraft/entity/passive/EntityChicken",
                        "<init>",
                        "(Lnet/minecraft/world/World;)V"
                ));
            }

            if (settings.HeartBlinkFixEnabled) {
                addPatcher(new HeartBlinkFixPatcher(
                    "HeartBlinkFix",
                    "net/minecraft/client/entity/EntityPlayerSP",
                    MappingRegistry.getMethodNameFor("EntityPlayerSP.setPlayerSPHealth"),
                    "(F)V"
                ));
            }

            if (settings.HeartFlashFixEnabled) {
                addPatcher(new HeartFlashFixPatcher(
                        "HeartFlashFix",
                        "net/minecraft/client/entity/EntityClientPlayerMP",
                        MappingRegistry.getMethodNameFor("EntityClientPlayerMP.attackEntityFrom"),
                        "(Lnet/minecraft/util/DamageSource;F)Z"
                ));

            }

            if (settings.HeartBlinkFixEnabled && settings.HeartFlashFixEnabled) {
//                addPatcher(new HeartFlashFixCompatPatcher(
//                        "HeartFlashFix|Compat",
//                        MappingRegistry.getClassNameFor("net/minecraft/client/entity/EntityClientPlayerMP"),
//                        MappingRegistry.getMethodNameFor("EntityClientPlayerMP.setPlayerSPHealth"),
//                        "(F)V"
//                ));
            }

            if (settings.ItemHopperBounceFixEnabled) {
                addPatcher(new ItemHopperBounceFixPatcher(
                        "ItemHopperBounceFix",
                        "net/minecraft/block/BlockHopper",
                        MappingRegistry.getMethodNameFor("BlockHopper.addCollisionBoxesToList"),
                        "(Lnet/minecraft/world/World;IIIL" +
                                "net/minecraft/util/AxisAlignedBB;Ljava/util/List;L" +
                                "net/minecraft/entity/Entity;)V"
                ));
            }

            if (settings.ItemStairBounceFixEnabled) {
                addPatcher(new ItemStairBounceFixPatcher(
                        "ItemStairBounceFix",
                        "net/minecraft/block/BlockStairs",
                        MappingRegistry.getMethodNameFor("BlockStairs.addCollisionBoxesToList"),
                        "(Lnet/minecraft/world/World;IIIL" +
                                "net/minecraft/util/AxisAlignedBB;Ljava/util/List;L" +
                                "net/minecraft/entity/Entity;)V"
                ));
            }

            if (settings.SnowballFixEnabled) {
                addPatcher(new SnowballFixPatcher(
                        "SnowballFix",
                        "net/minecraft/entity/player/EntityPlayer",
                        MappingRegistry.getMethodNameFor("EntityPlayer.attackEntityFrom"),
                        "(Lnet/minecraft/util/DamageSource;F)Z"
                ));
            }

            String sig2 = "(Lnet/minecraft/world/World;"
                    + "Ljava/util/Random;"
                    + "Lnet/minecraft/world/gen/structure/StructureBoundingBox;)Z";

            if (settings.VillageAnvilTweakEnabled) {
                addPatcher(new VillageAnvilTweakPatcher(
                        "VillageAnvilTweak",
                        "net/minecraft/world/gen/structure/StructureVillagePieces$House2",
                        MappingRegistry.getMethodNameFor("StructureVillagePieces$House2.addComponentParts"),
                        sig2 // break out into separate block above for readability
                ));
            }

            if (settings.gbEnableDebugger) {
                addPatcher(new GhostBusterHookPatcher(
                        "GhostBusterHook",
                        "net/minecraft/world/gen/ChunkProviderServer",
                        MappingRegistry.getMethodNameFor("ChunkProviderServer.provideChunk"),
                        null
                ));
            }

            if (settings.gbEnableFixes) {
                if (settings.gbFixGrassVanilla) {
                    addPatcher(GhostBusterEarlyReturnPatcher.updateTick(
                            "net/minecraft/block/BlockGrass", 3
                    ));
                }

                if (settings.gbFixGrassBOP) {
                    addPatcher(GhostBusterEarlyReturnPatcher.updateTick(
                            "biomesoplenty/common/blocks/BlockBOPGrass", 3
                    ));
                }
            }
        }
    }

    private void addPatcher(AbstractPatcher patcher) {
        patchers.put(patcher.getTargetClassName(), patcher);
    }
}

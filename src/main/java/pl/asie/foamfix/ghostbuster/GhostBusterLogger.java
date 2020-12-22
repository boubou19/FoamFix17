package pl.asie.foamfix.ghostbuster;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.ChunkProviderServer;
import pl.asie.foamfix.FoamFixMod;

public class GhostBusterLogger {
	public static boolean debugChunkProviding = false;
	public static boolean countNotifyBlock = false;

	public static void onProvideChunk(ChunkProviderServer server, int x, int z) {
		if (debugChunkProviding) {
			Chunk chunk = (Chunk) server.loadedChunkHashMap.getValueByKey(ChunkCoordIntPair.chunkXZ2Int(x, z));
			if (chunk != null) {
				return;
			}

			if (!server.worldObj.getPersistentChunks().containsKey(new ChunkCoordIntPair(x, z))) {
				int i = 0;
				StackTraceElement[] stea = new Throwable().getStackTrace();

				// TODO 1.12 -> 1.7.10
				/* if (stea.length > 1 && stea[1].toString().startsWith("net.minecraft.server.management.PlayerChunkMapEntry")) {
					i = -1;
				} */

				if (i >= 0 && !countNotifyBlock) {
					for (StackTraceElement ste : stea) {
						if (ste.toString().startsWith("net.minecraft.world.World.markAndNotifyBlock")) {
							i = -1;
							break;
						}
					}
				}

				if (i >= 0) {
					FoamFixMod.logger.info("Block in chunk [" + x + ", " + z + "] may be ghostloaded!");

					for (StackTraceElement ste : stea) {
						try {
							Class c = GhostBusterLogger.class.getClassLoader().loadClass(ste.getClassName());
							if (MinecraftServer.class.isAssignableFrom(c)) {
								break;
							}
							if ((i++) > 0) {
								FoamFixMod.logger.info("- " + ste.toString());
							}
						} catch (Exception e) {

						}
					}
				}
			}
		}
	}
}

/*
 * Copyright (c) 2015 Vincent Lee
 * Copyright (c) 2020, 2021 Adrian "asie" Siekierka
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

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

				if (stea.length > 3 && stea[3].toString().startsWith("net.minecraft.world.WorldServer.func_147456_g")) {
					i = -1;
				}

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

					// different hook method than 1.12 - skip provideChunk, we know as much
					for (StackTraceElement ste : stea) {
						try {
							Class c = GhostBusterLogger.class.getClassLoader().loadClass(ste.getClassName());
							if (MinecraftServer.class.isAssignableFrom(c)) {
								break;
							}
							if ((i++) > 1) {
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

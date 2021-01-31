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

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class GhostBusterSafeAccessors {
	public static boolean isAreaLoaded(IBlockAccess access, int x, int y, int z, int radius) {
		if (access instanceof World) {
			return ((World) access).checkChunksExist(x - radius, y, z - radius, x + radius, y, z + radius);
		} else {
			return true;
		}
	}

	public static boolean isBlockLoaded(IBlockAccess access, int x, int y, int z) {
		if (access instanceof World) {
			return ((World) access).getChunkProvider().chunkExists(x >> 4, z >> 4);
		} else {
			return true;
		}
	}

	public static Block getBlock(IBlockAccess access, int x, int y, int z) {
		return isBlockLoaded(access, x, y, z) ? access.getBlock(x, y, z) : Blocks.air;
	}

	public static boolean isAirBlock(IBlockAccess access, int x, int y, int z) {
		return !isBlockLoaded(access, x, y, z) || access.isAirBlock(x, y, z);
	}
}

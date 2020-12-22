/*
 * Copyright (C) 2016, 2017, 2018, 2019 Adrian Siekierka
 *
 * This file is part of FoamFix.
 *
 * FoamFix is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * FoamFix is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with FoamFix.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Additional permission under GNU GPL version 3 section 7
 *
 * If you modify this Program, or any covered work, by linking or
 * combining it with the Minecraft game engine, the Mojang Launchwrapper,
 * the Mojang AuthLib and the Minecraft Realms library (and/or modified
 * versions of said software), containing parts covered by the terms of
 * their respective licenses, the licensors of this Program grant you
 * additional permission to convey the resulting work.
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

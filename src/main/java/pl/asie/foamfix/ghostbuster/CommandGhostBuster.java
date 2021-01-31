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

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;

public class CommandGhostBuster extends CommandBase {
	@Override
	public String getCommandName() {
		return "ghostbuster";
	}

	@Override
	public String getCommandUsage(ICommandSender sender) {
		return "/ghostbuster [on|off]";
	}

	@Override
	public void processCommand(ICommandSender sender, String[] args) {
		if (args.length >= 1) {
			if ("on".equals(args[0])) {
				GhostBusterLogger.debugChunkProviding = true;
				sender.addChatMessage(new ChatComponentText("Ghost chunkload logging ON!"));
				return;
			} else if ("off".equals(args[0])) {
				GhostBusterLogger.debugChunkProviding = false;
				sender.addChatMessage(new ChatComponentText("Ghost chunkload logging OFF!"));
				return;
			}
		}

		sender.addChatMessage(new ChatComponentText("Ghost chunkload logging status: " + (GhostBusterLogger.debugChunkProviding ? "ON" : "OFF")));
	}
}

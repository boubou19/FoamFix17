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

package io.github.skepter.chairs;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ChairsCommand implements CommandExecutor {
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (label.toLowerCase().equals("chairs") || label.toLowerCase().equals("chair")) {
			if (sender instanceof Player) {
				Player player = (Player) sender;
				if(args.length == 0) {
					if(!Main.getChairPlayers().contains(player.getUniqueId().toString())) {
						Main.getChairPlayers().add(player.getUniqueId().toString());
						player.sendMessage(ChatColor.GRAY + "You have disabled chairs for yourself!");
					} else {
						Main.getChairPlayers().remove(player.getUniqueId().toString());
						player.sendMessage(ChatColor.GRAY + "You have enabled chairs for yourself!");
					}
					return true;
				} else if(args.length == 1) {
					if(args[0].equalsIgnoreCase("on")) {
						Main.getChairPlayers().remove(player.getUniqueId().toString());
						player.sendMessage(ChatColor.GRAY + "You have enabled chairs for yourself!");
						return true;
					} else if(args[0].equalsIgnoreCase("off")) {
						Main.getChairPlayers().add(player.getUniqueId().toString());
						player.sendMessage(ChatColor.GRAY + "You have disabled chairs for yourself!");
						return true;
					} else if(args[0].equalsIgnoreCase("reload")) {
						if(player.hasPermission("chairs.reload")) {
							Main.getInstance().reloadConfig();
							player.sendMessage(ChatColor.GRAY + "Chairs config reloaded");
							return true;
						} else {
							player.sendMessage(ChatColor.RED + "You do not have permission to execute this command");
							return true;
						}
					}
					
				} else if(args.length > 1) {
					
					player.sendMessage(ChatColor.RED + "Incorrect syntax. Use /chairs to toggle chairs. Use /chairs on to turn on chairs or /chairs off to turn off chairs");
					return true;
				}
			}
			return true;
		}
		return false;
	}

}

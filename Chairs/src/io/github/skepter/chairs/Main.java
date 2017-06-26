package io.github.skepter.chairs;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends org.bukkit.plugin.java.JavaPlugin implements org.bukkit.event.Listener {

	//TODO:
	/*
	 * permission support -> permission = chairs.use
	 * 
	 * reload command -> chairs reload needs permission chairs.reload
	 */
	
	public ChairStorage chairs;
	private static Set<String> chairPlayers;
	private static boolean worldGuard = false;
	
	@Override
	public void onEnable() {
		getServer().getPluginManager().registerEvents(new ChairListener(), this);
		getCommand("chairs").setExecutor(new ChairsCommand());

		chairs = new ChairStorage(this);
		chairPlayers = new HashSet<String>();
		chairPlayers.addAll(chairs.get());
		
		saveDefaultConfig();
		
		if(Bukkit.getPluginManager().getPlugin("WorldGuard") != null) {
			worldGuard = true;
		}
	}
	@Override
	public void onDisable() {
		if (chairs != null && chairPlayers != null)
			chairs.store(chairPlayers);
		
		worldGuard = false;
	}
	
	public static Main getInstance() {
		return JavaPlugin.getPlugin(Main.class);
	}
	
	public static Set<String> getChairPlayers() {
		return chairPlayers;
	}
	
	public static boolean hasWorldGuard() {
		return worldGuard;
	}
}

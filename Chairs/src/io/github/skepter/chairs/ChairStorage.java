package io.github.skepter.chairs;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bukkit.plugin.java.JavaPlugin;

public class ChairStorage {

	private File file;

	public ChairStorage(JavaPlugin plugin) {
		file = new File(plugin.getDataFolder(), "chairs.txt");
		if(!file.exists()) {
			plugin.getDataFolder().mkdirs();
			try {
				file.createNewFile();
			} catch (IOException e) {
				Main.getInstance().getLogger().severe("Could not create chairs file!");
			}
		}
	}

	public Set<String> get() {
		Set<String> players = new HashSet<String>();
		if (getFromFile() == null) {
			return players;
		}
		for (String str : getFromFile()) {
			players.add(str);
		}
		return players;
	}

	private List<String> getFromFile() {
		try {
			List<String> lines = new ArrayList<String>();
			final BufferedReader in = new BufferedReader(
					new InputStreamReader(new FileInputStream(file)));
			String inputLine;
			while ((inputLine = in.readLine()) != null) {
				lines.add(inputLine);
			}
			in.close();
			return lines;
		} catch (Exception e) {
			Main.getInstance().getLogger().severe("Could not get players from chairs file!");
		}
		return null;
	}

	public void store(Set<String> players) {
		file.delete();
		try {
			file.createNewFile();
			BufferedWriter bw = new BufferedWriter(new FileWriter(file));
			for(String s : players) {
				bw.write(s);
				bw.newLine();
			}
			bw.flush();
			bw.close();
		} catch (IOException e) {
			Main.getInstance().getLogger().severe("Could not store players to chairs file!");
		}
	}

}

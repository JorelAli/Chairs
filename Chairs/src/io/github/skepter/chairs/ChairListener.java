package io.github.skepter.chairs;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.material.Stairs;
import org.spigotmc.event.entity.EntityDismountEvent;
import org.spigotmc.event.entity.EntityMountEvent;

import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.bukkit.WGBukkit;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

public class ChairListener implements Listener {

	public Map<UUID, Block> sittingPlayers;
	
	public ChairListener() {
		sittingPlayers = new HashMap<UUID, Block>();
	}
	
	@EventHandler
	public void onBlock(PlayerInteractEvent event) {
		if(event.getPlayer().hasPermission("chairs.use") || event.getPlayer().isOp()) {
			if(!Main.getChairPlayers().contains(event.getPlayer().getUniqueId().toString())) {
				if(event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
					if(event.getPlayer().getInventory().getItemInMainHand().getType().equals(Material.AIR)) {
						if(!event.getPlayer().isSneaking()) {
							Block b = event.getClickedBlock();
							if(worldGuardCheck(event.getPlayer(), b)) {
								if(!event.getBlockFace().equals(BlockFace.DOWN)) {
									if(isStair(b)) {
										Stairs s = (Stairs) b.getState().getData();
										if(!s.isInverted()) {
											if(isMaterialHollowForChairs(b.getRelative(BlockFace.UP).getType())) {
												if(!sittingPlayers.values().contains(b)) {
													sit(event.getPlayer(), b);
													return;
												} else {
													event.getPlayer().sendMessage(ChatColor.GRAY + "This seat is occupied!");
													return;
												}
											}
										}
									}
								}
							}
						}
					}
				}
			}
		}
	}
	
	/*
	 * Returns true if they CAN sit on a chair
	 */
	private boolean worldGuardCheck(Player player, Block block) {
		if(Main.hasWorldGuard()) {
			List<String> regions = Main.getInstance().getConfig().getStringList("regions");
			for(String region : regions) {
				ProtectedRegion wgRegion = WGBukkit.getPlugin().getRegionManager(player.getWorld()).getRegion(region);
				if(wgRegion != null) {
					if(wgRegion.contains(block.getX(), block.getY(), block.getZ())) {
						LocalPlayer p = WGBukkit.getPlugin().wrapPlayer(player);
						if(wgRegion.isOwner(p) || wgRegion.isMember(p))
							return true;
						else
							return false;
					}
				}
			}
			return true;
		} else {
			return true;
		}
	}
	
	public static <A, B> Map<B, A> reverse(final Map<A, B> map) {
		final Map<B, A> newMap = new HashMap<B, A>();
		for (final Entry<A, B> entry : map.entrySet())
			newMap.put(entry.getValue(), entry.getKey());
		return newMap;
	}
	
	@EventHandler
	public void onBlockBreak(BlockBreakEvent event) {
		if(sittingPlayers.values().contains(event.getBlock())) {
			Player player = Bukkit.getPlayer(reverse(sittingPlayers).get(event.getBlock()));
			player.getVehicle().remove();
			sittingPlayers.remove(player);
		}
	}
	
	@EventHandler
	public void onSit(EntityMountEvent event) {
		if(event.getMount().getType().equals(EntityType.ARMOR_STAND) && event.getEntity() instanceof Player) {
			Player player = (Player) event.getEntity();
			if(!sittingPlayers.containsKey(player.getUniqueId())) {
				sittingPlayers.put(player.getUniqueId(), event.getMount().getLocation().getBlock().getRelative(0, 1, 0));
			}
		}
	}
	
	@EventHandler
	public void onUnsit(EntityDismountEvent event) {
		if(event.getDismounted().getType().equals(EntityType.ARMOR_STAND) && event.getEntity() instanceof Player) {
			Player player = (Player) event.getEntity();
			if(sittingPlayers.containsKey(player.getUniqueId())) {
				sittingPlayers.remove(player.getUniqueId());
				player.sendMessage(ChatColor.GRAY + "You are no longer sitting.");
				event.getDismounted().remove();
				Bukkit.getScheduler().scheduleSyncDelayedTask(Main.getInstance(), new Runnable() {
					
					@Override
					public void run() {
						if(!sittingPlayers.containsKey(player.getUniqueId()))
							player.teleport(player.getLocation().add(0, 0.6, 0));
					}
				}, 2);
			}
		}
	}
	
	@EventHandler
	public void onLeave(PlayerQuitEvent event) {
		if(sittingPlayers.containsKey(event.getPlayer().getUniqueId())) {
			event.getPlayer().getVehicle().remove();
			event.getPlayer().teleport(event.getPlayer().getLocation().add(0, 0.6, 0));
			sittingPlayers.remove(event.getPlayer().getUniqueId());
		}
	}
	
	@EventHandler
	public void onDisable(PluginDisableEvent event) {
		if(event.getPlugin().getName().equals(Main.getInstance().getDescription().getName())) {
			for(UUID u : sittingPlayers.keySet()) {
				Bukkit.getPlayer(u).getVehicle().remove();
				Bukkit.getPlayer(u).teleport(Bukkit.getPlayer(u).getLocation().add(0, 0.6, 0));
			}
		}
	}
	
	private void sit(Player player, Block b) {		
		Stairs stair = (Stairs) b.getState().getData();	
		Location location = getCenter(b.getLocation().add(0, -1, 0));
		location.setYaw(blockFaceToYaw(stair.getFacing()));
		ArmorStand stand = (ArmorStand) player.getLocation().getWorld().spawnEntity(location, EntityType.ARMOR_STAND);
		
		stand.setSmall(true);
		stand.setVisible(false);
		stand.setGravity(false);
		stand.setInvulnerable(true);
		stand.addPassenger(player);
		player.sendMessage(ChatColor.GRAY + "You are now sitting.");
	}
	
	private int blockFaceToYaw(BlockFace face) {
		switch(face) {
			case EAST:
				return -90;
			case NORTH:
				return 180;
			case NORTH_EAST:
				return -135;
			case NORTH_WEST:
				return 135;
			case SOUTH:
				return 0;
			case SOUTH_EAST:
				return -45;
			case SOUTH_WEST:
				return 45;
			case WEST:
				return 90;
			default:
				return 0;
		}
	}
	
	private boolean isStair(Block b) {
		switch(b.getType()) {
			default:
				return false;
			case ACACIA_STAIRS:
			case BIRCH_WOOD_STAIRS:
			case BRICK_STAIRS:
			case COBBLESTONE_STAIRS:
			case DARK_OAK_STAIRS:
			case JUNGLE_WOOD_STAIRS:
			case NETHER_BRICK_STAIRS:
			case PURPUR_STAIRS:
			case QUARTZ_STAIRS:
			case RED_SANDSTONE_STAIRS:
			case SANDSTONE_STAIRS:
			case SMOOTH_STAIRS:
			case SPRUCE_WOOD_STAIRS:
			case WOOD_STAIRS:
				return true;
		}
	}
	
	private boolean isMaterialHollowForChairs(Material material) {
		switch(material) {
			default:
				return false;
			case AIR:
			case TORCH:
			case SIGN_POST:
			case LADDER:
			case WALL_SIGN:
			case LEVER:
			case REDSTONE_TORCH_OFF:
			case REDSTONE_TORCH_ON:
			case STONE_BUTTON:
			case VINE:
				return true;
		}
		
	}
	
	public Location getCenter(Location location) {
		return new Location(location.getWorld(), getCenter(location.getBlockX()), getCenter(location.getBlockY()), getCenter(location.getBlockZ()));
	}

	private double getCenter(final int location) {
		final double newLocation = location;
		return newLocation + 0.5D;
	}
	
}

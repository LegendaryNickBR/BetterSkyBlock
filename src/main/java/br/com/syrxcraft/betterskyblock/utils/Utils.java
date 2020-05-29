package br.com.syrxcraft.betterskyblock.utils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.UUID;

import br.com.syrxcraft.betterskyblock.BetterSkyBlock;
import com.flowpowered.math.vector.Vector3i;
import com.sk89q.worldedit.blocks.TileEntityBlock;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.Vector2D;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.function.biome.BiomeReplace;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.session.ClipboardHolder;
import com.sk89q.worldedit.world.biome.BaseBiome;
import com.sk89q.worldedit.world.biome.Biomes;
import com.sk89q.worldedit.world.registry.LegacyWorldData;
import com.sk89q.worldedit.world.registry.WorldData;
import org.bukkit.scheduler.BukkitRunnable;

public class Utils {

	public static UUID toUUID(byte[] bytes) {
	    if (bytes.length != 16) {
	        throw new IllegalArgumentException();
	    }
	    int i = 0;
	    long msl = 0;
	    for (; i < 8; i++) {
	        msl = (msl << 8) | (bytes[i] & 0xFF);
	    }
	    long lsl = 0;
	    for (; i < 16; i++) {
	        lsl = (lsl << 8) | (bytes[i] & 0xFF);
	    }
	    return new UUID(msl, lsl);
	}
	
	public static String UUIDtoHexString(UUID uuid) {
		if (uuid==null) return "0x0";
		return "0x"+org.apache.commons.lang.StringUtils.leftPad(Long.toHexString(uuid.getMostSignificantBits()), 16, "0")+org.apache.commons.lang.StringUtils.leftPad(Long.toHexString(uuid.getLeastSignificantBits()), 16, "0");
	}
	
	public static boolean isFakePlayer(Player player) {
		for (Player p : Bukkit.getOnlinePlayers()) {
			if(player==p) {
				return false;
			}
		}
		return true;
	}
	
	public static String sanitizeSql(String string) {
		return string.replace("\\", "\\\\").replace("\"", "\\\"");
	}
	
	public static void loadSchematic(File schematic, Location location) {
		try {
			// read schematic file
			FileInputStream fis = new FileInputStream(schematic);
			BufferedInputStream bis = new BufferedInputStream(fis);
			ClipboardReader reader = ClipboardFormat.SCHEMATIC.getReader(bis);
			
			// create clipboard
			WorldData worldData = LegacyWorldData.getInstance();
			Clipboard clipboard = reader.read(worldData);
			fis.close();
			
			ClipboardHolder clipboardHolder = new ClipboardHolder(clipboard, worldData);
			EditSession editSession = WorldEdit.getInstance().getEditSessionFactory().getEditSession(fromBukkitToWorldEditWorld(location.getWorld()), 1000000);
			
			Operation operation = clipboardHolder.createPaste(editSession, LegacyWorldData.getInstance()).to(toVector(location)).ignoreAirBlocks(true).build();
			Operations.completeLegacy(operation);
		} catch (MaxChangedBlocksException | IOException e) {
			e.printStackTrace();
		}
	}
	
	public static boolean regen(Location center, int blockRadius) {
		com.sk89q.worldedit.world.World world = fromBukkitToWorldEditWorld(center.getWorld());
		EditSession editSession = WorldEdit.getInstance().getEditSessionFactory().getEditSession(world, 1000000);
		Region region = new CuboidRegion(new Vector(center.getBlockX()-blockRadius,0,center.getBlockZ()-blockRadius), new Vector(center.getBlockX()+blockRadius,255,center.getBlockZ()+blockRadius));
		
		
		return world.regenerate(region, editSession);
	}
	
	public static void setBiome(Location location, int radius, String biomeName) {
		com.sk89q.worldedit.world.World world = fromBukkitToWorldEditWorld(location.getWorld());
		EditSession editSession = WorldEdit.getInstance().getEditSessionFactory().getEditSession((com.sk89q.worldedit.world.World) world, 1000000);
		world.getWorldData().getBiomeRegistry().getBiomes();
		BaseBiome biome = Biomes.findBiomeByName(world.getWorldData().getBiomeRegistry().getBiomes(), biomeName, world.getWorldData().getBiomeRegistry());
		if (biome == null) {
			throw new IllegalStateException("Biome not found");
		}
		
		BiomeReplace biomeReplace = new BiomeReplace(editSession, biome);
		try {
			for (int x = location.getBlockX()-radius; x<=location.getBlockX()+radius; x++) {
				for (int z = location.getBlockZ()-radius; z<=location.getBlockZ()+radius; z++) {
					biomeReplace.apply(new Vector2D(x, z));
				}
			}
		} catch (WorldEditException e) {
			e.printStackTrace();
		}
	}
	
	public static Biome matchAllowedBiome(String biomeName) {
		biomeName = biomeName.toLowerCase();
		for (Biome biome : BetterSkyBlock.getInstance().config().getAllowedBiomes()) {
			if (biome.toString().replace("_", "").toLowerCase().equals(biomeName)) {
				return biome;
			}
		}
		return null;
	}

	public static String fromSnakeToCamelCase(String string) {
		StringBuilder sb = new StringBuilder();
		
		for (String s : string.split("_")) {
			sb.append((""+s.charAt(0)).toUpperCase());
			sb.append(s.substring(1).toLowerCase());
		}
		
		return sb.toString();
	}
	

	public static com.sk89q.worldedit.world.World fromBukkitToWorldEditWorld(org.bukkit.World world) {
		for (com.sk89q.worldedit.world.World w : WorldEdit.getInstance().getServer().getWorlds()) {
			if (world.getName().equals(w.getName())) {
				return w;
			}
		}
		return null;
	}
	
	public static Vector toVector(Location loc) {
		return new Vector(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
	}

	public static String toString(Location location) {
		return "[World: " + location.getWorld().getName() + ", X: " + location.getBlockX() + ", Y: " + location.getBlockY() + ", Z: " + location.getBlockZ()+"]";
	}

	public static int validate(int Min, int Max, int value){

		if(value < Min){
			return Min;
		}else if(value > Max){
			return Max;
		}

		return value;
	}

	public static int validate(int Min, int Max, int defaultValue, int value){

		if(value < Min || value > Max) return defaultValue;

		return value;
	}

	public static Biome getBiome(String name){

		for(Biome biome : Biome.values()){
			if(biome.name().equalsIgnoreCase(name)){
				return biome;
			}
		}

		return null;
	}

	public static World worldFromUUID(UUID uuid){
		return Bukkit.getWorld(uuid);
	}

	public static void bukkitSync(Runnable runnable){
		Bukkit.getScheduler().runTask(BetterSkyBlock.getInstance(), runnable);
	}

	public static Player asBukkitPlayer(UUID uuid){
		return Bukkit.getPlayer(uuid);
	}

	public static Vector3i locationToVector(Location location){
		return new Vector3i(location.getBlockX(),location.getBlockY(),location.getBlockZ());
	}
}

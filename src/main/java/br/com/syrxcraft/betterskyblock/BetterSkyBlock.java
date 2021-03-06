package br.com.syrxcraft.betterskyblock;

import br.com.syrxcraft.betterskyblock.core.api.BetterSkyBlockAPI;
import br.com.syrxcraft.betterskyblock.border.IslandBorder;
import br.com.syrxcraft.betterskyblock.commands.CommandManager;
import br.com.syrxcraft.betterskyblock.integration.IntegrationManager;
import br.com.syrxcraft.betterskyblock.config.Config;
import br.com.syrxcraft.betterskyblock.core.data.DataStore;
import br.com.syrxcraft.betterskyblock.listeners.EventManager;
import br.com.syrxcraft.betterskyblock.utils.LoggerHelper;
import com.griefdefender.api.GriefDefender;
import com.griefdefender.api.claim.ClaimManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;

public class BetterSkyBlock extends JavaPlugin {

	private static BetterSkyBlock instance;

	private Config config;
	private DataStore dataStore;
	private LoggerHelper loggerHelper;
	private IntegrationManager integrationManager;
	private CommandManager commandManager;
	private EventManager eventManager;
	private World islandWorld;
	private BetterSkyBlockAPI betterSkyBlockAPI;

	@Override
	public void onEnable() {
		instance = this;

		loggerHelper = new LoggerHelper(this);

		loggerHelper.info("Hello World!");
		loggerHelper.info("Better Sky Block - " + getDescription().getVersion());


		config = new Config(this);

		islandWorld = loadWorld();

		integrationManager = new IntegrationManager(this);

		dataStore = DataStore.getInstance();

		eventManager = new EventManager(this);

		commandManager = new CommandManager().load();

		IslandBorder.REGISTER(this);

		betterSkyBlockAPI = new BetterSkyBlockAPI(this);

		showInfo();
	}

	@Override
	public void onDisable() {
		loggerHelper.info("Saving all islands.");
		dataStore.getDataProvider().onStop(this);
	}

	public LoggerHelper getLoggerHelper() {
		return loggerHelper;
	}

	public EventManager getEventManager() {
		return eventManager;
	}

	public static BetterSkyBlock getInstance() {
		return instance;
	}

	public Config config() {
		return config;
	}

	public DataStore getDataStore() {
		return dataStore;
	}

	public Location getSpawn() {
		return islandWorld.getSpawnLocation();
	}

	public IntegrationManager getIntegrationManager() {
		return integrationManager;
	}

	public Config getPluginConfig() {
		return config;
	}

	public World getIslandWorld() {
		return islandWorld;
	}

	public ClaimManager getClaimManager(){
		return GriefDefender.getCore().getClaimManager(islandWorld.getUID());
	}

	public CommandManager getCommandManager() {
		return commandManager;
	}

	public BetterSkyBlockAPI getBetterSkyBlockAPI() {
		return betterSkyBlockAPI;
	}

	public void showInfo(){
		loggerHelper.info("Some Info: ");
		loggerHelper.info(" Server version:			"	+ Bukkit.getVersion().replace("git-",""));
		loggerHelper.info(" Island World:			"	+ getIslandWorld().getName());
		loggerHelper.info(" DataProvider:			"	+ getDataStore().getDataProvider().getProvider().name());
		loggerHelper.info(" GriefDefenderAPI:		"	+ GriefDefender.getVersion().getApiVersion());
		loggerHelper.info(" GriefDefender:			"	+ Bukkit.getPluginManager().getPlugin("GriefDefender").getDescription().getVersion());
		loggerHelper.info("   --+--   ");
	}

	public World loadWorld(){
		return Bukkit.getWorld(config.getWorldName());
	}

}

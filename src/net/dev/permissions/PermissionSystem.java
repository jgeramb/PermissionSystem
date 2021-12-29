package net.dev.permissions;

import java.lang.reflect.Field;
import java.util.*;

import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.*;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.StringUtil;

import net.dev.eazynick.api.NickManager;
import net.dev.permissions.commands.*;
import net.dev.permissions.hooks.*;
import net.dev.permissions.listeners.*;
import net.dev.permissions.nms.*;
import net.dev.permissions.sql.MySQL;
import net.dev.permissions.sql.MySQLPermissionManager;
import net.dev.permissions.utilities.*;
import net.dev.permissions.utilities.mojang.*;
import net.dev.permissions.utilities.permissionmanagement.*;
import net.dev.permissions.webserver.WebFileManager;
import net.dev.permissions.webserver.WebServerManager;
import net.milkbowl.vault.chat.Chat;

public class PermissionSystem extends JavaPlugin {

	private static PermissionSystem instance;
	
	public static PermissionSystem getInstance() {
		return instance;
	}
	
	private MySQLPermissionManager mysqlPermissionManager;
	private Utilities utilities;
	private FileUtils fileUtils;
	private PermissionConfigUtils permissionConfigUtils;
	private ImportUtils importUtils;
	private ReflectionHelper reflectionHelper;
	private ScoreboardTeamHandler scoreboardTeamHandler;
	private PermissionGroupManager permissionGroupManager;
	private PermissionUserManager permissionUserManager;
	private PermissionManager permissionManager;
	private UUIDFetcher_1_7 uuidFetcher_1_7;
	private UUIDFetcher_1_8_R1 uuidFetcher_1_8_R1;
	private UUIDFetcher uuidFetcher;
	private UUIDFetching uuidFetching;
	
	private HashMap<String, String> groupNames = new HashMap<>();
	private MySQL mysql;
	private Field field;
	private Timer timer;
	
	private WebServerManager webServerManager;
	private WebFileManager webFileManager;
	
	private Object vaultPermission, vaultChat;
	
	@Override
	public void onEnable() {
		instance = this;
		
		fileUtils = new FileUtils();
		permissionConfigUtils = new PermissionConfigUtils();
		reflectionHelper = new ReflectionHelper();
		scoreboardTeamHandler = new ScoreboardTeamHandler();
		permissionGroupManager = new PermissionGroupManager();
		permissionUserManager = new PermissionUserManager();
		permissionManager = new PermissionManager();

		String version = reflectionHelper.getVersion();
		
		if(version.equals("1_7_R4"))
			uuidFetcher_1_7 = new UUIDFetcher_1_7();
		else if(version.equals("1_8_R1"))
			uuidFetcher_1_8_R1 = new UUIDFetcher_1_8_R1();
		else
			uuidFetcher = new UUIDFetcher();

		uuidFetching = new UUIDFetching();
		utilities = new Utilities();
		
		fileUtils.reloadConfig();
		
		YamlConfiguration configuration = fileUtils.getConfiguration();
		
		if (configuration.getBoolean("MySQL.Enabled")) {
			mysql = new MySQL(configuration.getString("MySQL.host"), configuration.getString("MySQL.port"), configuration.getString("MySQL.database"), configuration.getString("MySQL.user"), configuration.getString("MySQL.password"));
			mysql.connect();

			mysql.update("CREATE TABLE IF NOT EXISTS PermissionUsers (uuid varchar(64) PRIMARY KEY NOT NULL, prefix varchar(64), suffix varchar(64), chat_prefix varchar(64), chat_suffix varchar(64), permissions text(32000), temp_group_name varchar(256), temp_group_time bigint)");
			mysql.update("CREATE TABLE IF NOT EXISTS PermissionGroups (name varchar(256) PRIMARY KEY NOT NULL, prefix varchar(64), suffix varchar(64), chat_prefix varchar(64), chat_suffix varchar(64), is_default boolean, weight int, parent varchar(64), members text(32000), permissions text(32000))");
			mysql.update("CREATE TABLE IF NOT EXISTS UUIDCache (uuid varchar(64) PRIMARY KEY NOT NULL)");
			
			mysqlPermissionManager = new MySQLPermissionManager(mysql);
			importUtils = new ImportUtils();
				
			timer = new Timer();
			timer.schedule(new TimerTask() {
				
				@Override
				public void run() {
					mysqlPermissionManager.setValues(new HashMap<>());
					mysqlPermissionManager.collectUserData();
					mysqlPermissionManager.collectGroupData();
				}
			}, 0, 10000);
		}

		getCommand("perms").setTabCompleter(new TabCompleter() {
			
			@Override
			public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
				List<String> tabCompletions = new ArrayList<>();

				if(cmd.getName().equalsIgnoreCase("perms")) {
					if(sender.hasPermission(new Permission("permissions.*", PermissionDefault.FALSE))) {
						if(args.length <= 1) {
							tabCompletions.add("user");
							tabCompletions.add("group");
							tabCompletions.add("import");
						} else if(args.length == 2) {
							if(args[0].equalsIgnoreCase("import")) {
								tabCompletions.add("sql");
								tabCompletions.add("file");
							} else if(args[0].equalsIgnoreCase("user"))
								Bukkit.getOnlinePlayers().forEach(all -> tabCompletions.add(all.getName()));
							else if(args[0].equalsIgnoreCase("group"))
								permissionGroupManager.getPermissionGroups().forEach(group -> tabCompletions.add(group.getName()));
						} else if(args.length == 3) {
							if(args[0].equalsIgnoreCase("user")) {
								tabCompletions.add("setgroup");
								tabCompletions.add("setprefix");
								tabCompletions.add("setsuffix");
								tabCompletions.add("setchatprefix");
								tabCompletions.add("setchatsuffix");
								tabCompletions.add("add");
								tabCompletions.add("remove");
								tabCompletions.add("clear");
							} else if(args[0].equalsIgnoreCase("group")) {
								tabCompletions.add("addmember");
								tabCompletions.add("removemember");
								tabCompletions.add("setweight");
								tabCompletions.add("setdefault");
								tabCompletions.add("setprefix");
								tabCompletions.add("setsuffix");
								tabCompletions.add("setchatprefix");
								tabCompletions.add("setchatsuffix");
								tabCompletions.add("setparent");
								tabCompletions.add("add");
								tabCompletions.add("remove");
								tabCompletions.add("clear");
								tabCompletions.add("create");
								tabCompletions.add("delete");
							}
						} else if(args.length == 4) {
							if(args[0].equalsIgnoreCase("user") && args[2].equalsIgnoreCase("setgroup"))
								permissionGroupManager.getPermissionGroups().forEach(group -> tabCompletions.add(group.getName()));
							else if(args[0].equalsIgnoreCase("group")) {
								if(args[2].equalsIgnoreCase("addmember") || args[2].equalsIgnoreCase("removemember"))
									Bukkit.getOnlinePlayers().forEach(group -> tabCompletions.add(group.getName()));
								else if(args[2].equalsIgnoreCase("setparent"))
									permissionGroupManager.getPermissionGroups().stream().filter(group -> !(group.getName().equals(args[1]))).forEach(group -> tabCompletions.add(group.getName()));
							}
						} else if(args.length == 6) {
							if(args[0].equalsIgnoreCase("user") && args[2].equalsIgnoreCase("setgroup")) {
								tabCompletions.add("seconds");
								tabCompletions.add("minutes");
								tabCompletions.add("hours");
								tabCompletions.add("days");
								tabCompletions.add("months");
								tabCompletions.add("years");
							}
						}
						
						tabCompletions.removeIf(tabCompletion -> !(StringUtil.copyPartialMatches(args[args.length - 1], tabCompletions, new ArrayList<String>()).contains(tabCompletion)));
						Collections.sort(tabCompletions);
					}
				}
				
				return tabCompletions;
			}
		});
		
		getCommand("rank").setTabCompleter(new TabCompleter() {
			
			@Override
			public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
				List<String> tabCompletions = new ArrayList<>();

				if(cmd.getName().equalsIgnoreCase("rank")) {
					if(sender.hasPermission(new Permission("permissions.*", PermissionDefault.FALSE)) || sender.hasPermission(new Permission("permissions.rank", PermissionDefault.FALSE))) {
						if(args.length <= 1)
							Bukkit.getOnlinePlayers().forEach(all -> tabCompletions.add(all.getName()));
						else if(args.length == 2)
							permissionGroupManager.getPermissionGroups().forEach(group -> tabCompletions.add(group.getName()));
						else if(args.length == 4) {
							tabCompletions.add("seconds");
							tabCompletions.add("minutes");
							tabCompletions.add("hours");
							tabCompletions.add("days");
							tabCompletions.add("months");
							tabCompletions.add("years");
						}
						
						tabCompletions.removeIf(tabCompletion -> !(StringUtil.copyPartialMatches(args[args.length - 1], tabCompletions, new ArrayList<String>()).contains(tabCompletion)));
						Collections.sort(tabCompletions);
					}
				}
				
				return tabCompletions;
			}
		});
		
		getCommand("debugperms").setExecutor(new DebugPermsCommand());
		getCommand("perms").setExecutor(new PermsCommand());
		getCommand("rank").setExecutor(new RankCommand());

		PluginManager pm = Bukkit.getPluginManager();
		pm.registerEvents(new AsyncPlayerChatListener(), this);
		pm.registerEvents(new PlayerJoinListener(), this);
		pm.registerEvents(new PlayerQuitListener(), this);
		pm.registerEvents(new PlayerKickListener(), this);
		pm.registerEvents(new PlayerChangedWorldListener(), this);

		Bukkit.getScheduler().runTaskLater(this, new Runnable() {
			
			@Override
			public void run() {
				Bukkit.getOnlinePlayers().forEach(all -> inject(all));
				permissionManager.updateAllPermissions();

				if (permissionGroupManager.getPermissionGroups().isEmpty()) {
					PermissionGroup group = new PermissionGroup("DEFAULT");
					group.registerGroupIfNotExisting();
					group.setDefault(true);
					group.setWeight(999);
				}
				
				if(configuration.getBoolean("WebServer.Enabled"))
					webServerManager = new WebServerManager(configuration.getInt("WebServer.Port"));
				
				if (configuration.getBoolean("Settings.UsePrefixesAndSuffixes")) {
					updatePrefixesAndSuffixes();
					
					Bukkit.getScheduler().scheduleSyncRepeatingTask(instance, new Runnable() {

						@Override
						public void run() {
							for (Player all : Bukkit.getOnlinePlayers()) {
								if (isEazyNickInstalled()) {
									NickManager api = new NickManager(all);

									if (!(api.isNicked()))
										scoreboardTeamHandler.addPlayerToTeam(scoreboardTeamHandler.getTeamName(all), all.getName());
									else
										scoreboardTeamHandler.removePlayerFromTeams(api.getRealName());
								} else
									scoreboardTeamHandler.addPlayerToTeam(scoreboardTeamHandler.getTeamName(all), all.getName());
							}

							scoreboardTeamHandler.updateTeams();

							if(configuration.getBoolean("MySQL.Enabled")) {
								for(String uuid : mysqlPermissionManager.getUUIDCache()) {
									String tempGroupName = mysqlPermissionManager.getPlayerTempGroupName(uuid);
									
									if((tempGroupName != null) && (mysqlPermissionManager.getPlayerTempGroupTime(uuid) <= System.currentTimeMillis())) {
										new PermissionGroup(tempGroupName).removeMemberWithUUID(uuid);
										
										updatePrefixesAndSuffixes();
										
										mysqlPermissionManager.setPlayerTempGroup(uuid, null, 0L);
									}
								}
							} else
								permissionConfigUtils.updateTempRanks();
						}
					}, 50, 20);
				}
			}
		}, 25);
		
		if(isPlaceholderAPIInstalled())
			new PlaceHolderExpansion(instance).register();
		
		if(isVaultInstalled()) {
			vaultPermission = new VaultPermissionHandler(this);
			vaultChat = new VaultChatHandler(this, (net.milkbowl.vault.permission.Permission) vaultPermission);
			
			ServicesManager servicesManager = Bukkit.getServicesManager();
	        servicesManager.register(net.milkbowl.vault.permission.Permission.class, (net.milkbowl.vault.permission.Permission) vaultPermission, this, ServicePriority.High);
	        servicesManager.register(net.milkbowl.vault.chat.Chat.class, (net.milkbowl.vault.chat.Chat) vaultChat, this, ServicePriority.High);   
		}
		
		utilities.sendConsole("§eThe system has been enabled§7!");
	}

	@Override
	public void onDisable() {
		if(timer != null)
			timer.cancel();
		
		if(webServerManager != null)
			webServerManager.stop();
		
		if (fileUtils.getConfiguration().getBoolean("Settings.UsePrefixesAndSuffixes")) {
			for (String team : scoreboardTeamHandler.getTeamMembers().keySet()) {
				if (team != null)
					scoreboardTeamHandler.destroyTeam(team);
			}
		}
		
		permissionConfigUtils.saveFile();
		
		if(isVaultInstalled()) {
			ServicesManager servicesManager = Bukkit.getServicesManager();
			servicesManager.unregister(net.milkbowl.vault.permission.Permission.class, vaultPermission);
	        servicesManager.unregister(Chat.class, vaultChat);
		}
		
		utilities.sendConsole("§cThe system has been disabled§7!");
	}

	public void updatePrefixesAndSuffixes() {
		if (fileUtils.getConfiguration().getBoolean("Settings.UsePrefixesAndSuffixes")) {
			scoreboardTeamHandler.setTeamMembers(new HashMap<>());
			scoreboardTeamHandler.setTeamPrefixes(new HashMap<>());
			scoreboardTeamHandler.setTeamPrefixesChat(new HashMap<>());
			scoreboardTeamHandler.setTeamPrefixesPlayerList(new HashMap<>());
			scoreboardTeamHandler.setTeamSuffixes(new HashMap<>());
			scoreboardTeamHandler.setTeamSuffixesChat(new HashMap<>());
			scoreboardTeamHandler.setTeamSuffixesPlayerList(new HashMap<>());
			
			for (PermissionGroup group : permissionGroupManager.getPermissionGroups()) {
				group.registerGroupIfNotExisting();
				
				String name = group.getName();
				String weight = String.valueOf(group.getWeight());
				String rankWeight = weight;

				for (int i = 1; i < (3 - weight.length()); i++) {
					if((rankWeight + name).length() < 16)
						rankWeight = "0" + rankWeight;
				}

				String rankName = rankWeight + name;
				String prefix = group.getPrefix();
				String chatPrefix = group.getChatPrefix();
				String suffix = group.getSuffix();
				String chatSuffix = group.getChatSuffix();
				
				if(rankName.length() > 16)
					rankName = rankName.substring(rankName.length() - 16);
				
				if(prefix.length() > 16)
					prefix = prefix.substring(0, 16);
				
				if(suffix.length() > 16)
					suffix = suffix.substring(0, 16);
				
				groupNames.put(name, rankName);
				scoreboardTeamHandler.getTeamMembers().put(rankName, new ArrayList<>());
				scoreboardTeamHandler.getTeamPrefixes().put(rankName, prefix);
				scoreboardTeamHandler.getTeamPrefixesChat().put(rankName, chatPrefix);
				scoreboardTeamHandler.getTeamPrefixesPlayerList().put(rankName, prefix);
				scoreboardTeamHandler.getTeamSuffixes().put(rankName, suffix);
				scoreboardTeamHandler.getTeamSuffixesChat().put(rankName, chatSuffix);
				scoreboardTeamHandler.getTeamSuffixesPlayerList().put(rankName, suffix);
				
				utilities.sendDebugMessage("§eTeam of group §a" + name + " §eis §d" + rankName + "§7!");
			}

			for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
				PermissionUser user = permissionUserManager.getPermissionPlayer(onlinePlayer.getName());
				PermissionGroup group = user.getHighestGroup();
				String rankWeight = String.valueOf(group.getWeight());

				for (int i = 1; i < (3 - String.valueOf(group.getWeight()).length()); i++) {
					if((rankWeight + onlinePlayer.getName()).length() < 16)
						rankWeight = "0" + rankWeight;
				}
				
				String rankName = rankWeight + onlinePlayer.getName();
				String prefix = user.getPrefix() + user.getGroupPrefix();
				String chatPrefix = user.getChatPrefix() + user.getGroupChatPrefix();
				String suffix = user.getGroupSuffix() + user.getSuffix();
				String chatSuffix = user.getChatSuffix() + user.getGroupChatSuffix();

				if(rankName.length() > 16)
					rankName = rankName.substring(0, 16);
				
				if(prefix.length() > 16)
					prefix = prefix.substring(0, 16);
				
				if(suffix.length() > 16)
					suffix = suffix.substring(0, 16);
				
				scoreboardTeamHandler.getTeamMembers().put(rankName, new ArrayList<>());
				scoreboardTeamHandler.getTeamPrefixes().put(rankName, prefix);
				scoreboardTeamHandler.getTeamPrefixesChat().put(rankName, chatPrefix);
				scoreboardTeamHandler.getTeamPrefixesPlayerList().put(rankName, prefix);
				scoreboardTeamHandler.getTeamSuffixes().put(rankName, suffix);
				scoreboardTeamHandler.getTeamSuffixesChat().put(rankName, chatSuffix);
				scoreboardTeamHandler.getTeamSuffixesPlayerList().put(rankName, suffix);
				
				utilities.sendDebugMessage("§eTeam of player §a" + onlinePlayer.getName() + " §eis §d" + rankName + "§7!");
			}
		}
	}
	
	public void inject(Player player) {
		try {
			if(field == null)
				field = Class.forName("org.bukkit.craftbukkit.v" + reflectionHelper.getVersion() + ".entity.CraftHumanEntity").getDeclaredField("perm");
			
			field.setAccessible(true);
			field.set(player, new PermissibleBaseOverride(player));
			field.setAccessible(false);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public boolean isEazyNickInstalled() {
		Plugin plugin = Bukkit.getPluginManager().getPlugin("EazyNick");
		
		return ((plugin != null) && plugin.isEnabled());
	}
	
	public boolean isClansAPIInstalled() {
		return (Bukkit.getPluginManager().getPlugin("ClansAPI") != null);
	}
	
	public boolean isPlaceholderAPIInstalled() {
		return (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null);
	}
	
	public boolean isVaultInstalled() {
		return (Bukkit.getPluginManager().getPlugin("Vault") != null);
	}
	
	public HashMap<String, String> getGroupNames() {
		return groupNames;
	}
	
	public MySQL getMySQL() {
		return mysql;
	}
	
	public WebServerManager getWebServerManager() {
		return webServerManager;
	}
	
	public WebFileManager getWebFileManager() {
		return webFileManager;
	}
	
	public MySQLPermissionManager getMySQLPermissionManager() {
		return mysqlPermissionManager;
	}
	
	public Utilities getUtils() {
		return utilities;
	}
	
	public FileUtils getFileUtils() {
		return fileUtils;
	}

	public PermissionConfigUtils getPermissionConfigUtils() {
		return permissionConfigUtils;
	}
	
	public ImportUtils getImportUtils() {
		return importUtils;
	}
	
	public ScoreboardTeamHandler getScoreboardTeamHandler() {
		return scoreboardTeamHandler;
	}
	
	public ReflectionHelper getReflectionHelper() {
		return reflectionHelper;
	}
	
	public PermissionGroupManager getPermissionGroupManager() {
		return permissionGroupManager;
	}
	
	public PermissionUserManager getPermissionUserManager() {
		return permissionUserManager;
	}
	
	public UUIDFetcher getUUIDFetcher() {
		return uuidFetcher;
	}
	
	public UUIDFetcher_1_7 getUUIDFetcher_1_7() {
		return uuidFetcher_1_7;
	}
	
	public UUIDFetcher_1_8_R1 getUUIDFetcher_1_8_R1() {
		return uuidFetcher_1_8_R1;
	}
	
	public UUIDFetching getUUIDFetching() {
		return uuidFetching;
	}
	
	public void setWebFileManager(WebFileManager webFileManager) {
		this.webFileManager = webFileManager;
	}
	
	public void setPermissionConfigUtils(PermissionConfigUtils permissionConfigUtils) {
		this.permissionConfigUtils = permissionConfigUtils;
	}

}
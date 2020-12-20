package net.dev.permissions;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.StringUtil;

import net.dev.eazynick.api.NickManager;
import net.dev.permissions.commands.DebugPermsCommand;
import net.dev.permissions.commands.PermsCommand;
import net.dev.permissions.commands.RankCommand;
import net.dev.permissions.listeners.AsyncPlayerChatListener;
import net.dev.permissions.listeners.PlayerChangedWorldListener;
import net.dev.permissions.listeners.PlayerJoinListener;
import net.dev.permissions.listeners.PlayerKickListener;
import net.dev.permissions.listeners.PlayerQuitListener;
import net.dev.permissions.placeholders.PlaceHolderExpansion;
import net.dev.permissions.sql.MySQL;
import net.dev.permissions.sql.MySQLPermissionManager;
import net.dev.permissions.utils.FileUtils;
import net.dev.permissions.utils.ImportUtils;
import net.dev.permissions.utils.PermissionConfigUtils;
import net.dev.permissions.utils.Utils;
import net.dev.permissions.utils.fetching.UUIDFetcher;
import net.dev.permissions.utils.fetching.UUIDFetcher_1_7;
import net.dev.permissions.utils.fetching.UUIDFetcher_1_8_R1;
import net.dev.permissions.utils.fetching.UUIDFetching;
import net.dev.permissions.utils.permissionmanagement.PermissionGroup;
import net.dev.permissions.utils.permissionmanagement.PermissionGroupManager;
import net.dev.permissions.utils.permissionmanagement.PermissionManager;
import net.dev.permissions.utils.permissionmanagement.PermissionUser;
import net.dev.permissions.utils.permissionmanagement.PermissionUserManager;
import net.dev.permissions.utils.reflect.PermissibleBaseOverride;
import net.dev.permissions.utils.reflect.ReflectUtils;
import net.dev.permissions.utils.reflect.TeamUtils;
import net.dev.permissions.webserver.WebFileManager;
import net.dev.permissions.webserver.WebServerManager;

public class PermissionSystem extends JavaPlugin {

	private static PermissionSystem instance;
	
	public static PermissionSystem getInstance() {
		return instance;
	}
	
	private MySQLPermissionManager mysqlPermissionManager;
	private Utils utils;
	private FileUtils fileUtils;
	private PermissionConfigUtils permissionConfigUtils;
	private ImportUtils importUtils;
	private ReflectUtils reflectUtils;
	private TeamUtils teamUtils;
	private PermissionGroupManager permissionGroupManager;
	private PermissionUserManager permissionUserManager;
	private PermissionManager permissionManager;
	private UUIDFetcher_1_7 uuidFetcher_1_7;
	private UUIDFetcher_1_8_R1 uuidFetcher_1_8_R1;
	private UUIDFetcher uuidFetcher;
	private UUIDFetching uuidFetching;
	
	private HashMap<String, String> groupNames = new HashMap<>();
	private MySQL mysql;
	private Field f;
	private Timer t;
	
	private WebServerManager webServerManager;
	private WebFileManager webFileManager;
	
	@Override
	public void onEnable() {
		instance = this;
		
		fileUtils = new FileUtils();
		permissionConfigUtils = new PermissionConfigUtils();
		reflectUtils = new ReflectUtils();
		teamUtils = new TeamUtils();
		permissionGroupManager = new PermissionGroupManager();
		permissionUserManager = new PermissionUserManager();
		permissionManager = new PermissionManager();
		
		String version = reflectUtils.getVersion();
		
		if(version.equals("v1_7_R4"))
			uuidFetcher_1_7 = new UUIDFetcher_1_7();
		else if(version.equals("v1_8_R1"))
			uuidFetcher_1_8_R1 = new UUIDFetcher_1_8_R1();
		else
			uuidFetcher = new UUIDFetcher();

		uuidFetching = new UUIDFetching();
		utils = new Utils();
		
		fileUtils.reloadConfig();
		
		YamlConfiguration cfg = fileUtils.getConfig();
		
		if (cfg.getBoolean("MySQL.Enabled")) {
			mysql = new MySQL(cfg.getString("MySQL.host"), cfg.getString("MySQL.port"), cfg.getString("MySQL.database"), cfg.getString("MySQL.user"), cfg.getString("MySQL.password"));
			mysql.connect();

			mysql.update("CREATE TABLE IF NOT EXISTS PermissionUsers (uuid varchar(64) PRIMARY KEY NOT NULL, prefix varchar(64), suffix varchar(64), chat_prefix varchar(64), chat_suffix varchar(64), permissions text(32000), temp_group_name varchar(256), temp_group_time bigint)");
			mysql.update("CREATE TABLE IF NOT EXISTS PermissionGroups (name varchar(256) PRIMARY KEY NOT NULL, prefix varchar(64), suffix varchar(64), chat_prefix varchar(64), chat_suffix varchar(64), is_default boolean, weight int, parent varchar(64), members text(32000), permissions text(32000))");
			mysql.update("CREATE TABLE IF NOT EXISTS UUIDCache (uuid varchar(64) PRIMARY KEY NOT NULL)");
			
			mysqlPermissionManager = new MySQLPermissionManager(mysql);
			importUtils = new ImportUtils();
				
			t = new Timer();
			t.schedule(new TimerTask() {
				
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
				
				if(cfg.getBoolean("WebServer.Enabled"))
					webServerManager = new WebServerManager(cfg.getInt("WebServer.Port"));
				
				if (cfg.getBoolean("Settings.UsePrefixesAndSuffixes")) {
					updatePrefixesAndSuffixes();
					
					Bukkit.getScheduler().scheduleSyncRepeatingTask(instance, new Runnable() {

						@Override
						public void run() {
							for (Player all : Bukkit.getOnlinePlayers()) {
								if (isEazyNickInstalled()) {
									NickManager api = new NickManager(all);

									if (!(api.isNicked()))
										teamUtils.addPlayerToTeam(teamUtils.getTeamName(all), all.getName());
									else
										teamUtils.removePlayerFromTeams(api.getRealName());
								} else
									teamUtils.addPlayerToTeam(teamUtils.getTeamName(all), all.getName());
							}

							teamUtils.updateTeams();

							if(cfg.getBoolean("MySQL.Enabled")) {
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
		
		utils.sendConsole("§eThe system has been enabled§7!");
	}

	@Override
	public void onDisable() {
		if(t != null)
			t.cancel();
		
		if(webServerManager != null)
			webServerManager.stop();
		
		permissionConfigUtils.saveFile();
		
		utils.sendConsole("§cThe system has been disabled§7!");
	}

	public void updatePrefixesAndSuffixes() {
		if (fileUtils.getConfig().getBoolean("Settings.UsePrefixesAndSuffixes")) {
			teamUtils.setTeamMembers(new HashMap<>());
			teamUtils.setTeamPrefixes(new HashMap<>());
			teamUtils.setTeamPrefixesChat(new HashMap<>());
			teamUtils.setTeamPrefixesPlayerList(new HashMap<>());
			teamUtils.setTeamSuffixes(new HashMap<>());
			teamUtils.setTeamSuffixesChat(new HashMap<>());
			teamUtils.setTeamSuffixesPlayerList(new HashMap<>());
			
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
				teamUtils.getTeamMembers().put(rankName, new ArrayList<>());
				teamUtils.getTeamPrefixes().put(rankName, prefix);
				teamUtils.getTeamPrefixesChat().put(rankName, chatPrefix);
				teamUtils.getTeamPrefixesPlayerList().put(rankName, prefix);
				teamUtils.getTeamSuffixes().put(rankName, suffix);
				teamUtils.getTeamSuffixesChat().put(rankName, chatSuffix);
				teamUtils.getTeamSuffixesPlayerList().put(rankName, suffix);
				
				utils.sendDebugMessage("§eTeam of group §a" + name + " §eis §d" + rankName + "§7!");
			}

			for (Player p : Bukkit.getOnlinePlayers()) {
				PermissionUser user = permissionUserManager.getPermissionPlayer(p.getName());
				PermissionGroup group = user.getHighestGroup();
				String rankWeight = String.valueOf(group.getWeight());

				for (int i = 1; i < (3 - String.valueOf(group.getWeight()).length()); i++) {
					if((rankWeight + p.getName()).length() < 16)
						rankWeight = "0" + rankWeight;
				}
				
				String rankName = rankWeight + p.getName();
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
				
				teamUtils.getTeamMembers().put(rankName, new ArrayList<>());
				teamUtils.getTeamPrefixes().put(rankName, prefix);
				teamUtils.getTeamPrefixesChat().put(rankName, chatPrefix);
				teamUtils.getTeamPrefixesPlayerList().put(rankName, prefix);
				teamUtils.getTeamSuffixes().put(rankName, suffix);
				teamUtils.getTeamSuffixesChat().put(rankName, chatSuffix);
				teamUtils.getTeamSuffixesPlayerList().put(rankName, suffix);
				
				utils.sendDebugMessage("§eTeam of player §a" + p.getName() + " §eis §d" + rankName + "§7!");
			}
		}
	}
	
	public void inject(Player p) {
		try {
			if(f == null)
				f = Class.forName("org.bukkit.craftbukkit." + reflectUtils.getVersion() + ".entity.CraftHumanEntity").getDeclaredField("perm");
			
			f.setAccessible(true);
			f.set(p, new PermissibleBaseOverride(p));
			f.setAccessible(false);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public boolean isEazyNickInstalled() {
		Plugin plugin = Bukkit.getPluginManager().getPlugin("EazyNick");
		
		return ((plugin != null) && plugin.isEnabled());
	}
	
	public boolean isClansInstalled() {
		return (Bukkit.getPluginManager().getPlugin("Clans") != null);
	}
	
	public boolean isClansAPIInstalled() {
		return (Bukkit.getPluginManager().getPlugin("ClansAPI") != null);
	}
	
	public boolean isPlaceholderAPIInstalled() {
		return (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null);
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
	
	public Utils getUtils() {
		return utils;
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
	
	public TeamUtils getTeamUtils() {
		return teamUtils;
	}
	
	public ReflectUtils getReflectUtils() {
		return reflectUtils;
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
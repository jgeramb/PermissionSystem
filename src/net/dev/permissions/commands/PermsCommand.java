package net.dev.permissions.commands;

import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

import net.dev.permissions.PermissionSystem;
import net.dev.permissions.sql.MySQLPermissionManager;
import net.dev.permissions.utilities.*;
import net.dev.permissions.utilities.mojang.UUIDFetching;
import net.dev.permissions.utilities.permissionmanagement.*;
import net.dev.permissions.webserver.WebServerManager;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;

public class PermsCommand implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		PermissionSystem permissionSystem = PermissionSystem.getInstance();
		Utilities utilities = permissionSystem.getUtils();
		FileUtils fileUtils = permissionSystem.getFileUtils();
		PermissionConfigUtils permissionConfigUtils = permissionSystem.getPermissionConfigUtils();
		ImportUtils importUtils = permissionSystem.getImportUtils();
		PermissionGroupManager permissionGroupManager = permissionSystem.getPermissionGroupManager();
		PermissionUserManager permissionUserManager = permissionSystem.getPermissionUserManager();
		UUIDFetching uuidFetching = permissionSystem.getUUIDFetching();
		MySQLPermissionManager mysqlPermissionManager = permissionSystem.getMySQLPermissionManager();
		
		WebServerManager webServerManager = permissionSystem.getWebServerManager();
		
		String prefix = utilities.getPrefix();
		
		if(sender instanceof Player) {
			Player player = (Player) sender;
			
			if(player.hasPermission(new Permission("permissions.*", PermissionDefault.FALSE))) {
				if(args.length == 1) {
					if(args[0].equalsIgnoreCase("groups") || args[0].equalsIgnoreCase("group")) {
						List<PermissionGroup> list = permissionGroupManager.getPermissionGroups();
						
						if(!(list.isEmpty())) {
							player.sendMessage(prefix + "§eCached groups§8:");
							
							for (PermissionGroup permissionGroup : list)
								player.sendMessage(prefix + "§7- §a" + permissionGroup.getName());
						} else
							player.sendMessage(prefix + "§cThere are no groups to be shown§7!");
					} else if(args[0].equalsIgnoreCase("users") || args[0].equalsIgnoreCase("user")) {
						List<String> list = permissionUserManager.getPermissionPlayerUUIDs();
						
						if(!(list.isEmpty())) {
							player.sendMessage(prefix + "§eCached users-uuids§8:");
							
							for (String uuid : list)
								player.sendMessage(prefix + "§7- §a" + uuid);
						} else
							player.sendMessage(prefix + "§cThere are no users to be shown§7!");
					} else if(args[0].equalsIgnoreCase("reload")) {
						fileUtils.reloadConfig();
						permissionConfigUtils.reloadConfig();
						
						player.sendMessage(prefix + "§eThe config files were reloaded§7!");
					} else if(args[0].equalsIgnoreCase("editor")) {
						if(fileUtils.getConfiguration().getBoolean("WebServer.Enabled") && (webServerManager != null)) {
							String url = "http://" + utilities.getIpAddress() + ":" + fileUtils.getConfiguration().getInt("WebServer.Port") + "?authKey=" + webServerManager.getAuthKey();
							TextComponent base = new TextComponent(prefix + "§eClick on the link to get to the web editor§8: ");
							TextComponent clickable = new TextComponent("§a" + url);
							clickable.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, url));
							base.addExtra(clickable);
							
							player.spigot().sendMessage(base);
						} else
							player.sendMessage(prefix + "§cThe web server is not enabled or not running§7!");
					} else
						utilities.sendHelpMessage(player);
				} else if(args.length == 2) {
					if(args[0].equalsIgnoreCase("group")) {
						PermissionGroup permissionGroup = new PermissionGroup(args[1]);
						
						if(permissionGroup.exists()) {
							player.sendMessage(prefix + "§8§m----------§8 [ §6" + permissionGroup.getName() + " §8] §m----------");
							player.sendMessage(prefix + "§eMembers§8:" + (permissionGroup.getMemberUUIDs().isEmpty() ? " §cNone" : ""));
							
							for (String value : permissionGroup.getMemberUUIDs())
								player.sendMessage(prefix + "§7- §a" + value);
							
							player.sendMessage(prefix + "§ePermissions§8:" + (permissionGroup.getPermissions().isEmpty() ? " §cNone" : ""));
							
							for (String value : permissionGroup.getPermissions())
								player.sendMessage(prefix + "§7- §a" + value);
							
							String parent = permissionGroup.getParent();
							
							player.sendMessage(prefix + "§eParent§8: " + (((parent != null) && !(parent.isEmpty())) ? ("§f'§a" + parent + "§f'") : "§cNone"));
							player.sendMessage(prefix + "§ePrefix§8: §f'§a" + permissionGroup.getPrefix() + "§f'");
							player.sendMessage(prefix + "§eSuffix§8: §f'§a" + permissionGroup.getSuffix() + "§f'");
							player.sendMessage(prefix + "§eChat-Prefix§8: §f'§a" + permissionGroup.getChatPrefix() + "§f'");
							player.sendMessage(prefix + "§eChat-Suffix§8: §f'§a" + permissionGroup.getChatSuffix() + "§f'");
							player.sendMessage(prefix + "§eDefault§8: §f'§a" + permissionGroup.isDefault() + "§f'");
							player.sendMessage(prefix + "§eWeight§8: §f'§a" + permissionGroup.getWeight() + "§f'");
							player.sendMessage(prefix + "§8§m----------§8 [ §6" + permissionGroup.getName() + " §8] §m----------");
						} else
							player.sendMessage(prefix + "§cThe group §a" + args[1] + " §cdoes not exist§7!");
					} else if(args[0].equalsIgnoreCase("user")) {
						PermissionUser permissionUser;
						Player targetPlayer  = Bukkit.getPlayer(args[1]);
						UUID targetUniqueId;
						String targetName;
						
						if(targetPlayer != null) {
							targetUniqueId = targetPlayer.getUniqueId();
							targetName = targetPlayer.getName();
						} else {
							targetName = args[1];
							targetUniqueId = uuidFetching.fetchUUID(targetName);
						}
						
						permissionUser = new PermissionUser(targetUniqueId);
						
						player.sendMessage(prefix + "§8§m----------§8 [ §6" + targetName + " §8] §m----------");
						player.sendMessage(prefix + "§eGroups§8:" + (permissionUser.getGroups().isEmpty() ? " §cNone" : ""));
						
						String timedGroup = "NONE";
						String formattedTime = "";
						
						if(fileUtils.getConfiguration().getBoolean("MySQL.Enabled")) {
							String tempGroupName = mysqlPermissionManager.getPlayerTempGroupName(targetUniqueId.toString());
							
							if(tempGroupName != null) {
								timedGroup = tempGroupName;
								formattedTime = utilities.formatTime((mysqlPermissionManager.getPlayerTempGroupTime(targetUniqueId.toString()) - System.currentTimeMillis()) / 1000L);
							}
						} else if(permissionConfigUtils.getConfiguration().getStringList("TempRanks").contains(targetUniqueId.toString())) {
							timedGroup = permissionConfigUtils.getConfiguration().getString("Ranks." + targetUniqueId.toString() + ".GroupName");
							formattedTime = utilities.formatTime((permissionConfigUtils.getConfiguration().getLong("Ranks." + targetUniqueId.toString() + ".Time") - System.currentTimeMillis()) / 1000L);
						}
						
						for (PermissionGroup group : permissionUser.getGroups())
							player.sendMessage(prefix + "§7- §a" + group.getName() + (group.getName().equalsIgnoreCase(timedGroup) ? (" §8[§d" + formattedTime + "§8]") : ""));
						
						player.sendMessage(prefix + "§ePermissions§8:" + (permissionUser.getPermissions().isEmpty() ? " §cNone" : ""));
						
						for (String value : permissionUser.getPermissions())
							player.sendMessage(prefix + "§7- §a" + value);
						
						player.sendMessage(prefix + "§ePrefix§8: §f'§a" + permissionUser.getPrefix() + "§f'");
						player.sendMessage(prefix + "§eSuffix§8: §f'§a" + permissionUser.getSuffix() + "§f'");
						player.sendMessage(prefix + "§eChat-Prefix§8: §f'§a" + permissionUser.getChatPrefix() + "§f'");
						player.sendMessage(prefix + "§eChat-Suffix§8: §f'§a" + permissionUser.getChatSuffix() + "§f'");
						player.sendMessage(prefix + "§8§m----------§8 [ §6" + targetName + " §8] §m----------");
					} else if(args[0].equalsIgnoreCase("import")) {
						if(args[1].equalsIgnoreCase("sql")) {
							if(fileUtils.getConfiguration().getBoolean("MySQL.Enabled")) {
								long start = System.currentTimeMillis();
								
								importUtils.importFromSQL();
								
								player.sendMessage(prefix + "§eImported data from §aMySQL §ein §d" + (System.currentTimeMillis() - start) + "ms§7!");
							} else
								player.sendMessage(prefix + "§cPlease enable MySQL in the config.yml and reload/restart the server§7!");
						} else if(args[1].equalsIgnoreCase("file")) {
							if(fileUtils.getConfiguration().getBoolean("MySQL.Enabled")) {
								long start = System.currentTimeMillis();
								
								importUtils.importFromFile();
								
								player.sendMessage(prefix + "§eImported data from §apermissions.yml §ein §d" + (System.currentTimeMillis() - start) + "ms§7!");
							} else
								player.sendMessage(prefix + "§cPlease enable MySQL in the config.yml and reload/restart the server§7!");
						} else
							player.sendMessage(prefix + "§e/perm import «sql | file»");
					} else
						utilities.sendHelpMessage(player);
				} else if(args.length == 3) {
					if(args[0].equalsIgnoreCase("group")) {
						PermissionGroup permissionGroup = new PermissionGroup(args[1]);
						
						if(args[2].equalsIgnoreCase("create")) {
							if(!(permissionGroup.exists())) {
								permissionGroup.registerGroupIfNotExisting();
								
								player.sendMessage(prefix + "§eThe group §a" + permissionGroup.getName() + " §ewas created§7!");
							} else
								player.sendMessage(prefix + "§cA group with the name §a" + args[1] + " §cdoes already exist§7!");
						} else if(args[2].equalsIgnoreCase("delete")) {
							if(permissionGroup.exists()) {
								List<PermissionGroup> list = permissionGroupManager.getPermissionGroups();
								
								if(list.size() == 1)
									player.sendMessage(prefix + "§cYou can not delete this group§7, §cbecause it is the last group§7!");
								else {
									boolean defaultGroupExists = false;
									
									for (PermissionGroup permissionGroup2 : list) {
										if(!(permissionGroup.getName().equalsIgnoreCase(permissionGroup2.getName()))) {
											if(permissionGroup2.isDefault())
												defaultGroupExists = true;
										}
									}
									
									if(!(defaultGroupExists)) {
										for (PermissionGroup permissionGroup2 : list) {
											if(!(permissionGroup.getName().equalsIgnoreCase(permissionGroup2.getName()))) {
												if(!(permissionGroup2.isDefault()))
													permissionGroup2.setDefault(true);
											
												if(list.size() == 2) {
													for (Player all : Bukkit.getOnlinePlayers()) {
														if(permissionUserManager.getPermissionPlayer(all.getUniqueId()).getGroups().contains(permissionGroup))
															permissionGroup2.addMember(all.getName(), true);
													}
												}
												
												break;
											}
										}
									}
									
									permissionGroup.deleteGroup();

									permissionSystem.updatePrefixesAndSuffixes();
									
									player.sendMessage(prefix + "§cThe group §a" + permissionGroup.getName() + " §cwas deleted§7!");
								}
							} else
								player.sendMessage(prefix + "§cThe group §a" + args[1] + " §cdoes not exist§7!");
						} else if(permissionGroup.exists()) {
							if(args[2].equalsIgnoreCase("clear")) {
								permissionGroup.clearPermissions();
								
								player.sendMessage(prefix + "§cThe permissions of the group §a" + permissionGroup.getName() + " §cwere cleared§7!");
							} else
								utilities.sendHelpMessage(player);
						} else if(!(permissionGroup.exists()))
							player.sendMessage(prefix + "§cThe group §a" + args[1] + " §cdoes not exist§7!");
						else
							utilities.sendHelpMessage(player);
					} else if(args[0].equalsIgnoreCase("user")) {
						PermissionUser permissionUser = permissionUserManager.getPermissionPlayer(args[1]);
						
						if(args[2].equalsIgnoreCase("clear")) {
							permissionUser.clearPermissions();
							
							player.sendMessage(prefix + "§cThe permissions of the player §a" + args[1] + " §cwere cleared§7!");
						} else
							utilities.sendHelpMessage(player);
					} else
						utilities.sendHelpMessage(player);
				} else if(args.length >= 4) {
					if(args[0].equalsIgnoreCase("group")) {
						PermissionGroup permissionGroup = new PermissionGroup(args[1]);
						
						if(permissionGroup.exists()) {
							if(args[2].equalsIgnoreCase("add")) {
								permissionGroup.addPermission(args[3]);
								
								player.sendMessage(prefix + "§eThe permission §a" + args[3] + " §ewas added to the group §d" + permissionGroup.getName() + "§7!");
							} else if(args[2].equalsIgnoreCase("remove")) {
								String permToRemove = args[3];
								
								for (String permission : permissionGroup.getPermissions()) {
									if(permission.toLowerCase().startsWith(args[3].toLowerCase())) {
										permToRemove = permission;
										break;
									}
								}
								
								permissionGroup.removePermission(permToRemove);
								
								player.sendMessage(prefix + "§cThe permission §a" + permToRemove + " §cwas removed from the group §d" + permissionGroup.getName() + "§7!");
							} else if(args[2].equalsIgnoreCase("addMember")) {
								String name = args[3];
								PermissionUser permissionUser = new PermissionUser(uuidFetching.fetchUUID(name));
								PermissionGroup defaultGroup = new PermissionGroup("default");
								
								for (PermissionGroup group : permissionGroupManager.getPermissionGroups()) {
									if(group.isDefault())
										defaultGroup = group;
								}
								
								if((permissionUser.getGroups().size() == 1) && permissionUser.getGroups().get(0).isDefault())
									defaultGroup.removeMember(name, false);
								
								permissionGroup.addMember(name, true);
								
								permissionSystem.updatePrefixesAndSuffixes();
								
								player.sendMessage(prefix + "§eThe player §a" + args[3] + " §ewas added to the group §d" + permissionGroup.getName() + "§7!");
							} else if(args[2].equalsIgnoreCase("removeMember")) {
								String name = args[3];
								permissionGroup.removeMember(name, true);
								
								PermissionGroup defaultGroup = new PermissionGroup("default");
								
								for (PermissionGroup group : permissionGroupManager.getPermissionGroups()) {
									if(group.isDefault())
										defaultGroup = group;
								}
								
								if(new PermissionUser(uuidFetching.fetchUUID(name)).getGroups().isEmpty())
									defaultGroup.addMember(name, true);
								
								permissionSystem.updatePrefixesAndSuffixes();
								
								player.sendMessage(prefix + "§cThe player §a" + args[3] + " §cwas removed from the group §d" + permissionGroup.getName() + "§7!");
							} else if(args[2].equalsIgnoreCase("setDefault")) {
								for (PermissionGroup group : permissionGroupManager.getPermissionGroups()) {
									if(group != permissionGroup) {
										if(group.isDefault())
											group.setDefault(false);
									}
								}
								
								try {
									permissionGroup.setDefault(Boolean.parseBoolean(args[3]));
								} catch (Exception e) {
									permissionGroup.setDefault(false);
								}
								
								player.sendMessage(prefix + "§eThe default group value of the group §a" + permissionGroup.getName() + " §ewas updated§7!");
							} else if(args[2].equalsIgnoreCase("setPrefix")) {
								String value = "";
								
								for (int i = 3; i < args.length; i++)
									value = value + " " + args[i];
								
								value = value.trim().replace("\"", "");
								
								if(value.length() > 16)
									value = value.substring(0, 16);
								
								permissionGroup.setPrefix(value);
								permissionSystem.updatePrefixesAndSuffixes();
								
								player.sendMessage(prefix + "§eThe prefix of the group §a" + permissionGroup.getName() + " §ewas updated§7!");
							} else if(args[2].equalsIgnoreCase("setChatPrefix")) {
								String value = "";
								
								for (int i = 3; i < args.length; i++)
									value = value + " " + args[i];
								
								value = value.trim().replace("\"", "");
								
								permissionGroup.setChatPrefix(value);
								permissionSystem.updatePrefixesAndSuffixes();
								
								player.sendMessage(prefix + "§eThe chat prefix of the group §a" + permissionGroup.getName() + " §ewas updated§7!");
							} else if(args[2].equalsIgnoreCase("setSuffix")) {
								String value = "";
								
								for (int i = 3; i < args.length; i++)
									value = value + " " + args[i];
								
								value = value.trim().replace("\"", "");
								
								if(value.length() > 16)
									value = value.substring(0, 16);
								
								permissionGroup.setSuffix(value);
								permissionSystem.updatePrefixesAndSuffixes();
								
								player.sendMessage(prefix + "§eThe suffix of the group §a" + permissionGroup.getName() + " §ewas updated§7!");
							} else if(args[2].equalsIgnoreCase("setChatSuffix")) {
								String value = "";
								
								for (int i = 3; i < args.length; i++)
									value = value + " " + args[i];
								
								value = value.trim().replace("\"", "");
								
								permissionGroup.setChatSuffix(value);
								permissionSystem.updatePrefixesAndSuffixes();
								
								player.sendMessage(prefix + "§eThe chat suffix of the group §a" + permissionGroup.getName() + " §ewas updated§7!");
							} else if(args[2].equalsIgnoreCase("setWeight")) {
								int weight = 0;
								
								try {
									weight = Integer.parseInt(args[3]);
									
									if(weight > 999)
										weight = 999;
									else if(weight < 1)
										weight = 1;
								} catch (NumberFormatException e) {
								}
								
								permissionGroup.setWeight(weight);
								
								permissionSystem.updatePrefixesAndSuffixes();
							
								player.sendMessage(prefix + "§eThe weight of the group §a" + permissionGroup.getName() + " §ewas updated§7!");
							} else if(args[2].equalsIgnoreCase("setParent")) {
								permissionGroup.setParent(args[3].equals("none") ? null : args[3]);
								permissionGroup.updatePermissions();
							
								player.sendMessage(prefix + "§eThe parent of the group §a" + permissionGroup.getName() + " §ewas updated§7!");
							} else
								utilities.sendHelpMessage(player);
						} else
							player.sendMessage(prefix + "§cThe group §a" + args[1] + " §cdoes not exist§7!");
					} else if(args[0].equalsIgnoreCase("user")) {
						PermissionUser permissionUser = permissionUserManager.getPermissionPlayer(args[1]);
						
						if(permissionUser.exists()) {
							if(args[2].equalsIgnoreCase("add")) {
								permissionUser.addPermission(args[3]);
								
								player.sendMessage(prefix + "§eThe permission §a" + args[3] + " §ewas added to the player §d" + args[1] + "§7!");
							} else if(args[2].equalsIgnoreCase("remove")) {
								String permToRemove = args[3];
								
								for (String permission : permissionUser.getPermissions()) {
									if(permission.toLowerCase().startsWith(args[3].toLowerCase())) {
										permToRemove = permission;
										break;
									}
								}
								
								permissionUser.removePermission(permToRemove);
								
								player.sendMessage(prefix + "§cThe permission §a" + permToRemove + " §cwas removed from the player §d" + args[1] + "§7!");
							} else if(args[2].equalsIgnoreCase("setgroup")) {
								String name = args[1];
								PermissionGroup permissionGroup = new PermissionGroup(args[3]);
								
								if(permissionGroup.exists()) {
									if(args.length >= 6) {
										int value = 0;
										
										try {
											value = Integer.parseInt(args[4]);
										} catch (Exception e) {
										}
										
										if(args[5].equalsIgnoreCase("seconds") || args[5].equalsIgnoreCase("minutes") || args[5].equalsIgnoreCase("hours") || args[5].equalsIgnoreCase("days") || args[5].equalsIgnoreCase("months") || args[5].equalsIgnoreCase("years")) {
											String unit = args[5].toUpperCase();
											int time = value;
											
											if(unit.equalsIgnoreCase("minutes"))
												time *= 60;
											else if(unit.equalsIgnoreCase("hours"))
												time *= 60 * 60;
											else if(unit.equalsIgnoreCase("days"))
												time *= 60 * 60 * 24;
											else if(unit.equalsIgnoreCase("months"))
												time *= 60 * 60 * 24 * 30;
											else if(unit.equalsIgnoreCase("years"))
												time *= 60 * 60 * 24 * 30 * 12;
											
											for (PermissionGroup group : permissionUser.getGroups())
												group.removeMember(name, false);
											
											permissionGroup.addMember(name, true);
											
											UUID uuid = uuidFetching.fetchUUID(name);
											
											if(fileUtils.getConfiguration().getBoolean("MySQL.Enabled"))
												permissionSystem.getMySQLPermissionManager().setPlayerTempGroup(uuid.toString(), permissionGroup.getName(), System.currentTimeMillis() + (time * 1000L));
											else {
												List<String> ranks = permissionConfigUtils.getConfiguration().getStringList("TempRanks");
												
												if(!(ranks.contains(uuid.toString())))
													ranks.add(uuid.toString());
												
												permissionConfigUtils.getConfiguration().set("TempRanks", ranks);
												permissionConfigUtils.getConfiguration().set("Ranks." + uuid.toString() + ".GroupName", permissionGroup.getName());
												permissionConfigUtils.getConfiguration().set("Ranks." + uuid.toString() + ".Time", System.currentTimeMillis() + (time * 1000L));
												permissionConfigUtils.saveFile();
											}
											
											permissionSystem.updatePrefixesAndSuffixes();
											
											player.sendMessage(prefix + "§eThe group of the player §a" + name + " §ewas set to §d" + permissionGroup.getName() + " §efor §d" + value + " " + unit + "§7!");
										} else {
											utilities.sendHelpMessage(player);
										}
									} else {
										for (PermissionGroup group : permissionUser.getGroups())
											group.removeMember(name, false);
										
										permissionGroup.addMember(name, true);
										
										permissionSystem.updatePrefixesAndSuffixes();
										
										player.sendMessage(prefix + "§eThe group of the player §a" + name + " §ewas set to §d" + permissionGroup.getName() + "§7!");
									}
								} else
									player.sendMessage(prefix + "§cThe group §a" + args[3] + " §cdoes not exist§7!");
							} else if(args[2].equalsIgnoreCase("clear")) {
								permissionUser.clearPermissions();
								
								player.sendMessage(prefix + "§cThe permissions of the player §a" + args[1] + " §cwere cleared§7!");
							} else if(args[2].equalsIgnoreCase("setPrefix")) {
								String value = "";
								
								for (int i = 3; i < args.length; i++)
									value = value + " " + args[i];
								
								value = value.trim().replace("\"", "");
								
								if(value.length() > 16)
									value = value.substring(0, 16);
								
								permissionUser.setPrefix(value);
								permissionSystem.updatePrefixesAndSuffixes();
								
								player.sendMessage(prefix + "§eThe prefix of the player §a" + args[1] + " §ewas updated§7!");
							} else if(args[2].equalsIgnoreCase("setChatPrefix")) {
								String value = "";
								
								for (int i = 3; i < args.length; i++)
									value = value + " " + args[i];
								
								value = value.trim().replace("\"", "");
								
								permissionUser.setChatPrefix(value);
								permissionSystem.updatePrefixesAndSuffixes();
								
								player.sendMessage(prefix + "§eThe chat prefix of the player §a" + args[1] + " §ewas updated§7!");
							} else if(args[2].equalsIgnoreCase("setSuffix")) {
								String value = "";
								
								for (int i = 3; i < args.length; i++)
									value = value + " " + args[i];
								
								value = value.trim().replace("\"", "");
								
								if(value.length() > 16)
									value = value.substring(0, 16);
								
								permissionUser.setSuffix(value);
								permissionSystem.updatePrefixesAndSuffixes();
								
								player.sendMessage(prefix + "§eThe suffix of the player §a" + args[1] + " §ewas updated§7!");
							} else if(args[2].equalsIgnoreCase("setChatSuffix")) {
								String value = "";
								
								for (int i = 3; i < args.length; i++)
									value = value + " " + args[i];
								
								value = value.trim().replace("\"", "");
								
								permissionUser.setChatSuffix(value);
								permissionSystem.updatePrefixesAndSuffixes();
								
								player.sendMessage(prefix + "§eThe chat suffix of the player §a" + args[1] + " §ewas updated§7!");
							} else
								utilities.sendHelpMessage(player);
						} else
							player.sendMessage(prefix + "§cThe group §a" + args[1] + " §cdoes not exist§7!");
					} else
						utilities.sendHelpMessage(player);
				} else
					utilities.sendHelpMessage(player);
			} else
				player.sendMessage(utilities.getNoPerm());
		} else {
			if(args.length == 1) {
				if(args[0].equalsIgnoreCase("groups") || args[0].equalsIgnoreCase("group")) {
					List<PermissionGroup> list = permissionGroupManager.getPermissionGroups();
					
					if(!(list.isEmpty())) {
						utilities.sendConsole("§eCached groups§8:");
						
						for (PermissionGroup permissionGroup : list)
							utilities.sendConsole("§e- §a" + permissionGroup.getName());
					} else
						utilities.sendConsole("§cThere are no groups to show!");
				} else if(args[0].equalsIgnoreCase("users") || args[0].equalsIgnoreCase("user")) {
					List<String> list = permissionUserManager.getPermissionPlayerUUIDs();
					
					if(!(list.isEmpty())) {
						utilities.sendConsole("§eCached user-uuids§8:");
						
						for (String uuid : list)
							utilities.sendConsole("§e- §a" + uuid);
					} else
						utilities.sendConsole("§cThere are no users to show!");
				} else if(args[0].equalsIgnoreCase("reload")) {
					fileUtils.reloadConfig();
					permissionConfigUtils.reloadConfig();
					
					utilities.sendConsole("§eThe config files were reloaded§7!");
				} else if(args[0].equalsIgnoreCase("editor")) {
					if(fileUtils.getConfiguration().getBoolean("WebServer.Enabled") && (webServerManager != null))
						utilities.sendConsole("§eOpen the link in your browser to get to the web editor§8: §ahttp://" + utilities.getIpAddress() + ":" + fileUtils.getConfiguration().getInt("WebServer.Port") + "?authKey=" + webServerManager.getAuthKey());
					else
						utilities.sendConsole("§cThe web server is not enabled or not running§7!");
				} else
					utilities.sendHelpMessage(sender);
			} else if(args.length == 2) {
				if(args[0].equalsIgnoreCase("group")) {
					PermissionGroup permissionGroup = new PermissionGroup(args[1]);
					
					if(permissionGroup.exists()) {
						utilities.sendConsole("§8§m----------§8 [ §6" + permissionGroup.getName() + " §8] §m----------");
						utilities.sendConsole("§eMembers§8:" + (permissionGroup.getMemberUUIDs().isEmpty() ? " §cNone" : ""));
						
						for (String value : permissionGroup.getMemberUUIDs())
							utilities.sendConsole("§e- §a" + value);
						
						utilities.sendConsole("§ePermissions§8:" + (permissionGroup.getPermissions().isEmpty() ? " §cNone" : ""));
						
						for (String value : permissionGroup.getPermissions())
							utilities.sendConsole("§e- §a" + value);
						
						String parent = permissionGroup.getParent();
						
						utilities.sendConsole("§eParent§8: " + (((parent != null) && !(parent.isEmpty())) ? ("§f'§a" + parent + "§f'") : "§cNone"));
						utilities.sendConsole("§ePrefix§8: §f'§a" + permissionGroup.getPrefix() + "§f'");
						utilities.sendConsole("§eSuffix§8: §f'§a" + permissionGroup.getSuffix() + "§f'");
						utilities.sendConsole("§eChat-Prefix§8: §f'§a" + permissionGroup.getChatPrefix() + "§f'");
						utilities.sendConsole("§eChat-Suffix§8: §f'§a" + permissionGroup.getChatSuffix() + "§f'");
						utilities.sendConsole("§eDefault§8: §f'§a" + permissionGroup.isDefault() + "§f'");
						utilities.sendConsole("§eWeight§8: §f'§a" + permissionGroup.getWeight() + "§f'");
						utilities.sendConsole("§8§m----------§8 [ §6" + permissionGroup.getName() + " §8] §m----------");
					} else
						utilities.sendConsole("§cThe group §a" + args[1] + " §cdoes not exist§7!");
				} else if(args[0].equalsIgnoreCase("user")) {
					PermissionUser permissionUser;
					Player targetPlayer  = Bukkit.getPlayer(args[1]);
					UUID targetUniqueId;
					String targetName;
					
					if(targetPlayer != null) {
						targetUniqueId = targetPlayer.getUniqueId();
						targetName = targetPlayer.getName();
					} else {
						targetName = args[1];
						targetUniqueId = uuidFetching.fetchUUID(targetName);
					}
					
					permissionUser = new PermissionUser(targetUniqueId);
					
					utilities.sendConsole("§8§m----------§8 [ §6" + targetName + " §8] §m----------");
					utilities.sendConsole("§eGroups§8:" + (permissionUser.getGroups().isEmpty() ? " §cNone" : ""));
					
					String timedGroup = "NONE";
					String formattedTime = "";
					
					if(fileUtils.getConfiguration().getBoolean("MySQL.Enabled")) {
						String tempGroupName = mysqlPermissionManager.getPlayerTempGroupName(targetUniqueId.toString());
						
						if(tempGroupName != null) {
							timedGroup = tempGroupName;
							formattedTime = utilities.formatTime((mysqlPermissionManager.getPlayerTempGroupTime(targetUniqueId.toString()) - System.currentTimeMillis()) / 1000L);
						}
					} else if(permissionConfigUtils.getConfiguration().getStringList("TempRanks").contains(targetUniqueId.toString())) {
						timedGroup = permissionConfigUtils.getConfiguration().getString("Ranks." + targetUniqueId.toString() + ".GroupName");
						formattedTime = utilities.formatTime((permissionConfigUtils.getConfiguration().getLong("Ranks." + targetUniqueId.toString() + ".Time") - System.currentTimeMillis()) / 1000L);
					}
					
					for (PermissionGroup group : permissionUser.getGroups())
						utilities.sendConsole("§e- §a" + group.getName() + (group.getName().equalsIgnoreCase(timedGroup) ? (" §8[§d" + formattedTime + "§8]") : ""));
					
					utilities.sendConsole("§ePermissions§8:" + (permissionUser.getPermissions().isEmpty() ? " §cNone" : ""));
					
					for (String value : permissionUser.getPermissions())
						utilities.sendConsole("§e- §a" + value);
					
					utilities.sendConsole("§ePrefix§8: §f'§a" + permissionUser.getPrefix() + "§f'");
					utilities.sendConsole("§eSuffix§8: §f'§a" + permissionUser.getSuffix() + "§f'");
					utilities.sendConsole("§eChat-Prefix§8: §f'§a" + permissionUser.getChatPrefix() + "§f'");
					utilities.sendConsole("§eChat-Suffix§8: §f'§a" + permissionUser.getChatSuffix() + "§f'");
					utilities.sendConsole("§8§m----------§8 [ §6" + targetName + " §8] §m----------");
				} else if(args[0].equalsIgnoreCase("import")) {
					if(args[1].equalsIgnoreCase("sql")) {
						if(fileUtils.getConfiguration().getBoolean("MySQL.Enabled")) {
							long start = System.currentTimeMillis();
							
							importUtils.importFromSQL();
							
							utilities.sendConsole("§eImported data from §aMySQL §ein §d" + (System.currentTimeMillis() - start) + "ms§7!");
						} else
							utilities.sendConsole("§cPlease enable MySQL in the config.yml and reload/restart the server§7!");
					} else if(args[1].equalsIgnoreCase("file")) {
						if(fileUtils.getConfiguration().getBoolean("MySQL.Enabled")) {
							long start = System.currentTimeMillis();
							
							importUtils.importFromFile();
							
							utilities.sendConsole("§eImported data from §apermissions.yml §ein §d" + (System.currentTimeMillis() - start) + "ms§7!");
						} else
							utilities.sendConsole("§cPlease enable MySQL in the config.yml and reload/restart the server§7!");
					} else
						utilities.sendConsole("§e/perm import «sql | file»");
				} else
					utilities.sendHelpMessage(sender);
			} else if(args.length == 3) {
				if(args[0].equalsIgnoreCase("group")) {
					PermissionGroup permissionGroup = new PermissionGroup(args[1]);
					
					if(args[2].equalsIgnoreCase("create")) {
						if(!(permissionGroup.exists())) {
							permissionGroup.registerGroupIfNotExisting();
							
							utilities.sendConsole("§eThe group §a" + permissionGroup.getName() + " §ewas created§7!");
						} else
							utilities.sendConsole("§cA group with the name §a" + args[1] + " §cdoes already exist§7!");
					} else if(args[2].equalsIgnoreCase("delete")) {
						if(permissionGroup.exists()) {
							List<PermissionGroup> list = permissionGroupManager.getPermissionGroups();
							
							if(list.size() == 1)
								utilities.sendConsole("§cYou can not delete this group§7, §cbecause it is the last group§7!");
							else {
								boolean defaultGroupExists = permissionGroup.isDefault();
								
								for (PermissionGroup permissionGroup2 : list) {
									if(!(permissionGroup.getName().equalsIgnoreCase(permissionGroup2.getName()))) {
										if(permissionGroup2.isDefault())
											defaultGroupExists = true;
									}
								}
								
								if(!(defaultGroupExists)) {
									for (PermissionGroup permissionGroup2 : list) {
										if(!(permissionGroup.getName().equalsIgnoreCase(permissionGroup2.getName()))) {
											if(!(permissionGroup2.isDefault()))
												permissionGroup2.setDefault(true);
										
											if(list.size() == 2) {
												for (Player all : Bukkit.getOnlinePlayers()) {
													if(permissionUserManager.getPermissionPlayer(all.getUniqueId()).getGroups().contains(permissionGroup))
														permissionGroup2.addMember(all.getName(), true);
												}
											}
											
											break;
										}
									}
								}
								
								permissionGroup.deleteGroup();
								
								permissionSystem.updatePrefixesAndSuffixes();
								
								utilities.sendConsole("§cThe group §a" + permissionGroup.getName() + " §cwas deleted§7!");
							}
						} else
							utilities.sendConsole("§cThe group §a" + args[1] + " §cdoes not exist§7!");
					} else if(permissionGroup.exists()) {
						if(args[2].equalsIgnoreCase("clear")) {
							permissionGroup.clearPermissions();
							
							utilities.sendConsole("§cThe permissions of the group §a" + permissionGroup.getName() + " §cwere cleared§7!");
						} else
							utilities.sendHelpMessage(sender);
					} else if(!(permissionGroup.exists()))
						utilities.sendConsole("§cThe group §a" + args[1] + " §cdoes not exist§7!");
					else
						utilities.sendHelpMessage(sender);
				} else if(args[0].equalsIgnoreCase("user")) {
					PermissionUser permissionUser = permissionUserManager.getPermissionPlayer(args[1]);
					
					if(args[2].equalsIgnoreCase("clear")) {
						permissionUser.clearPermissions();
						
						utilities.sendConsole("§cThe permissions of the player §a" + args[1] + " §cwere cleared§7!");
					} else
						utilities.sendHelpMessage(sender);
				} else
					utilities.sendHelpMessage(sender);
			} else if(args.length >= 4) {
				if(args[0].equalsIgnoreCase("group")) {
					PermissionGroup permissionGroup = new PermissionGroup(args[1]);
					
					if(permissionGroup.exists()) {
						if(args[2].equalsIgnoreCase("add")) {
							permissionGroup.addPermission(args[3]);
							
							utilities.sendConsole("§eThe permission §a" + args[3] + " §ewas added to the group §a" + permissionGroup.getName() + "§7!");
						} else if(args[2].equalsIgnoreCase("remove")) {
							String permToRemove = args[3];
							
							for (String permission : permissionGroup.getPermissions()) {
								if(permission.toLowerCase().startsWith(args[3].toLowerCase())) {
									permToRemove = permission;
									break;
								}
							}
							
							permissionGroup.removePermission(permToRemove);
							
							utilities.sendConsole("§cThe permission §a" + permToRemove + " §cwas removed from the group §a" + permissionGroup.getName() + "§7!");
						} else if(args[2].equalsIgnoreCase("addMember")) {
							String name = args[3];
							PermissionUser permissionUser = new PermissionUser(uuidFetching.fetchUUID(name));
							PermissionGroup defaultGroup = new PermissionGroup("default");
							
							for (PermissionGroup group : permissionGroupManager.getPermissionGroups()) {
								if(group.isDefault())
									defaultGroup = group;
							}
							
							if((permissionUser.getGroups().size() == 1) && permissionUser.getGroups().get(0).isDefault())
								defaultGroup.removeMember(name, false);
							
							permissionGroup.addMember(name, true);
							
							permissionSystem.updatePrefixesAndSuffixes();
							
							utilities.sendConsole("§eThe player §a" + args[3] + " §ewas added to the group §d" + permissionGroup.getName() + "§7!");
						} else if(args[2].equalsIgnoreCase("removeMember")) {
							String name = args[3];
							permissionGroup.removeMember(name, true);
							
							PermissionGroup defaultGroup = new PermissionGroup("default");
							
							for (PermissionGroup group : permissionGroupManager.getPermissionGroups()) {
								if(group.isDefault())
									defaultGroup = group;
							}
							
							if(new PermissionUser(uuidFetching.fetchUUID(name)).getGroups().isEmpty())
								defaultGroup.addMember(name, true);
							
							permissionSystem.updatePrefixesAndSuffixes();
							
							utilities.sendConsole("§cThe player §a" + args[3] + " §cwas removed from the group §d" + permissionGroup.getName() + "§7!");
						} else if(args[2].equalsIgnoreCase("setDefault")) {
							for (PermissionGroup group : permissionGroupManager.getPermissionGroups()) {
								if(group != permissionGroup) {
									if(group.isDefault())
										group.setDefault(false);
								}
							}
							
							try {
								permissionGroup.setDefault(Boolean.parseBoolean(args[3]));
							} catch (Exception e) {
								permissionGroup.setDefault(false);
							}
							
							permissionSystem.updatePrefixesAndSuffixes();
							
							utilities.sendConsole("§eThe default group value of the group §a" + permissionGroup.getName() + " §ewas updated§7!");
						} else if(args[2].equalsIgnoreCase("setPrefix")) {
							String value = "";
							
							for (int i = 3; i < args.length; i++)
								value = value + " " + args[i];
							
							value = value.trim().replace("\"", "");
							
							if(value.length() > 16)
								value = value.substring(0, 16);
							
							permissionGroup.setPrefix(value);
							permissionSystem.updatePrefixesAndSuffixes();
							
							utilities.sendConsole("§eThe prefix of the group §a" + permissionGroup.getName() + " §ewas updated§7!");
						} else if(args[2].equalsIgnoreCase("setChatPrefix")) {
							String value = "";
							
							for (int i = 3; i < args.length; i++)
								value = value + " " + args[i];
							
							value = value.trim().replace("\"", "");
							
							permissionGroup.setChatPrefix(value);
							permissionSystem.updatePrefixesAndSuffixes();
							
							utilities.sendConsole("§eThe chat prefix of the group §a" + permissionGroup.getName() + " §ewas updated§7!");
						} else if(args[2].equalsIgnoreCase("setSuffix")) {
							String value = "";
							
							for (int i = 3; i < args.length; i++)
								value = value + " " + args[i];
							
							value = value.trim().replace("\"", "");
							
							if(value.length() > 16)
								value = value.substring(0, 16);
							
							permissionGroup.setSuffix(value);
							permissionSystem.updatePrefixesAndSuffixes();
							
							utilities.sendConsole("§eThe suffix of the group §a" + permissionGroup.getName() + " §ewas updated§7!");
						} else if(args[2].equalsIgnoreCase("setChatSuffix")) {
							String value = "";
							
							for (int i = 3; i < args.length; i++)
								value = value + " " + args[i];
							
							value = value.trim().replace("\"", "");
							
							permissionGroup.setChatSuffix(value);
							permissionSystem.updatePrefixesAndSuffixes();
							
							utilities.sendConsole("§eThe chat suffix of the group §a" + permissionGroup.getName() + " §ewas updated§7!");
						} else if(args[2].equalsIgnoreCase("setWeight")) {
							try {
								int weight = Integer.parseInt(args[3]);
								
								if(weight > 999)
									weight = 999;
								
								permissionGroup.setWeight(weight);
							} catch (NumberFormatException e) {
								permissionGroup.setWeight(0);
							}
							
							permissionSystem.updatePrefixesAndSuffixes();
						
							utilities.sendConsole("§eThe weight of the group §a" + permissionGroup.getName() + " §ewas updated§7!");
						} else if(args[2].equalsIgnoreCase("setParent")) {
							permissionGroup.setParent(args[3].equals("none") ? null : args[3]);
							permissionGroup.updatePermissions();
						
							utilities.sendConsole("§eThe parent of the group §a" + permissionGroup.getName() + " §ewas updated§7!");
						} else
							utilities.sendHelpMessage(sender);
					} else
						utilities.sendConsole("§cThe group §a" + args[1] + " §cdoes not exist§7!");
				} else if(args[0].equalsIgnoreCase("user")) {
					PermissionUser permissionUser = permissionUserManager.getPermissionPlayer(args[1]);
					
					if(permissionUser.exists()) {
						if(args[2].equalsIgnoreCase("add")) {
							permissionUser.addPermission(args[3]);
							
							utilities.sendConsole("§eThe permission §a" + args[3] + " §ewas added to the player §a" + args[1] + "§7!");
						} else if(args[2].equalsIgnoreCase("remove")) {
							String permToRemove = args[3];
							
							for (String permission : permissionUser.getPermissions()) {
								if(permission.toLowerCase().startsWith(args[3].toLowerCase())) {
									permToRemove = permission;
									break;
								}
							}
							
							permissionUser.removePermission(permToRemove);
							
							utilities.sendConsole("§cThe permission §a" + permToRemove + " §cwas removed from the player §d" + args[1] + "§7!");
						} else if(args[2].equalsIgnoreCase("clear")) {
							permissionUser.clearPermissions();
							
							utilities.sendConsole("§cThe permissions of the player §a" + args[1] + " §cwere cleared§7!");
						} else if(args[2].equalsIgnoreCase("setgroup")) {
							String name = args[1];
							PermissionGroup permissionGroup = new PermissionGroup(args[3]);
							
							if(permissionGroup.exists()) {
								if(args.length >= 6) {
									int value = 0;
									
									try {
										value = Integer.parseInt(args[4]);
									} catch (Exception e) {
									}
									
									if(args[5].equalsIgnoreCase("seconds") || args[5].equalsIgnoreCase("minutes") || args[5].equalsIgnoreCase("hours") || args[5].equalsIgnoreCase("days") || args[5].equalsIgnoreCase("months") || args[5].equalsIgnoreCase("years")) {
										String unit = args[5].toUpperCase();
										int time = value;
										
										if(unit.equalsIgnoreCase("minutes"))
											time *= 60;
										else if(unit.equalsIgnoreCase("hours"))
											time *= 60 * 60;
										else if(unit.equalsIgnoreCase("days"))
											time *= 60 * 60 * 24;
										else if(unit.equalsIgnoreCase("months"))
											time *= 60 * 60 * 24 * 30;
										else if(unit.equalsIgnoreCase("years"))
											time *= 60 * 60 * 24 * 30 * 12;
										
										for (PermissionGroup group : permissionUser.getGroups())
											group.removeMember(name, false);
										
										permissionGroup.addMember(name, true);
										
										UUID uuid = uuidFetching.fetchUUID(name);
										
										if(fileUtils.getConfiguration().getBoolean("MySQL.Enabled"))
											permissionSystem.getMySQLPermissionManager().setPlayerTempGroup(uuid.toString(), permissionGroup.getName(), System.currentTimeMillis() + (time * 1000L));
										else {
											List<String> ranks = permissionConfigUtils.getConfiguration().getStringList("TempRanks");
											
											if(!(ranks.contains(uuid.toString())))
												ranks.add(uuid.toString());
											
											permissionConfigUtils.getConfiguration().set("TempRanks", ranks);
											permissionConfigUtils.getConfiguration().set("Ranks." + uuid.toString() + ".GroupName", permissionGroup.getName());
											permissionConfigUtils.getConfiguration().set("Ranks." + uuid.toString() + ".Time", System.currentTimeMillis() + (time * 1000L));
											permissionConfigUtils.saveFile();
										}
										
										permissionSystem.updatePrefixesAndSuffixes();
										
										utilities.sendConsole("§eThe group of the player §a" + name + " §ewas set to §d" + permissionGroup.getName() + " §efor §d" + value + " " + unit + "§7!");
									} else
										utilities.sendHelpMessage(sender);
								} else {
									for (PermissionGroup group : permissionUser.getGroups())
										group.removeMember(name, false);
									
									permissionGroup.addMember(name, true);
									
									permissionSystem.updatePrefixesAndSuffixes();
									
									utilities.sendConsole("§eThe group of the player §a" + name + " §ewas set to §d" + permissionGroup.getName() + "§7!");
								}
							} else
								utilities.sendConsole("§cThe group §a" + args[3] + " §cdoes not exist§7!");
						} else if(args[2].equalsIgnoreCase("setPrefix")) {
							String value = "";
							
							for (int i = 3; i < args.length; i++)
								value = value + " " + args[i];
							
							value = value.trim().replace("\"", "");
							
							if(value.length() > 16)
								value = value.substring(0, 16);
							
							permissionUser.setPrefix(value);
							permissionSystem.updatePrefixesAndSuffixes();
							
							utilities.sendConsole("§eThe prefix of the player §a" + args[1] + " §ewas updated§7!");
						} else if(args[2].equalsIgnoreCase("setChatPrefix")) {
							String value = "";
							
							for (int i = 3; i < args.length; i++)
								value = value + " " + args[i];
							
							value = value.trim().replace("\"", "");
							
							permissionUser.setChatPrefix(value);
							permissionSystem.updatePrefixesAndSuffixes();
							
							utilities.sendConsole("§eThe chat prefix of the player §a" + args[1] + " §ewas updated§7!");
						} else if(args[2].equalsIgnoreCase("setSuffix")) {
							String value = "";
							
							for (int i = 3; i < args.length; i++)
								value = value + " " + args[i];
							
							value = value.trim().replace("\"", "");
							
							if(value.length() > 16)
								value = value.substring(0, 16);
							
							permissionUser.setSuffix(value);
							permissionSystem.updatePrefixesAndSuffixes();
							
							utilities.sendConsole("§eThe suffix of the player §a" + args[1] + " §ewas updated§7!");
						} else if(args[2].equalsIgnoreCase("setChatSuffix")) {
							String value = "";
							
							for (int i = 3; i < args.length; i++)
								value = value + " " + args[i];
							
							value = value.trim().replace("\"", "");
							
							permissionUser.setChatSuffix(value);
							permissionSystem.updatePrefixesAndSuffixes();
							
							utilities.sendConsole("§eThe chat suffix of the player §a" + args[1] + " §ewas updated§7!");
						} else
							utilities.sendHelpMessage(sender);
					} else
						utilities.sendConsole("§cThe group §a" + args[1] + " §cdoes not exist§7!");
				} else
					utilities.sendHelpMessage(sender);
			} else
				utilities.sendHelpMessage(sender);
		}
		
		return true;
	}

}

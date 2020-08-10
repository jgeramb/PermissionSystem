package net.dev.permissions.commands;

import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

import net.dev.permissions.PermissionSystem;
import net.dev.permissions.utils.FileUtils;
import net.dev.permissions.utils.ImportUtils;
import net.dev.permissions.utils.PermissionConfigUtils;
import net.dev.permissions.utils.Utils;
import net.dev.permissions.utils.fetching.UUIDFetching;
import net.dev.permissions.utils.permissionmanagement.PermissionGroup;
import net.dev.permissions.utils.permissionmanagement.PermissionGroupManager;
import net.dev.permissions.utils.permissionmanagement.PermissionUser;
import net.dev.permissions.utils.permissionmanagement.PermissionUserManager;
import net.dev.permissions.webserver.WebServerManager;

public class PermsCommand implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		PermissionSystem permissionSystem = PermissionSystem.getInstance();
		Utils utils = permissionSystem.getUtils();
		FileUtils fileUtils = permissionSystem.getFileUtils();
		PermissionConfigUtils permissionConfigUtils = permissionSystem.getPermissionConfigUtils();
		ImportUtils importUtils = permissionSystem.getImportUtils();
		PermissionGroupManager permissionGroupManager = permissionSystem.getPermissionGroupManager();
		PermissionUserManager permissionUserManager = permissionSystem.getPermissionUserManager();
		UUIDFetching uuidFetching = permissionSystem.getUUIDFetching();
		
		WebServerManager webServerManager = permissionSystem.getWebServerManager();
		
		String prefix = utils.getPrefix();
		
		if(sender instanceof Player) {
			Player p = (Player) sender;
			
			if(p.hasPermission(new Permission("permissions.*", PermissionDefault.FALSE))) {
				if(args.length == 1) {
					if(args[0].equalsIgnoreCase("groups") || args[0].equalsIgnoreCase("group")) {
						List<PermissionGroup> list = permissionGroupManager.getPermissionGroups();
						
						if(!(list.isEmpty())) {
							p.sendMessage(prefix + "§eCached groups§8:");
							
							for (PermissionGroup permissionGroup : list)
								p.sendMessage(prefix + "§7- §a" + permissionGroup.getName());
						} else
							p.sendMessage(prefix + "§cThere are no groups to be shown§7!");
					} else if(args[0].equalsIgnoreCase("users") || args[0].equalsIgnoreCase("user")) {
						List<String> list = permissionUserManager.getPermissionPlayerUUIDs();
						
						if(!(list.isEmpty())) {
							p.sendMessage(prefix + "§eCached users-uuids§8:");
							
							for (String uuid : list)
								p.sendMessage(prefix + "§7- §a" + uuid);
						} else
							p.sendMessage(prefix + "§cThere are no users to be shown§7!");
					} else if(args[0].equalsIgnoreCase("reload")) {
						fileUtils.reloadConfig();
						permissionConfigUtils.reloadConfig();
						
						p.sendMessage(prefix + "§eThe config files were reloaded§7!");
					} else if(args[0].equalsIgnoreCase("editor")) {
						if(fileUtils.getConfig().getBoolean("WebServer.Enabled") && (webServerManager != null))
							p.sendMessage(prefix + "§eClick on the link to get to the web editor§8: §ahttp://" + utils.getIpAddress() + ":" + fileUtils.getConfig().getInt("WebServer.Port") + "?authKey=" + webServerManager.getAuthKey());
						else
							p.sendMessage(prefix + "§cThe web server is not enabled or not running§7!");
					} else
						utils.sendHelpMessage(p);
				} else if(args.length == 2) {
					if(args[0].equalsIgnoreCase("group")) {
						PermissionGroup permissionGroup = new PermissionGroup(args[1]);
						
						if(permissionGroup.exists()) {
							p.sendMessage(prefix + "§8§m----------§8 [ §6" + permissionGroup.getName() + " §8] §m----------");
							p.sendMessage(prefix + "§eMembers§8:" + (permissionGroup.getMemberUUIDs().isEmpty() ? " §cNone" : ""));
							
							for (String value : permissionGroup.getMemberUUIDs())
								p.sendMessage(prefix + "§7- §a" + value);
							
							p.sendMessage(prefix + "§ePermissions§8:" + (permissionGroup.getPermissions().isEmpty() ? " §cNone" : ""));
							
							for (String value : permissionGroup.getPermissions())
								p.sendMessage(prefix + "§7- §a" + value);
							
							String parent = permissionGroup.getParent();
							
							p.sendMessage(prefix + "§eParent§8: " + (((parent != null) && !(parent.isEmpty())) ? ("§f'§a" + parent + "§f'") : "§cNone"));
							p.sendMessage(prefix + "§ePrefix§8: §f'§a" + permissionGroup.getPrefix() + "§f'");
							p.sendMessage(prefix + "§eSuffix§8: §f'§a" + permissionGroup.getSuffix() + "§f'");
							p.sendMessage(prefix + "§eChat-Prefix§8: §f'§a" + permissionGroup.getChatPrefix() + "§f'");
							p.sendMessage(prefix + "§eChat-Suffix§8: §f'§a" + permissionGroup.getChatSuffix() + "§f'");
							p.sendMessage(prefix + "§eDefault§8: §f'§a" + permissionGroup.isDefault() + "§f'");
							p.sendMessage(prefix + "§eWeight§8: §f'§a" + permissionGroup.getWeight() + "§f'");
							p.sendMessage(prefix + "§8§m----------§8 [ §6" + permissionGroup.getName() + " §8] §m----------");
						} else
							p.sendMessage(prefix + "§cThe group §a" + args[1] + " §cdoes not exist§7!");
					} else if(args[0].equalsIgnoreCase("user")) {
						PermissionUser permissionUser;
						Player t  = Bukkit.getPlayer(args[1]);
						UUID uuid;
						String name;
						
						if(t != null) {
							uuid = t.getUniqueId();
							name = t.getName();
						} else {
							name = args[1];
							uuid = uuidFetching.fetchUUID(name);
						}
						
						permissionUser = new PermissionUser(uuid);
						
						p.sendMessage(prefix + "§8§m----------§8 [ §6" + name + " §8] §m----------");
						p.sendMessage(prefix + "§eGroups§8:" + (permissionUser.getGroups().isEmpty() ? " §cNone" : ""));
						
						String timedGroup = "NONE";
						String formattedTime = "";
						
						if(permissionConfigUtils.getConfig().getStringList("TempRanks").contains(uuid.toString())) {
							timedGroup = permissionConfigUtils.getConfig().getString("Ranks." + uuid.toString() + ".GroupName");
							formattedTime = utils.formatTime(permissionConfigUtils.getConfig().getInt("Ranks." + uuid.toString() + ".Time"));
						}
						
						for (PermissionGroup group : permissionUser.getGroups())
							p.sendMessage(prefix + "§7- §a" + group.getName() + (group.getName().equalsIgnoreCase(timedGroup) ? (" §8[§d" + formattedTime + "§8]") : ""));
						
						p.sendMessage(prefix + "§ePermissions§8:" + (permissionUser.getPermissions().isEmpty() ? " §cNone" : ""));
						
						for (String value : permissionUser.getPermissions())
							p.sendMessage(prefix + "§7- §a" + value);
						
						p.sendMessage(prefix + "§ePrefix§8: §f'§a" + permissionUser.getPrefix() + "§f'");
						p.sendMessage(prefix + "§eSuffix§8: §f'§a" + permissionUser.getSuffix() + "§f'");
						p.sendMessage(prefix + "§eChat-Prefix§8: §f'§a" + permissionUser.getChatPrefix() + "§f'");
						p.sendMessage(prefix + "§eChat-Suffix§8: §f'§a" + permissionUser.getChatSuffix() + "§f'");
						p.sendMessage(prefix + "§8§m----------§8 [ §6" + name + " §8] §m----------");
					} else if(args[0].equalsIgnoreCase("import")) {
						if(args[1].equalsIgnoreCase("sql")) {
							if(fileUtils.getConfig().getBoolean("MySQL.Enabled")) {
								long start = System.currentTimeMillis();
								
								importUtils.importFromSQL();
								
								p.sendMessage(prefix + "§eImported data from §aMySQL §ein §d" + (System.currentTimeMillis() - start) + "ms§7!");
							} else
								p.sendMessage(prefix + "§cPlease enable MySQL in the config.yml and reload/restart the server§7!");
						} else if(args[1].equalsIgnoreCase("file")) {
							if(fileUtils.getConfig().getBoolean("MySQL.Enabled")) {
								long start = System.currentTimeMillis();
								
								importUtils.importFromFile();
								
								p.sendMessage(prefix + "§eImported data from §apermissions.yml §ein §d" + (System.currentTimeMillis() - start) + "ms§7!");
							} else
								p.sendMessage(prefix + "§cPlease enable MySQL in the config.yml and reload/restart the server§7!");
						} else
							p.sendMessage(prefix + "§e/perm import «sql | file»");
					} else
						utils.sendHelpMessage(p);
				} else if(args.length == 3) {
					if(args[0].equalsIgnoreCase("group")) {
						PermissionGroup permissionGroup = new PermissionGroup(args[1]);
						
						if(args[2].equalsIgnoreCase("create")) {
							if(!(permissionGroup.exists())) {
								permissionGroup.registerGroupIfNotExisting();
								
								p.sendMessage(prefix + "§eThe group §a" + permissionGroup.getName() + " §ewas created§7!");
							} else
								p.sendMessage(prefix + "§cA group with the name §a" + args[1] + " §cdoes already exist§7!");
						} else if(args[2].equalsIgnoreCase("delete")) {
							if(permissionGroup.exists()) {
								List<PermissionGroup> list = permissionGroupManager.getPermissionGroups();
								
								if(list.size() == 1)
									p.sendMessage(prefix + "§cYou can not delete this group§7, §cbecause it is the last group§7!");
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
									
									p.sendMessage(prefix + "§cThe group §a" + permissionGroup.getName() + " §cwas deleted§7!");
								}
							} else
								p.sendMessage(prefix + "§cThe group §a" + args[1] + " §cdoes not exist§7!");
						} else if(permissionGroup.exists()) {
							if(args[2].equalsIgnoreCase("clear")) {
								permissionGroup.clearPermissions();
								
								p.sendMessage(prefix + "§cThe permissions of the group §a" + permissionGroup.getName() + " §cwere cleared§7!");
							} else
								utils.sendHelpMessage(p);
						} else if(!(permissionGroup.exists()))
							p.sendMessage(prefix + "§cThe group §a" + args[1] + " §cdoes not exist§7!");
						else
							utils.sendHelpMessage(p);
					} else if(args[0].equalsIgnoreCase("user")) {
						PermissionUser permissionUser = permissionUserManager.getPermissionPlayer(args[1]);
						
						if(args[2].equalsIgnoreCase("clear")) {
							permissionUser.clearPermissions();
							
							p.sendMessage(prefix + "§cThe permissions of the player §a" + args[1] + " §cwere cleared§7!");
						} else
							utils.sendHelpMessage(p);
					} else
						utils.sendHelpMessage(p);
				} else if(args.length >= 4) {
					if(args[0].equalsIgnoreCase("group")) {
						PermissionGroup permissionGroup = new PermissionGroup(args[1]);
						
						if(permissionGroup.exists()) {
							if(args[2].equalsIgnoreCase("add")) {
								permissionGroup.addPermission(args[3]);
								
								p.sendMessage(prefix + "§eThe permission §a" + args[3] + " §ewas added to the group §d" + permissionGroup.getName() + "§7!");
							} else if(args[2].equalsIgnoreCase("remove")) {
								String permToRemove = args[3];
								
								for (String permission : permissionGroup.getPermissions()) {
									if(permission.toLowerCase().startsWith(args[3].toLowerCase())) {
										permToRemove = permission;
										break;
									}
								}
								
								permissionGroup.removePermission(permToRemove);
								
								p.sendMessage(prefix + "§cThe permission §a" + permToRemove + " §cwas removed from the group §d" + permissionGroup.getName() + "§7!");
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
								
								p.sendMessage(prefix + "§eThe player §a" + args[3] + " §ewas added to the group §d" + permissionGroup.getName() + "§7!");
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
								
								p.sendMessage(prefix + "§cThe player §a" + args[3] + " §cwas removed from the group §d" + permissionGroup.getName() + "§7!");
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
								
								p.sendMessage(prefix + "§eThe default group value of the group §a" + permissionGroup.getName() + " §ewas updated§7!");
							} else if(args[2].equalsIgnoreCase("setPrefix")) {
								String value = "";
								
								for (int i = 3; i < args.length; i++)
									value = value + " " + args[i];
								
								value = value.trim().replace("\"", "");
								
								if(value.length() > 16)
									value = value.substring(0, 16);
								
								permissionGroup.setPrefix(value);
								permissionSystem.updatePrefixesAndSuffixes();
								
								p.sendMessage(prefix + "§eThe prefix of the group §a" + permissionGroup.getName() + " §ewas updated§7!");
							} else if(args[2].equalsIgnoreCase("setChatPrefix")) {
								String value = "";
								
								for (int i = 3; i < args.length; i++)
									value = value + " " + args[i];
								
								value = value.trim().replace("\"", "");
								
								permissionGroup.setChatPrefix(value);
								permissionSystem.updatePrefixesAndSuffixes();
								
								p.sendMessage(prefix + "§eThe chat prefix of the group §a" + permissionGroup.getName() + " §ewas updated§7!");
							} else if(args[2].equalsIgnoreCase("setSuffix")) {
								String value = "";
								
								for (int i = 3; i < args.length; i++)
									value = value + " " + args[i];
								
								value = value.trim().replace("\"", "");
								
								if(value.length() > 16)
									value = value.substring(0, 16);
								
								permissionGroup.setSuffix(value);
								permissionSystem.updatePrefixesAndSuffixes();
								
								p.sendMessage(prefix + "§eThe suffix of the group §a" + permissionGroup.getName() + " §ewas updated§7!");
							} else if(args[2].equalsIgnoreCase("setChatSuffix")) {
								String value = "";
								
								for (int i = 3; i < args.length; i++)
									value = value + " " + args[i];
								
								value = value.trim().replace("\"", "");
								
								permissionGroup.setChatSuffix(value);
								permissionSystem.updatePrefixesAndSuffixes();
								
								p.sendMessage(prefix + "§eThe chat suffix of the group §a" + permissionGroup.getName() + " §ewas updated§7!");
							} else if(args[2].equalsIgnoreCase("setWeight")) {
								int i = 0;
								
								try {
									i = Integer.parseInt(args[3]);
									
									if(i > 999)
										i = 999;
									else if(i < 1)
										i = 1;
								} catch (NumberFormatException e) {
								}
								
								permissionGroup.setWeight(i);
								
								permissionSystem.updatePrefixesAndSuffixes();
							
								p.sendMessage(prefix + "§eThe weight of the group §a" + permissionGroup.getName() + " §ewas updated§7!");
							} else if(args[2].equalsIgnoreCase("setParent")) {
								permissionGroup.setParent(args[3].equals("none") ? null : args[3]);
								permissionGroup.updatePermissions();
							
								p.sendMessage(prefix + "§eThe parent of the group §a" + permissionGroup.getName() + " §ewas updated§7!");
							} else
								utils.sendHelpMessage(p);
						} else
							p.sendMessage(prefix + "§cThe group §a" + args[1] + " §cdoes not exist§7!");
					} else if(args[0].equalsIgnoreCase("user")) {
						PermissionUser permissionUser = permissionUserManager.getPermissionPlayer(args[1]);
						
						if(permissionUser.exists()) {
							if(args[2].equalsIgnoreCase("add")) {
								permissionUser.addPermission(args[3]);
								
								p.sendMessage(prefix + "§eThe permission §a" + args[3] + " §ewas added to the player §d" + args[1] + "§7!");
							} else if(args[2].equalsIgnoreCase("remove")) {
								String permToRemove = args[3];
								
								for (String permission : permissionUser.getPermissions()) {
									if(permission.toLowerCase().startsWith(args[3].toLowerCase())) {
										permToRemove = permission;
										break;
									}
								}
								
								permissionUser.removePermission(permToRemove);
								
								p.sendMessage(prefix + "§cThe permission §a" + permToRemove + " §cwas removed from the player §d" + args[1] + "§7!");
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
											
											List<String> ranks = permissionConfigUtils.getConfig().getStringList("TempRanks");
											UUID uuid = uuidFetching.fetchUUID(name);
											
											if(!(ranks.contains(uuid.toString())))
												ranks.add(uuid.toString());
											
											permissionConfigUtils.getConfig().set("TempRanks", ranks);
											permissionConfigUtils.getConfig().set("Ranks." + uuid.toString() + ".GroupName", permissionGroup.getName());
											permissionConfigUtils.getConfig().set("Ranks." + uuid.toString() + ".Time", time);
											permissionConfigUtils.saveFile();
											
											permissionSystem.updatePrefixesAndSuffixes();
											
											p.sendMessage(prefix + "§eThe group of the player §a" + name + " §ewas set to §d" + permissionGroup.getName() + " §efor §d" + value + " " + unit + "§7!");
										} else {
											utils.sendHelpMessage(p);
										}
									} else {
										for (PermissionGroup group : permissionUser.getGroups())
											group.removeMember(name, false);
										
										permissionGroup.addMember(name, true);
										
										permissionSystem.updatePrefixesAndSuffixes();
										
										p.sendMessage(prefix + "§eThe group of the player §a" + name + " §ewas set to §d" + permissionGroup.getName() + "§7!");
									}
								} else
									p.sendMessage(prefix + "§cThe group §a" + args[3] + " §cdoes not exist§7!");
							} else if(args[2].equalsIgnoreCase("clear")) {
								permissionUser.clearPermissions();
								
								p.sendMessage(prefix + "§cThe permissions of the player §a" + args[1] + " §cwere cleared§7!");
							} else if(args[2].equalsIgnoreCase("setPrefix")) {
								String value = "";
								
								for (int i = 3; i < args.length; i++)
									value = value + " " + args[i];
								
								value = value.trim().replace("\"", "");
								
								if(value.length() > 16)
									value = value.substring(0, 16);
								
								permissionUser.setPrefix(value);
								permissionSystem.updatePrefixesAndSuffixes();
								
								p.sendMessage(prefix + "§eThe prefix of the player §a" + args[1] + " §ewas updated§7!");
							} else if(args[2].equalsIgnoreCase("setChatPrefix")) {
								String value = "";
								
								for (int i = 3; i < args.length; i++)
									value = value + " " + args[i];
								
								value = value.trim().replace("\"", "");
								
								permissionUser.setChatPrefix(value);
								permissionSystem.updatePrefixesAndSuffixes();
								
								p.sendMessage(prefix + "§eThe chat prefix of the player §a" + args[1] + " §ewas updated§7!");
							} else if(args[2].equalsIgnoreCase("setSuffix")) {
								String value = "";
								
								for (int i = 3; i < args.length; i++)
									value = value + " " + args[i];
								
								value = value.trim().replace("\"", "");
								
								if(value.length() > 16)
									value = value.substring(0, 16);
								
								permissionUser.setSuffix(value);
								permissionSystem.updatePrefixesAndSuffixes();
								
								p.sendMessage(prefix + "§eThe suffix of the player §a" + args[1] + " §ewas updated§7!");
							} else if(args[2].equalsIgnoreCase("setChatSuffix")) {
								String value = "";
								
								for (int i = 3; i < args.length; i++)
									value = value + " " + args[i];
								
								value = value.trim().replace("\"", "");
								
								permissionUser.setChatSuffix(value);
								permissionSystem.updatePrefixesAndSuffixes();
								
								p.sendMessage(prefix + "§eThe chat suffix of the player §a" + args[1] + " §ewas updated§7!");
							} else
								utils.sendHelpMessage(p);
						} else
							p.sendMessage(prefix + "§cThe group §a" + args[1] + " §cdoes not exist§7!");
					} else
						utils.sendHelpMessage(p);
				} else
					utils.sendHelpMessage(p);
			} else
				p.sendMessage(utils.getNoPerm());
		} else {
			if(args.length == 1) {
				if(args[0].equalsIgnoreCase("groups") || args[0].equalsIgnoreCase("group")) {
					List<PermissionGroup> list = permissionGroupManager.getPermissionGroups();
					
					if(!(list.isEmpty())) {
						utils.sendConsole("§eCached groups§8:");
						
						for (PermissionGroup permissionGroup : list)
							utils.sendConsole("§e- §a" + permissionGroup.getName());
					} else
						utils.sendConsole("§cThere are no groups to show!");
				} else if(args[0].equalsIgnoreCase("users") || args[0].equalsIgnoreCase("user")) {
					List<String> list = permissionUserManager.getPermissionPlayerUUIDs();
					
					if(!(list.isEmpty())) {
						utils.sendConsole("§eCached user-uuids§8:");
						
						for (String uuid : list)
							utils.sendConsole("§e- §a" + uuid);
					} else
						utils.sendConsole("§cThere are no users to show!");
				} else if(args[0].equalsIgnoreCase("reload")) {
					fileUtils.reloadConfig();
					permissionConfigUtils.reloadConfig();
					
					utils.sendConsole("§eThe config files were reloaded§7!");
				} else if(args[0].equalsIgnoreCase("editor")) {
					if(fileUtils.getConfig().getBoolean("WebServer.Enabled") && (webServerManager != null))
						utils.sendConsole("§eOpen the link in your browser to get to the web editor§8: §ahttp://" + utils.getIpAddress() + ":" + fileUtils.getConfig().getInt("WebServer.Port") + "?authKey=" + webServerManager.getAuthKey());
					else
						utils.sendConsole("§cThe web server is not enabled or not running§7!");
				} else
					utils.sendHelpMessage(sender);
			} else if(args.length == 2) {
				if(args[0].equalsIgnoreCase("group")) {
					PermissionGroup permissionGroup = new PermissionGroup(args[1]);
					
					if(permissionGroup.exists()) {
						utils.sendConsole("§8§m----------§8 [ §6" + permissionGroup.getName() + " §8] §m----------");
						utils.sendConsole("§eMembers§8:" + (permissionGroup.getMemberUUIDs().isEmpty() ? " §cNone" : ""));
						
						for (String value : permissionGroup.getMemberUUIDs())
							utils.sendConsole("§e- §a" + value);
						
						utils.sendConsole("§ePermissions§8:" + (permissionGroup.getPermissions().isEmpty() ? " §cNone" : ""));
						
						for (String value : permissionGroup.getPermissions())
							utils.sendConsole("§e- §a" + value);
						
						String parent = permissionGroup.getParent();
						
						utils.sendConsole("§eParent§8: " + (((parent != null) && !(parent.isEmpty())) ? ("§f'§a" + parent + "§f'") : "§cNone"));
						utils.sendConsole("§ePrefix§8: §f'§a" + permissionGroup.getPrefix() + "§f'");
						utils.sendConsole("§eSuffix§8: §f'§a" + permissionGroup.getSuffix() + "§f'");
						utils.sendConsole("§eChat-Prefix§8: §f'§a" + permissionGroup.getChatPrefix() + "§f'");
						utils.sendConsole("§eChat-Suffix§8: §f'§a" + permissionGroup.getChatSuffix() + "§f'");
						utils.sendConsole("§eDefault§8: §f'§a" + permissionGroup.isDefault() + "§f'");
						utils.sendConsole("§eWeight§8: §f'§a" + permissionGroup.getWeight() + "§f'");
						utils.sendConsole("§8§m----------§8 [ §6" + permissionGroup.getName() + " §8] §m----------");
					} else
						utils.sendConsole("§cThe group §a" + args[1] + " §cdoes not exist§7!");
				} else if(args[0].equalsIgnoreCase("user")) {
					PermissionUser permissionUser;
					Player t  = Bukkit.getPlayer(args[1]);
					UUID uuid;
					String name;
					
					if(t != null) {
						uuid = t.getUniqueId();
						name = t.getName();
					} else {
						name = args[1];
						uuid = uuidFetching.fetchUUID(name);
					}
					
					permissionUser = new PermissionUser(uuid);
					
					utils.sendConsole("§8§m----------§8 [ §6" + name + " §8] §m----------");
					utils.sendConsole("§eGroups§8:" + (permissionUser.getGroups().isEmpty() ? " §cNone" : ""));
					
					String timedGroup = "NONE";
					String formattedTime = "";
					
					if(permissionConfigUtils.getConfig().getStringList("TempRanks").contains(uuid.toString())) {
						timedGroup = permissionConfigUtils.getConfig().getString("Ranks." + uuid.toString() + ".GroupName");
						formattedTime = utils.formatTime(permissionConfigUtils.getConfig().getInt("Ranks." + uuid.toString() + ".Time"));
					}
					
					for (PermissionGroup group : permissionUser.getGroups())
						utils.sendConsole("§e- §a" + group.getName() + (group.getName().equalsIgnoreCase(timedGroup) ? (" §8[§d" + formattedTime + "§8]") : ""));
					
					utils.sendConsole("§ePermissions§8:" + (permissionUser.getPermissions().isEmpty() ? " §cNone" : ""));
					
					for (String value : permissionUser.getPermissions())
						utils.sendConsole("§e- §a" + value);
					
					utils.sendConsole("§ePrefix§8: §f'§a" + permissionUser.getPrefix() + "§f'");
					utils.sendConsole("§eSuffix§8: §f'§a" + permissionUser.getSuffix() + "§f'");
					utils.sendConsole("§eChat-Prefix§8: §f'§a" + permissionUser.getChatPrefix() + "§f'");
					utils.sendConsole("§eChat-Suffix§8: §f'§a" + permissionUser.getChatSuffix() + "§f'");
					utils.sendConsole("§8§m----------§8 [ §6" + name + " §8] §m----------");
				} else if(args[0].equalsIgnoreCase("import")) {
					if(args[1].equalsIgnoreCase("sql")) {
						if(fileUtils.getConfig().getBoolean("MySQL.Enabled")) {
							long start = System.currentTimeMillis();
							
							importUtils.importFromSQL();
							
							utils.sendConsole("§eImported data from §aMySQL §ein §d" + (System.currentTimeMillis() - start) + "ms§7!");
						} else
							utils.sendConsole("§cPlease enable MySQL in the config.yml and reload/restart the server§7!");
					} else if(args[1].equalsIgnoreCase("file")) {
						if(fileUtils.getConfig().getBoolean("MySQL.Enabled")) {
							long start = System.currentTimeMillis();
							
							importUtils.importFromFile();
							
							utils.sendConsole("§eImported data from §apermissions.yml §ein §d" + (System.currentTimeMillis() - start) + "ms§7!");
						} else
							utils.sendConsole("§cPlease enable MySQL in the config.yml and reload/restart the server§7!");
					} else
						utils.sendConsole("§e/perm import «sql | file»");
				} else
					utils.sendHelpMessage(sender);
			} else if(args.length == 3) {
				if(args[0].equalsIgnoreCase("group")) {
					PermissionGroup permissionGroup = new PermissionGroup(args[1]);
					
					if(args[2].equalsIgnoreCase("create")) {
						if(!(permissionGroup.exists())) {
							permissionGroup.registerGroupIfNotExisting();
							
							utils.sendConsole("§eThe group §a" + permissionGroup.getName() + " §ewas created§7!");
						} else
							utils.sendConsole("§cA group with the name §a" + args[1] + " §cdoes already exist§7!");
					} else if(args[2].equalsIgnoreCase("delete")) {
						if(permissionGroup.exists()) {
							List<PermissionGroup> list = permissionGroupManager.getPermissionGroups();
							
							if(list.size() == 1)
								utils.sendConsole("§cYou can not delete this group§7, §cbecause it is the last group§7!");
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
								
								utils.sendConsole("§cThe group §a" + permissionGroup.getName() + " §cwas deleted§7!");
							}
						} else
							utils.sendConsole("§cThe group §a" + args[1] + " §cdoes not exist§7!");
					} else if(permissionGroup.exists()) {
						if(args[2].equalsIgnoreCase("clear")) {
							permissionGroup.clearPermissions();
							
							utils.sendConsole("§cThe permissions of the group §a" + permissionGroup.getName() + " §cwere cleared§7!");
						} else
							utils.sendHelpMessage(sender);
					} else if(!(permissionGroup.exists()))
						utils.sendConsole("§cThe group §a" + args[1] + " §cdoes not exist§7!");
					else
						utils.sendHelpMessage(sender);
				} else if(args[0].equalsIgnoreCase("user")) {
					PermissionUser permissionUser = permissionUserManager.getPermissionPlayer(args[1]);
					
					if(args[2].equalsIgnoreCase("clear")) {
						permissionUser.clearPermissions();
						
						utils.sendConsole("§cThe permissions of the player §a" + args[1] + " §cwere cleared§7!");
					} else
						utils.sendHelpMessage(sender);
				} else
					utils.sendHelpMessage(sender);
			} else if(args.length >= 4) {
				if(args[0].equalsIgnoreCase("group")) {
					PermissionGroup permissionGroup = new PermissionGroup(args[1]);
					
					if(permissionGroup.exists()) {
						if(args[2].equalsIgnoreCase("add")) {
							permissionGroup.addPermission(args[3]);
							
							utils.sendConsole("§eThe permission §a" + args[3] + " §ewas added to the group §a" + permissionGroup.getName() + "§7!");
						} else if(args[2].equalsIgnoreCase("remove")) {
							String permToRemove = args[3];
							
							for (String permission : permissionGroup.getPermissions()) {
								if(permission.toLowerCase().startsWith(args[3].toLowerCase())) {
									permToRemove = permission;
									break;
								}
							}
							
							permissionGroup.removePermission(permToRemove);
							
							utils.sendConsole("§cThe permission §a" + permToRemove + " §cwas removed from the group §a" + permissionGroup.getName() + "§7!");
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
							
							utils.sendConsole("§eThe player §a" + args[3] + " §ewas added to the group §d" + permissionGroup.getName() + "§7!");
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
							
							utils.sendConsole("§cThe player §a" + args[3] + " §cwas removed from the group §d" + permissionGroup.getName() + "§7!");
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
							
							utils.sendConsole("§eThe default group value of the group §a" + permissionGroup.getName() + " §ewas updated§7!");
						} else if(args[2].equalsIgnoreCase("setPrefix")) {
							String value = "";
							
							for (int i = 3; i < args.length; i++)
								value = value + " " + args[i];
							
							value = value.trim().replace("\"", "");
							
							if(value.length() > 16)
								value = value.substring(0, 16);
							
							permissionGroup.setPrefix(value);
							permissionSystem.updatePrefixesAndSuffixes();
							
							utils.sendConsole("§eThe prefix of the group §a" + permissionGroup.getName() + " §ewas updated§7!");
						} else if(args[2].equalsIgnoreCase("setChatPrefix")) {
							String value = "";
							
							for (int i = 3; i < args.length; i++)
								value = value + " " + args[i];
							
							value = value.trim().replace("\"", "");
							
							permissionGroup.setChatPrefix(value);
							permissionSystem.updatePrefixesAndSuffixes();
							
							utils.sendConsole("§eThe chat prefix of the group §a" + permissionGroup.getName() + " §ewas updated§7!");
						} else if(args[2].equalsIgnoreCase("setSuffix")) {
							String value = "";
							
							for (int i = 3; i < args.length; i++)
								value = value + " " + args[i];
							
							value = value.trim().replace("\"", "");
							
							if(value.length() > 16)
								value = value.substring(0, 16);
							
							permissionGroup.setSuffix(value);
							permissionSystem.updatePrefixesAndSuffixes();
							
							utils.sendConsole("§eThe suffix of the group §a" + permissionGroup.getName() + " §ewas updated§7!");
						} else if(args[2].equalsIgnoreCase("setChatSuffix")) {
							String value = "";
							
							for (int i = 3; i < args.length; i++)
								value = value + " " + args[i];
							
							value = value.trim().replace("\"", "");
							
							permissionGroup.setChatSuffix(value);
							permissionSystem.updatePrefixesAndSuffixes();
							
							utils.sendConsole("§eThe chat suffix of the group §a" + permissionGroup.getName() + " §ewas updated§7!");
						} else if(args[2].equalsIgnoreCase("setWeight")) {
							try {
								int i = Integer.parseInt(args[3]);
								
								if(i > 999)
									i = 999;
								
								permissionGroup.setWeight(i);
							} catch (NumberFormatException e) {
								permissionGroup.setWeight(0);
							}
							
							permissionSystem.updatePrefixesAndSuffixes();
						
							utils.sendConsole("§eThe weight of the group §a" + permissionGroup.getName() + " §ewas updated§7!");
						} else if(args[2].equalsIgnoreCase("setParent")) {
							permissionGroup.setParent(args[3].equals("none") ? null : args[3]);
							permissionGroup.updatePermissions();
						
							utils.sendConsole("§eThe parent of the group §a" + permissionGroup.getName() + " §ewas updated§7!");
						} else
							utils.sendHelpMessage(sender);
					} else
						utils.sendConsole("§cThe group §a" + args[1] + " §cdoes not exist§7!");
				} else if(args[0].equalsIgnoreCase("user")) {
					PermissionUser permissionUser = permissionUserManager.getPermissionPlayer(args[1]);
					
					if(permissionUser.exists()) {
						if(args[2].equalsIgnoreCase("add")) {
							permissionUser.addPermission(args[3]);
							
							utils.sendConsole("§eThe permission §a" + args[3] + " §ewas added to the player §a" + args[1] + "§7!");
						} else if(args[2].equalsIgnoreCase("remove")) {
							String permToRemove = args[3];
							
							for (String permission : permissionUser.getPermissions()) {
								if(permission.toLowerCase().startsWith(args[3].toLowerCase())) {
									permToRemove = permission;
									break;
								}
							}
							
							permissionUser.removePermission(permToRemove);
							
							utils.sendConsole("§cThe permission §a" + permToRemove + " §cwas removed from the player §d" + args[1] + "§7!");
						} else if(args[2].equalsIgnoreCase("clear")) {
							permissionUser.clearPermissions();
							
							utils.sendConsole("§cThe permissions of the player §a" + args[1] + " §cwere cleared§7!");
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
										
										List<String> ranks = permissionConfigUtils.getConfig().getStringList("TempRanks");
										UUID uuid = uuidFetching.fetchUUID(name);
										
										if(!(ranks.contains(uuid.toString())))
											ranks.add(uuid.toString());
										
										permissionConfigUtils.getConfig().set("TempRanks", ranks);
										permissionConfigUtils.getConfig().set("Ranks." + uuid.toString() + ".GroupName", permissionGroup.getName());
										permissionConfigUtils.getConfig().set("Ranks." + uuid.toString() + ".Time", time);
										permissionConfigUtils.saveFile();
										
										permissionSystem.updatePrefixesAndSuffixes();
										
										utils.sendConsole("§eThe group of the player §a" + name + " §ewas set to §d" + permissionGroup.getName() + " §efor §d" + value + " " + unit + "§7!");
									} else
										utils.sendHelpMessage(sender);
								} else {
									for (PermissionGroup group : permissionUser.getGroups())
										group.removeMember(name, false);
									
									permissionGroup.addMember(name, true);
									
									permissionSystem.updatePrefixesAndSuffixes();
									
									utils.sendConsole("§eThe group of the player §a" + name + " §ewas set to §d" + permissionGroup.getName() + "§7!");
								}
							} else
								utils.sendConsole("§cThe group §a" + args[3] + " §cdoes not exist§7!");
						} else if(args[2].equalsIgnoreCase("setPrefix")) {
							String value = "";
							
							for (int i = 3; i < args.length; i++)
								value = value + " " + args[i];
							
							value = value.trim().replace("\"", "");
							
							if(value.length() > 16)
								value = value.substring(0, 16);
							
							permissionUser.setPrefix(value);
							permissionSystem.updatePrefixesAndSuffixes();
							
							utils.sendConsole("§eThe prefix of the player §a" + args[1] + " §ewas updated§7!");
						} else if(args[2].equalsIgnoreCase("setChatPrefix")) {
							String value = "";
							
							for (int i = 3; i < args.length; i++)
								value = value + " " + args[i];
							
							value = value.trim().replace("\"", "");
							
							permissionUser.setChatPrefix(value);
							permissionSystem.updatePrefixesAndSuffixes();
							
							utils.sendConsole("§eThe chat prefix of the player §a" + args[1] + " §ewas updated§7!");
						} else if(args[2].equalsIgnoreCase("setSuffix")) {
							String value = "";
							
							for (int i = 3; i < args.length; i++)
								value = value + " " + args[i];
							
							value = value.trim().replace("\"", "");
							
							if(value.length() > 16)
								value = value.substring(0, 16);
							
							permissionUser.setSuffix(value);
							permissionSystem.updatePrefixesAndSuffixes();
							
							utils.sendConsole("§eThe suffix of the player §a" + args[1] + " §ewas updated§7!");
						} else if(args[2].equalsIgnoreCase("setChatSuffix")) {
							String value = "";
							
							for (int i = 3; i < args.length; i++)
								value = value + " " + args[i];
							
							value = value.trim().replace("\"", "");
							
							permissionUser.setChatSuffix(value);
							permissionSystem.updatePrefixesAndSuffixes();
							
							utils.sendConsole("§eThe chat suffix of the player §a" + args[1] + " §ewas updated§7!");
						} else
							utils.sendHelpMessage(sender);
					} else
						utils.sendConsole("§cThe group §a" + args[1] + " §cdoes not exist§7!");
				} else
					utils.sendHelpMessage(sender);
			} else
				utils.sendHelpMessage(sender);
		}
		
		return true;
	}

}

package net.dev.permissions.nms;

import java.lang.reflect.Constructor;
import java.util.*;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import net.dev.clansapi.ClansAPI;
import net.dev.clansapi.sql.MySQLClanManager;
import net.dev.eazynick.api.NickManager;
import net.dev.permissions.PermissionSystem;
import net.dev.permissions.utilities.permissionmanagement.PermissionGroup;
import net.dev.permissions.utilities.permissionmanagement.PermissionUser;

import me.clip.placeholderapi.PlaceholderAPI;

public class ScoreboardTeamHandler {

	private PermissionSystem permissionSystem;
	private ReflectionHelper reflectionHelper;
	
	private HashMap<String, ArrayList<String>> teamMembers = new HashMap<>();
	private HashMap<String, String> teamPrefixes = new HashMap<>();
	private HashMap<String, String> teamSuffixes = new HashMap<>();
	private HashMap<String, String> teamPrefixesChat = new HashMap<>();
	private HashMap<String, String> teamSuffixesChat = new HashMap<>();
	private HashMap<String, String> teamPrefixesPlayerList = new HashMap<>();
	private HashMap<String, String> teamSuffixesPlayerList = new HashMap<>();
	private ArrayList<Player> receivedPacket = new ArrayList();
	private Object packet;
	private String version;
	
	public ScoreboardTeamHandler() {
		this.permissionSystem = PermissionSystem.getInstance();
		this.reflectionHelper = permissionSystem.getReflectionHelper();
		this.version = reflectionHelper.getVersion();
	}
	
	public String getTeamName(Player player) {
		HashMap<String, String> groupNames = permissionSystem.getGroupNames();
		
		PermissionUser user = permissionSystem.getPermissionUserManager().getPermissionPlayer(player.getName());
		PermissionGroup group = user.getHighestGroup();
		
		if(group != null) {
			boolean hasCustomPrefixSuffix = !(((user.getChatPrefix() == null) && (user.getChatSuffix() == null)) || (user.getChatPrefix().isEmpty() && user.getChatSuffix().isEmpty()));
			String rankWeight = String.valueOf(group.getWeight());
	
			for (int i = 1; i < (3 - String.valueOf(group.getWeight()).length()); i++) {
				if(!(hasCustomPrefixSuffix)) {
					if((rankWeight + group.getName()).length() < 16)
						rankWeight = "0" + rankWeight;
				} else {
					if((rankWeight + player.getName()).length() < 16)
						rankWeight = "0" + rankWeight;
				}
			}
			
			return rankWeight + (hasCustomPrefixSuffix ? ((((rankWeight + player.getName()).length()) > 16) ? player.getName().substring(0, 16 - rankWeight.length()) : player.getName()) : group.getName());
		}
		
		for (PermissionGroup groupAll : permissionSystem.getPermissionGroupManager().getPermissionGroups()) {
			if(groupAll.isDefault())
				return groupNames.get(groupAll.getName());
		}
		
		return new ArrayList<>(groupNames.values()).get(0);
	}
	
	public void destroyTeam(String teamName) {
		try {
			boolean is17 = version.startsWith("1_17"), is18 = version.startsWith("1_18");
			
			//Create packet instance
			Constructor<?> constructor = reflectionHelper.getNMSClass((is17 || is18) ? "network.protocol.game.PacketPlayOutScoreboardTeam" : "PacketPlayOutScoreboardTeam").getDeclaredConstructor((is17 || is18) ? new Class[] { String.class, int.class, Optional.class, Collection.class } : new Class[0]);
			constructor.setAccessible(true);
			
			packet = constructor.newInstance((is17 || is18) ? new Object[] { null, 0, null, new ArrayList<>() } : new Object[0]);
			
			//Set packet fields
			if(!(version.equals("1_7_R4") || version.equals("1_8_R1"))) {
				if(reflectionHelper.isNewVersion()) {
					try {
						reflectionHelper.setField(packet, "a", teamName);
						reflectionHelper.setField(packet, "b", getAsIChatBaseComponent(teamName));
						reflectionHelper.setField(packet, "e", "ALWAYS");
						reflectionHelper.setField(packet, "i", 1);
					} catch (Exception ex) {
						if(is17 || is18) {
							reflectionHelper.setField(packet, "h", 1);
							reflectionHelper.setField(packet, "i", teamName);
							
							Object scoreboardTeam = reflectionHelper.getNMSClass("world.scores.ScoreboardTeam").getConstructor(reflectionHelper.getNMSClass("world.scores.Scoreboard"), String.class).newInstance(null, teamName);
							reflectionHelper.setField(scoreboardTeam, is18 ? "d" : "e", teamName);
							
							reflectionHelper.setField(packet, "k", Optional.of(reflectionHelper.getSubClass(packet.getClass(), "b").getConstructor(scoreboardTeam.getClass()).newInstance(scoreboardTeam)));
						} else {
							reflectionHelper.setField(packet, "a", teamName);
							reflectionHelper.setField(packet, "b", getAsIChatBaseComponent(teamName));
							reflectionHelper.setField(packet, "e", "ALWAYS");
							reflectionHelper.setField(packet, "j", 1);
						}
					}
				} else {
					try {
						reflectionHelper.setField(packet, "a", teamName);
						reflectionHelper.setField(packet, "b", teamName);
						reflectionHelper.setField(packet, "e", "ALWAYS");
						reflectionHelper.setField(packet, "h", 1);
					} catch (Exception ex) {
						reflectionHelper.setField(packet, "a", teamName);
						reflectionHelper.setField(packet, "b", teamName);
						reflectionHelper.setField(packet, "e", "ALWAYS");
						reflectionHelper.setField(packet, "i", 1);
					}
				}
			}
			
			Bukkit.getOnlinePlayers().stream().filter(receivedPacket::contains).forEach(currentPlayer -> sendPacket(currentPlayer, packet));
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private void createTeam(String teamName, String rawPrefix, String rawSuffix) {
		boolean is17 = version.startsWith("1_17"), is18 = version.startsWith("1_18");
		
		Bukkit.getOnlinePlayers().forEach(currentPlayer -> {
			String prefix = rawPrefix, suffix = rawSuffix;
			
			if(permissionSystem.isPlaceholderAPIInstalled()) {
				prefix = PlaceholderAPI.setPlaceholders(currentPlayer, prefix);
				suffix = PlaceholderAPI.setPlaceholders(currentPlayer, suffix);
			}
			
			try {
				//Create packet instance
				Constructor<?> constructor = reflectionHelper.getNMSClass((is17 || is18) ? "network.protocol.game.PacketPlayOutScoreboardTeam" : "PacketPlayOutScoreboardTeam").getDeclaredConstructor((is17 || is18) ? new Class[] { String.class, int.class, Optional.class, Collection.class } : new Class[0]);
				constructor.setAccessible(true);
				
				packet = constructor.newInstance((is17 || is18) ? new Object[] { null, 0, null, new ArrayList<>() } : new Object[0]);
				
				//Set packet fields
				if(!(version.equals("1_7_R4") || version.equals("1_8_R1"))) {
					if(reflectionHelper.isNewVersion()) {
						try {
							reflectionHelper.setField(packet, "a", teamName);
							reflectionHelper.setField(packet, "b", getAsIChatBaseComponent(teamName));
							reflectionHelper.setField(packet, "c", getAsIChatBaseComponent(prefix));
							reflectionHelper.setField(packet, "d", getAsIChatBaseComponent(suffix));
							reflectionHelper.setField(packet, "e", "ALWAYS");
							reflectionHelper.setField(packet, "g", teamMembers.get(teamName));
							reflectionHelper.setField(packet, "i", 0);
						} catch (Exception ex) {
							String colorName = (is17 || is18) ? "v" : "RESET";
							
							if(prefix.length() > 1) {
								for (int i = prefix.length() - 1; i >= 0; i--) {
									if(i < (prefix.length() - 1)) {
										if(prefix.charAt(i) == '§') {
											char c = prefix.charAt(i + 1);
											
											if((c != 'k') && (c != 'l') && (c != 'm') && (c != 'n') && (c != 'o')) {
												switch (c) {
													case '0':
														colorName = (is17 || is18) ? "a" : "BLACK";
														break;
													case '1':
														colorName = (is17 || is18) ? "b" : "DARK_BLUE";
														break;
													case '2':
														colorName = (is17 || is18) ? "c" : "DARK_GREEN";
														break;
													case '3':
														colorName = (is17 || is18) ? "d" : "DARK_AQUA";
														break;
													case '4':
														colorName = (is17 || is18) ? "e" : "DARK_RED";
														break;
													case '5':
														colorName = (is17 || is18) ? "f" : "DARK_PURPLE";
														break;
													case '6':
														colorName = (is17 || is18) ? "g" : "GOLD";
														break;
													case '7':
														colorName = (is17 || is18) ? "h" : "GRAY";
														break;
													case '8':
														colorName = (is17 || is18) ? "i" : "DARK_GRAY";
														break;
													case '9':
														colorName = (is17 || is18) ? "j" : "BLUE";
														break;
													case 'a':
														colorName = (is17 || is18) ? "k" : "GREEN";
														break;
													case 'b':
														colorName = (is17 || is18) ? "l" : "AQUA";
														break;
													case 'c':
														colorName = (is17 || is18) ? "m" : "RED";
														break;
													case 'd':
														colorName = (is17 || is18) ? "n" : "LIGHT_PURPLE";
														break;
													case 'e':
														colorName = (is17 || is18) ? "o" : "YELLOW";
														break;
													case 'f':
														colorName = (is17 || is18) ? "p" : "WHITE";
														break;
													case 'r':
														colorName = (is17 || is18) ? "v" : "RESET";
														break;
													default:
														break;
												}
												
												break;
											}
										}
									}
								}
							}
							
							if(is17 || is18) {
								reflectionHelper.setField(packet, "h", 0);
								reflectionHelper.setField(packet, "i", teamName);
								reflectionHelper.setField(packet, "j", teamMembers.get(teamName));
								
								Object scoreboardTeam = reflectionHelper.getNMSClass("world.scores.ScoreboardTeam").getConstructor(reflectionHelper.getNMSClass("world.scores.Scoreboard"), String.class).newInstance(null, teamName);
								reflectionHelper.setField(scoreboardTeam, is18 ? "d" : "e", teamName);
								reflectionHelper.setField(scoreboardTeam, is18 ? "g" : "h", getAsIChatBaseComponent(prefix));							
								reflectionHelper.setField(scoreboardTeam, is18 ? "h" : "i", getAsIChatBaseComponent(suffix));							
								reflectionHelper.setField(scoreboardTeam, is18 ? "i" : "j", false);							
								reflectionHelper.setField(scoreboardTeam, is18 ? "j" : "k", false);							
								reflectionHelper.setField(scoreboardTeam, is18 ? "m" : "n", reflectionHelper.getField(reflectionHelper.getNMSClass("EnumChatFormat"), colorName).get(null));
								
								reflectionHelper.setField(packet, "k", Optional.of(reflectionHelper.getSubClass(packet.getClass(), "b").getConstructor(scoreboardTeam.getClass()).newInstance(scoreboardTeam)));
							} else {
								reflectionHelper.setField(packet, "a", teamName);
								reflectionHelper.setField(packet, "b", getAsIChatBaseComponent(teamName));
								reflectionHelper.setField(packet, "c", getAsIChatBaseComponent(prefix));
								reflectionHelper.setField(packet, "d", getAsIChatBaseComponent(suffix));
								reflectionHelper.setField(packet, "e", "ALWAYS");
								reflectionHelper.setField(packet, "g", reflectionHelper.getField(reflectionHelper.getNMSClass("EnumChatFormat"), colorName).get(null));
								reflectionHelper.setField(packet, "h", teamMembers.get(teamName));
								reflectionHelper.setField(packet, "j", 0);
							}
						}
					} else {
						try {
							reflectionHelper.setField(packet, "a", teamName);
							reflectionHelper.setField(packet, "b", teamName);
							reflectionHelper.setField(packet, "c", prefix);
							reflectionHelper.setField(packet, "d", suffix);
							reflectionHelper.setField(packet, "e", "ALWAYS");
							reflectionHelper.setField(packet, "g", teamMembers.get(teamName));
							reflectionHelper.setField(packet, "h", 0);
						} catch (Exception ex) {
							reflectionHelper.setField(packet, "a", teamName);
							reflectionHelper.setField(packet, "b", teamName);
							reflectionHelper.setField(packet, "c", prefix);
							reflectionHelper.setField(packet, "d", suffix);
							reflectionHelper.setField(packet, "e", "ALWAYS");
							reflectionHelper.setField(packet, "h", teamMembers.get(teamName));
							reflectionHelper.setField(packet, "i", 0);
						}
					}
				}
			
				sendPacket(currentPlayer, packet);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			
			if(!(receivedPacket.contains(currentPlayer)))
				receivedPacket.add(currentPlayer);
		});
	}
	
	public void updateTeams() {
		if (!(teamMembers.isEmpty())) {
			for (String team : teamMembers.keySet()) {
				if (team != null) {
					if (!(teamMembers.get(team).isEmpty())) {
						destroyTeam(team);
						createTeam(team, teamPrefixes.get(team), teamSuffixes.get(team));

						ArrayList<String> toRemove = new ArrayList<>();
						
						for (String playerName : teamMembers.get(team)) {
							Player player = Bukkit.getPlayer(playerName);

							if (player != null) {
								String clan = "", clan2 = "";
								
								if(permissionSystem.isClansAPIInstalled()) {
									MySQLClanManager mysqlClanManager = ClansAPI.getInstance().getClanManager();
									String clanName = mysqlClanManager.getClanOfUser(player.getUniqueId());
									
									if(!(clanName.equals("-------NONE-------")))
										clan = " §7[§e" + mysqlClanManager.getDisplayNameOfClan(clanName) + "§7]";
								}
								
								if (permissionSystem.isEazyNickInstalled()) {
									if (!(new NickManager(player).isNicked())) {
										player.setDisplayName(clan2 + teamPrefixesChat.get(team) + player.getName() + teamSuffixesChat.get(team));
										
										if(!(version.equals("1_7_R4")))
											player.setPlayerListName(teamPrefixesPlayerList.get(team) + player.getName() + teamSuffixesPlayerList.get(team) + clan);
									}
								} else {
									player.setDisplayName(clan2 + teamPrefixesChat.get(team) + player.getName() + teamSuffixesChat.get(team));
									
									if(!(version.equals("1_7_R4")))
										player.setPlayerListName(teamPrefixesPlayerList.get(team) + player.getName() + teamSuffixesPlayerList.get(team) + clan);
								}
							} else
								toRemove.add(playerName);
						}
						
						toRemove.forEach(playerName -> teamMembers.get(team).remove(playerName));
					}
				}
			}
		}
	}

	public void addPlayerToTeam(String teamName, String playerName) {
		removePlayerFromTeams(playerName);

		if (teamMembers.containsKey(teamName))
			teamMembers.get(teamName).add(playerName);
	}

	public void removePlayerFromTeams(String playerName) {
		for (String teamName : teamMembers.keySet()) {
			if (teamMembers.get(teamName).contains(playerName))
				teamMembers.get(teamName).remove(playerName);
		}
	}
	
	private Object getAsIChatBaseComponent(String text) {
		try {
			return reflectionHelper.getNMSClass((version.startsWith("1_17") || version.startsWith("1_18")) ? "network.chat.IChatBaseComponent" : "IChatBaseComponent").getDeclaredClasses()[0].getMethod("a", String.class).invoke(null, "{\"text\":\"" + text + "\"}");
		} catch (Exception e) {
			return null;
		}
	}

	private void sendPacket(Player player, Object packet) {
		boolean is17 = version.startsWith("1_17"), is18 = version.startsWith("1_18");
		
		try {
			Object playerHandle = player.getClass().getMethod("getHandle").invoke(player);
			Object playerConnection = playerHandle.getClass().getField((is17 || is18) ? "b" : "playerConnection").get(playerHandle);
			playerConnection.getClass().getMethod(is18 ? "a" : "sendPacket", reflectionHelper.getNMSClass((is17 || is18) ? "network.protocol.Packet" : "Packet")).invoke(playerConnection, packet);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public HashMap<String, ArrayList<String>> getTeamMembers() {
		return teamMembers;
	}

	public HashMap<String, String> getTeamPrefixes() {
		return teamPrefixes;
	}

	public HashMap<String, String> getTeamSuffixes() {
		return teamSuffixes;
	}

	public HashMap<String, String> getTeamPrefixesChat() {
		return teamPrefixesChat;
	}

	public HashMap<String, String> getTeamSuffixesChat() {
		return teamSuffixesChat;
	}

	public HashMap<String, String> getTeamPrefixesPlayerList() {
		return teamPrefixesPlayerList;
	}

	public HashMap<String, String> getTeamSuffixesPlayerList() {
		return teamSuffixesPlayerList;
	}
	
	public void setTeamMembers(HashMap<String, ArrayList<String>> teamMembers) {
		this.teamMembers = teamMembers;
	}

	public void setTeamPrefixes(HashMap<String, String> teamPrefixes) {
		this.teamPrefixes = teamPrefixes;
	}

	public void setTeamSuffixes(HashMap<String, String> teamSuffixes) {
		this.teamSuffixes = teamSuffixes;
	}

	public void setTeamPrefixesChat(HashMap<String, String> teamPrefixesChat) {
		this.teamPrefixesChat = teamPrefixesChat;
	}

	public void setTeamSuffixesChat(HashMap<String, String> teamSuffixesChat) {
		this.teamSuffixesChat = teamSuffixesChat;
	}

	public void setTeamPrefixesPlayerList(HashMap<String, String> teamPrefixesPlayerList) {
		this.teamPrefixesPlayerList = teamPrefixesPlayerList;
	}

	public void setTeamSuffixesPlayerList(HashMap<String, String> teamSuffixesPlayerList) {
		this.teamSuffixesPlayerList = teamSuffixesPlayerList;
	}
	
}

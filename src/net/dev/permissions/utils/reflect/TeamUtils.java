package net.dev.permissions.utils.reflect;

import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import net.dev.clansapi.ClansAPI;
import net.dev.clansapi.sql.MySQLClanManager;
import net.dev.eazynick.api.NickManager;
import net.dev.permissions.PermissionSystem;
import net.dev.permissions.utils.permissionmanagement.PermissionGroup;
import net.dev.permissions.utils.permissionmanagement.PermissionUser;

import eu.neropvp.clans.Clans;

public class TeamUtils {

	private PermissionSystem permissionSystem;
	private ReflectUtils reflectUtils;
	
	private HashMap<String, ArrayList<String>> teamMembers = new HashMap<>();
	private HashMap<String, String> teamPrefixes = new HashMap<>();
	private HashMap<String, String> teamSuffixes = new HashMap<>();
	private HashMap<String, String> teamPrefixesChat = new HashMap<>();
	private HashMap<String, String> teamSuffixesChat = new HashMap<>();
	private HashMap<String, String> teamPrefixesPlayerList = new HashMap<>();
	private HashMap<String, String> teamSuffixesPlayerList = new HashMap<>();
	private Object packet;
	private String version;
	
	public TeamUtils() {
		this.permissionSystem = PermissionSystem.getInstance();
		this.reflectUtils = permissionSystem.getReflectUtils();
		this.version = reflectUtils.getVersion();
	}
	
	public String getTeamName(Player p) {
		HashMap<String, String> groupNames = permissionSystem.getGroupNames();
		
		PermissionUser user = permissionSystem.getPermissionUserManager().getPermissionPlayer(p.getName());
		PermissionGroup group = user.getHighestGroup();
		
		if(group != null) {
			boolean hasCustomPrefixSuffix = !(((user.getChatPrefix() == null) && (user.getChatSuffix() == null)) || (user.getChatPrefix().isEmpty() && user.getChatSuffix().isEmpty()));
			String rankWeight = String.valueOf(group.getWeight());
	
			for (int i = 1; i < (3 - String.valueOf(group.getWeight()).length()); i++) {
				if(!(hasCustomPrefixSuffix)) {
					if((rankWeight + group.getName()).length() < 16)
						rankWeight = "0" + rankWeight;
				} else {
					if((rankWeight + p.getName()).length() < 16)
						rankWeight = "0" + rankWeight;
				}
			}
			
			return rankWeight + (hasCustomPrefixSuffix ? ((((rankWeight + p.getName()).length()) > 16) ? p.getName().substring(0, 16 - rankWeight.length()) : p.getName()) : group.getName());
		}
		
		for (PermissionGroup groupAll : permissionSystem.getPermissionGroupManager().getPermissionGroups()) {
			if(groupAll.isDefault())
				return groupNames.get(groupAll.getName());
		}
		
		return new ArrayList<>(groupNames.values()).get(0);
	}
	
	private void destroyTeam(String teamName) {
		try {
			packet = reflectUtils.getNMSClass("PacketPlayOutScoreboardTeam").getConstructor(new Class[0]).newInstance(new Object[0]);
			
			if(!(version.equalsIgnoreCase("v1_7_R4"))) {
				boolean newVersion = Integer.parseInt(version.replace("v", "").split("_")[1]) > 12;
				
				try {
					reflectUtils.setField(packet, "a", teamName);
					reflectUtils.setField(packet, "b", newVersion ? getAsIChatBaseComponent(teamName) : teamName);
					reflectUtils.setField(packet, "e", "ALWAYS");
					reflectUtils.setField(packet, "h", 1);
				} catch (Exception ex) {
					reflectUtils.setField(packet, "a", teamName);
					reflectUtils.setField(packet, "b", newVersion ? getAsIChatBaseComponent(teamName) : teamName);
					reflectUtils.setField(packet, "e", "ALWAYS");
					reflectUtils.setField(packet, "i", 1);
				}
			} else {
				try {
					reflectUtils.setField(packet, "a", teamName);
					reflectUtils.setField(packet, "f", 1);
				} catch (Exception ex) {
				}
			}
			
			for(Player t : Bukkit.getOnlinePlayers())
				sendPacket(t, packet);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void createTeam(String teamName, String prefix, String suffix) {
		try {
			packet = reflectUtils.getNMSClass("PacketPlayOutScoreboardTeam").getConstructor(new Class[0]).newInstance(new Object[0]);
			
			if(!(version.equalsIgnoreCase("v1_7_R4"))) {
				boolean newVersion = Integer.parseInt(version.replace("v", "").split("_")[1]) > 12;
				
				try {
					reflectUtils.setField(packet, "a", teamName);
					reflectUtils.setField(packet, "b", newVersion ? getAsIChatBaseComponent(teamName) : teamName);
					reflectUtils.setField(packet, "c", newVersion ? getAsIChatBaseComponent(prefix) : prefix);
					reflectUtils.setField(packet, "d", newVersion ? getAsIChatBaseComponent(suffix) : suffix);
					reflectUtils.setField(packet, "e", "ALWAYS");
					reflectUtils.setField(packet, "g", teamMembers.get(teamName));
					reflectUtils.setField(packet, "h", 0);
				} catch (Exception ex) {
					reflectUtils.setField(packet, "a", teamName);
					reflectUtils.setField(packet, "b", newVersion ? getAsIChatBaseComponent(teamName) : teamName);
					reflectUtils.setField(packet, "c", newVersion ? getAsIChatBaseComponent(prefix) : prefix);
					reflectUtils.setField(packet, "d", newVersion ? getAsIChatBaseComponent(suffix) : suffix);
					reflectUtils.setField(packet, "e", "ALWAYS");
					reflectUtils.setField(packet, "h", teamMembers.get(teamName));
					reflectUtils.setField(packet, "i", 0);
				} 
			} else {
				reflectUtils.setField(packet, "a", teamName);
				reflectUtils.setField(packet, "b", teamName);
				reflectUtils.setField(packet, "c", prefix);
				reflectUtils.setField(packet, "d", suffix);
				reflectUtils.setField(packet, "e", teamMembers.get(teamName));
				reflectUtils.setField(packet, "f", 0);
				reflectUtils.setField(packet, "g", 0);
			}
			
			for(Player t : Bukkit.getOnlinePlayers())
				sendPacket(t, packet);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private Object getAsIChatBaseComponent(String text) {
		try {
			return reflectUtils.getNMSClass("IChatBaseComponent").getDeclaredClasses()[0].getMethod("a", String.class).invoke(null, "{\"text\":\"" + text + "\"}");
		} catch (Exception e) {
			return null;
		}
	}

	public void updateTeams() {
		if (teamMembers.size() >= 1) {
			for (String team : teamMembers.keySet()) {
				if (team != null) {
					destroyTeam(team);
					createTeam(team, teamPrefixes.get(team), teamSuffixes.get(team));

					if (teamMembers.get(team).size() >= 1) {
						ArrayList<String> toRemove = new ArrayList<>();
						
						for (String playerName : teamMembers.get(team)) {
							Player p = Bukkit.getPlayer(playerName);

							if (p != null) {
								String clan = "", clan2 = "";
								
								if(permissionSystem.isClansAPIInstalled()) {
									MySQLClanManager mysqlClanManager = ClansAPI.getInstance().getClanManager();
									String clanName = mysqlClanManager.getClanOfUser(p.getUniqueId());
									
									if(!(clanName.equals("-------NONE-------")))
										clan = " §7[§e" + mysqlClanManager.getDisplayNameOfClan(clanName) + "§7]";
								}
								
								if(permissionSystem.isClansInstalled()) {
									eu.neropvp.clans.sql.MySQLClanManager mysqlClanManager = Clans.getInstance().getMySQLClanManager();
									String clanName = mysqlClanManager.getClanOfUser(p.getUniqueId());
									
									if(!(clanName.equals("-------NONE-------")))
										clan2 = "§7[§e" + mysqlClanManager.getDisplayNameOfClan(clanName) + "§7] ";
								}
								
								if (permissionSystem.isEazyNickInstalled()) {
									if (!(new NickManager(p).isNicked())) {
										p.setDisplayName(clan2 + teamPrefixesChat.get(team) + p.getName() + teamSuffixesChat.get(team));
										
										if(!(version.equals("v1_7_R4")))
											p.setPlayerListName(teamPrefixesPlayerList.get(team) + p.getName() + teamSuffixesPlayerList.get(team) + clan);
									}
								} else {
									p.setDisplayName(clan2 + teamPrefixesChat.get(team) + p.getName() + teamSuffixesChat.get(team));
									
									if(!(version.equals("v1_7_R4")))
										p.setPlayerListName(teamPrefixesPlayerList.get(team) + p.getName() + teamSuffixesPlayerList.get(team) + clan);
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

	private void sendPacket(Player p, Object packet) {
		try {
			Object playerHandle = p.getClass().getMethod("getHandle", new Class[0]).invoke(p, new Object[0]);
			Object playerConnection = playerHandle.getClass().getField("playerConnection").get(playerHandle);
			playerConnection.getClass().getMethod("sendPacket", new Class[] { reflectUtils.getNMSClass("Packet") }).invoke(playerConnection, new Object[] { packet });
		} catch (Exception e) {
			e.printStackTrace();
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

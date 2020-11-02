package net.dev.permissions.utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachment;

import net.dev.permissions.PermissionSystem;

public class Utils {

	private String prefix, noPerm;
	private HashMap<UUID, PermissionAttachment> attachments = new HashMap<>();
	private ArrayList<Player> debugging = new ArrayList<>();
	
	public void sendConsole(String msg) {
		Bukkit.getConsoleSender().sendMessage(prefix + msg);
	}

	public void sendHelpMessage(CommandSender sender) {
		sender.sendMessage(prefix + "§3" + PermissionSystem.getInstance().getDescription().getName() + " §8- §eCommand-Help§8:");
		sender.sendMessage(prefix);
		sender.sendMessage(prefix + "§eInformation§8:");
		sender.sendMessage(prefix + "§8┣ §a/perm groups §8| §7Show a list of all groups");
		sender.sendMessage(prefix + "§8┣ §a/perm users §8| §7Show a list of all user uuids");
		sender.sendMessage(prefix + "§8┣ §a/perm group «group name» §8| §7Show some information of a group");
		sender.sendMessage(prefix + "§8┗ §a/perm user «player name» §8| §7Show some information of a player");
		sender.sendMessage(prefix);
		sender.sendMessage(prefix + "§eBackend-Management");
		sender.sendMessage(prefix + "§8┣ §a/perm import «sql | file» §8| §7Import permission data from SQL to file or from file to SQL");
		sender.sendMessage(prefix + "§8┣ §a/perm reload §8| §7Reload all configuration files");
		sender.sendMessage(prefix + "§8┣ §a/perm editor §8| §7Open the web editor");
		sender.sendMessage(prefix + "§8┗ §a/debugperms §8| §7Enter/Leave debug mode");
		sender.sendMessage(prefix);
		sender.sendMessage(prefix + "§ePermission-Management§8:");
		sender.sendMessage(prefix + "§8┣ §a/perm group «group name» add «permission» §8| §7Add a permission to a group");
		sender.sendMessage(prefix + "§8┣ §a/perm group «group name» remove «permission» §8| §7Remove a permission from a group");
		sender.sendMessage(prefix + "§8┣ §a/perm group «group name» clear §8| §7Clear all permissions of a group");
		sender.sendMessage(prefix + "§8┣ §a/perm user «player name» add «permission» §8| §7Add a permission to a player");
		sender.sendMessage(prefix + "§8┣ §a/perm user «player name» remove «permission» §8| §7Remove a permission from a player");
		sender.sendMessage(prefix + "§8┗ §a/perm user «player name» clear §8| §7Clear all permissions of a player");
		sender.sendMessage(prefix);
		sender.sendMessage(prefix + "§eGroup-Management§8:");
		sender.sendMessage(prefix + "§8┣ §a/perm group «group name» create §8| §7Create a group");
		sender.sendMessage(prefix + "§8┣ §a/perm group «group name» delete §8| §7Delete a group");
		sender.sendMessage(prefix + "§8┣ §a/perm group «group name» addMember «player name» §8| §7Add a player to a group");
		sender.sendMessage(prefix + "§8┣ §a/perm group «group name» removeMember «player name» §8| §7Remove a player from a group");
		sender.sendMessage(prefix + "§8┣ §a/perm group «group name» setPrefix «value» §8| §7Set the prefix of a group");
		sender.sendMessage(prefix + "§8┣ §a/perm group «group name» setChatPrefix «value» §8| §7Set the chat prefix of a group");
		sender.sendMessage(prefix + "§8┣ §a/perm group «group name» setSuffix «value» §8| §7Set the suffix of a group");
		sender.sendMessage(prefix + "§8┣ §a/perm group «group name» setChatSuffix «value» §8| §7Set the chat suffix of a group");
		sender.sendMessage(prefix + "§8┣ §a/perm group «group name» setDefault «value» §8| §7Set a group as the (not) primary group");
		sender.sendMessage(prefix + "§8┣ §a/perm group «group name» setWeight «value» §8| §7Set the weight of a group in the player list");
		sender.sendMessage(prefix + "§8┗ §a/perm group «group name» setParent «value | none» §8| §7Set/Remove the parent of a group");
		sender.sendMessage(prefix);
		sender.sendMessage(prefix + "§eUser-Management§8:");
		sender.sendMessage(prefix + "§8┣ §a/perm user «player name» setGroup «group name» («value» «seconds | minutes | hours| days | months | years») §8| §7Set the group of a player");
		sender.sendMessage(prefix + "§8┣ §a/perm user «player name» setPrefix «value» §8| §7Set the prefix of a player");
		sender.sendMessage(prefix + "§8┣ §a/perm user «player name» setChatPrefix «value» §8| §7Set the chat prefix of a player");
		sender.sendMessage(prefix + "§8┣ §a/perm user «player name» setSuffix «value» §8| §7Set the suffix of a player");
		sender.sendMessage(prefix + "§8┣ §a/perm user «player name» setChatSuffix «value» §8| §7Set the chat suffix of a player");
		sender.sendMessage(prefix + "§8┗ §a/rank «player name» «group name» («value» «seconds | minutes | hours | days | months | years») §8| §7Set the rank of a player");
	}
	
	public String getIpAddress() {
		String ip = null;
		
		try {
			ip = new BufferedReader(new InputStreamReader(new URL("http://bot.whatismyipaddress.com").openStream())).readLine();
		} catch (Exception ex) {
		}
		
		if(ip == null)
			ip = "127.0.0.1";
		
		return ip;
	}

	public String formatTime(long rawTime) {
		String time = "";
		String[] expiryTime = getFormattedTime(rawTime);
		
		if(Integer.valueOf(expiryTime[0]) >= 1)
			time += expiryTime[0] + " year" + ((expiryTime[0].equals("1")) ? "" : "s") + " ";
		
		if(Integer.valueOf(expiryTime[1]) >= 1)
			time += expiryTime[1] + " month" + ((expiryTime[1].equals("1")) ? "" : "s") + " ";
		
		if(Integer.valueOf(expiryTime[2]) >= 1)
			time += expiryTime[2] + " day" + ((expiryTime[2].equals("1")) ? "" : "s") + " ";
		
		if(Integer.valueOf(expiryTime[3]) >= 1)
			time += expiryTime[3] + " hour" + ((expiryTime[3].equals("1")) ? "" : "s") + " ";
		
		if(Integer.valueOf(expiryTime[4]) >= 1)
			time += expiryTime[4] + " minute" + ((expiryTime[4].equals("1")) ? "" : "s") + " ";
		
		if(Integer.valueOf(expiryTime[5]) >= 1)
			time += expiryTime[5] + " second" + ((expiryTime[5].equals("1")) ? "" : "s");
		
		return time.trim();
	}

	public String[] getFormattedTime(long rawTime) {
		String[] time = new String[6];
		long timeToFormat = rawTime;
		
		int secondMax = 1;
		int minuteMax = 60;
		int hourMax   = 60 * 60;
		int dayMax    = 60 * 60 * 24;
		int monthMax  = 60 * 60 * 24 * 30;
		int yearMax   = 60 * 60 * 24 * 30 * 12;
		
		for (int i = 0; i < time.length; i++)
			time[i] = "0";
		
		long years = timeToFormat / yearMax;
		
		if(years >= 1) {
			time[0] = "" + years;
			
			timeToFormat -= years * yearMax;
		}
		
		long months = timeToFormat / monthMax;
		
		if(months >= 1) {
			time[1] = "" + months;
			
			timeToFormat -= months * monthMax;
		}
		
		long days = timeToFormat / dayMax;
		
		if(days >= 1) {
			time[2] = "" + days;
			
			timeToFormat -= days * dayMax;
		}
		
		long hours = timeToFormat / hourMax;
		
		if(hours >= 1) {
			time[3] = "" + hours;
			
			timeToFormat -= hours * hourMax;
		}
		
		long minutes = timeToFormat / minuteMax;
		
		if(minutes >= 1) {
			time[4] = "" + minutes;
			
			timeToFormat -= minutes * minuteMax;
		}
		
		long seconds = timeToFormat;
		
		if(seconds >= 1) {
			time[5] = "" + seconds;
			
			timeToFormat -= seconds * secondMax;
		}
		
		return time;
	}
	
	public void sendDebugMessage(String msg) {
		for (Player all : debugging)
			all.sendMessage(prefix + "§aDebug §8| " + msg);
	}
	
	public String getPrefix() {
		return prefix;
	}
	
	public String getNoPerm() {
		return noPerm;
	}
	
	public HashMap<UUID, PermissionAttachment> getAttachments() {
		return attachments;
	}
	
	public ArrayList<Player> getDebugging() {
		return debugging;
	}
	
	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}
	
	public void setNoPerm(String noPerm) {
		this.noPerm = noPerm;
	}

}

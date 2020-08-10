package net.dev.permissions.webserver;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;

import net.dev.permissions.PermissionSystem;
import net.dev.permissions.utils.permissionmanagement.PermissionGroup;
import net.dev.permissions.utils.permissionmanagement.PermissionUser;

import lib.com.sun.net.httpserver.HttpExchange;
import lib.com.sun.net.httpserver.HttpHandler;
import lib.com.sun.net.httpserver.HttpServer;

public class WebFileManager implements HttpHandler {

	private PermissionSystem permissionSystem;
	private File webServerDirectory;
	
	public WebFileManager() {
		permissionSystem = PermissionSystem.getInstance();
		
		HttpServer server = permissionSystem.getWebServerManager().getServer();
		
		webServerDirectory = new File("plugins/" + permissionSystem.getDescription().getName() + "/htdocs/");
		
		if(permissionSystem.getFileUtils().getConfig().getBoolean("WebServer.DeleteFiles")) {
			try {
				Runtime.getRuntime().exec(new String[] { "rd", "/s", "/q", webServerDirectory.getAbsolutePath() });
			} catch (IOException ex) {
			}
			
			try {
				Runtime.getRuntime().exec(new String[] { "rm", "-r", webServerDirectory.getAbsolutePath() });
			} catch (IOException ex) {
			}
		}
		
		Bukkit.getScheduler().runTaskLater(permissionSystem, () -> {
			if(!(webServerDirectory.exists())) {
				webServerDirectory.mkdir();
				
				copyFromJar("/htdocs/", Paths.get(webServerDirectory.toURI()));
			}
			
			for (String fileName : webServerDirectory.list())
				server.createContext("/" + fileName, this);
			
			server.createContext("/", this);
			
			System.out.println("Loaded webserver files!");
		}, 20);
	}
	
	public void copyFromJar(String source, final Path target) {
	    try {
	    	URI uri = getClass().getResource("/htdocs/index.php").toURI();
	    	
	    	try {
				FileSystem fileSystem = FileSystems.getFileSystem(uri);
				
				if(fileSystem.isOpen())
					fileSystem.close();
			} catch (Exception ex) {
			}
	    	
			Path jarPath = FileSystems.newFileSystem(uri, Collections.emptyMap()).getPath(source);

			Files.walkFileTree(jarPath, new SimpleFileVisitor<Path>() {

			    @Override
			    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
			        Files.createDirectories(target.resolve(jarPath.relativize(dir).toString()));
			        
			        return FileVisitResult.CONTINUE;
			    }

			    @Override
			    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
			        Files.copy(file, target.resolve(jarPath.relativize(file).toString()), StandardCopyOption.REPLACE_EXISTING);
			        
			        return FileVisitResult.CONTINUE;
			    }
			});
		} catch (Exception ex) {
			System.out.println("Could not copy files: " + ex.fillInStackTrace());
		}
	}
	
	public String readFile(String path) {
		try {
			String fileContent = "", line = "";
			BufferedReader bufferedReader = new BufferedReader(new FileReader(path));
			
			while ((line = bufferedReader.readLine()) != null)
				fileContent += line + System.lineSeparator();
			
			bufferedReader.close();
			
			return fileContent;
		} catch (IOException ex) {
		}
		
		return "<!DOCTYPE html>"
				+ "\n<html>"
				+ "\n<head>"
				+ "\n<title>File not found</title>"
				+ "\n</head>"
				+ "\n<body style='color: #A00; text-align: center; font-family: Impact; font-size: 40px; letter-spacing: 3px; background-color: #101011;'>"
				+ "\n<h1 style='position: fixed; top: 50%; left: 50%; transform: translate(-50%, -104%)'>404 NOT FOUND</h1>"
				+ "\n</body>"
				+ "\n</html>";
	}
	
	public HashMap<String, String> mapFromStringArray(String[] source, String splitter) {
		HashMap<String, String> map = new HashMap<>();
		
		for (String s : source) {
			String[] splitted = s.split(splitter);
			
			map.put(splitted[0], splitted[1]);
		}
		
		return map;
	}

	@Override
	public void handle(HttpExchange exchange) throws IOException {
		String authKey = permissionSystem.getWebServerManager().getAuthKey(), requestedUri = exchange.getRequestURI().toString(), path = exchange.getRequestURI().getPath().toString(), response = readFile(webServerDirectory.getPath() + "/" + (path.equals("/") ? "index.php" : (path.startsWith("/") ? path.substring(1) : path)));
		HashMap<String, String> parameters = new HashMap<>();
		List<PermissionGroup> permissionGroups = permissionSystem.getPermissionGroupManager().getPermissionGroups();
		List<String> userUuids = permissionSystem.getPermissionUserManager().getPermissionPlayerUUIDs();
		boolean reloadPage = false;
		
		if(requestedUri.contains("?"))
			parameters = mapFromStringArray(requestedUri.split("\\?")[1].split("\\&"), "\\=");
		
		if(response.contains("<title>WebPerms</title>") && !(parameters.containsKey("authKey") && parameters.get("authKey").equals(authKey)))
			response = "<!DOCTYPE html><html><head><title>Access not permitted</title></head><body style='color: #A00; text-align: center; font-family: Impact; font-size: 40px; letter-spacing: 3px; background-color: #101011;'><h1 style='position: fixed; top: 50%; left: 50%; transform: translate(-50%, -104%)'>403 NO PERMISSION</h1></body></html>";
		else {
			if(response.contains("%group_amount%"))
				response = response.replace("%group_amount%", String.valueOf(permissionGroups.size()));
			
			if(response.contains("%user_amount%"))
				response = response.replace("%user_amount%", String.valueOf(userUuids.size()));
			
			if(response.contains("%groups%")) {
				String groups = "";
				
				for (PermissionGroup permissionGroup : permissionGroups) {
					String name = permissionGroup.getName();
					
					groups += "<li " + ((parameters.containsKey("group") && parameters.get("group").equals(name)) ? "class=\"selected\"" : "onclick=\"window.location.href='?authKey=" + authKey + "&group=" + name + "';\"") + ">" + name + "</li>";
				}
				
				response = response.replace("%groups%", groups);
			}
			
			if(response.contains("%users%")) {
				String groups = "";
				
				for (String uuid : userUuids)
					groups += "<li " + ((parameters.containsKey("user") && parameters.get("user").equals(uuid)) ? "class=\"selected\"" : "onclick=\"window.location.href='?authKey=" + authKey + "&user=" + uuid + "';\"") + "><p>" + permissionSystem.getUUIDFetching().fetchName(UUID.fromString(uuid)) + "</p><img src=\"https://cravatar.eu/helmavatar/" + uuid + "/30.png\" alt=\"Player head\"></li>";
				
				response = response.replace("%users%", groups);
			}
			
			if(response.contains("%permissions%")) {
				String permissions = "<h1 style=\"position: relative; top: calc(50vh - 65px - 20px); text-align: center; letter-spacing: -0.8px; transform: translateY(-50%);\">⇦ Select a group or a player from the sidebar</h1>";
				
				if(parameters.containsKey("group") || parameters.containsKey("user")) {
					permissions = "<div id=\"headerRow\" class=\"unselectable\"><p id=\"firstHeaderPart\">Permission</p><p>Value</p></div>";
					
					if(parameters.containsKey("group")) {
						PermissionGroup permissionGroup = new PermissionGroup(parameters.get("group"));
						
						for (String permission : permissionGroup.getPermissions()) {
							boolean active = !(permission.startsWith("-"));
							
							permissions += "<div class=\"row\"><p class=\"firstRowPart\">" + (active ? permission : permission.substring(1)) + "</p><p class=\"secondRowPart" + (active ? " active" : "") + "\" onclick=\"window.location.href += '&action=updatePerm&value=" + permission + "';\">" + active + "</p><img class=\"thirdRowPart\" src=\"https://img.icons8.com/material-rounded/26/B3B3B3/x-coordinate.png\" alt=\"Remove permission\" onclick=\"removePermission(this)\"></div>";
						}
					} else if(parameters.containsKey("user")) {
						PermissionUser permissionUser = new PermissionUser(UUID.fromString(parameters.get("user")));
						
						for (String permission : permissionUser.getPermissions()) {
							boolean active = !(permission.startsWith("-"));
							
							permissions += "<div class=\"row\"><p class=\"firstRowPart\">" + (active ? permission : permission.substring(1)) + "</p><p class=\"secondRowPart" + (active ? " active" : "") + "\" onclick=\"window.location.href += '&action=updatePerm&value=" + permission + "';\">" + active + "</p><img class=\"thirdRowPart\" src=\"https://img.icons8.com/material-rounded/26/B3B3B3/x-coordinate.png\" alt=\"Remove permission\" onclick=\"removePermission(this)\"></div>";
						}
					}
				}
				
				response = response.replace("%permissions%", permissions);
			}
			
			if(response.contains("%auth_key%"))
				response = response.replace("%auth_key%", authKey);
			
			if(response.contains("%current_type%"))
				response = response.replace("%current_type%", parameters.containsKey("group") ? "group" : (parameters.containsKey("user") ? "user" : ""));
			
			if(response.contains("%current_name%"))
				response = response.replace("%current_name%", parameters.containsKey("group") ? parameters.get("group") : (parameters.containsKey("user") ? parameters.get("user") : ""));
			
			if(parameters.containsKey("action") && parameters.containsKey("value")) {
				String action = parameters.get("action"), permission = parameters.get("value");
				
				if(parameters.containsKey("group")) {
					PermissionGroup permissionGroup = new PermissionGroup(parameters.get("group"));
					permissionGroup.removePermission(permission);
					
					if(action.equals("updatePerm"))
						permissionGroup.addPermission(permission.startsWith("-") ? permission.substring(1) : ("-" + permission));
				} else if(parameters.containsKey("user")) {
					PermissionUser permissionUser = new PermissionUser(UUID.fromString(parameters.get("user")));
					permissionUser.removePermission(permission);
					
					if(action.equals("updatePerm"))
						permissionUser.addPermission(permission.startsWith("-") ? permission.substring(1) : ("-" + permission));
				}
				
				reloadPage = true;
			}
			
			if(parameters.containsKey("permission")) {
				String permission = parameters.get("permission");
				boolean valueEnabled = parameters.containsKey("value");
				
				if(parameters.containsKey("group"))
					new PermissionGroup(parameters.get("group")).addPermission((valueEnabled ? "" : "-") + permission);
				else if(parameters.containsKey("user"))
					new PermissionUser(UUID.fromString(parameters.get("user"))).addPermission((valueEnabled ? "" : "-") + permission);
				
				reloadPage = true;
			}
			
			if(response.contains("%javascript%"))
				response = response.replace("%javascript%", reloadPage ? "window.location.href='?authKey=" + authKey + (parameters.containsKey("group") ? ("&group=" + parameters.get("group")) : (parameters.containsKey("user") ? ("&user=" + parameters.get("user")) : "")) + "';" : "");
		}
		
		exchange.sendResponseHeaders(200, response.getBytes().length);
		
		OutputStream outputStream = exchange.getResponseBody();
		outputStream.write(response.getBytes());
		outputStream.close();
	}
	
}

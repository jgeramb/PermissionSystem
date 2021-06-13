package net.dev.permissions.webserver;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.UUID;

import net.dev.permissions.PermissionSystem;

import lib.com.sun.net.httpserver.HttpServer;

public class WebServerManager {

	private HttpServer server;
	private String authKey;
	
	public WebServerManager(int port) {
		try {
			server = HttpServer.create(new InetSocketAddress(port), 0);
			server.start();
			
			authKey = UUID.randomUUID().toString().replace("-", "");
			
			PermissionSystem.getInstance().setWebFileManager(new WebFileManager(server));
			
			System.out.println("Started webserver on port " + port + "!");
		} catch (IOException ex) {
			System.out.println("Could not start webserver on port " + port + ": " + ex.getMessage());
		}
	}
	
	public void stop() {
		if(server != null)
			server.stop(0);
	}
	
	public String getAuthKey() {
		return authKey;
	}
	
	public HttpServer getServer() {
		return server;
	}
	
}
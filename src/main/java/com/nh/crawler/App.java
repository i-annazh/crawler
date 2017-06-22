package com.nh.crawler;

import java.io.BufferedInputStream;

import org.mortbay.jetty.Server;
import org.mortbay.xml.XmlConfiguration;

/**
 * Hello world!
 *
 */
public class App 
{
	public static void startApiService(){
		Server server = new Server();
		XmlConfiguration configuration;
		
		try {
			configuration = new XmlConfiguration(new BufferedInputStream(App.class.getResourceAsStream("/jetty.xml")));
			configuration.configure(server);
			server.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	
    public static void main( String[] args )
    {
        System.out.println( "Hello World!" );
        
        startApiService();
    }
}

package com.nijikokun.bukkit.Tipper;

import com.nijikokun.bukkit.Tipper.Database.Type;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Logger;
import org.bukkit.Server;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginLoader;
import org.bukkit.plugin.java.JavaPlugin;

/*
 * Tipper v1.0
 * Copyright (C) 2011  Nijikokun <nijikokun@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
public class Tipper extends JavaPlugin {
    /*
     * Loggery Foggery
     */
    public static final Logger log = Logger.getLogger("Minecraft");
    
    /*
     * Central Data pertaining directly to the plugin name & versioning.
     */
    public static String name = "Tipper";
    public static String codename = "Coins";
    public static String version = "1.0";

    /**
     * Listener for the plugin system.
     */
    public iListen l = new iListen(this);

    /**
     * Controller for permissions and security.
     */
    public static iWatch Watch = new iWatch();

    /**
     * Things the controller needs to watch permissions for
     */
    private final String[] watching = { "manage-tips" };

    /**
     * Default settings for the permissions
     */
    private final String[] defaults = { "admins name," };

    /**
     * Miscellaneous object for various functions that don't belong anywhere else
     */
    public static Misc Misc = new Misc();

    /**
     * Internal Properties controllers
     */
    public static iProperty Settings, Permissions;

    /*
     * Variables
     */
    public static final String directory = "Tipper" + File.separator;
    public static int tip_method = 0, tip_interval = 300; // 0 = random, 1 = ascending, 2 = descending, 300 = 5 minutes
    // public static boolean show_time_warnings = false; // Time is now falling over ______ !
    public static String server_name = "Alfenia", tip_tag = "&3[&fTip&3]&f ";
    
    /*
     * Database connections
     */
    private String database_type = "flatfile";
    public static String flatfile = directory + "tips.flat";
    public static String sqlite = "jdbc:sqlite:" + directory + "tips.db";
    public static String mysql = "jdbc:mysql://localhost:3306/minecraft";
    public static String mysql_user = "root";
    public static String mysql_pass = "pass";
    public static Timer timer = null;
    
    /*
     * Database connection
     */
    public static Database db = null;

    public Tipper(PluginLoader pluginLoader, Server instance, PluginDescriptionFile desc, File folder, File plugin, ClassLoader cLoader) {
        super(pluginLoader, instance, desc, folder, plugin, cLoader);

        registerEvents();
	log.info(Messaging.bracketize(name) + " version " + Messaging.bracketize(version) + " ("+codename+") loaded");
    }

    public void onDisable() {
	log.info(Messaging.bracketize(name) + " version " + Messaging.bracketize(version) + " ("+codename+") disabled");
    }

    public void onEnable() {
	setup();
	setupPermissions();
	setupTimer();
    }

    private void registerEvents() {
        getServer().getPluginManager().registerEvent(Event.Type.PLAYER_COMMAND, l, Priority.Normal, this);
    }

    public void setup() {
	(new File(directory)).mkdir();

	// Properties
	Settings = new iProperty(directory + "tipper.settings");
	Permissions = new iProperty(directory + "tipper.permissions");
	
	// Tip settings
	tip_tag = Settings.getString("tip-tag", tip_tag);
	tip_method = Settings.getInt("tip-method", tip_method);
	tip_interval = Settings.getInt("tip-interval", tip_interval); // seconds
	
	// Time Warning Settings
	// server_name = Settings.getString("server-name", server_name);
	// show_time_warnings = Settings.getBoolean("time-warnings", show_time_warnings);

	// Database
	database_type = Settings.getString("database-type","flatfile");

	// Connect & Create
	if(database_type.equalsIgnoreCase("mysql")) {
	    mysql = Settings.getString("mysql-db", mysql);
	    mysql_user = Settings.getString("mysql-user", mysql_user);
	    mysql_pass = Settings.getString("mysql-pass", mysql_pass);

	    db = new Database(Database.Type.MYSQL);
	} else if(database_type.equalsIgnoreCase("sqlite")) {
	    db = new Database(Database.Type.SQLITE);
	} else {
	    db = new Database(Database.Type.FLATFILE);
	}
    }

    public void setupPermissions() {
	for(int x = 0; x < watching.length; x++) {
	    Watch.add(watching[x], Permissions.getString("can-" + watching[x], defaults[x]));
	}
    }

    public void setupTimer() {
	timer = new Timer();
	timer.scheduleAtFixedRate(new TimerTask() {
		public void run() {
		    String tip = null;

		    if(Tipper.tip_method == 1) {
			tip = Tipper.db.next();
		    } else if(Tipper.tip_method == 2) {
			tip = Tipper.db.previous();
		    } else {
			tip = Tipper.db.random();
		    }

		    if(tip != null) {
			Messaging.broadcast(Tipper.tip_tag + tip);
		    }
		}
	    },
	    0L,
	    (Tipper.tip_interval*1000L)
	);
    }
}

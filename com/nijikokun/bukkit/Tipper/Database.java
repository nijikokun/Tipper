/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.nijikokun.bukkit.Tipper;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 *
 * @author Nijiko
 */
public class Database {

    public enum Type {
	SQLITE,
	MYSQL,
	FLATFILE;
    };

    public Type database = null;

    /*
     * Tip array for less database usage
     */
    public ArrayList<String> Tips = new ArrayList<String>();
    public int i = 0;

    public Database(Type database) {
	this.database = database;
	this.initialize();
    }

    private void initialize() {
	if(this.database.equals(database.FLATFILE)) {
	    try {
		(new File(Tipper.flatfile)).createNewFile();
	    } catch (IOException ex) {
		Tipper.log.info("Could not create flatfile database.");
	    }

	    try {
		BufferedReader in = new BufferedReader(new FileReader(Tipper.flatfile));
		String str;

		while ((str = in.readLine()) != null) {
		    this.Tips.add(str);

		    if(Tipper.Settings.getBoolean("show-tip-loading")) {
			Tipper.log.info("Loading Tip: " + str);
		    }
		}

		in.close();
	    } catch (IOException e) {
		Tipper.log.info("Could not load tip from database.");
	    }
	} else {
	    if(!checkTable()) {
		Tipper.log.info("[Tipper] Creating database.");
		createTable();
	    }

	    visualize();
	}
    }

    private boolean checkTable() {
	Connection conn = null;
	ResultSet rs = null;

	try {
	    if (this.database.equals(database.SQLITE)) {
		Class.forName("org.sqlite.JDBC");
		conn = DriverManager.getConnection(Tipper.sqlite);
	    } else {
		Class.forName("com.mysql.jdbc.Driver");
		conn = DriverManager.getConnection(Tipper.mysql, Tipper.mysql_user, Tipper.mysql_pass);
	    }

	    DatabaseMetaData dbm = conn.getMetaData();

	    rs = dbm.getTables(null, null, "tipper", null);
	    return rs.next();
	} catch (SQLException ex) {
	    Tipper.log.severe("[Tipper]: Table check for " + (this.database.equals(database.SQLITE) ? "sqlite" : "mysql") + " Failed: " + ex); return false;
	} catch (ClassNotFoundException e) {
	    Tipper.log.severe("[Tipper]: Database connector not found for " + (this.database.equals(database.SQLITE) ? "sqlite" : "mysql") + ": " + e); return false;
	} finally {
	    try {
		if (rs != null) {
		    rs.close();
		}
		if (conn != null) {
		    conn.close();
		}
	    } catch (SQLException ex) {
		Tipper.log.severe("[Tipper]: Failed to close connection");
	    }
	}
    }

    private void createTable() {
	Connection conn = null;
	Statement st = null;

	try {
	    if (this.database.equals(database.SQLITE)) {
		Class.forName("org.sqlite.JDBC");
		conn = DriverManager.getConnection(Tipper.sqlite);
	    } else {
		Class.forName("com.mysql.jdbc.Driver");
		conn = DriverManager.getConnection(Tipper.mysql, Tipper.mysql_user, Tipper.mysql_pass);
	    }

	  st = conn.createStatement();

	  if (this.database.equals(database.SQLITE)) {
	    st.executeUpdate("CREATE TABLE `tipper` (`id` INTEGER PRIMARY KEY, `tip` TEXT);CREATE INDEX tipIndex on tipper (tip);");
	  } else {
	    st.executeUpdate("CREATE TABLE `tipper` (`id` INT(255) NOT NULL, `tip` TEXT NOT NULL, PRIMARY KEY (`id`)) ENGINE = InnoDB;");
	  }
	} catch (SQLException ex) {
	    Tipper.log.severe("[Tipper]: Could not create table for " + (this.database.equals(database.SQLITE) ? "sqlite" : "mysql") + ": " + ex); return;
	} catch (ClassNotFoundException e) {
	    Tipper.log.severe("[Tipper]: Database connector not found for " + (this.database.equals(database.SQLITE) ? "sqlite" : "mysql") + ": " + e); return;
	} finally {
	    try {
		if (st != null) {
		    st.close();
		}
		if (conn != null) {
		    conn.close();
		}
	    } catch (SQLException ex) {
		Tipper.log.severe("[Tipper]: Failed to close connection");
	    }
	}
    }

    public void add(String tip) {
	this.Tips.add(tip);

	if (this.database.equals(database.FLATFILE)) {
	    this.save();
	} else {
	    Connection conn = null;
	    PreparedStatement ps = null;
	    ResultSet rs = null;

	    try {
		if (this.database.equals(database.SQLITE)) {
		    Class.forName("org.sqlite.JDBC");
		    conn = DriverManager.getConnection(Tipper.sqlite);
		} else {
		    Class.forName("com.mysql.jdbc.Driver");
		    conn = DriverManager.getConnection(Tipper.mysql, Tipper.mysql_user, Tipper.mysql_pass);
		}

		ps = conn.prepareStatement("INSERT INTO tipper (tip) VALUES (?)");
		ps.setString(1, tip);
		ps.executeUpdate();
	    } catch (SQLException ex) {
		Tipper.log.severe("[Tipper]: Could not add tip for " + (this.database.equals(database.SQLITE) ? "sqlite" : "mysql") + ": " + ex); return;
	    } catch (ClassNotFoundException e) {
		Tipper.log.severe("[Tipper]: Database connector not found for " + (this.database.equals(database.SQLITE) ? "sqlite" : "mysql") + ": " + e); return;
	    } finally {
		try {
		    if (rs != null) {
			rs.close();
		    }
		    if (ps != null) {
			ps.close();
		    }
		    if (conn != null) {
			conn.close();
		    }
		} catch (SQLException ex) {
		    Tipper.log.severe("[Tipper]: Failed to close connection");
		}
	    }
	}
    }

    public void remove(String tip) {
	this.Tips.remove(tip);

	if (this.database.equals(database.FLATFILE)) {
	    this.save();
	} else {
	    Connection conn = null;
	    PreparedStatement ps = null;
	    ResultSet rs = null;

	    try {
		if (this.database.equals(database.SQLITE)) {
		    Class.forName("org.sqlite.JDBC");
		    conn = DriverManager.getConnection(Tipper.sqlite);
		} else {
		    Class.forName("com.mysql.jdbc.Driver");
		    conn = DriverManager.getConnection(Tipper.mysql, Tipper.mysql_user, Tipper.mysql_pass);
		}

		ps = conn.prepareStatement("DELETE FROM tipper WHERE tip = ?" + (this.database.equals(database.SQLITE) ? "" : " LIMIT 1"));
		ps.setString(1, tip);
		ps.executeUpdate();
	    } catch (SQLException ex) {
		Tipper.log.severe("[Tipper]: Could not remove tip for " + (this.database.equals(database.SQLITE) ? "sqlite" : "mysql") + ": " + ex); return;
	    } catch (ClassNotFoundException e) {
		Tipper.log.severe("[Tipper]: Database connector not found for " + (this.database.equals(database.SQLITE) ? "sqlite" : "mysql") + ": " + e); return;
	    } finally {
		try {
		    if (rs != null) {
			rs.close();
		    }
		    if (ps != null) {
			ps.close();
		    }
		    if (conn != null) {
			conn.close();
		    }
		} catch (SQLException ex) {
		    Tipper.log.severe("[Tipper]: Failed to close connection");
		}
	    }
	}
    }

    public String random() {
	int i = (int)(this.Tips.size()*Math.random());
	return this.Tips.get(i);
    }

    public String next() {
	if((this.i+1) == this.Tips.size()) {
	    this.i = 0;
	    return this.Tips.get(0);
	}

	++this.i;
	return this.Tips.get(this.i);
    }

    public String previous() {
	if((this.i-1) == -1) {
	    this.i = this.Tips.size()-1;
	    return this.Tips.get(this.Tips.size()-1);
	}

	--this.i;
	return this.Tips.get(i);
    }

    /**
     * Flatfile only~
     */
    private void save() {
	File file = new File(Tipper.flatfile);

	try {
	    PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(file)), true);

	    for (int i = 0; i < Tips.size(); i++) {
		out.println((String) Tips.get(i));
	    }

	    out.close();
	} catch (IOException e) {
	    Tipper.log.info("Could not write to tips.flat!");
	}
    }

    public void visualize() {
	this.Tips.clear();

	if (this.database.equals(database.FLATFILE)) {
	    try {
		BufferedReader in = new BufferedReader(new FileReader(Tipper.flatfile));
		String str;

		while ((str = in.readLine()) != null) {
		    this.Tips.add(str);

		    if(Tipper.Settings.getBoolean("show-tip-loading")) {
			Tipper.log.info("Loading Tip: " + str);
		    }
		}

		in.close();
	    } catch (IOException e) {
		Tipper.log.info("Could not load tip from database.");
	    }
	} else {
	    Connection conn = null;
	    PreparedStatement ps = null;
	    ResultSet rs = null;

	    try {
		if (this.database.equals(database.SQLITE)) {
		    Class.forName("org.sqlite.JDBC");
		    conn = DriverManager.getConnection(Tipper.sqlite);
		} else {
		    Class.forName("com.mysql.jdbc.Driver");
		    conn = DriverManager.getConnection(Tipper.mysql, Tipper.mysql_user, Tipper.mysql_pass);
		}

		ps = conn.prepareStatement("SELECT * FROM tipper");
		rs = ps.executeQuery();

		while(rs.next()) {
		    this.Tips.add(rs.getString("tip"));
		}
	    } catch (SQLException ex) {
		Tipper.log.severe("[Tipper]: Could not create table for " + (this.database.equals(database.SQLITE) ? "sqlite" : "mysql") + ": " + ex); return;
	    } catch (ClassNotFoundException e) {
		Tipper.log.severe("[Tipper]: Database connector not found for " + (this.database.equals(database.SQLITE) ? "sqlite" : "mysql") + ": " + e); return;
	    } finally {
		try {
		    if (rs != null) {
			rs.close();
		    }
		    if (ps != null) {
			ps.close();
		    }
		    if (conn != null) {
			conn.close();
		    }
		} catch (SQLException ex) {
		    Tipper.log.severe("[Tipper]: Failed to close connection");
		}
	    }
	}
    }
}

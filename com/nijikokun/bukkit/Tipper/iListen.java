package com.nijikokun.bukkit.Tipper;


import java.util.logging.Logger;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerListener;
import net.minecraft.server.WorldServer;

/**
 * iListen.java
 * <br /><br />
 * Listens for calls from bukkit, and reacts accordingly.
 *
 * @author Nijikokun <nijikokun@gmail.com>
 */
public class iListen extends PlayerListener {

    private static final Logger log = Logger.getLogger("Minecraft");

    /**
     * Miscellaneous object for various functions that don't belong anywhere else
     */
    public Misc Misc = new Misc();

    public static Tipper plugin;
    public WorldServer server;

    public iListen(Tipper instance) {
        plugin = instance;
    }

    /**
     * Sends simple condensed help lines to the current player
     */
    private void showSimpleHelp() {
	Messaging.send("&e-----------------------------------------------------");
	Messaging.send("&f Tipper (&c"+Tipper.codename+"&f)                    ");
	Messaging.send("&e-----------------------------------------------------");
	Messaging.send("&f [] Required, () Optional                            ");
	Messaging.send("&e-----------------------------------------------------");
	Messaging.send("&f /tip add [tip] - Add your tip to the list of tips   ");
	Messaging.send("&f /tip remove [tip] - Remove tip from announced tips  ");
	Messaging.send("&f /tip reload - Reload tips                           ");
	Messaging.send("&f /tip list - List tips                               ");
	Messaging.send("&e-----------------------------------------------------");
    }

    public static long getRelativeTime() {
        long time = (plugin.getServer().getTime() % 24000);
        // Java modulus is stupid.
        if (time < 0) {
            time += 24000;
        }

        return time;
    }

    public static int closerToTime() {
	long time = getRelativeTime();

	if(time > 0 && time < 13000) {
	    return 0; // night
	} else {
	    return 1; // day
	}
    }

    public static long timeTill() {
	long time = getRelativeTime();

	if(closerToTime() == 0) {
	    return 13000L-time;
	} else {
	    if(time < 24000) {
		return 24000L - time;
	    }

	    return 0; // day
	}
    }

    @Override
    public void onPlayerCommand(PlayerChatEvent event) {
        String[] split = event.getMessage().split(" ");
        Player player = event.getPlayer();
	Messaging.save(player);
	String base = split[0];

	if(Misc.isEither(base, "/tipper", "/tip")) {
	    if(split.length < 2) {
		showSimpleHelp(); event.setCancelled(true);
		return;
	    }

	    String command = split[1];

	    if (Misc.isEither(command, "add", "-a") && split.length >= 2) {
		if (!Tipper.Watch.permission("manage-tips", player)) {
		    return;
		}

		String tip = Misc.combineSplit(2, split, " ");

		Tipper.db.add(tip);
		Messaging.send(Tipper.tip_tag + "Tip was added to database."); event.setCancelled(true);
		return;
	    }

	    if (Misc.isEither(command, "remove", "-r") && split.length >= 2) {
		if (!Tipper.Watch.permission("manage-tips", player)) {
		    return;
		}

		String tip = Misc.combineSplit(2, split, " ");

		Tipper.db.remove(tip);
		Messaging.send(Tipper.tip_tag + "Tip was removed from database."); event.setCancelled(true);
		return;
	    }

	    if (Misc.isEither(command, "list", "-l")) {
		if (!Tipper.Watch.permission("manage-tips", player)) {
		    return;
		}

		for(String tip : Tipper.db.Tips) {
		    Messaging.send(Tipper.tip_tag + tip);
		}
		
		event.setCancelled(true); return;
	    }

	    if (Misc.isEither(command, "reload", "-re")) {
		if (!Tipper.Watch.permission("manage-tips", player)) {
		    return;
		}

		Tipper.timer.cancel();
		Tipper.db.visualize();
		plugin.setupTimer();
		Messaging.send(Tipper.tip_tag + "Tips have been reloaded."); event.setCancelled(true);
		return;
	    }
	}
    }
}

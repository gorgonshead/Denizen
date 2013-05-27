package net.aufdemrand.denizen.tags.core;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

import net.aufdemrand.denizen.Denizen;
import net.aufdemrand.denizen.events.ReplaceableTagEvent;
import net.aufdemrand.denizen.utilities.Utilities;
import net.aufdemrand.denizen.objects.aH;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class UtilTags implements Listener {

    public UtilTags(Denizen denizen) {
        denizen.getServer().getPluginManager().registerEvents(this, denizen);
    }

    @EventHandler
    public void utilTags(ReplaceableTagEvent event) {
        if (!event.matches("UTIL")) return;

        String type = event.getType() != null ? event.getType() : "";
        String subType = event.getSubType() != null ? event.getSubType() : "";
        String subTypeContext = event.getSubTypeContext() != null ? event.getSubTypeContext().toUpperCase() : "";
        String specifier = event.getSpecifier() != null ? event.getSpecifier() : "";
        String specifierContext = event.getSpecifierContext() != null ? event.getSpecifierContext().toUpperCase() : "";
        
        if (type.equalsIgnoreCase("RANDOM")) {
            if (subType.equalsIgnoreCase("INT")) {
                if (specifier.equalsIgnoreCase("TO")) {
                    if (aH.matchesInteger(subTypeContext) && aH.matchesInteger(specifierContext)) {
                        int min = aH.getIntegerFrom(subTypeContext);
                        int max = aH.getIntegerFrom(specifierContext);

                        // in case the first number is larger than the second, reverse them
                        if (min > max) {
                            int store = min;
                            min = max;
                            max = store;
                        }

                        event.setReplaced(String.valueOf(Utilities.getRandom().nextInt(max - min + 1) + min));
                    }
                }
            }

            else if (subType.equalsIgnoreCase("UUID"))
                event.setReplaced(UUID.randomUUID().toString());
        }

        else if (type.equalsIgnoreCase("TRIM")) {
            String item_to_trim = event.getTypeContext();
            int from = 1;
            try {
                if (subType.equalsIgnoreCase("FROM"))
                    from = Integer.valueOf(subTypeContext);
            } catch (NumberFormatException e) { }
            int to = item_to_trim.length();
            try {
                if (specifier.equalsIgnoreCase("TO"))
                    to = Integer.valueOf(specifierContext);
            } catch (NumberFormatException e) { }

            if (to > item_to_trim.length())
                to = item_to_trim.length()+1;

            event.setReplaced(item_to_trim.substring(from - 1, to - 1));
        }

        else if (type.equalsIgnoreCase("REPLACE")) {
            String item_to_replace = event.getTypeContext();
            String replace = event.getSubTypeContext();
            String replacement = event.getSpecifierContext();
            event.setReplaced(item_to_replace.replace(replace, replacement));
        }

        else if (type.equalsIgnoreCase("UPPERCASE")) {
            String item_to_uppercase = event.getTypeContext();
            event.setReplaced(item_to_uppercase.toUpperCase());
        }

        else if (type.equalsIgnoreCase("LOWERCASE")) {
            String item_to_uppercase = event.getTypeContext();
            event.setReplaced(item_to_uppercase.toLowerCase());
        }
        
        else if (type.equalsIgnoreCase("DATE")) {
        	Date currentDate = new Date();
        	SimpleDateFormat format = new SimpleDateFormat();
        	
        	if (subType.equalsIgnoreCase("TIME")) {
        		if (specifier.equalsIgnoreCase("24HOUR")) {
        			format.applyPattern("k:mm");
        		} else format.applyPattern("K:mm a");
        		
        	} else format.applyPattern("EEE, MMM d, yyyy");
        	
        	event.setReplaced(format.format(currentDate));

        }

    }

}
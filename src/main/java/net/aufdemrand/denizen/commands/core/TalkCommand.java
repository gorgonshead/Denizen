package net.aufdemrand.denizen.commands.core;

import java.util.logging.Level;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import net.aufdemrand.denizen.bookmarks.BookmarkHelper.BookmarkType;
import net.aufdemrand.denizen.commands.AbstractCommand;
import net.aufdemrand.denizen.npc.DenizenNPC;
import net.aufdemrand.denizen.npc.SpeechEngine.TalkType;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.command.exception.CommandException;

/**
 * Handles Denizens talking via script entries.
 * 
 * @author Jeremy Schroeder
 */

public class TalkCommand extends AbstractCommand {

	/* CHAT|SHOUT|WHISPER|EMOTE|ANNOUNCE|NARRATE "[TEXT]" */

	/* 
	 * Arguments: [] - Required, () - Optional 
	 * [TYPICAL] argument with a description if necessary.
	 * (ARGUMENTS) should be clear and concise.
	 *   
	 * Modifiers:
	 * (NPCID:#) Changes the Denizen affected to the Citizens2 NPCID specified
	 * (NOPLAYER) Makes the chat by the Denizen not directed to the Player.
	 *   Note: Only applies to Chat, Shout and Whisper.
	 * 
	 * Example Usage:
	 * CHAT "That's a nice Helmet you have!"
	 * SHOUT "HELLO WORLD!"
	 * WHISPER 'NPCID:3' "Shhh!"
	 * 
	 */

	@Override
	public boolean execute(ScriptEntry theEntry) throws CommandException {

		/* Initialize variables */ 

		DenizenNPC theDenizen = null;
		String theMessage = null;
		Boolean noPlayer = false;
		Player thePlayer = null;

		/* Match arguments to expected variables */

		if (theEntry.arguments() == null)
			throw new CommandException("No talk text!");


		if (theEntry.arguments().length > 3) {
			aH.echoError("Woah! Lots of arguments detected in " + theEntry.getCommand() + ".  Perhaps you are failing to enclose your Talk Text in quotes?  Example usage: CHAT 'Hello world!'");
		} else {

			for (String thisArg : theEntry.arguments()) {

				if (thisArg.toUpperCase().equalsIgnoreCase("NOPLAYER")) {
					aH.echoError("...will not target Player.");
					noPlayer = true;
				}

				else if (aH.matchesNPCID(thisArg)) {
					theDenizen = aH.getNPCIDModifier(thisArg);
					if (theDenizen != null)
						aH.echoDebug("...specified '%s'.", thisArg);
				} 

				else {
					aH.echoDebug("...text: '%s'", thisArg);
					theMessage = thisArg;
				}
			}

		}	

		// Set to Denizen carried over by the ScriptEntry, if possible.
		if (theDenizen == null && theEntry.getDenizen() != null) theDenizen = theEntry.getDenizen();
		if (!noPlayer) thePlayer = theEntry.getPlayer();
		
		// Catch TASK script trying to use CHAT/etc. without setting NPCID.
		// Exception is using NARRATE, which does NOT need an NPC.
		if (theDenizen == null && !theEntry.getCommand().equals("NARRATE")) {
				aH.echoError("Seems this was sent from a TASK-type script. Must specify NPCID:#!");
				return false;
		}

		/* Execute the command, if all required variables are filled. */
		if (theMessage != null && theDenizen != null) {
			theDenizen.talk(TalkType.valueOf(theEntry.getCommand()), thePlayer, theMessage);
			return true;
		}
		
		else if (theMessage != null && theEntry.getCommand().equals("NARRATE"))
			plugin.getSpeechEngine().talk(null, thePlayer, theMessage, TalkType.NARRATE);
		
		return false;
	}

}
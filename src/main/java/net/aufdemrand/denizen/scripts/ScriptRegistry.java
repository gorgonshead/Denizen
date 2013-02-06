package net.aufdemrand.denizen.scripts;

import net.aufdemrand.denizen.scripts.containers.ScriptContainer;
import net.aufdemrand.denizen.scripts.containers.core.*;
import net.aufdemrand.denizen.utilities.debugging.dB;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ScriptRegistry {

    // Currently loaded 'script-containers'
    private static Map<String, ScriptContainer> scriptContainers = new HashMap<String, ScriptContainer>();
    private static Map<String, Class<? extends ScriptContainer>> scriptContainerTypes = new HashMap<String, Class<? extends ScriptContainer>>();

    public static void _registerType(String typeName, Class<? extends ScriptContainer> scriptContainerClass) {
        scriptContainerTypes.put(typeName.toUpperCase(), scriptContainerClass);
    }

    public static void _registerCoreTypes() {
        _registerType("interact", InteractScriptContainer.class);
        _registerType("book", BookScriptContainer.class);
        _registerType("item", ItemScriptContainer.class);
        _registerType("entity", EntityScriptContainer.class);
        _registerType("task", TaskScriptContainer.class);
        _registerType("activity", ActivityScriptContainer.class);
        _registerType("assignment", AssignmentScriptContainer.class);
    }

    public static boolean containsScript(String id) {
        return scriptContainers.containsKey(id.toUpperCase());
    }

    public static void _buildCoreYamlScriptContainers(FileConfiguration yamlScripts) {
        // Get a set of key names in concantenated Denizen Scripts
        Set<String> scripts = yamlScripts.getKeys(false);
        // Iterate through set
        for (String scriptName : scripts)
        // Make sure the script has a type
            if (yamlScripts.contains(scriptName + ".TYPE")) {
                String type = yamlScripts.getString(scriptName + ".TYPE");
                // Check that types is a registered type
                if (!scriptContainerTypes.containsKey(type.toUpperCase())) {
                    dB.log("<G>Trying to load an invalid script. '<T>" + scriptName + "<Y>(" + type + ")'<G> is an unknown type.");
                    continue;
                }
                // Instantize a new scriptContainer of specified type.
                Class typeClass = scriptContainerTypes.get(type.toUpperCase());
                try {
                    ScriptContainer scriptContainer = (ScriptContainer) typeClass.getConstructor(typeClass).newInstance(scriptName);
                    scriptContainers.put(scriptName,scriptContainer);
                } catch (Exception e) { }
            }
    }

    public static void _buildYamlScriptContainer(ConfigurationSection configurationSection) {
        // TODO: Allow others to register dScript with Denizen without adding to Denizen's script folder.
    }

    public static <T extends ScriptContainer> T getScriptContainerAs(String name, Class<T> type) {
        try {
        if (scriptContainers.containsKey(name.toUpperCase()))
            return (T) type.cast(scriptContainers.get(name.toUpperCase()));
        else return null;
        } catch (Exception e) { }
        return null;
    }

    public static ScriptContainer getScriptContainer(String name) {
        if (scriptContainers.containsKey(name.toUpperCase()))
            return scriptContainers.get(name.toUpperCase());

        else return null;
    }

}

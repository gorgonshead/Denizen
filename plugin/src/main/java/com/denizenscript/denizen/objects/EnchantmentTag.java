package com.denizenscript.denizen.objects;

import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.scripts.containers.core.EnchantmentScriptContainer;
import com.denizenscript.denizen.utilities.Utilities;
import com.denizenscript.denizen.utilities.debugging.Debug;
import com.denizenscript.denizencore.DenizenCore;
import com.denizenscript.denizencore.flags.AbstractFlagTracker;
import com.denizenscript.denizencore.flags.FlaggableObject;
import com.denizenscript.denizencore.flags.RedirectionFlagTracker;
import com.denizenscript.denizencore.objects.Fetchable;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.MapTag;
import com.denizenscript.denizencore.scripts.ScriptRegistry;
import com.denizenscript.denizencore.tags.Attribute;
import com.denizenscript.denizencore.tags.ObjectTagProcessor;
import com.denizenscript.denizencore.tags.TagContext;
import com.denizenscript.denizencore.tags.TagRunnable;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.entity.EntityDamageEvent;

public class EnchantmentTag implements ObjectTag, FlaggableObject {

    // <--[ObjectType]
    // @name EnchantmentTag
    // @prefix enchantment
    // @base ElementTag
    // @implements FlaggableObject
    // @format
    // The identity format for enchantments is the vanilla ID, Denizen ID, or full key. Can also be constructed by Denizen script name.
    // For example, 'enchantment@sharpness', 'enchantment@my_custom_ench', or 'enchantment@otherplugin:customench'.
    //
    // @description
    // An EnchantmentTag represents an item enchantment abstractly (the enchantment itself, like 'sharpness', which *can be* applied to an item, as opposed to the specific reference to an enchantment on a specific item).
    //
    // This object type is flaggable.
    // Flags on this object type will be stored in the server saves file, under special sub-key "__enchantments"
    //
    // -->

    //////////////////
    //    Object Fetcher
    ////////////////

    @Deprecated
    public static EnchantmentTag valueOf(String string) {
        return valueOf(string, null);
    }

    @Fetchable("enchantment")
    public static EnchantmentTag valueOf(String string, TagContext context) {
        if (string == null) {
            return null;
        }
        string = CoreUtilities.toLowerCase(string);
        if (string.startsWith("enchantment@")) {
            string = string.substring("enchantment@".length());
        }
        Enchantment ench;
        NamespacedKey key = Utilities.parseNamespacedKey(string);
        ench = Enchantment.getByKey(key);
        if (ench == null) {
            ench = Enchantment.getByName(string.toUpperCase());
        }
        if (ench == null) {
            ench = Enchantment.getByKey(new NamespacedKey("denizen", Utilities.cleanseNamespaceID(string)));
        }
        if (ench == null && ScriptRegistry.containsScript(string, EnchantmentScriptContainer.class)) {
            ench = ScriptRegistry.getScriptContainerAs(string, EnchantmentScriptContainer.class).enchantment;
        }
        if (ench == null) {
            if (context == null || context.debug) {
                Debug.echoError("Unknown enchantment '" + string + "'");
            }
            return null;
        }
        return new EnchantmentTag(ench);

    }

    public static boolean matches(String arg) {
        if (CoreUtilities.toLowerCase(arg).startsWith("enchantment@")) {
            return true;
        }
        return valueOf(arg, CoreUtilities.noDebugContext) != null;
    }

    public EnchantmentTag(Enchantment enchantment) {
        this.enchantment = enchantment;
    }

    public Enchantment enchantment;

    private String prefix = "Enchantment";

    @Override
    public String getObjectType() {
        return "Enchantment";
    }

    @Override
    public String getPrefix() {
        return prefix;
    }

    @Override
    public boolean isUnique() {
        return true;
    }

    @Override
    public String identify() {
        return "enchantment@" + getCleanName();
    }

    @Override
    public String identifySimple() {
        return identify();
    }

    @Override
    public String toString() {
        return identify();
    }

    @Override
    public EnchantmentTag setPrefix(String prefix) {
        this.prefix = prefix;
        return this;
    }

    public String getCleanName() {
        NamespacedKey key = enchantment.getKey();
        if (key.getNamespace().equals("minecraft") || key.getNamespace().equals("denizen")) {
            return key.getKey();
        }
        return key.toString();
    }

    @Override
    public AbstractFlagTracker getFlagTracker() {
        return new RedirectionFlagTracker(DenizenCore.getImplementation().getServerFlags(), "__enchantments." + getCleanName().replace(".", "&dot"));
    }

    @Override
    public void reapplyTracker(AbstractFlagTracker tracker) {
        // Nothing to do.
    }

    public static void registerTags() {

        AbstractFlagTracker.registerFlagHandlers(tagProcessor);

        // <--[tag]
        // @attribute <EnchantmentTag.name>
        // @returns ElementTag
        // @description
        // Gets the name of this enchantment. For vanilla enchantments, uses the vanilla name like 'sharpness'.
        // For Denizen custom enchantments, returns the 'id' specified in the script.
        // For any other enchantments, returns the full key.
        // -->
        registerTag("name", (attribute, object) -> {
            return new ElementTag(object.getCleanName());
        });

        // <--[tag]
        // @attribute <EnchantmentTag.key>
        // @returns ElementTag
        // @description
        // Returns the full key for this enchantment, like "minecraft:sharpness".
        // -->
        registerTag("key", (attribute, object) -> {
            return new ElementTag(object.enchantment.getKey().toString());
        });

        // <--[tag]
        // @attribute <EnchantmentTag.min_level>
        // @returns ElementTag(Number)
        // @description
        // Returns the minimum level of this enchantment. Usually '1'.
        // -->
        registerTag("min_level", (attribute, object) -> {
            return new ElementTag(object.enchantment.getStartLevel());
        });

        // <--[tag]
        // @attribute <EnchantmentTag.max_level>
        // @returns ElementTag(Number)
        // @description
        // Returns the minimum level of this enchantment. Usually between 1 and 5.
        // -->
        registerTag("max_level", (attribute, object) -> {
            return new ElementTag(object.enchantment.getMaxLevel());
        });

        // <--[tag]
        // @attribute <EnchantmentTag.treasure_only>
        // @returns ElementTag(Boolean)
        // @description
        // Returns whether this enchantment is only for spawning as treasure.
        // -->
        registerTag("treasure_only", (attribute, object) -> {
            return new ElementTag(object.enchantment.isTreasure());
        });

        // <--[tag]
        // @attribute <EnchantmentTag.is_tradable>
        // @returns ElementTag(Boolean)
        // @description
        // Returns whether this enchantment is only considered to be tradable. Villagers won't trade this enchantment if set to false.
        // -->
        registerTag("is_tradable", (attribute, object) -> {
            return new ElementTag(NMSHandler.enchantmentHelper.isTradable(object.enchantment));
        });

        // <--[tag]
        // @attribute <EnchantmentTag.is_discoverable>
        // @returns ElementTag(Boolean)
        // @description
        // Returns whether this enchantment is only considered to be discoverable.
        // If true, this will spawn from vanilla sources like the enchanting table. If false, it can only be given directly by script.
        // -->
        registerTag("is_discoverable", (attribute, object) -> {
            return new ElementTag(NMSHandler.enchantmentHelper.isDiscoverable(object.enchantment));
        });

        // <--[tag]
        // @attribute <EnchantmentTag.is_curse>
        // @returns ElementTag(Boolean)
        // @description
        // Returns whether this enchantment is only considered to be a curse. Curses are removed at grindstones, and spread from crafting table repairs.
        // -->
        registerTag("is_curse", (attribute, object) -> {
            return new ElementTag(NMSHandler.enchantmentHelper.isCurse(object.enchantment));
        });

        // <--[tag]
        // @attribute <EnchantmentTag.category>
        // @returns ElementTag
        // @description
        // Returns the category of this enchantment. Can be any of: ARMOR, ARMOR_FEET, ARMOR_LEGS, ARMOR_CHEST, ARMOR_HEAD,
        // WEAPON, DIGGER, FISHING_ROD, TRIDENT, BREAKABLE, BOW, WEARABLE, CROSSBOW, VANISHABLE
        // -->
        registerTag("category", (attribute, object) -> {
            return new ElementTag(object.enchantment.getItemTarget().name());
        });

        // <--[tag]
        // @attribute <EnchantmentTag.rarity>
        // @returns ElementTag
        // @description
        // Returns the rarity of this enchantment. Can be any of: COMMON, UNCOMMON, RARE, VERY_RARE
        // -->
        registerTag("rarity", (attribute, object) -> {
            return new ElementTag(NMSHandler.enchantmentHelper.getRarity(object.enchantment));
        });

        // <--[tag]
        // @attribute <EnchantmentTag.can_enchant[<item>]>
        // @returns ElementTag(Boolean)
        // @description
        // Returns whether this enchantment can enchant the given ItemTag (based on material mainly).
        // -->
        registerTag("can_enchant", (attribute, object) -> {
            if (!attribute.hasContext(1)) {
                return null;
            }
            return new ElementTag(object.enchantment.canEnchantItem(attribute.contextAsType(1, ItemTag.class).getItemStack()));
        });

        // <--[tag]
        // @attribute <EnchantmentTag.is_compatible[<enchantment>]>
        // @returns ElementTag(Boolean)
        // @description
        // Returns whether this enchantment is compatible with another given enchantment.
        // -->
        registerTag("is_compatible", (attribute, object) -> {
            if (!attribute.hasContext(1)) {
                return null;
            }
            return new ElementTag(!object.enchantment.conflictsWith(attribute.contextAsType(1, EnchantmentTag.class).enchantment));
        });

        // <--[tag]
        // @attribute <EnchantmentTag.min_cost[<level>]>
        // @returns ElementTag(Decimal)
        // @description
        // Returns the minimum cost for this enchantment for the given level.
        // -->
        registerTag("min_cost", (attribute, object) -> {
            if (!attribute.hasContext(1)) {
                return null;
            }
            return new ElementTag(NMSHandler.enchantmentHelper.getMinCost(object.enchantment, attribute.getIntContext(1)));
        });

        // <--[tag]
        // @attribute <EnchantmentTag.max_cost[<level>]>
        // @returns ElementTag(Decimal)
        // @description
        // Returns the maximum cost for this enchantment for the given level.
        // -->
        registerTag("max_cost", (attribute, object) -> {
            if (!attribute.hasContext(1)) {
                return null;
            }
            return new ElementTag(NMSHandler.enchantmentHelper.getMaxCost(object.enchantment, attribute.getIntContext(1)));
        });

        // <--[tag]
        // @attribute <EnchantmentTag.damage_bonus[level=<level>;type=<type>]>
        // @returns ElementTag(Decimal)
        // @description
        // Returns the damage bonus this enchantment applies against the given monster type.
        // The input is a MapTag with a level value and a monster type specified, where the type can be any of: ARTHROPOD, ILLAGER, WATER, UNDEAD, or UNDEFINED
        // For example, <[my_enchantment].damage_bonus[level=3;type=undead]>
        // -->
        registerTag("damage_bonus", (attribute, object) -> {
            if (!attribute.hasContext(1)) {
                return null;
            }
            MapTag map = attribute.contextAsType(1, MapTag.class);
            if (map == null) {
                attribute.echoError("Invalid MapTag input to damage_bonus - not a valid map.");
                return null;
            }
            ObjectTag level = map.getObject("level");
            ObjectTag type = map.getObject("type");
            if (level == null || type == null) {
                attribute.echoError("Invalid MapTag input to damage_bonus - missing 'level' or 'type'");
                return null;
            }
            return new ElementTag(NMSHandler.enchantmentHelper.getDamageBonus(object.enchantment, new ElementTag(level.toString()).asInt(), CoreUtilities.toLowerCase(type.toString())));
        });

        // <--[tag]
        // @attribute <EnchantmentTag.damage_protection[level=<level>;type=<cause>;attacker=<entity>]>
        // @returns ElementTag(Number)
        // @description
        // Returns the damage protection this enchantment applies against the given damage cause and optional attacker.
        // The input is a MapTag with a level value and a damage type specified, where the damage type must be from <@link language Damage Cause>.
        // For entity damage causes, optionally specify the entity attacker.
        // For example, <[my_enchantment].damage_protection[level=3;type=undead]>
        // -->
        registerTag("damage_protection", (attribute, object) -> {
            if (!attribute.hasContext(1)) {
                return null;
            }
            MapTag map = attribute.contextAsType(1, MapTag.class);
            if (map == null) {
                attribute.echoError("Invalid MapTag input to damage_protection - not a valid map.");
                return null;
            }
            ObjectTag level = map.getObject("level");
            ObjectTag type = map.getObject("type");
            if (level == null || type == null) {
                attribute.echoError("Invalid MapTag input to damage_protection - missing 'level' or 'type'");
                return null;
            }
            EntityDamageEvent.DamageCause cause;
            try {
                cause = EntityDamageEvent.DamageCause.valueOf(type.toString().toUpperCase());
            }
            catch (IllegalArgumentException ex) {
                attribute.echoError("Invalid MapTag input to damage_protection - cause '" + type.toString() + "' is not a valid DamageCause.");
                return null;
            }
            ObjectTag attacker = map.getObject("attacker");
            return new ElementTag(NMSHandler.enchantmentHelper.getDamageProtection(object.enchantment, new ElementTag(level.toString()).asInt(), cause, attacker == null ? null : attacker.asType(EntityTag.class, attribute.context).getBukkitEntity()));
        });
    }

    public static ObjectTagProcessor<EnchantmentTag> tagProcessor = new ObjectTagProcessor<>();

    public static void registerTag(String name, TagRunnable.ObjectInterface<EnchantmentTag> runnable, String... variants) {
        tagProcessor.registerTag(name, runnable, variants);
    }

    @Override
    public ObjectTag getObjectAttribute(Attribute attribute) {
        return tagProcessor.getObjectAttribute(this, attribute);
    }
}
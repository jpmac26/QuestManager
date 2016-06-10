package com.skyisland.questmanager.magic.spell;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.bukkit.configuration.file.YamlConfiguration;

public class SpellManager {
	
	private Map<String, Spell> spells;
	
	public SpellManager() {
		this.spells = new HashMap<>();
	}
	
	public SpellManager(File spellDirectory) {
		this();
		load(spellDirectory);
	}
	
	private void load(File directory) {
		if (directory == null || !directory.exists()) {
			return;
		}
		
		if (!directory.isDirectory()) {
			loadFile(directory);
		}
		
		//else loop through files
		for (File file : directory.listFiles()) {
			if (file.isDirectory()) {
				load(file);
			}
			
			String ln = file.getName().toLowerCase();
			
			if (ln.endsWith(".yml") || ln.endsWith(".yaml")) {
				loadFile(file);
			}
		}
	}
	
	private void loadFile(File spellFile) {
		//get config, grab all spells
		YamlConfiguration config = new YamlConfiguration();
		try {
			config.load(spellFile);
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Unable to load spell file: " + spellFile.getAbsolutePath());
			return;
		}
		
		for (String key : config.getKeys(false)) {
			Spell spell = (Spell) config.get(key);
			addSpell(spell);
		}
	}
	
	/**
	 * Adds the spell to the manager, overwriting any with a conflicting name;
	 * @return true if there was a spell by that name before, false otherwise
	 */
	public boolean addSpell(Spell spell) {
		return spells.put(spell.getName(), spell) != null;
	}
	
	public Spell getSpell(String name) {
		return spells.get(name);
	}
	
	/**
	 * Returns a list of all registered spell names
	 */
	public Set<String> getSpells() {
		return spells.keySet();
	}
}
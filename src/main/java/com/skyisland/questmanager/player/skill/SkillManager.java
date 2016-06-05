package com.skyisland.questmanager.player.skill;

import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

/**
 * Holds registered skills, configuration for skills in general, and some basic skill types.
 * @author Skyler
 *
 */
public class SkillManager {
	
	private Set<Skill> skills;
	
	
	public SkillManager() {
		skills = new HashSet<>();
	}
	
	public void registerSkill(Skill skill) {
		skills.add(skill);
	}
	
	public Set<Skill> getAllSkills() {
		return skills;
	}
	
	public Set<Skill> getSkills(Skill.Type skillType) {
		Set<Skill> ret = new TreeSet<>();
		for (Skill skill : skills) {
			if (skill.getType() == skillType) {
				ret.add(skill);
			}
		}
		
		return ret;
	}
	
}

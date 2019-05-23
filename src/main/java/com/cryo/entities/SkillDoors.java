package com.cryo.entities;

import com.runemate.game.api.hybrid.local.Skill;

public enum SkillDoors {

    MAGICAL_BARRIER(Skill.MAGIC.getIndex(), 1),
    RUNED_DOOR(Skill.RUNECRAFTING.getIndex(), 1),
    BARRED_DOOR(Skill.STRENGTH.getIndex(), 1),
    PILE_OF_ROCKS(Skill.MINING.getIndex(), 1),
    FLAMMABLE_DEBRIS(Skill.FIREMAKING.getIndex(), 1),
    DARK_SPIRIT(Skill.PRAYER.getIndex(), 1),
    WOODEN_BARRICADE(Skill.WOODCUTTING.getIndex(), 1),
    BROKEN_KEY_DOOR(Skill.SMITHING.getIndex(), 1),
    BROKEN_PULLEY_DOOR(Skill.CRAFTING.getIndex(), 1),
    LOCKED_DOOR(Skill.AGILITY.getIndex(), 1),
    PADLOCKED_DOOR(Skill.THIEVING.getIndex(), 1),
    RAMOKEE_EXILE(Skill.SUMMONING.getIndex(), 1),
    LIQUID_ROCK(Skill.HERBLORE.getIndex(), 1),
    VINE_COVERED_DOOR(Skill.FARMING.getIndex(), 1),
    COLLAPSING_DOORFRAME(Skill.CONSTRUCTION.getIndex(), 1),
    DIVINE_DOOR(Skill.DIVINATION.getIndex(), 1);

    private int skillId;
    private int potionId;

    SkillDoors(int skillId, int potionId) {
        this.skillId = skillId;
        this.potionId = potionId;
    }

    public int getSkillId() {
        return skillId;
    }

    public int getPotionId() {
        return potionId;
    }

    public static SkillDoors getDoor(String name) {
        for (SkillDoors door : SkillDoors.values()) {
            String doorName = door.name().toLowerCase().replaceAll("_", " ");
            if (doorName.equalsIgnoreCase(name))
                return door;
        }
        return null;
    }
}

package com.cryo.entities;

import com.runemate.game.api.hybrid.entities.GroundItem;
import com.runemate.game.api.hybrid.local.Skill;
import com.runemate.game.api.hybrid.local.hud.interfaces.Inventory;
import com.runemate.game.api.hybrid.location.Area;
import com.runemate.game.api.hybrid.region.GroundItems;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Optional;

public enum Foods {

    HEIM_CRAB(18159, 0, 200),
    RED_EYE(18161, 0, 250),
    DUSK_EEL(18163, 20),
    GIANT_FLATFISH(18165, 30),
    SHORT_FINNED_EEL(18167, 40),
    WEB_SNIPPER(18169, 50),
    BOULDABASS(18171, 60),
    SALVE_EEL(18173, 70),
    BLUE_CRAB(18175, 80),
    CAVE_MORAY(18177, 90);

    private int id;
    private int max;
    private int hp;

    private static HashMap<Integer, Foods> foods;

    static {
        loadFoods();
    }

    Foods(int id, int max, int hp) {
        this.id = id;
        this.max = max;
        this.hp = hp;
    }

    Foods(int id, int max) {
        this(id, max, 0);
    }

    public int getHeal(int level) {
        if (hp != 0) return hp;
        if (level > max)
            level = max;
        return ((level - 10) * 25) + 250;
    }

    private static void loadFoods() {
        foods = new HashMap<>();
        Arrays.stream(Foods.values()).forEach(f -> foods.put(f.id, f));
    }

    public static GroundItem getPriorityItem(Area area) {
        int level = Skill.CONSTITUTION.getBaseLevel();
        Optional<GroundItem> optional = GroundItems.getLoadedWithin(area)
                .stream()
                .filter(v -> getFood(v.getId()) != null)
                .sorted(Comparator.comparingInt(s -> getFood(s.getId())
                        .getHeal(level)))
                .findFirst();
        if (!optional.isPresent()) return null;
        return optional.get();
    }

    public static int getTotalHealing() {
        int level = Skill.CONSTITUTION.getBaseLevel();
        Optional<Integer> optional = Inventory.getItems()
                .stream()
                .map(item -> getFood(item.getId()))
                .filter(value -> value != null)
                .map(f -> f.getHeal(level))
                .reduce((a, b) -> a + b);
        if (optional.isPresent()) return optional.get();
        return 1;
    }

    private static Foods getFood(int id) {
        return !isFood(id) ? null : foods.get(id);
    }

    public static boolean isFood(int id) {
        return foods.containsKey(id);
    }
}

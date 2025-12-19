package org.example.mygame.fx.model;

public enum OreType {
    GOLD(1.5, "Золото"),
    DIAMOND(2.0, "Алмаз"),
    IRON(1.2, "Железо"),
    STONE(0.8, "Камень"),
    COAL(0.9, "Уголь"),
    DIRT(1.0, "Земля");
    
    public final double speedMultiplier;
    public final String name;
    
    OreType(double speedMultiplier, String name) {
        this.speedMultiplier = speedMultiplier;
        this.name = name;
    }
}


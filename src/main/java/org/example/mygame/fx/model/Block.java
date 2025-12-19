package org.example.mygame.fx.model;

import java.io.Serializable;

public class Block implements Serializable {
    private static final long serialVersionUID = 1L;

    private double x;
    private double depth;
    private BlockType type;
    private OreType ore;
    private boolean isMined;
    private boolean hasOre;
    
    public enum BlockType {
        STONE, DIRT, BEDROCK
    }
    
    public Block(double x, double depth, BlockType type) {
        this.x = x;
        this.depth = depth;
        this.type = type;
        this.isMined = false;
        this.hasOre = false;
        this.ore = null;
    }

    public void addOre(OreType ore) {
        this.hasOre = true;
        this.ore = ore;
    }

    public OreType mine() {
        if (isMined) return null;
        
        isMined = true;
        return hasOre ? ore : null;
    }

    public double getX() { return x; }
    public double getDepth() { return depth; }
    public BlockType getType() { return type; }
    public boolean isMined() { return isMined; }
    public boolean hasOre() { return hasOre; }
    public OreType getOre() { return ore; }
}


package org.example.mygame.fx.model;

import java.io.Serializable;

public class Miner implements Serializable {
    private static final long serialVersionUID = 1L;

    public enum Direction {
        LEFT, RIGHT, DOWN
    }
    
    private int id;
    private String name;
    private double x;
    private double depth;
    private double baseMiningSpeed;
    private double currentMiningSpeed;
    private double miningProgress;
    private OreType activeOreEffect;
    private double oreEffectTime;
    private int blocksMined;
    private boolean reachedBottom;
    private Direction direction;

    private double moveCooldown = 0.0;
    private static final double MOVE_COOLDOWN_TIME = 0.2;
    
    public Miner(int id, String name, double x, double baseMiningSpeed) {
        this.id = id;
        this.name = name;
        this.x = x;
        this.depth = 0;
        this.baseMiningSpeed = baseMiningSpeed;
        this.currentMiningSpeed = baseMiningSpeed;
        this.miningProgress = 0;
        this.activeOreEffect = null;
        this.oreEffectTime = 0;
        this.blocksMined = 0;
        this.reachedBottom = false;
        this.direction = Direction.DOWN;
    }

    public void update(double deltaTime) {
        if (moveCooldown > 0) {
            moveCooldown -= deltaTime;
        }

        if (activeOreEffect != null) {
            oreEffectTime -= deltaTime;
            if (oreEffectTime <= 0) {
                activeOreEffect = null;
                currentMiningSpeed = baseMiningSpeed;
            }
        }
    }

    public boolean tryMove() {
        if (moveCooldown <= 0) {
            moveCooldown = MOVE_COOLDOWN_TIME;
            return true;
        }
        return false;
    }
    
    public void resetMoveCooldown() {
        moveCooldown = 0;
    }

    public boolean mineBlock(double deltaTime) {
        if (reachedBottom) return false;
        
        miningProgress += currentMiningSpeed * deltaTime;
        if (miningProgress >= 1.0) {
            miningProgress = 0;
            blocksMined++;
            return true;
        }
        return false;
    }

    public void turnLeft() {
        if (reachedBottom) return;
        direction = Direction.LEFT;
    }

    public void turnRight() {
        if (reachedBottom) return;
        direction = Direction.RIGHT;
    }

    public void turnDown() {
        if (reachedBottom) return;
        direction = Direction.DOWN;
    }

    public void moveLeft(double deltaTime) {
        if (reachedBottom) return;
        direction = Direction.LEFT;
        double moveSpeed = 3.0;
        double newX = x - moveSpeed * deltaTime;
        if (newX >= 0) {
            x = newX;
        } else {
            x = 0;
        }
    }

    public void moveRight(double deltaTime) {
        if (reachedBottom) return;
        direction = Direction.RIGHT;
        double moveSpeed = 3.0;
        double newX = x + moveSpeed * deltaTime;
        if (newX < GameState.WORLD_WIDTH) {
            x = newX;
        } else {
            x = GameState.WORLD_WIDTH - 0.1;
        }
    }


    public boolean canMine() {
        if (reachedBottom) return false;
        
        switch (direction) {
            case LEFT:
                return x > 0;
            case RIGHT:
                return x < GameState.WORLD_WIDTH - 1;
            case DOWN:
                return depth < GameState.BOTTOM_DEPTH - 1.0;
            default:
                return false;
        }
    }

    public boolean canMoveLeft() {
        if (reachedBottom) return false;
        return x > 0;
    }

    public boolean canMoveRight() {
        if (reachedBottom) return false;
        return x < GameState.WORLD_WIDTH - 1;
    }

    public void collectOre(OreType oreType) {
        this.activeOreEffect = oreType;
        this.oreEffectTime = 5.0;
        this.currentMiningSpeed = baseMiningSpeed * oreType.speedMultiplier;
    }

    public void checkBottomReached(double bottomDepth) {
        if (depth >= bottomDepth - 1.0 - 0.1 && !reachedBottom) {
            reachedBottom = true;
            miningProgress = 0;
            System.out.println("Miner " + id + " reached bottom at depth " + depth + " (target: " + (bottomDepth - 1) + ")");
        }
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public double getX() { return x; }
    public double getDepth() { return depth; }
    public double getBaseMiningSpeed() { return baseMiningSpeed; }
    public double getCurrentMiningSpeed() { return currentMiningSpeed; }
    public double getMiningProgress() { return miningProgress; }
    public OreType getActiveOreEffect() { return activeOreEffect; }
    public double getOreEffectTime() { return oreEffectTime; }
    public int getBlocksMined() { return blocksMined; }
    public boolean hasReachedBottom() { return reachedBottom; }
    
    public void setX(double x) { 
        if (!reachedBottom) {
            this.x = x;
        }
    }
    
    public void setDepth(double depth) {
        if (depth >= 0 && depth <= GameState.BOTTOM_DEPTH) {
            this.depth = depth;
        }
    }
    
    public Direction getDirection() { return direction; }

    public void setMiningProgress(double progress) {
        this.miningProgress = Math.max(0, Math.min(1, progress));
    }
    
    public void setBlocksMined(int blocks) {
        this.blocksMined = blocks;
    }
    
    public void setDirection(Direction dir) {
        this.direction = dir;
    }
    
    public void setReachedBottom(boolean reached) {
        this.reachedBottom = reached;
    }
    
    public void setOreEffect(OreType ore, double time) {
        this.activeOreEffect = ore;
        this.oreEffectTime = time;
        if (ore != null) {
            this.currentMiningSpeed = baseMiningSpeed * ore.speedMultiplier;
        } else {
            this.currentMiningSpeed = baseMiningSpeed;
        }
    }
}


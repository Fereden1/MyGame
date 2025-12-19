package org.example.mygame.fx.model;

import java.io.Serializable;
import java.util.*;

/**
 * Состояние игры
 */
public class GameState implements Serializable {
    private static final long serialVersionUID = 1L;

    public static final int WORLD_WIDTH = 10;
    public static final double BOTTOM_DEPTH = 91.0;
    public static final double BLOCK_SIZE = 40.0;
    
    private List<Miner> miners;
    private final List<Map<String, Block>> blocksByMiner;
    private Miner winner;
    private boolean gameOver;
    private Random random;
    
    public GameState() {
        this.miners = new ArrayList<>();
        this.blocksByMiner = new ArrayList<>();
        this.random = new Random();
        this.gameOver = false;
        this.winner = null;

        miners.add(new Miner(1, "Шахтер 1", 2.0, 1.0)); // 1 блок в секунду
        miners.add(new Miner(2, "Шахтер 2", 7.0, 1.0)); // 1 блок в секунду

        generateWorld();
    }

    private void generateWorld() {
        OreType[] ores = OreType.values();

        for (int m = 0; m < 2; m++) {
            Map<String, Block> minerBlocks = new HashMap<>();
            for (int x = 0; x < WORLD_WIDTH; x++) {
                for (int depth = 0; depth < BOTTOM_DEPTH; depth++) {
                    String key = x + "_" + depth;
                    Block.BlockType blockType = depth >= BOTTOM_DEPTH - 1
                        ? Block.BlockType.BEDROCK
                        : (depth < 5 ? Block.BlockType.DIRT : Block.BlockType.STONE);
                    
                    Block block = new Block(x, depth, blockType);
                    
                    if (random.nextDouble() < 0.15 && depth > 5 && depth < BOTTOM_DEPTH - 2) {
                        OreType ore = ores[random.nextInt(ores.length)];
                        block.addOre(ore);
                    }
                    
                    minerBlocks.put(key, block);
                }
            }
            blocksByMiner.add(minerBlocks);
        }
    }
    

    public Block getBlock(int minerIndex, double x, double depth) {
        int blockX = (int) Math.floor(x);
        int blockDepth = (int) Math.floor(depth);
        String key = blockX + "_" + blockDepth;
        if (minerIndex < 0 || minerIndex >= blocksByMiner.size()) return null;
        return blocksByMiner.get(minerIndex).get(key);
    }
    

    public void update(double deltaTime) {
        if (gameOver) return;

        for (Miner miner : miners) {
            miner.update(deltaTime);

            miner.checkBottomReached(BOTTOM_DEPTH);

            if (miner.hasReachedBottom() && !gameOver) {
                System.out.println("WINNER DETECTED in update(): Miner " + miner.getId() + " (" + miner.getName() + ") reached bottom!");
                gameOver = true;
                winner = miner;
                for (Miner m : miners) {
                    m.setReachedBottom(true);
                }
                return;
            }
        }
    }

    public void mineBlock(int minerId, double deltaTime) {
        mineBlock(minerId, deltaTime, 0);
    }

    private void mineBlock(int minerId, double deltaTime, int recursionDepth) {
        if (gameOver) return;

        if (recursionDepth > 10) {
            return;
        }
        
        Miner miner = miners.stream()
            .filter(m -> m.getId() == minerId)
            .findFirst()
            .orElse(null);
        
        if (miner == null || miner.hasReachedBottom()) {
            return;
        }

        Block targetBlock = getTargetBlock(miner);
        
        if (targetBlock == null) {
            return;
        }

        if (targetBlock.isMined()) {
            return;
        }

        if (!miner.canMine()) {
            System.out.println("Miner " + minerId + " cannot mine (canMine=false)");
            return;
        }

        boolean blockCompleted = miner.mineBlock(deltaTime);

        if (blockCompleted) {
            System.out.println("Miner " + minerId + " BROKE BLOCK at " + targetBlock.getX() + "," + targetBlock.getDepth());
            OreType ore = targetBlock.mine();
            if (ore != null) {
                miner.collectOre(ore);
            }

            miner.resetMoveCooldown();

            miner.setX(targetBlock.getX());
            miner.setDepth(targetBlock.getDepth());

            miner.checkBottomReached(BOTTOM_DEPTH); 

            if (miner.hasReachedBottom() && !gameOver) {
                System.out.println("WINNER DETECTED: Miner " + minerId + " (" + miner.getName() + ") reached bottom at depth " + miner.getDepth() + "!");
                gameOver = true;
                winner = miner;
                for (Miner m : miners) {
                    if (m.getId() != minerId) {
                        m.setReachedBottom(true);
                    }
                }
            }
        }
    }

    private boolean moveToBlock(Miner miner, Miner.Direction direction) {
        if (!miner.tryMove()) {
            return false;
        }

        double currentX = miner.getX();
        double currentDepth = miner.getDepth();
        int blockX = (int) Math.floor(currentX);
        int blockDepth = (int) Math.floor(currentDepth);
        
        switch (direction) {
            case LEFT:
                int targetXLeft = blockX - 1;
                if (targetXLeft >= 0) {
                    Block block = getBlock(miner.getId() - 1, targetXLeft, blockDepth);
                    if (block != null && block.isMined()) {
                        miner.setX(targetXLeft);
                        return true;
                    }
                }
                break;
            case RIGHT:
                int targetXRight = blockX + 1;
                if (targetXRight < WORLD_WIDTH) {
                    Block block = getBlock(miner.getId() - 1, targetXRight, blockDepth);
                    if (block != null && block.isMined()) {
                        miner.setX(targetXRight);
                        return true;
                    }
                }
                break;
            case DOWN:
                int targetDepth = blockDepth + 1;
                if (targetDepth <= BOTTOM_DEPTH - 1) {
                    Block block = getBlock(miner.getId() - 1, blockX, targetDepth);
                    if (block != null && block.isMined()) {
                        miner.setDepth(targetDepth);
                        return true;
                    }
                }
                break;
        }
        return false;
    }

    private Block getTargetBlock(Miner miner) {
        double x = miner.getX();
        double depth = miner.getDepth();
        
        switch (miner.getDirection()) {
            case DOWN:
                return getBlock(miner.getId() - 1, x, depth + 1.0);
            case LEFT:
                return getBlock(miner.getId() - 1, x - 1.0, depth);
            case RIGHT:
                return getBlock(miner.getId() - 1, x + 1.0, depth);
            default:
                return getBlock(miner.getId() - 1, x, depth + 1.0);
        }
    }

    public void turnMiner(int minerId, Miner.Direction direction) {
        if (gameOver) return;
        
        Miner miner = miners.stream()
            .filter(m -> m.getId() == minerId)
            .findFirst()
            .orElse(null);
        
        if (miner == null || miner.hasReachedBottom()) return;
        
        switch (direction) {
            case LEFT:
                miner.turnLeft();
                break;
            case RIGHT:
                miner.turnRight();
                break;
            case DOWN:
                miner.turnDown();
                break;
        }
    }

    public void moveMinerInDirection(int minerId, double deltaTime) {
        if (gameOver) return;
        
        Miner miner = miners.stream()
            .filter(m -> m.getId() == minerId)
            .findFirst()
            .orElse(null);
        
        if (miner == null || miner.hasReachedBottom()) return;

        moveToBlock(miner, miner.getDirection());
    }

    public void moveMinerLeft(int minerId, double deltaTime) {
        if (gameOver) return;
        
        Miner miner = miners.stream()
            .filter(m -> m.getId() == minerId)
            .findFirst()
            .orElse(null);
        
        if (miner == null || miner.hasReachedBottom()) return;
        
        int blockX = (int) Math.floor(miner.getX() - 1);
        int blockDepth = (int) Math.floor(miner.getDepth());
        Block blockLeft = getBlock(miner.getId() - 1, blockX, blockDepth);

        if (blockLeft != null && blockLeft.isMined()) {
            miner.setX(blockX);
        } else {
            miner.turnLeft();
            mineBlock(minerId, deltaTime);
        }
    }

    public void moveMinerRight(int minerId, double deltaTime) {
        if (gameOver) return;
        
        Miner miner = miners.stream()
            .filter(m -> m.getId() == minerId)
            .findFirst()
            .orElse(null);
        
        if (miner == null || miner.hasReachedBottom()) return;
        
        int blockX = (int) Math.floor(miner.getX() + 1);
        int blockDepth = (int) Math.floor(miner.getDepth());
        Block blockRight = getBlock(miner.getId() - 1, blockX, blockDepth);

        if (blockRight != null && blockRight.isMined()) {
            miner.setX(blockX);
        } else {
            miner.turnRight();
            mineBlock(minerId, deltaTime);
        }
    }

    public void setGameOver(boolean over) {
        this.gameOver = over;
    }
    
    public void setWinner(int minerId) {
        if (minerId > 0 && minerId <= miners.size()) {
            this.winner = miners.get(minerId - 1);
        } else {
            this.winner = null;
        }
    }

    public List<Miner> getMiners() { return miners; }
    public Map<String, Block> getBlocksForMiner(int minerIndex) { 
        if (minerIndex < 0 || minerIndex >= blocksByMiner.size()) return Collections.emptyMap();
        return blocksByMiner.get(minerIndex); 
    }
    public Miner getWinner() { return winner; }
    public boolean isGameOver() { return gameOver; }
}


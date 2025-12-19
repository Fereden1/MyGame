package org.example.mygame.fx.view.renderer;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import org.example.mygame.fx.model.Block;
import org.example.mygame.fx.model.GameState;
import org.example.mygame.fx.model.Miner;
import org.example.mygame.fx.model.OreType;

public class MinerRaceRenderer implements IRenderer {
    private final double[] cameraY = new double[]{0, 0};
    
    public MinerRaceRenderer() {
        this.cameraY[0] = 0;
        this.cameraY[1] = 0;
    }
    
    @Override
    public void render(GraphicsContext gc, GameState gameState, double width, double height) {
        if (gameState == null) {
            renderLoadingScreen(gc, width, height);
            return;
        }

        gc.setFill(Color.DARKSLATEGRAY);
        gc.fillRect(0, 0, width, height);

        double halfWidth = width / 2.0;
        double maxBlockSizeByWidth = (halfWidth - 20) / GameState.WORLD_WIDTH;
        double renderBlockSize = Math.min(GameState.BLOCK_SIZE, Math.max(20, maxBlockSizeByWidth));

        updateCameras(gameState, height, renderBlockSize);

        renderViewport(gc, gameState, 0, 0, halfWidth, renderBlockSize, false, height);
        renderViewport(gc, gameState, 1, halfWidth, halfWidth, renderBlockSize, false, height);

        gc.setFill(Color.DARKSLATEBLUE);
        gc.fillRect(halfWidth - 3, 0, 6, height);
    }
    
    private void renderLoadingScreen(GraphicsContext gc, double width, double height) {
        gc.setFill(Color.DARKSLATEGRAY);
        gc.fillRect(0, 0, width, height);
        gc.setFill(Color.WHITE);
        gc.setFont(Font.font(20));
        gc.fillText("Ожидание данных от сервера...", width / 2 - 150, height / 2);
    }
    
    private void updateCameras(GameState gameState, double height, double blockSize) {
        if (gameState.getMiners().isEmpty()) return;
        
        for (int i = 0; i < Math.min(2, gameState.getMiners().size()); i++) {
            Miner miner = gameState.getMiners().get(i);
            double targetY = miner.getDepth() - 5;
            double newCam = cameraY[i] * 0.9 + targetY * 0.1;
            newCam = Math.max(0, Math.min(newCam, GameState.BOTTOM_DEPTH - 1));
            cameraY[i] = newCam;
        }
    }
    
    private void renderViewport(GraphicsContext gc, GameState gameState, int minerIndex, 
                                double offsetX, double viewportWidth, double blockSize, 
                                boolean mirrored, double height) {
        if (minerIndex >= gameState.getMiners().size()) {
            return;
        }
        
        double baseOffsetX = offsetX + (viewportWidth - GameState.WORLD_WIDTH * blockSize) / 2;
        double camY = cameraY[minerIndex];

        renderBlocks(gc, gameState, minerIndex, baseOffsetX, camY, blockSize, height, mirrored);

        renderMiner(gc, gameState.getMiners().get(minerIndex), blockSize, baseOffsetX, camY, mirrored, height);
    }
    
    private void renderBlocks(GraphicsContext gc, GameState gameState, int minerIndex, 
                             double baseOffsetX, double camY, double blockSize, 
                             double height, boolean mirrored) {
        int startDepth = (int) Math.max(0, camY - 2);
        int endDepth = (int) Math.min(GameState.BOTTOM_DEPTH, camY + height / blockSize + 2);
        
        for (int x = 0; x < GameState.WORLD_WIDTH; x++) {
            for (int depth = startDepth; depth < endDepth; depth++) {
                Block block = gameState.getBlock(minerIndex, x, depth);
                if (block == null || block.isMined()) continue;
                
                double localX = mirrored
                    ? (GameState.WORLD_WIDTH - 1 - x) * blockSize
                    : x * blockSize;
                double screenX = baseOffsetX + localX;
                double screenY = (depth - camY) * blockSize;
                
                if (screenY < -blockSize || screenY > height) continue;

                renderBlock(gc, block, screenX, screenY, blockSize, depth);
            }
        }
    }
    
    private void renderBlock(GraphicsContext gc, Block block, double x, double y, 
                            double size, int depth) {
        if (block.getType() == Block.BlockType.BEDROCK) {
            gc.setFill(Color.DARKGRAY);
        } else if (block.getType() == Block.BlockType.DIRT) {
            gc.setFill(Color.SADDLEBROWN);
        } else {
            gc.setFill(Color.GRAY);
        }
        
        gc.fillRect(x, y, size, size);

        if (depth >= GameState.BOTTOM_DEPTH - 1) {
            gc.setStroke(Color.RED);
            gc.setLineWidth(3);
            gc.strokeRect(x, y, size, size);
            
            gc.setFill(Color.RED);
            gc.setFont(Font.font(16));
            gc.fillText("ДНО", x + size / 4, y + size / 2);
        }

        if (block.hasOre()) {
            renderOre(gc, block, x, y, size);
        }

        gc.setStroke(Color.BLACK);
        gc.setLineWidth(1);
        gc.strokeRect(x, y, size, size);
    }
    
    private void renderOre(GraphicsContext gc, Block block, double x, double y, double size) {
        OreType ore = block.getOre();
        if (ore == null) return;

        switch (ore) {
            case GOLD -> gc.setFill(Color.GOLD);
            case DIAMOND -> gc.setFill(Color.CYAN);
            case IRON -> gc.setFill(Color.SILVER);
            case COAL -> gc.setFill(Color.BLACK);
            case STONE -> gc.setFill(Color.DARKSLATEGRAY);
            default -> gc.setFill(Color.BROWN);
        }

        double oreSize = size * 0.6;
        double offset = (size - oreSize) / 2;
        gc.fillOval(x + offset, y + offset, oreSize, oreSize);
    }
    
    private void renderMiner(GraphicsContext gc, Miner miner, double blockSize, 
                            double baseOffsetX, double camY, boolean mirrored, double canvasHeight) {
        Color[] colors = {Color.BLUE, Color.RED};
        int colorIndex = Math.max(0, Math.min(1, miner.getId() - 1));
        
        double localX = mirrored
            ? (GameState.WORLD_WIDTH - 1 - miner.getX()) * blockSize
            : miner.getX() * blockSize;
        double screenX = baseOffsetX + localX;
        double screenY = (miner.getDepth() - camY) * blockSize;
        
        if (screenY < -blockSize || screenY > canvasHeight) return;
        
        gc.setFill(colors[colorIndex]);
        double minerSize = blockSize * 0.8;
        double offset = (blockSize - minerSize) / 2;
        double centerX = screenX + blockSize / 2;
        double centerY = screenY + blockSize / 2;
        
        gc.fillOval(screenX + offset, screenY + offset, minerSize, minerSize);

        renderDirection(gc, miner.getDirection(), centerX, centerY, minerSize);

        if (miner.getMiningProgress() > 0) {
            double barWidth = minerSize + 10;
            double barHeight = 6;
            double barX = screenX + (blockSize - barWidth) / 2;
            double barY = screenY - 10;

            gc.setStroke(Color.BLACK);
            gc.setLineWidth(1);
            gc.setFill(Color.DARKGRAY);
            gc.fillRect(barX, barY, barWidth, barHeight);
            gc.strokeRect(barX, barY, barWidth, barHeight);

            gc.setFill(Color.LIME);
            gc.fillRect(barX + 1, barY + 1, (barWidth - 2) * miner.getMiningProgress(), barHeight - 2);
        }

        if (miner.getActiveOreEffect() != null) {
            gc.setFill(Color.YELLOW);
            gc.fillOval(screenX + offset + minerSize - 10, screenY + offset, 10, 10);
        }

        gc.setFill(Color.WHITE);
        gc.setFont(Font.font(10));
        gc.fillText(miner.getName(), screenX, screenY - 5);
    }
    
    private void renderDirection(GraphicsContext gc, Miner.Direction direction, 
                                double centerX, double centerY, double minerSize) {
        gc.setStroke(Color.WHITE);
        gc.setLineWidth(3);
        double arrowLength = minerSize * 0.4;
        
        switch (direction) {
            case LEFT -> {
                gc.strokeLine(centerX, centerY, centerX - arrowLength, centerY);
                gc.strokeLine(centerX - arrowLength, centerY, centerX - arrowLength + 5, centerY - 5);
                gc.strokeLine(centerX - arrowLength, centerY, centerX - arrowLength + 5, centerY + 5);
            }
            case RIGHT -> {
                gc.strokeLine(centerX, centerY, centerX + arrowLength, centerY);
                gc.strokeLine(centerX + arrowLength, centerY, centerX + arrowLength - 5, centerY - 5);
                gc.strokeLine(centerX + arrowLength, centerY, centerX + arrowLength - 5, centerY + 5);
            }
            case DOWN -> {
                gc.strokeLine(centerX, centerY, centerX, centerY + arrowLength);
                gc.strokeLine(centerX, centerY + arrowLength, centerX - 5, centerY + arrowLength - 5);
                gc.strokeLine(centerX, centerY + arrowLength, centerX + 5, centerY + arrowLength - 5);
            }
        }
    }
    
    @Override
    public void cleanup() {
    }
}


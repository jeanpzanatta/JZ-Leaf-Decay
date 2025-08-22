package com.jean.JZLeafDecay;

import java.util.*;

import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.type.Leaves;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.LeavesDecayEvent;
import org.bukkit.plugin.java.JavaPlugin;

// Código fortemente "inspirado" em https://github.com/StarTux/FastLeafDecay.git, muito melhor que o meu original.

public final class LeafDecay extends JavaPlugin implements Listener {
    private final Set<Block> scheduledBlocks = new HashSet<>();
    private static final List<BlockFace> NEIGHBORS = Arrays
            .asList(BlockFace.UP,
                    BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST,
                    BlockFace.DOWN);

    @Override
    public void onEnable() {
        // Register events
        getServer().getPluginManager().registerEvents(this, this);
    }

    @Override
    public void onDisable() {
        // Clean up
        scheduledBlocks.clear();
    }

    /* Os dois @EventHandler a seguir chamam onBlockRemove, então idependete de quebrar um tronco ou uma folha
     * decair naturalmente, a função sempre vai ser chamada para verificar as folhas vizinhas. */
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        onBlockRemove(event.getBlock(), 1);
    }

    @EventHandler
    public void onLeavesDecay(LeavesDecayEvent event) {
        onBlockRemove(event.getBlock(), 1);
    }

    // Apenas coloca folhas vizinhas na fila para futura verificação.
    private void onBlockRemove(final Block oldBlock, long delay) {
        if (!Tag.LOGS.isTagged(oldBlock.getType())
                && !Tag.LEAVES.isTagged(oldBlock.getType())) {
            return;
        }
        for (BlockFace neighborFace: NEIGHBORS) {
            final Block block = oldBlock.getRelative(neighborFace);
            if (!Tag.LEAVES.isTagged(block.getType())) continue; // Ignora blocos que não são folhas.
            Leaves leaves = (Leaves) block.getBlockData();
            if (leaves.isPersistent()) continue; // Ignora folhas colocadas por jogadores.
            if (scheduledBlocks.add(block)){
                getServer().getScheduler().runTaskLater(this, () -> decay(block), delay);
            }
        }
    }

    // Decide se a folha deve ser quebrada ou não, respeitando distância e o carregamento dos chuncks
    private boolean decay(Block block) {
        if (!scheduledBlocks.remove(block)) return false;
        if (!block.getWorld().isChunkLoaded(block.getX() >> 4, block.getZ() >> 4)) return false;
        if (!Tag.LEAVES.isTagged(block.getType())) return false;
        Leaves leaves = (Leaves) block.getBlockData();
        if (leaves.getDistance() < 7) return false;
        LeavesDecayEvent event = new LeavesDecayEvent(block);
        getServer().getPluginManager().callEvent(event);
        block.breakNaturally();
        return true;
    }
}
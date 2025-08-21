package com.jean.JZLeafDecay;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;
import java.util.Set;

public class LeafDecay extends JavaPlugin implements Listener {

    // Cria um HashSet para a coleção de folhas 'filhas' do tronco.
    private final Set<Block> leavesToDecay = new HashSet<>();
    private final int RADIUS = 6;

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);

        // Processa todos os blocos do HashSet quebrando as folhas a cada 1 tick;
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!leavesToDecay.isEmpty()) {
                    Block leaf = leavesToDecay.iterator().next();
                    leavesToDecay.remove(leaf);
                    if (leaf.getType().toString().contains("LEAVES")) {
                        leaf.breakNaturally();
                    }
                }
            }
        }.runTaskTimer(this, 1L, 1L); // processa uma folha por tick
    }

    @EventHandler
    public void onLogBreak(BlockBreakEvent event) {
        Block block = event.getBlock();

        // Ignora blocos que não são "log"
        if (!block.getType().toString().contains("LOG")) return;

        if (checkLogs(block)){
            getLogger().info("Tem mais troncos");
            return;
        }
        // Verifica os blocos de folha a serem quebrados
        for (Block nearby : getNearbyBlocks(block, RADIUS)) {
            if (nearby.getType().toString().contains("LEAVES")) {
                // Pega apenas folhas geradas naturalmente.
                if (!nearby.getBlockData().getAsString().contains("persistent=true")) {
                    leavesToDecay.add(nearby);
                }
            }
        }
    }

    // Função que busca quais são os blocos próximos e popula o HashSet com as folhas ao redor do tronco.
    private Set<Block> getNearbyBlocks(Block start, int radius) {
        Set<Block> blocks = new HashSet<>();
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    blocks.add(start.getRelative(x, y, z));
                }
            }
        }
        return blocks;
    }
    private boolean checkLogs(Block start){
        for (int x = -1; x <= 1; x += 2) {
            for (int y = -1; y <= 1; y += 2) {
                for (int z = -1; z <= 1; z += 2) {
                    getLogger().info("Tipo de bloco próximo: " + start.getRelative(x, y, z).getType().toString());
                    if (start.getRelative(x, y, z).getType().toString().contains("LOG")) return true;
                }
            }
        }
        return false;
    }
}

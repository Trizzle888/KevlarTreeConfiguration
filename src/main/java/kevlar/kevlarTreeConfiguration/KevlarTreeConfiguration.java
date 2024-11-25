package kevlar.kevlarTreeConfiguration;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.LeavesDecayEvent;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Random;

public class KevlarTreeConfiguration extends JavaPlugin implements Listener {
    private int decayDelay;
    private double appleDropChance;
    private double saplingDropChance;
    private final Random random = new Random();

    @Override
    public void onEnable() {
        saveDefaultConfig();
        loadConfig();
        getServer().getPluginManager().registerEvents(this, this);
        getLogger().info("KevlarLeaves has been enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().info("KevlarLeaves has been disabled!");
    }

    private void loadConfig() {
        reloadConfig();
        FileConfiguration config = getConfig();
        decayDelay = config.getInt("decay-delay-ticks", 200);
        appleDropChance = config.getDouble("apple-drop-chance", 0.5);
        saplingDropChance = config.getDouble("sapling-drop-chance", 0.05);
    }

    @EventHandler
    public void onLeavesDecay(LeavesDecayEvent event) {
        if (decayDelay <= 0) {
            return; // Use default decay if delay is 0 or negative
        }

        event.setCancelled(true);
        Block block = event.getBlock();

        // Schedule the decay
        new BukkitRunnable() {
            @Override
            public void run() {
                if (block.getType().name().contains("LEAVES")) {
                    handleLeafBreak(block, true);
                    block.setType(Material.AIR);
                }
            }
        }.runTaskLater(this, decayDelay);
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        if (block.getType().name().contains("LEAVES")) {
            handleLeafBreak(block, false);
        }
    }

    private void handleLeafBreak(Block block, boolean isNaturalDecay) {
        // Handle apple drops (only from oak leaves)
        if (block.getType() == Material.OAK_LEAVES) {
            if (random.nextDouble() < appleDropChance) {
                block.getWorld().dropItemNaturally(block.getLocation(), new ItemStack(Material.APPLE, 1));
            }
        }

        // Handle sapling drops
        if (random.nextDouble() < saplingDropChance) {
            Material saplingType = getSaplingForLeaf(block.getType());
            if (saplingType != null) {
                block.getWorld().dropItemNaturally(block.getLocation(), new ItemStack(saplingType, 1));
            }
        }
    }

    private Material getSaplingForLeaf(Material leafType) {
        return switch (leafType) {
            case OAK_LEAVES -> Material.OAK_SAPLING;
            case BIRCH_LEAVES -> Material.BIRCH_SAPLING;
            case SPRUCE_LEAVES -> Material.SPRUCE_SAPLING;
            case JUNGLE_LEAVES -> Material.JUNGLE_SAPLING;
            case ACACIA_LEAVES -> Material.ACACIA_SAPLING;
            case DARK_OAK_LEAVES -> Material.DARK_OAK_SAPLING;
            case MANGROVE_LEAVES -> Material.MANGROVE_PROPAGULE;
            case CHERRY_LEAVES -> Material.CHERRY_SAPLING;
            default -> null;
        };
    }
}
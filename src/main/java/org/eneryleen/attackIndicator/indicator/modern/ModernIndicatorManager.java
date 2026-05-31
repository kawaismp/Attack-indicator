package org.eneryleen.attackIndicator.indicator.modern;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.minimessage.tag.standard.StandardTags;
import org.bukkit.Location;
import org.bukkit.entity.Display;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.TextDisplay;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Transformation;
import org.eneryleen.attackIndicator.AttackIndicator;
import org.eneryleen.attackIndicator.ConfigManager;
import org.eneryleen.attackIndicator.indicator.IndicatorSpawner;
import org.joml.Vector3f;

import java.text.DecimalFormat;
import java.util.Set;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

public class ModernIndicatorManager implements IndicatorSpawner {

    private final AttackIndicator plugin;
    private final MiniMessage miniMessage;
    private final DecimalFormat damageFormat;
    private final Random random;
    private final Set<TextDisplay> activeIndicators;

    public ModernIndicatorManager(AttackIndicator plugin) {
        this.plugin = plugin;
        // Restrict to formatting tags only. Interactive tags (click/hover/insertion)
        // are intentionally excluded so a config-supplied format cannot inject
        // clickable links or hover payloads onto every player's screen.
        this.miniMessage = MiniMessage.builder()
                .tags(TagResolver.resolver(
                        StandardTags.color(),
                        StandardTags.decorations(),
                        StandardTags.gradient(),
                        StandardTags.rainbow(),
                        StandardTags.reset()
                ))
                .build();
        this.damageFormat = new DecimalFormat("0.#");
        this.random = new Random();
        this.activeIndicators = ConcurrentHashMap.newKeySet();
    }

    @Override
    public void spawnIndicator(LivingEntity entity, double damage) {
        ConfigManager config = plugin.getConfigManager();

        final int max = config.getMaxActiveIndicators();
        // Cheap early-out; the authoritative cap check happens inside the spawn
        // task below, since the set is only incremented there.
        if (max > 0 && activeIndicators.size() >= max) {
            return;
        }

        Location location = entity.getLocation().clone();
        double entityHeight = entity.getHeight();
        location.add(0, entityHeight + config.getVerticalOffset(), 0);

        if (config.isRandomOffsetEnabled()) {
            double offsetX = (random.nextDouble() - 0.5) * config.getRandomOffsetX();
            double offsetY = (random.nextDouble() - 0.5) * config.getRandomOffsetY();
            double offsetZ = (random.nextDouble() - 0.5) * config.getRandomOffsetZ();
            location.add(offsetX, offsetY, offsetZ);
        }

        String damageText = damageFormat.format(damage);
        String formattedText = config.getIndicatorFormat().replace("{damage}", damageText);
        Component textComponent = miniMessage.deserialize(formattedText);

        plugin.getServer().getScheduler().runTask(plugin, () -> {
            // Re-check here so a same-tick burst (e.g. AoE damage) cannot
            // overshoot the cap: the set is incremented in this same task.
            if (max > 0 && activeIndicators.size() >= max) {
                return;
            }

            TextDisplay display = location.getWorld().spawn(location, TextDisplay.class, textDisplay -> {
                textDisplay.text(textComponent);
                textDisplay.setBillboard(Display.Billboard.CENTER);
                textDisplay.setAlignment(TextDisplay.TextAlignment.CENTER);
                textDisplay.setSeeThrough(true);
                // Transient entity: never written to disk, so a crash or chunk
                // unload mid-animation cannot leave orphaned indicators behind.
                textDisplay.setPersistent(false);

                float scale = config.getIndicatorScale();
                Transformation transformation = textDisplay.getTransformation();
                transformation.getScale().set(new Vector3f(scale, scale, scale));
                textDisplay.setTransformation(transformation);
            });

            activeIndicators.add(display);

            animateIndicator(display, config.getUpwardSpeed(), config.getDisplayDuration());
        });
    }

    private void animateIndicator(TextDisplay display, double speed, int duration) {
        new BukkitRunnable() {
            int ticks = 0;

            @Override
            public void run() {
                if (!display.isValid() || ticks >= duration) {
                    display.remove();
                    activeIndicators.remove(display);
                    cancel();
                    return;
                }

                Location newLocation = display.getLocation().add(0, speed, 0);
                display.teleport(newLocation);

                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    @Override
    public void cleanup() {
        for (TextDisplay display : activeIndicators) {
            if (display.isValid()) {
                display.remove();
            }
        }
        activeIndicators.clear();
    }
}

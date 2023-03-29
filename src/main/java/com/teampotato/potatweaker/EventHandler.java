package com.teampotato.potatweaker;

import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Objects;

@Mod.EventBusSubscriber
public class EventHandler {
    @SubscribeEvent
    public static void ctrlSpawn(EntityJoinWorldEvent event) {
        World world = event.getWorld();
        if (world.isClientSide || Config.REPLACED.get().isEmpty()) return;

        Entity replaced = event.getEntity();
        String name = Objects.requireNonNull(replaced.getType().getRegistryName()).toString();

        int index = Config.REPLACED.get().indexOf(name);
        if (index == -1) return;
        if (replaced.getTags().contains("potatweaker.spawned")) return;

        boolean removalMatchesChance = Config.REMOVAL_MATCHES_CHANCE.get().get(index);
        boolean isRemoval = Config.REMOVE.get().get(index);
        int replaceChance = Config.REPLACE_CHANCE.get().get(index);

        if (isRemoval) {
            if (removalMatchesChance) {
                if (replaceChance > world.getRandom().nextInt(101)) event.setCanceled(true);
            } else {
                event.setCanceled(true);
            }
        } else {
            if (replaceChance > world.getRandom().nextInt(101)) {
                event.setCanceled(true);
                summonHelper(Objects.requireNonNull(world.getServer()), index, replaced);
            }
        }
        if(!event.isCanceled()) replaced.addTag("potatweaker.spawned");
    }

    private static void summonHelper(MinecraftServer server, Integer index, Entity replaced) {
        Vector3d pos = replaced.position();

        String dim = replaced.level.dimension().location().toString();
        String replacer = Config.REPLACER.get().get(index);
        String nbt = Config.NBT.get().get(index);

        String cmd = "/execute in " + dim + " run summon " + replacer + " " + pos.x + " " + pos.y + " " + pos.z + " {" + nbt + "}";

        server.getCommands().performCommand(server.createCommandSourceStack().withSuppressedOutput(), cmd);
    }
}

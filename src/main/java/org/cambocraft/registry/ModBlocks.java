package org.cambocraft.registry;

import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;
import org.cambocraft.Cambocraft;

/** Enregistrements communs, utilisables sur un serveur dédié. */
public final class ModBlocks {
    public static final Block LOTUS_STONE = register("lotus_stone",
            new Block(Block.Settings.create().strength(1.5f, 6.0f).sounds(BlockSoundGroup.STONE)));

    private ModBlocks() { }

    private static Block register(String name, Block block) {
        Identifier id = Identifier.of(Cambocraft.ID, name);
        Registry.register(Registries.BLOCK, id, block);
        Registry.register(Registries.ITEM, id, new BlockItem(block, new Item.Settings()));
        return block;
    }

    public static void initialize() {
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.BUILDING_BLOCKS).register(entries -> entries.add(LOTUS_STONE));
    }
}

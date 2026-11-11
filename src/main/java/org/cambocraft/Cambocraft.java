package org.cambocraft;

import net.fabricmc.api.ModInitializer;
import org.cambocraft.registry.ModBlocks;
import org.cambocraft.registry.ModAttachments;
import org.cambocraft.world.SeasonRuntime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Point d'entrée commun au client et au serveur dédié. */
public final class Cambocraft implements ModInitializer {
    public static final String ID = "cambocraft";
    private static final Logger LOGGER = LoggerFactory.getLogger(ID);

    @Override
    public void onInitialize() {
        ModBlocks.initialize();
        ModAttachments.initialize();
        SeasonRuntime.initialize();
        LOGGER.info("Initialisation des fondations Cambocraft");
    }
}

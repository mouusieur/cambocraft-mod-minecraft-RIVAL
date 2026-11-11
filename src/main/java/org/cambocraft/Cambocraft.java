package org.cambocraft;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Point d'entrée commun ; aucune logique de jeu n'est encore enregistrée. */
public final class Cambocraft implements ModInitializer {
    public static final String ID = "cambocraft";
    private static final Logger LOGGER = LoggerFactory.getLogger(ID);

    @Override
    public void onInitialize() {
        LOGGER.info("Initialisation des fondations Cambocraft");
    }
}

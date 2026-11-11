package org.cambocraft.registry;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleResourceReloadListener;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;
import org.cambocraft.Cambocraft;
import org.cambocraft.progression.ZoneCatalog;
import org.cambocraft.progression.ZoneDefinition;

/** Charge les douze définitions hors thread de jeu avant de remplacer l'instantané. */
public final class ZoneData implements SimpleResourceReloadListener<ZoneCatalog> {
    private static final int MAX_FILE_CHARS = 16_384;
    private static volatile ZoneCatalog current;
    private static final Map<ResourceManager, ZoneCatalog> staged = new WeakHashMap<>();

    public static void initialize() {
        ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(new ZoneData());
        // Le chargement initial ne déclenche pas END_DATA_PACK_RELOAD.
        ServerLifecycleEvents.SERVER_STARTING.register(server -> publish(server.getResourceManager()));
        ServerLifecycleEvents.END_DATA_PACK_RELOAD.register((server, manager, success) -> {
            if (success) publish(manager);
        });
        ServerLifecycleEvents.SERVER_STOPPED.register(server -> {
            current = null;
            synchronized (staged) { staged.clear(); }
        });
    }

    private static void publish(ResourceManager manager) {
        ZoneCatalog result;
        synchronized (staged) { result = staged.remove(manager); }
        if (result == null) throw new IllegalStateException("Catalogue de zones préparé introuvable");
        current = result;
    }

    public static ZoneCatalog current() {
        ZoneCatalog value = current;
        if (value == null) throw new IllegalStateException("Les zones ne sont pas encore chargées");
        return value;
    }

    @Override
    public Identifier getFabricId() { return Identifier.of(Cambocraft.ID, "zones"); }

    @Override
    public CompletableFuture<ZoneCatalog> load(ResourceManager manager, Profiler profiler, Executor executor) {
        return CompletableFuture.supplyAsync(() -> {
            var zones = new ArrayList<ZoneDefinition>();
            var files = manager.findResources("zones", id -> id.getNamespace().equals(Cambocraft.ID)
                    && id.getPath().endsWith(".json"));
            if (files.size() != 12) throw new IllegalArgumentException("Douze fichiers de zone requis ; trouvé : " + files.size());
            files.forEach((id, resource) -> zones.add(readZone(id, resource)));
            return new ZoneCatalog(zones);
        }, executor);
    }

    @Override
    public CompletableFuture<Void> apply(ZoneCatalog data, ResourceManager manager, Profiler profiler, Executor executor) {
        // L'application d'un seul listener ne signifie pas encore que tout /reload a réussi.
        return CompletableFuture.runAsync(() -> {
            synchronized (staged) { staged.put(manager, data); }
        }, executor);
    }

    private static ZoneDefinition readZone(Identifier resourceId, Resource resource) {
        try (Reader reader = resource.getReader()) {
            char[] chars = new char[MAX_FILE_CHARS + 1];
            int count = 0;
            while (count < chars.length) {
                int n = reader.read(chars, count, chars.length - count);
                if (n < 0) break;
                count += n;
            }
            if (count > MAX_FILE_CHARS) throw new IllegalArgumentException("Fichier trop volumineux");
            JsonObject obj = JsonParser.parseString(new String(chars, 0, count)).getAsJsonObject();
            String id = text(obj, "id");
            String expected = resourceId.getNamespace() + ":" + resourceId.getPath().substring("zones/".length(), resourceId.getPath().length() - ".json".length());
            if (!id.equals(expected)) throw new IllegalArgumentException("ID différent du chemin : " + id);
            JsonElement previous = obj.get("previous_boss");
            if (previous == null) throw new IllegalArgumentException("Champ previous_boss absent");
            String previousBoss = previous.isJsonNull() ? null : text(obj, "previous_boss");
            JsonElement flag = obj.get("requires_rebirth");
            if (flag == null || !flag.isJsonPrimitive() || !flag.getAsJsonPrimitive().isBoolean()) {
                throw new IllegalArgumentException("requires_rebirth doit être un booléen");
            }
            return new ZoneDefinition(id, integer(obj, "order"), localized(obj, "name"), integer(obj, "minimum_level"),
                    text(obj, "boss"), localized(obj, "boss_name"), previousBoss, flag.getAsBoolean());
        } catch (IOException | IllegalArgumentException | IllegalStateException e) {
            throw new IllegalArgumentException("Zone invalide " + resourceId + " : " + e.getMessage(), e);
        }
    }

    private static String text(JsonObject obj, String field) {
        JsonElement value = obj.get(field);
        if (value == null || !value.isJsonPrimitive() || !value.getAsJsonPrimitive().isString()) {
            throw new IllegalArgumentException("Champ textuel absent : " + field);
        }
        return value.getAsString();
    }

    private static int integer(JsonObject obj, String field) {
        JsonElement value = obj.get(field);
        if (value == null || !value.isJsonPrimitive() || !value.getAsJsonPrimitive().isNumber()
                || !value.getAsString().matches("[0-9]{1,9}")) throw new IllegalArgumentException("Entier invalide : " + field);
        return Integer.parseInt(value.getAsString());
    }

    private static ZoneDefinition.LocalizedText localized(JsonObject obj, String field) {
        JsonObject value = obj.getAsJsonObject(field);
        if (value == null) throw new IllegalArgumentException("Traduction absente : " + field);
        return new ZoneDefinition.LocalizedText(text(value, "fr"), text(value, "en"));
    }
}

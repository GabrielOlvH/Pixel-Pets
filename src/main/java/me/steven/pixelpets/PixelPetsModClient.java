package me.steven.pixelpets;

import me.steven.pixelpets.items.PixelPetBakedModel;
import me.steven.pixelpets.pets.PixelPets;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.model.ModelLoadingRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.util.Identifier;

import java.util.Collection;

public class PixelPetsModClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ModelLoadingRegistry.INSTANCE.registerAppender((manager, consumer) -> {
            Collection<Identifier> pets = manager.findResources("models/item/pets", (r) -> r.endsWith(".json"));
            for (Identifier fileId : pets) {
                ModelIdentifier id = new ModelIdentifier(fileId.toString().replace("models/item/", "").replace(".json", ""), "inventory");
                consumer.accept(id);
                PixelPets.MODEL_IDENTIFIERS.add(id);
            }
        });
        ModelLoadingRegistry.INSTANCE.registerVariantProvider((manager) -> (modelIdentifier, modelProviderContext) -> {
            if (modelIdentifier.getNamespace().equals("pixelpets") && modelIdentifier.getPath().equals("pet"))
                return new PixelPetBakedModel();
            else return null;
        });
    }
}

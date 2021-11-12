package me.steven.pixelpets;

import me.steven.pixelpets.items.PixelPetBakedModel;
import me.steven.pixelpets.pets.PixelPets;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.model.ModelLoadingRegistry;
import net.fabricmc.fabric.api.object.builder.v1.client.model.FabricModelPredicateProviderRegistry;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.util.Identifier;

import java.util.Collection;

public class PixelPetsModClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ModelLoadingRegistry.INSTANCE.registerAppender((manager, consumer) -> {
            consumer.accept(new ModelIdentifier(new Identifier("pixelpets:pet_base"), "inventory"));
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

        FabricModelPredicateProviderRegistry.register(PixelPetsMod.EGG_ITEM, new Identifier(PixelPetsMod.MOD_ID, "hatching"), (stack, world, entity, a) -> {
            int hatching = stack.getOrCreateNbt().getInt("hatching");
            if (hatching < 50)
                return 0.3f;
            else if (hatching < 100)
                return 0.2f;
            else if (hatching < 230)
                return 0.1f;
            return 0.0f;
        });
    }
}

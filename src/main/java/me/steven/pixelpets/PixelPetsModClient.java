package me.steven.pixelpets;

import me.steven.pixelpets.items.PixelPetBakedModel;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.model.ModelLoadingRegistry;
import net.minecraft.client.MinecraftClient;

public class PixelPetsModClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ModelLoadingRegistry.INSTANCE.registerVariantProvider((manager) -> (modelIdentifier, modelProviderContext) -> {
            if (modelIdentifier.getNamespace().equals("pixelpets") && modelIdentifier.getPath().equals("pet")) return new PixelPetBakedModel();
            else return null;
        });
        System.out.println(MinecraftClient.getInstance().getResourceManager());
    }
}

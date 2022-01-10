package me.steven.pixelpets;

import me.steven.pixelpets.housing.HousingTooltipComponent;
import me.steven.pixelpets.housing.HousingTooltipData;
import me.steven.pixelpets.items.HousingBakedModel;
import me.steven.pixelpets.items.PixelPetBakedModel;
import me.steven.pixelpets.pets.PixelPets;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.model.ModelLoadingRegistry;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.TooltipComponentCallback;
import net.fabricmc.fabric.api.object.builder.v1.client.model.FabricModelPredicateProviderRegistry;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
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

            Collection<Identifier> housings = manager.findResources("models/item/housings", (r) -> r.endsWith(".json"));
            for (Identifier fileId : housings) {
                ModelIdentifier id = new ModelIdentifier(fileId.toString().replace("models/item/", "").replace(".json", ""), "inventory");
                consumer.accept(id);
            }
        });

        ModelLoadingRegistry.INSTANCE.registerVariantProvider((manager) -> (modelId, modelProviderContext) -> {
            if (!modelId.getNamespace().equals(PixelPetsMod.MOD_ID))
                return null;
            else if (modelId.getPath().equals("pet"))
                return new PixelPetBakedModel();
            else if (modelId.getPath().equals("housing"))
                return new HousingBakedModel();
            else
                return null;
        });

        FabricModelPredicateProviderRegistry.register(PixelPetsMod.EGG_ITEM, new Identifier(PixelPetsMod.MOD_ID, "hatching"), (stack, world, entity, a) -> {
            NbtCompound nbt = stack.getNbt();
            if (nbt == null || !nbt.contains("hatching")) return 0.3f;
            int hatching = nbt.getInt("hatching");
            if (hatching < 50)
                return 0.3f;
            else if (hatching < 100)
                return 0.2f;
            else if (hatching < 230)
                return 0.1f;
            return 0.0f;
        });

        TooltipComponentCallback.EVENT.register(data -> {
            if (data instanceof HousingTooltipData housingData)
                return new HousingTooltipComponent(housingData);
            else
                return null;
        });

        ClientPlayNetworking.registerGlobalReceiver(PixelPetsMod.SHOW_ITEM_PACKET, (client, handler, buf, responseSender) -> {
            ItemStack itemStack = buf.readItemStack();
            client.execute(() -> {
                client.gameRenderer.showFloatingItem(itemStack);
            });
        });
    }
}

package me.steven.pixelpets;

import me.steven.pixelpets.housing.HousingTooltipComponent;
import me.steven.pixelpets.housing.HousingTooltipData;
import me.steven.pixelpets.items.HousingBakedModel;
import me.steven.pixelpets.items.PixelPetBakedModel;
import me.steven.pixelpets.items.PixelPetEggItem;
import me.steven.pixelpets.pets.PixelPets;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.TooltipComponentCallback;
import net.fabricmc.fabric.api.object.builder.v1.client.model.FabricModelPredicateProviderRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.item.ModelPredicateProviderRegistry;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.item.EggItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

import java.util.Collection;

public class PixelPetsModClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ModelLoadingPlugin.register(ctx -> {
            ResourceManager manager = MinecraftClient.getInstance().getResourceManager();
            Collection<Identifier> pets = manager.findResources("models/item/pets", (r) -> r.toString().endsWith(".json")).keySet();
            ctx.addModels(new ModelIdentifier(new Identifier("pixelpets:pet_base"), "inventory"));
            for (Identifier fileId : pets) {
                ModelIdentifier id = new ModelIdentifier(new Identifier(fileId.toString().replace("models/item/", "").replace(".json", "")), "inventory");
                ctx.addModels(id);
                PixelPets.MODEL_IDENTIFIERS.add(id);
            }

            Collection<Identifier> housings = manager.findResources("models/item/housings", (r) -> r.toString().endsWith(".json")).keySet();
            for (Identifier fileId : housings) {
                ModelIdentifier id = new ModelIdentifier(new Identifier(fileId.toString().replace("models/item/", "").replace(".json", "")), "inventory");
                ctx.addModels(id);
            }


            ctx.modifyModelBeforeBake().register((model, context) -> {
                Identifier modelId = context.id();
                if (!modelId.getNamespace().equals(PixelPetsMod.MOD_ID))
                    return model;
                else if (modelId.getPath().equals("pet"))
                    return new PixelPetBakedModel();
                else if (modelId.getPath().equals("housing"))
                    return new HousingBakedModel();
                else
                    return model;
            });
        });

        ModelPredicateProviderRegistry.register(PixelPetsMod.OVERWORLD_EGG_ITEM, new Identifier(PixelPetsMod.MOD_ID, "hatching"), (stack, world, entity, a) -> {
            NbtCompound nbt = stack.getNbt();
            if (nbt == null || !nbt.contains(PixelPetEggItem.HATCH_TICKS)) return 0.0f;
            float hatching = nbt.getInt(PixelPetEggItem.HATCH_TICKS) / (float) PixelPetEggItem.TOTAL_HATCH_TIME;
            if (hatching < 0.1)
                return 0.3f;
            else if (hatching < 0.2)
                return 0.2f;
            else if (hatching < 0.3)
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

package me.steven.pixelpets.mixin;

import me.steven.pixelpets.pets.PixelPets;
import net.minecraft.client.render.model.BakedModelManager;
import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.client.render.model.UnbakedModel;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collection;

@Mixin(BakedModelManager.class)
public abstract class MixinBakedModelManager {
    private static final Logger LOGGER = LogManager.getLogger("Pet Sprite Callback");

    @Inject(method = "prepare", at = @At("RETURN"))
    private void a(ResourceManager resourceManager, Profiler profiler, CallbackInfoReturnable<ModelLoader> cir) {
        Collection<Identifier> pets = resourceManager.findResources("models/item/pets", (r) -> r.endsWith(".json"));
        LOGGER.info("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
        for (Identifier fileId : pets) {
            ModelIdentifier id = new ModelIdentifier(fileId.toString().replace("models/item/", "").replace(".json", ""), "inventory");
            PixelPets.MODELS_TO_BAKE.add(id);
            //ClientSpriteRegistryCallback.event(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE).register((c, a) -> a.register(id));
            LOGGER.info("Loaded " + id);
        }
    }

    /*@Shadow @Final private ReloadableResourceManager resourceManager;

    @Shadow @Final private BakedModelManager bakedModelManager;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void a(RunArgs args, CallbackInfo ci) {


        Collection<Identifier> pets = resourceManager.findResources("textures/pets", (r) -> r.endsWith(".png"));
        LOGGER.info("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
        for (Identifier fileId : pets) {
            Identifier id = new Identifier(fileId.toString().replace("textures/", "").replace(".png", ""));
            ClientSpriteRegistryCallback.event(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE).register((c, a) -> a.register(id));
            LOGGER.info("Loaded " + id);
        }
    }*/
}

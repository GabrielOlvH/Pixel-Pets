package me.steven.pixelpets.items;

import com.mojang.datafixers.util.Pair;
import me.steven.pixelpets.PixelPetsMod;
import me.steven.pixelpets.pets.PixelPet;
import me.steven.pixelpets.pets.PixelPets;
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;
import net.fabricmc.fabric.api.renderer.v1.mesh.MutableQuadView;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.fabricmc.fabric.impl.client.indigo.renderer.helper.GeometryHelper;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.model.*;
import net.minecraft.client.render.model.json.ModelOverrideList;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockRenderView;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class PixelPetBakedModel implements UnbakedModel, BakedModel, FabricBakedModel {

    public static ModelTransformation DEFAULT_TRANSFORM = null;

    private BakedModel model;
    private Map<Identifier, BakedModel> models = new HashMap<>();

    @Override
    public boolean isVanillaAdapter() {
        return false;
    }

    @Override
    public void emitBlockQuads(BlockRenderView blockRenderView, BlockState blockState, BlockPos blockPos, Supplier<Random> supplier, RenderContext renderContext) {

    }

    @Override
    public void emitItemQuads(ItemStack itemStack, Supplier<Random> supplier, RenderContext ctx) {
        PetData petData = PetData.fromTag(itemStack.getOrCreateTag());
        Identifier id = petData.getPet().getId();
        Identifier modelId = new Identifier(PixelPetsMod.MOD_ID, "pets/" + id.getPath() + "_" + petData.getVariant());
        BakedModel model = models.get(modelId);
        if (model != null)
            ctx.fallbackConsumer().accept(model);
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction face, Random random) {
        return Collections.emptyList();
    }

    @Override
    public boolean useAmbientOcclusion() {
        return false;
    }

    @Override
    public boolean hasDepth() {
        return false;
    }

    @Override
    public boolean isSideLit() {
        return false;
    }

    @Override
    public boolean isBuiltin() {
        return false;
    }

    @Override
    public Sprite getSprite() {
        return null;
    }

    @Override
    public ModelTransformation getTransformation() {
        if (DEFAULT_TRANSFORM == null) {
            DEFAULT_TRANSFORM = MinecraftClient.getInstance()
                    .getBakedModelManager()
                    .getModel(new ModelIdentifier(new Identifier("apple"), "inventory"))
                    .getTransformation();
        }
        return DEFAULT_TRANSFORM;
    }

    @Override
    public ModelOverrideList getOverrides() {
        return ModelOverrideList.EMPTY;
    }

    @Override
    public Collection<Identifier> getModelDependencies() {
        return Collections.emptyList();
    }

    @Override
    public Collection<SpriteIdentifier> getTextureDependencies(Function<Identifier, UnbakedModel> unbakedModelGetter, Set<Pair<String, String>> unresolvedTextureReferences) {
        return PixelPets.MODELS_TO_BAKE
                .stream()
                .flatMap(model -> unbakedModelGetter.apply(model).getTextureDependencies(unbakedModelGetter, unresolvedTextureReferences).stream())
                .collect(Collectors.toList());
    }

    @Nullable
    @Override
    public BakedModel bake(ModelLoader loader, Function<SpriteIdentifier, Sprite> textureGetter, ModelBakeSettings rotationContainer, Identifier modelId) {
        model = loader.getOrLoadModel(new ModelIdentifier(new Identifier(PixelPetsMod.MOD_ID, "pet_base"), "inventory")).bake(loader, textureGetter, rotationContainer, modelId);
        for (Identifier id : PixelPets.MODELS_TO_BAKE) {
            UnbakedModel unbakedModel = loader.getOrLoadModel(new Identifier(id.getNamespace(), "item/" + id.getPath()));
            BakedModel bakedModel = unbakedModel.bake(loader, textureGetter, rotationContainer, modelId);

            models.put(new Identifier(id.getNamespace(), id.getPath()), bakedModel);
        }
        return this;
    }
}

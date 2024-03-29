package me.steven.pixelpets.items;

import com.mojang.datafixers.util.Pair;
import me.steven.pixelpets.PixelPetsMod;
import me.steven.pixelpets.housing.Housing;
import me.steven.pixelpets.housing.HousingData;
import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.model.*;
import net.minecraft.client.render.model.json.ModelOverrideList;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockRenderView;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

public class HousingBakedModel implements UnbakedModel, BakedModel, FabricBakedModel {

    public static ModelTransformation DEFAULT_TRANSFORM = null;

    private final Map<Identifier, BakedModel> models = new HashMap<>();

    @Override
    public boolean isVanillaAdapter() {
        return false;
    }

    @Override
    public void emitBlockQuads(BlockRenderView blockRenderView, BlockState blockState, BlockPos blockPos, Supplier<Random> supplier, RenderContext renderContext) {
    }

    @Override
    public void emitItemQuads(ItemStack itemStack, Supplier<Random> supplier, RenderContext ctx) {
        HousingData housing = HousingData.fromTag(itemStack);
        Identifier id = housing.getId();
        Identifier modelId = new Identifier(PixelPetsMod.MOD_ID, "housings/" + id.getPath() + (housing.getStoredPets().isEmpty() ? "" : "_full"));
        BakedModel model = models.computeIfAbsent(modelId, (i) -> {
            ModelIdentifier modelIdentifier = new ModelIdentifier(new Identifier(modelId.getNamespace(), modelId.getPath()), "inventory");
            return MinecraftClient.getInstance().getBakedModelManager().getModel(modelIdentifier);
        });
        if (model != null)
            model.emitItemQuads(itemStack, supplier, ctx);

        if (itemStack.getNbt() != null && itemStack.getNbt().contains("BreedingProgress")) {
            Identifier m = new Identifier(PixelPetsMod.MOD_ID, "heart_anim");
            BakedModel loveModel = models.computeIfAbsent(m, (i) -> {
                ModelIdentifier modelIdentifier = new ModelIdentifier(new Identifier(m.getNamespace(), m.getPath()), "inventory");
                return MinecraftClient.getInstance().getBakedModelManager().getModel(modelIdentifier);
            });
            if (loveModel != null)
                loveModel.emitItemQuads(itemStack, supplier, ctx);
        }
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
    public Sprite getParticleSprite() {
        return null;
    }

    @Override
    public ModelTransformation getTransformation() {
        if (DEFAULT_TRANSFORM == null) {
            return MinecraftClient.getInstance()
                    .getBakedModelManager()
                    .getModel(new ModelIdentifier(new Identifier("pixelpets:pet_base"), "inventory"))
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
    public void setParents(Function<Identifier, UnbakedModel> modelLoader) {

    }

    @Nullable
    @Override
    public BakedModel bake(Baker baker, Function<SpriteIdentifier, Sprite> textureGetter, ModelBakeSettings rotationContainer, Identifier modelId) {
        return this;
    }
}
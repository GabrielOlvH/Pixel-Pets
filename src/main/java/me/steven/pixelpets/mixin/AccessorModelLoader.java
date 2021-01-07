package me.steven.pixelpets.mixin;

import com.google.common.collect.Maps;
import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.client.render.model.UnbakedModel;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.Map;

@Mixin(ModelLoader.class)
public interface AccessorModelLoader {
    @Accessor("unbakedModels")
    Map<Identifier, UnbakedModel> unbakedModels();
    @Accessor("modelsToBake")
    Map<Identifier, UnbakedModel> modelsToBake();
}

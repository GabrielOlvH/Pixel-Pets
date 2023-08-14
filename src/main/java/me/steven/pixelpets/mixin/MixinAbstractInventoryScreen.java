package me.steven.pixelpets.mixin;

import me.steven.pixelpets.abilities.Abilities;
import me.steven.pixelpets.extensions.PixelPetsDataHolder;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.AbstractInventoryScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffectUtil;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Iterator;

@Mixin(AbstractInventoryScreen.class)
public abstract class MixinAbstractInventoryScreen<T extends ScreenHandler> extends HandledScreen<T>{

    public MixinAbstractInventoryScreen(T handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
    }

    @Inject(method = "drawStatusEffectDescriptions", at = @At("INVOKE"))
    private void pixelPets_drawPetName(DrawContext context, int x, int height, Iterable<StatusEffectInstance> iterable, CallbackInfo ci) {
        int k = this.y;

        for(Iterator<StatusEffectInstance> it = iterable.iterator(); it.hasNext(); k += height) {
            StatusEffectInstance statusEffectInstance = it.next();
            PixelPetsDataHolder ext = (PixelPetsDataHolder) statusEffectInstance;
            if (ext.getPetData() != null) {
                MutableText text = Text.translatable(Abilities.REGISTRY.get(ext.getPetData().getAbilityId()).getTranslationKey()).formatted(Formatting.DARK_PURPLE);
             //   if (!statusEffectInstance.isPermanent()) //TODO
               //     text.append(" (").append(StatusEffectUtil.getDurationText(statusEffectInstance, 1.0F)).append(")");
                context.drawText(textRenderer, text, x + 28, k + 18, -1, false);
            }
        }
    }

    @Redirect(method = "drawStatusEffectDescriptions", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/effect/StatusEffectUtil;getDurationText(Lnet/minecraft/entity/effect/StatusEffectInstance;F)Lnet/minecraft/text/Text;"))
    private Text pixelPets_ignoreDescription(StatusEffectInstance effect, float multiplier) {
        return Text.empty();
    }
}

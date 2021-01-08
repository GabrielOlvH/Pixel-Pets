package me.steven.pixelpets.mixin;

import me.steven.pixelpets.player.PixelPetsDataHolder;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.AbstractInventoryScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;
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
    private void pixelPets_drawPetName(MatrixStack matrixStack, int x, int yOffset, Iterable<StatusEffectInstance> iterable, CallbackInfo ci) {
        int k = this.y;

        for(Iterator<StatusEffectInstance> it = iterable.iterator(); it.hasNext(); k += yOffset) {
            StatusEffectInstance statusEffectInstance = it.next();
            PixelPetsDataHolder ext = (PixelPetsDataHolder) statusEffectInstance;
            if (ext.getPetData() != null) {
                MinecraftClient.getInstance().textRenderer.draw(matrixStack, ext.getPetData().toText(), x + 28, k + 18, -1);
            }
        }
    }

    @Redirect(method = "drawStatusEffectDescriptions", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/effect/StatusEffectUtil;durationToString(Lnet/minecraft/entity/effect/StatusEffectInstance;F)Ljava/lang/String;"))
    private String pixelPets_ignoreDescription(StatusEffectInstance effect, float multiplier) {
        return "";
    }
}

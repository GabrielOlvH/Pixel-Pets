package me.steven.pixelpets.commands;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.steven.pixelpets.PixelPetsMod;
import me.steven.pixelpets.abilities.Abilities;
import me.steven.pixelpets.abilities.Ability;
import me.steven.pixelpets.commands.arguments.AbilityArgumentType;
import me.steven.pixelpets.items.PixelPetItem;
import me.steven.pixelpets.pets.PetData;
import net.fabricmc.fabric.api.command.v2.ArgumentTypeRegistry;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.argument.IdentifierArgumentType;
import net.minecraft.command.argument.serialize.ConstantArgumentSerializer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;

public class PixelPetsCommands {
    public static void register() {
        ArgumentTypeRegistry.registerArgumentType(new Identifier(PixelPetsMod.MOD_ID, "ability"), AbilityArgumentType.class, ConstantArgumentSerializer.of(IdentifierArgumentType::identifier));
        CommandRegistrationCallback.EVENT.register((commandDispatcher, b, a) ->
                commandDispatcher.register(CommandManager.literal("teachability")
                        .requires(src -> src.hasPermissionLevel(2))
                        .then(CommandManager.argument("abilityId", AbilityArgumentType.ability())
                                .suggests(AbilityArgumentType.ABILITIES)
                                .executes((ctx) -> setAbility(ctx, AbilityArgumentType.getId(ctx, "abilityId"))))));
    }

    private static int setAbility(CommandContext<ServerCommandSource> ctx, Identifier abilityId) throws CommandSyntaxException {
        ServerPlayerEntity player = ctx.getSource().getPlayer();
        ItemStack stack = player.getMainHandStack();
        Item item = stack.getItem();
        if (item instanceof PixelPetItem) {
            PetData petData = PetData.fromTag(stack);
            Ability ability = Abilities.REGISTRY.get(abilityId);
            petData.setAbilityId(abilityId);
            petData.update(stack);
            ctx.getSource().sendFeedback(() -> Text.literal("").append(stack.getName()).append(" has learned ").append(Text.translatable(Util.createTranslationKey("ability", abilityId))), false);
        } else {
            ctx.getSource().sendError(Text.literal("You are not holding a pet"));
        }
        return 1;
    }
}

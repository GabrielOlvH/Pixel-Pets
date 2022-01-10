package me.steven.pixelpets.commands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.steven.pixelpets.abilities.Abilities;
import me.steven.pixelpets.abilities.Ability;
import me.steven.pixelpets.commands.arguments.AbilityArgumentType;
import me.steven.pixelpets.items.PixelPetItem;
import me.steven.pixelpets.pets.PetData;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;

public class PixelPetsCommands {
    public static void register() {
        CommandRegistrationCallback.EVENT.register((commandDispatcher, b) ->
                commandDispatcher.register(CommandManager.literal("teachability")
                        .requires(src -> src.hasPermissionLevel(2))
                        .then(CommandManager.argument("abilityId", AbilityArgumentType.ability())
                                .suggests(AbilityArgumentType.ABILITIES)
                                .executes((ctx) -> addAbility(ctx, AbilityArgumentType.getId(ctx, "abilityId"), 0))
                                .then(CommandManager.argument("level", IntegerArgumentType.integer(0, 5))
                                .executes((ctx) -> addAbility(ctx, AbilityArgumentType.getId(ctx, "abilityId"), IntegerArgumentType.getInteger(ctx, "level")))))));
    }

    private static int addAbility(CommandContext<ServerCommandSource> ctx, Identifier abilityId, int level) throws CommandSyntaxException {
        ServerPlayerEntity player = ctx.getSource().getPlayer();
        ItemStack stack = player.getMainHandStack();
        Item item = stack.getItem();
        if (item instanceof PixelPetItem) {
            PetData petData = PetData.fromTag(stack);
            Ability ability = Abilities.REGISTRY.get(abilityId);
            if (ability.actions().length <= level) {
                ctx.getSource().sendError(new LiteralText("Max level is " + ability.actions().length));
                return 1;
            }

            petData.getAbilities().put(abilityId, level);
            stack.setSubNbt("PetData", petData.toTag());
            ctx.getSource().sendFeedback(new LiteralText("").append(stack.getName()).append(" has learned ").append(new TranslatableText(Util.createTranslationKey("ability", abilityId))), false);
        } else {
            ctx.getSource().sendError(new LiteralText("You are not holding a pet"));
        }
        return 1;
    }
}

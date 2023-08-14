package me.steven.pixelpets.commands.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import me.steven.pixelpets.PixelPetsMod;
import me.steven.pixelpets.abilities.Abilities;
import net.minecraft.command.CommandSource;
import net.minecraft.command.suggestion.SuggestionProviders;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;

import java.util.Arrays;
import java.util.Collection;

public class AbilityArgumentType implements ArgumentType<Identifier> {

    public static final SuggestionProvider<ServerCommandSource> ABILITIES = SuggestionProviders.register(new Identifier(PixelPetsMod.MOD_ID, "abilities"), (context, builder) -> CommandSource.suggestFromIdentifier(Abilities.REGISTRY.values(), builder, Abilities.REGISTRY.inverse()::get, (ability) -> Text.literal(Util.createTranslationKey("ability", Abilities.REGISTRY.inverse().get(ability)))));

    private static final Collection<String> EXAMPLES = Arrays.asList("minecraft:pig", "cow");
    public static final DynamicCommandExceptionType NOT_FOUND_EXCEPTION = new DynamicCommandExceptionType((id) -> Text.translatable("entity.notFound", id));

    public AbilityArgumentType() {
    }

    public static AbilityArgumentType ability() {
        return new AbilityArgumentType();
    }

    public static Identifier getId(CommandContext<ServerCommandSource> context, String name) throws CommandSyntaxException {
        return validate(context.getArgument(name, Identifier.class));
    }

    private static Identifier validate(Identifier id) throws CommandSyntaxException {
        if (!Abilities.REGISTRY.containsKey(id))
            throw NOT_FOUND_EXCEPTION.create(id);
        else
            return id;
    }

    public Identifier parse(StringReader stringReader) throws CommandSyntaxException {
        return validate(Identifier.fromCommandInput(stringReader));
    }

    public Collection<String> getExamples() {
        return EXAMPLES;
    }
}

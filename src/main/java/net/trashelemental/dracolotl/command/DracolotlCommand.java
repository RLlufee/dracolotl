package net.trashelemental.dracolotl.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.trashelemental.dracolotl.config.DracolotlConfig;

/**
 * Dracolotl 管理指令注册。
 * 提供 /dracolotl reload 指令以在游戏或控制台内热重载配置。
 */
public class DracolotlCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("dracolotl")
            .requires(source -> source.hasPermission(2))
            .then(Commands.literal("reload")
                .executes(DracolotlCommand::executeReload))
        );
    }

    private static int executeReload(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        try {
            DracolotlConfig.load();
            DracolotlConfig.applyToAllEntities(source.getServer());
            source.sendSuccess(() -> Component.translatable("commands.dracolotl.reload.success"), true);
            return 1;
        } catch (Exception e) {
            source.sendFailure(Component.translatable("commands.dracolotl.reload.failure", e.getMessage()));
            return 0;
        }
    }
}

package br.com.syrxcraft.betterskyblock.commands.command.subcommand.user;

import br.com.syrxcraft.betterskyblock.PermissionNodes;
import br.com.syrxcraft.betterskyblock.commands.CommandManager;
import br.com.syrxcraft.betterskyblock.commands.manager.HasSubCommand;
import br.com.syrxcraft.betterskyblock.commands.manager.ISubCommand;
import br.com.syrxcraft.betterskyblock.commands.manager.cSubCommand;
import br.com.syrxcraft.betterskyblock.core.islands.Island;
import br.com.syrxcraft.betterskyblock.core.permission.PermissionType;
import br.com.syrxcraft.betterskyblock.core.permission.PermissionsUtils;
import br.com.syrxcraft.betterskyblock.utils.FancyText;
import br.com.syrxcraft.betterskyblock.utils.IslandUtils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;

@HasSubCommand
public class SubCmdMember implements ISubCommand {

    @cSubCommand(name = "member", targetCommand = "island")
    public boolean execute(CommandSender commandSender, String command, String label, String[] args) {

        if(!(commandSender instanceof Player))
            return false;

        if (!commandSender.hasPermission(PermissionNodes.COMMAND_MEMBER)) {
            return CommandManager.noPermission(commandSender);
        }

        Player player = (Player) commandSender;

        Island island = IslandUtils.getPlayerIsland(player);

        if (island == null) {
            commandSender.sendMessage("§4§l ▶ §cVocê ainda não possui uma ilha nesse servidor! Para criar uma, use \"/" + command + " spawn\"");
            return false;
        }

        player.sendMessage("§6§m------------§6(  §e§lIlha de " + player.getName() + "§e  §6)§m------------");
        FancyText.sendTo(player, new FancyText("§6§l ▶ §aMembros:"));

        Map<UUID, PermissionType> map = island.permissionHolder.getPermissions();

        map.forEach((uuid, type) -> {

            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);

            if(offlinePlayer != null){
                FancyText.sendTo(player, new FancyText("   §e⦁ " + offlinePlayer.getName() + " - " + PermissionsUtils.getPrettyType(type)));
            }

        });

        return true;
    }
}
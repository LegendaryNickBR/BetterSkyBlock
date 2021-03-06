package br.com.syrxcraft.betterskyblock.commands.command.subcommand.user;

import br.com.syrxcraft.betterskyblock.BetterSkyBlock;
import br.com.syrxcraft.betterskyblock.PermissionNodes;
import br.com.syrxcraft.betterskyblock.commands.CommandManager;
import br.com.syrxcraft.betterskyblock.commands.manager.cSubCommand;
import br.com.syrxcraft.betterskyblock.commands.manager.HasSubCommand;
import br.com.syrxcraft.betterskyblock.commands.manager.ISubCommand;
import br.com.syrxcraft.betterskyblock.core.api.BetterSkyBlockAPI;
import br.com.syrxcraft.betterskyblock.core.islands.Island;
import br.com.syrxcraft.betterskyblock.core.permission.PermissionsUtils;
import br.com.syrxcraft.betterskyblock.core.tasks.SpawnTeleportTask;
import br.com.syrxcraft.betterskyblock.utils.GriefDefenderUtils;
import com.griefdefender.api.GriefDefender;
import com.griefdefender.api.data.PlayerData;
import com.griefdefender.api.permission.flag.Flags;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

@HasSubCommand
public class SubCmdSpawn implements ISubCommand {

    @cSubCommand(name = "spawn", targetCommand = "island")
    public boolean execute(CommandSender commandSender, String command, String label, String[] args) {

        if (!(commandSender instanceof Player)) {
            return false;
        }

        Player player = (Player)commandSender;

        if(!player.hasPermission(PermissionNodes.COMMAND_SPAWN)){
            return CommandManager.noPermission(player);
        }

        Island island;

        if (args.length >= 1 && args[0] != null && !args[0].isEmpty()){

            UUID argPlayer = Bukkit.getPlayerUniqueId(args[0]);
            PlayerData playerData = GriefDefender.getCore().getPlayerData(BetterSkyBlock.getInstance().getIslandWorld().getUID(), argPlayer).orElse(null);

            if (playerData == null || argPlayer == null){
                player.sendMessage("§4§l ▶ §cNão existem nenhum jogador chamado [" + args[0] + "] !");
                return false;
            }

            island = BetterSkyBlock.getInstance().getDataStore().getIsland(argPlayer);

            if (island == null) {
                player.sendMessage("§4§l ▶ §e" + args[0] + "§c não possui uma ilha nesse servidor!");
                return false;
            }

            if(!BetterSkyBlockAPI.getInstance().isIslandPublic(island) && !PermissionsUtils.canEnter(island.getPermissionHolder().getEffectivePermission(player)) && !player.hasPermission(PermissionNodes.OPTIONS_CAN_ENTER)){
                player.sendMessage("§4§l ▶ §c Você não tem permissão para entrar nessa ilha!");
                return false;
            }

        }else {

            island = BetterSkyBlock.getInstance().getDataStore().getIsland(player.getUniqueId());

            if (island == null) {

                try {
                    island = BetterSkyBlock.getInstance().getDataStore().createIsland(player.getUniqueId());
                } catch (Exception e) {
                    player.sendMessage(ChatColor.RED + "Um erro ocorreu ao gerar sua ilha: " + e.getMessage());
                    BetterSkyBlock.getInstance().getLoggerHelper().error(e.getMessage());
                    return false;
                }

            }

        }

        if (!island.ready) {
            player.sendMessage("§4§l ▶ §cExiste alguma operação pendente nessa ilha!");
            return false;
        }

        if (player.hasPermission(PermissionNodes.OPTIONS_NO_TELEPORT_DELAY)){
            player.sendMessage("§3§l ▶ §aVocê foi teleportado para a ilha!");
            SpawnTeleportTask.teleportTask(player, island, 0);
        }else {
            player.sendMessage("§3§l ▶ §aVocê será teleportado em " + BetterSkyBlock.getInstance().config().getTpCountdown() + " segundos!");
            SpawnTeleportTask.teleportTask(player, island, BetterSkyBlock.getInstance().config().getTpCountdown());
        }

        return true;
    }
}

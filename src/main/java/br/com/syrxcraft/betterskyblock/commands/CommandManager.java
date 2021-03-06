package br.com.syrxcraft.betterskyblock.commands;

import br.com.syrxcraft.betterskyblock.BetterSkyBlock;
import br.com.syrxcraft.betterskyblock.commands.manager.*;
import br.com.syrxcraft.betterskyblock.utils.Reflections;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;

public class CommandManager {

    private final Reflections reflections = Reflections.builder()
            .setClassLoader(BetterSkyBlock.class.getClassLoader())
            .setPackage("br.com.syrxcraft.betterskyblock.commands")
            .build();

    private final LinkedHashMap<cCommand,ICommand> commands = new LinkedHashMap<>();
    private final LinkedHashMap<cSubCommand, ISubCommand> subCommands = new LinkedHashMap<>();
    private final LinkedHashMap<String,String> confirmations = new LinkedHashMap<>();


    public CommandManager load(){

        HashSet<Class<?>> classes = reflections.scan();


        BetterSkyBlock.getInstance().getLoggerHelper().info("[CommandManager] » Looking for commands...");

        for(Class<?> clazz : classes){

            if(clazz.isAnnotationPresent(cCommand.class) && ICommand.class.isAssignableFrom(clazz)){
                try{

                    Object data = clazz.newInstance();

                    cCommand cCommand = data.getClass().getAnnotation(br.com.syrxcraft.betterskyblock.commands.manager.cCommand.class);
                    ICommand iCommand = (ICommand) data;

                    commands.put(cCommand, iCommand);

                } catch (IllegalAccessException | InstantiationException e) {
                    BetterSkyBlock.getInstance().getLoggerHelper().error("[CommandManager] » Cannot pass " + clazz.getName() + " as command.");
                }
            }
        }

        BetterSkyBlock.getInstance().getLoggerHelper().info("[CommandManager] » Found " + commands.size() + " commands.");

        if(!commands.isEmpty()){

            BetterSkyBlock.getInstance().getLoggerHelper().info("[CommandManager] » Looking for sub commands...");

            for(Class<?> clazz : classes){

                if(clazz.isAnnotationPresent(HasSubCommand.class) && ISubCommand.class.isAssignableFrom(clazz)){
                    try{

                        Object data = clazz.newInstance();
                        cSubCommand cSubCommand = null;

                        for(Method method : data.getClass().getDeclaredMethods()){
                            if(method.isAnnotationPresent(br.com.syrxcraft.betterskyblock.commands.manager.cSubCommand.class)){
                                cSubCommand = method.getDeclaredAnnotation(br.com.syrxcraft.betterskyblock.commands.manager.cSubCommand.class);
                            }
                        }

                        ISubCommand iSubCommand = (ISubCommand) data;

                        if (cSubCommand != null && existsCommand(cSubCommand.targetCommand())) {
                            subCommands.put(cSubCommand, iSubCommand);
                        }

                    } catch (IllegalAccessException | InstantiationException e) {
                        BetterSkyBlock.getInstance().getLoggerHelper().error("[CommandManager] » Cannot pass " + clazz.getName() + " as command.");
                    }
                }
            }

            BetterSkyBlock.getInstance().getLoggerHelper().info("[CommandManager] » Found " + subCommands.size() + " sub commands.");

        }

        registerCommands();


        return this;

    }

    public void registerCommands() {

        try{
            Constructor<PluginCommand> pluginCommandConstructor = PluginCommand.class.getDeclaredConstructor(String.class, Plugin.class);
            pluginCommandConstructor.setAccessible(true);

            for(cCommand command : commands.keySet()){

                PluginCommand pluginCommand = pluginCommandConstructor.newInstance(command.label(), BetterSkyBlock.getInstance());

                pluginCommand.setExecutor(commands.get(command));
                pluginCommand.setAliases(Arrays.asList(command.aliases()));
                pluginCommand.setLabel(command.label());

                registerOnBukkit(pluginCommand);

            }

            if(!commands.isEmpty()){
                BetterSkyBlock.getInstance().getLoggerHelper().info("[CommandManager] » Commands registered.");
            }

        }catch (Exception e){
            BetterSkyBlock.getInstance().getLoggerHelper().info("[CommandHandler] » Failed to register commands.");
        }
    }

    public void registerOnBukkit(PluginCommand command){
        try{

            Field commandMapField = Bukkit.getPluginManager().getClass().getDeclaredField("commandMap");
            commandMapField.setAccessible(true);

            CommandMap commandMap = (CommandMap) commandMapField.get(Bukkit.getPluginManager());

            commandMap.register(command.getPlugin().getName().toLowerCase(), command);

        }catch (Exception e){
            BetterSkyBlock.getInstance().getLoggerHelper().info("[CommandHandler] » Failed to register commands on bukkit. Command name:  " + command.getName() + ", command class: " + command.getClass().getName() + ".");
        }
    }

    public boolean existsCommand(String command){
        return getCommand(command) != null;
    }

    public cCommand getCommand(String command){

        if(command != null){
            for(br.com.syrxcraft.betterskyblock.commands.manager.cCommand cCommand : commands.keySet()){

                if(cCommand.label().equalsIgnoreCase(command)) return cCommand;

                for(String aliases : cCommand.aliases()){
                    if(aliases.equalsIgnoreCase(command)) return cCommand;
                }

            }
        }

        return null;
    }


    public boolean deliverSubCommand(CommandSender commandSender,String command, String label, String[] args){

        for(cSubCommand subCommand : subCommands.keySet()){
            if(subCommand.targetCommand().equalsIgnoreCase(command)){
                if(subCommand.name().equalsIgnoreCase(label)){
                    ISubCommand iSubCommand = subCommands.get(subCommand);

                    if(iSubCommand != null){

                        String[] args_;

                        if(args.length < 2){
                            args_ = new String[]{};
                        }else{
                            args_ = Arrays.copyOfRange(args, 1, args.length);
                        }

                        return iSubCommand.execute(commandSender, command, label, args_);
                    }
                }
            }
        }

        return false;
    }

    public LinkedHashMap<String, String> getConfirmations() {
        return confirmations;
    }

    public static boolean noPermission(CommandSender sender){
        sender.sendMessage("§cYou don't have permission to execute this action.");
        return true;
    }

}

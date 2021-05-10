package de.militaermiltz.tdv.commands;

import com.sun.istack.internal.Nullable;
import de.militaermiltz.tdv.util.NumberUtil;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.CommandBlock;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 *
 * @author Alexander Ley
 * @version 1.0
 *
 * This class defines /modifyplaysound command
 *
 */
public class ModifyPlaysoundCommand implements CommandExecutor, TabCompleter {

    public static final List<String> FILTER = new ArrayList<>();

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!isComplete(sender, args)) return false;

        final World world = (args.length == 9) ? Bukkit.getServer().getWorld(args[8]) : CommandUtil.getWorldFromSender(sender, args);
        final Location[] fromTo = CommandUtil.transformLocation(new Location(world, Integer.parseInt(args[0]), Integer.parseInt(args[1]), Integer.parseInt(args[2])),
                                                    new Location(world, Integer.parseInt(args[3]), Integer.parseInt(args[4]), Integer.parseInt(args[5])));

        //Count modified command Blocks
        int modified = 0;

        for (int x = fromTo[0].getBlockX(); x <= fromTo[1].getBlockX(); x++) {
            for (int y = fromTo[0].getBlockY(); y <= fromTo[1].getBlockY(); y++) {
                for (int z = fromTo[0].getBlockZ(); z <= fromTo[1].getBlockZ(); z++) {
                    assert world != null;
                    final Block block = world.getBlockAt(x, y, z);

                    if (block.getState() instanceof CommandBlock) {
                        //Get Command String
                        final String cmd = ((CommandBlock) block.getState()).getCommand();

                        //Split Command into arguments
                        List<String> cmdArgs = Arrays.asList(cmd.split(" "));

                        final PlaysoundArguments playsoundArguments = PlaysoundArguments.getFromString(args[6]);
                        if (playsoundArguments == null) return false;

                        final int argIndex = playsoundArguments.getArgIndex();

                        //Make compatible with execute
                        StringBuilder executePrepend = new StringBuilder();
                        if (cmdArgs.get(0).equals("execute") || cmdArgs.get(0).equals("/execute")){

                            if (!cmdArgs.contains("run")) {
                                sender.sendMessage(ChatColor.RED + "Execute command is not complete. Missing \"run\" argument.");
                                return true;
                            }

                            cmdArgs = new ArrayList<>(cmdArgs);

                            //Removes execute command from cmdArgs and store it into executePrepend
                            for (int i = 0; i < cmdArgs.size(); ){
                                //Append until "run"
                                if (cmdArgs.get(i).equals("run")) {
                                    executePrepend.append(cmdArgs.get(i)).append(" ");
                                    cmdArgs.remove(i);
                                    break;
                                }
                                executePrepend.append(cmdArgs.get(i)).append(" ");
                                cmdArgs.remove(i);
                            }
                        }

                        //Removes temporarily / (only from playsound commands) and saves that / was removed
                        boolean haveSlash = false;
                        if (cmdArgs.get(0).charAt(0) == '/') {
                            cmdArgs.set(0, cmdArgs.get(0).replaceFirst("/", ""));
                            haveSlash = true;
                        }

                        if (cmdArgs.get(0).equals("playsound") && cmdArgs.size() > argIndex) {
                            //Modify the command
                            if (playsoundArguments == PlaysoundArguments.POS){
                                cmdArgs = new ArrayList<>(cmdArgs);
                                cmdArgs.set(4, args[7]);
                                cmdArgs.set(5, args[8]);
                                cmdArgs.set(6, args[9]);
                            }
                            else {
                                cmdArgs.set(argIndex, args[7]);
                            }


                            StringBuilder builder = new StringBuilder();
                            //Prepend execute command
                            builder.append(executePrepend.toString());
                            //Puts "/" back to the front
                            if (haveSlash) builder.append("/");

                            //Reconstructs the playsound command
                            for (int i = 0; i < cmdArgs.size(); i++) {
                                builder.append(cmdArgs.get(i));
                                if (i != cmdArgs.size() - 1) builder.append(" ");
                            }

                            //Sets new command to command Block.
                            CommandUtil.setCMDinBlock(block, builder.toString());
                            modified++;
                        }
                    }
                }
            }
        }
        if (modified == 0) sender.sendMessage(ChatColor.RED + "No commands to modify.");
        else sender.sendMessage("" + ChatColor.RED + modified + ChatColor.GOLD + " command(s) are modified.");

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> list = new ArrayList<>();

        Location playerLookLocation = null;
        if (args.length <= 6 && sender instanceof Player) playerLookLocation = CommandUtil.getPlayerLookBlockPos((Player) sender);

        switch (args.length){
            case 1:
            case 4:
                list.add("" + ((playerLookLocation != null) ? playerLookLocation.getBlockX() : 0));
                break;
            case 2:
            case 5:
                list.add("" + ((playerLookLocation != null) ? playerLookLocation.getBlockY() : 0));
                break;
            case 3:
            case 6:
                list.add("" + ((playerLookLocation != null) ? playerLookLocation.getBlockZ() : 0));
                break;
            case 7:
                if (FILTER.isEmpty()) PlaysoundArguments.init();
                list.addAll(FILTER);
                break;
            case 8:
                final PlaysoundArguments playsoundArguments = PlaysoundArguments.getFromString(args[6]);
                if (playsoundArguments != null) list.addAll(playsoundArguments.getTabList(sender));
                break;
            case 9:
            case 10:
                if (PlaysoundArguments.getFromString(args[6]) != PlaysoundArguments.POS) {
                    list.addAll(Bukkit.getServer().getWorlds().stream().map(World::getName).collect(Collectors.toList()));
                }
                break;
            case 11:
                if (PlaysoundArguments.getFromString(args[6]) == PlaysoundArguments.POS) {
                    list.addAll(Bukkit.getServer().getWorlds().stream().map(World::getName).collect(Collectors.toList()));
                }
                break;
        }
        return list;
    }

    /**
     * @return Returns if all components are given to execute the command.
     */
    private boolean isComplete(CommandSender sender, String[] args){
        if ((sender instanceof Player || sender instanceof BlockCommandSender) && args.length < 8) return false;
        else if (sender instanceof ConsoleCommandSender &&  args.length < 9) return false;

        return isCorrect(args);
    }

    /**
     * @return Returns if the command syntax is correct.
     */
    private boolean isCorrect(String[] args){
        if (args.length == 0) return true;
        if (args.length > 7){
            if ((args[6].equals("-pos") && args.length > 11) || (!args[6].equals("-pos") && args.length > 9)) return false;
        }

        boolean isPos = false;

        for (int i = 0; i < args.length; i++){
            final String argument = args[i];
            if (argument.trim().equals("")) continue;

            switch (i){
                case 0:
                case 1:
                case 2:
                case 3:
                case 4:
                case 5:
                    if (!NumberUtil.isInteger(argument)) return false;
                    break;
                case 6:
                    if (!FILTER.contains(argument)) return false;
                    break;
                case 7:
                    final PlaysoundArguments playsoundArguments = PlaysoundArguments.getFromString(args[6]);
                    if (playsoundArguments == PlaysoundArguments.POS) isPos = true;
                    if (playsoundArguments == null || !playsoundArguments.isCorrect(argument)) return false;
                    break;
                case 8:
                case 9:
                case 10:
                    if (isPos) break;
                    if (Bukkit.getServer().getWorlds().stream().map(World::getName).noneMatch(s -> s.equals(args[8]))) return false;
                    break;
                case 11:
                    if (isPos){
                        if (Bukkit.getServer().getWorlds().stream().map(World::getName).noneMatch(s -> s.equals(args[8]))) return false;
                    }
                    break;
                default:
                    return false;
            }
        }
        return true;
    }

    /**
     * @return Returns if sel is a selector.
     */
    private static boolean isSelector(String sel){
        if (sel.trim().equals("")) return false;
        try {
            if (sel.charAt(0) != '@') return false;

            final String mode = "" + sel.charAt(0) + sel.charAt(1);
            if (!(mode.equals("@a") || mode.equals("@p") || mode.equals("@s"))) return false;

            return sel.charAt(2) == '[' && sel.charAt(sel.length() - 1) == ']';
        }
        catch (IndexOutOfBoundsException ex){
            return true;
        }
    }


    private enum PlaysoundArguments {
        SOUND(1, Arrays.stream(Sound.values())
                      .map(sound -> sound.getKey().toString())
                      .collect(Collectors.toList()),
              s -> true, "-sound", "-sou"),
        CATEGORY(2, Arrays.stream(SoundCategory.values())
                         .map(soundCategory -> soundCategory.toString().toLowerCase())
                         .collect(Collectors.toList()),
                 s -> Arrays.stream(SoundCategory.values())
                    .map(soundCategory -> soundCategory.toString().toLowerCase())
                    .anyMatch(s1 -> s1.equals(s)),
                 "-category", "-cat"),
        SELECTOR(3, Arrays.asList("@a", "@p", "@s", "@a[distance=..10]"), ModifyPlaysoundCommand::isSelector, "-selector", "-sel"),
        POS(4, new ArrayList<>(Collections.singleton("~ ~ ~")), s -> {
            final Scanner scanner = new Scanner(s);

            for (int i = 0; scanner.hasNext(); i++){
                if (i >= 3) return false;
                final String tmp = scanner.next();
                if (!NumberUtil.isInteger(tmp) && !tmp.equals("~")) return false;
            }
            return true;
        }, "-pos"),
        VOLUME(7, Collections.emptyList(), NumberUtil::isNumber, "-volume", "-v"),
        PITCH(8, Collections.emptyList(), NumberUtil::isNumber,"-pitch", "-p"),
        MIN_VOLUME(9, Collections.emptyList(), NumberUtil::isNumber,"-minVolume", "-minv");

        private final String[] alias;
        private final Predicate<String> correctPredicate;
        private final List<String> tabList;
        private final int argIndex;

        /**
         * @param argIndex Position in playsound command.
         * @param tabList Suggestion List.
         * @param correctPredicate Predicate testing if given argument is correct.
         * @param alias Available Filter Strings.
         */
        PlaysoundArguments(int argIndex, List<String> tabList, Predicate<String> correctPredicate, String... alias) {
            this.alias = alias;
            this.correctPredicate = correctPredicate;
            this.tabList = tabList;
            this.argIndex = argIndex;
            FILTER.addAll(Arrays.asList(alias));
        }

        //This method loads all information in this enum.
        public static void init(){

        }

        /**
         * @return Returns PlaysoundArgument from String. If no PlaysoundArgument is found it return null.
         */
        public static @Nullable PlaysoundArguments getFromString(String str){
            final List<PlaysoundArguments> list = Arrays.stream(values())
                    .filter(playsoundArguments -> playsoundArguments.isInAlias(str))
                    .collect(Collectors.toList());
            //assert list.size() == 1;
            if (list.isEmpty()) return null;
            return list.get(0);
        }

        public int getArgIndex() {
            return argIndex;
        }

        public boolean isCorrect(String arg){
            return correctPredicate.test(arg);
        }

        public boolean isInAlias(String str){
            return Arrays.asList(alias).contains(str);
        }

        public List<String> getTabList(CommandSender sender){
            if (this != POS) return tabList;

            final Location location = (sender instanceof Player) ? CommandUtil.getPlayerLookBlockPos((Player) sender) :
                    (sender instanceof BlockCommandSender) ? ((BlockCommandSender) sender).getBlock().getLocation() : null;

            tabList.add("" + ((location == null) ? 0 : location.getBlockX()) +
                                " " + ((location == null) ? 0 : location.getBlockY()) +
                                " " + ((location == null) ? 0 : location.getBlockZ()));

            return tabList;
        }
    }
}

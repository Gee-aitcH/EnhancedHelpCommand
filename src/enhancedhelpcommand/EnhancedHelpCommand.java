package enhancedhelpcommand;

import arc.Events;
import arc.math.Mathf;
import arc.struct.Seq;
import arc.util.CommandHandler;
import arc.util.Strings;
import mindustry.game.EventType;
import mindustry.gen.Player;
import pluginutil.GHPlugin;

import java.util.Arrays;
import java.util.HashSet;

import static pluginutil.PluginUtil.SendMode.info;
import static pluginutil.PluginUtil.f;

@SuppressWarnings("unused never written")
public class EnhancedHelpCommand extends GHPlugin {

    private HashSet<String> adminCommandsSet;

    public EnhancedHelpCommand() {
        defConfig();
    }

    public void init(){
        super.init();
        if(cfg().adminCommands.length > 0)
            adminCommandsSet.addAll(Arrays.asList(cfg().adminCommands));

        Events.on(EventType.ServerLoadEvent.class, e -> {
            Events.fire(new EnhancedHelpCommand());
            log(info, f("Help Command Overwritten. Amount of admin commands: %s", adminCommandsSet.size()));
        });
        log("Initialized\n");
    }

    @Override
    public void registerClientCommands(CommandHandler handler) {
        // Magic, NetServer:270
        handler.<Player>register("help", "[page]", "Lists all commands.", (args, player) -> {
            Seq<CommandHandler.Command> commands = handler.getCommandList().copy();
            Seq<CommandHandler.Command> adminOnlyCommands = commands.copy().removeAll(cmd -> !adminCommandsSet.contains(cmd.text));
            Seq<CommandHandler.Command> playerCommands = commands.copy().removeAll(cmd -> adminCommandsSet.contains(cmd.text));
            commands.clear();

            if (player.admin)
                commands.addAll(adminOnlyCommands);
            commands.addAll(playerCommands);

            if (args.length > 0 && !Strings.canParseInt(args[0])) {
                player.sendMessage("[scarlet]'page' must be a number.");
                return;
            }

            int commandsPerPage = 6;
            int page = args.length > 0 ? Strings.parseInt(args[0]) : 1;
            int pages = Mathf.ceil((float) commands.size / commandsPerPage);

            page--;

            if (page >= pages || page < 0) {
                player.sendMessage("[scarlet]'page' must be a number between[orange] 1[] and[orange] " + pages + "[scarlet].");
                return;
            }

            StringBuilder result = new StringBuilder();
            result.append(Strings.format("[orange]-- Commands Page[lightgray] @[gray]/[lightgray]@[orange] --\n\n", (page + 1), pages));

            for (int i = commandsPerPage * page; i < Math.min(commandsPerPage * (page + 1), commands.size); i++) {
                CommandHandler.Command command = commands.get(i);
                result.append(adminOnlyCommands.contains(command) ? "[scarlet]" : "[orange]").append(" /").append(command.text).append("[white] ").append(command.paramText).append("[lightgray] - ").append(command.description).append("\n");
            }
            player.sendMessage(result.toString());
        });
        // Magic
    }

    public void add(String cmd){
        adminCommandsSet.add(cmd);
    }

    public void add(String[] cmd){
        adminCommandsSet.addAll(Arrays.asList(cmd));
    }


    @Override
    protected void defConfig() {
        adminCommandsSet = new HashSet<>();
        cfg = new EnhancedHelpCommandConfig();
    }

    private EnhancedHelpCommandConfig cfg(){
        return (EnhancedHelpCommandConfig) cfg;
    }

    public static class EnhancedHelpCommandConfig extends GHPluginConfig {
        private String[] adminCommands;

        public void reset(){
            adminCommands = new String[0];
        }
    }
}

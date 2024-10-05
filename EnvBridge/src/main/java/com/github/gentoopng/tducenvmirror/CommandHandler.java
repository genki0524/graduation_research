package com.github.gentoopng.tducenvmirror;

import com.github.gentoopng.tducenvmirror.WebSocket.WebSocketClientForPose;
import com.github.gentoopng.tducenvmirror.area.Area;
import com.github.gentoopng.tducenvmirror.area.WorldsMismatchException;

import org.bukkit.EntityEffect;
import org.bukkit.World;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.List;

public class CommandHandler implements CommandExecutor {

    TDUCEnvMirror instance;

    CommandHandler(TDUCEnvMirror instance) {
        this.instance = instance;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("envmirror")) {
            if (args.length == 0) { // サブコマンドがない場合
                sender.sendMessage(
                        """
                        No subcommands provided! Available commands:
                                /env reload (reload config)
                                /env summonhere
                                /env summon [areaID or areaName] quantity
                                /env remove quantity (will remove oldest ones)
                                /env remove area quantity
                                /env addarea areaID x1 y1 z1 x2 y2 z2
                                /env removearea [areaID or areaName]
                                /env listarea
                                /env setentry areaID x y z
                                /env update areaID
                                /env status [areaID]
                                /env task start/cancel areaID
                                /env heateffect start/cancel areaID
                        """
                );
                return true;
            } else {
                // サブコマンドがある場合
                return switch (args[0].toLowerCase()) {
                    case "reload" -> executeReload(sender);
                    case "websocket", "ws" -> executeWebSocket(sender, args);
                    case "area" -> executeArea(sender, args);
                    case "task" -> executeTask(sender, args);
                    case "entity" -> executeEntity(sender,args);
                    case "counter" -> executeCounter(sender, args);
                    case "summonhere" -> executeSummonHere(sender, args);
                    case "summon" -> executeSummon(sender, args);
                    case "remove" -> executeRemove(sender, args);
                    case "addarea" -> executeAddArea(sender, args);
                    case "removearea" -> executeRemoveArea(sender, args);
                    case "listarea" -> executeListArea(sender, args);
                    case "setentry" -> executeSetEntryPoint(sender, args);
                    case "update" -> executeUpdate(sender, args);
                    case "status" -> executeStatus(sender, args);
                    case "starttask" -> executeStartTask(sender, args);
                    case "canceltask" -> executeCancelTask(sender, args);
                    case "statustext" -> executeShowStatusText(sender, args);
                    case "heateffect" -> executeHeatEffect(sender, args);
                    case "fire" -> executeFireEffect(sender, args);
                    default -> false;
                };
            }
        }
        // コマンド名がこのプラグインのもの (envmirror) ではない場合
        return false;
    }

    // /env reload
    private boolean executeReload(CommandSender sender) { //設定ファイルの再読み込み
        instance.reloadConfig();
        instance.config = instance.getConfig();
        return true;
    }

    // /env summonhere
    private boolean executeSummonHere(CommandSender sender, String[] args) { //村人を自分がいる場所に召喚するための関数
        Player player;
        if (sender instanceof Player) { // コマンド送信者がPlayerの場合のみ実行する
            player = (Player) sender;
        } else {
            sender.sendMessage("This command must be sent from in-game player!");
            return true; // Player以外からの場合ここで終了
        }
        // コマンドがPlayerからの場合
        int quantity;
        if (args.length >= 2) { // サブコマンドが2以上の場合
            try {
                quantity = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                quantity = TDUCEnvMirror.DEFAULT_SPAWN_QUANTITY;
                player.sendMessage("Set spawn quantity to default value: " + quantity);
            }
        } else {    // 2つめのサブコマンドがない場合
            quantity = TDUCEnvMirror.DEFAULT_SPAWN_QUANTITY;
            player.sendMessage("Set spawn quantity to default value: " + quantity);
        }
        sender.sendMessage("Spawning " + quantity + " villager(s)...");
        Location location = player.getLocation();
        instance.spawnEntity(location, EntityType.VILLAGER, quantity);
        return true;
    }

    // /env summon [areaID or areaName] int
    private boolean executeSummon(CommandSender sender, String[] args) { //特定のエリアに村人を配置する
        if (args.length >= 3) {
            String areaQuery;
            int quantity;

            try { // intに変換できない場合処理中止
                areaQuery = args[1];
                quantity = Integer.parseInt(args[2]);
            } catch (NumberFormatException e) {
                return false;
            }

            Area area = instance.areaManager.getArea(areaQuery);
            if (area == null) {
                return false;
            }

            Location location;
            if (area.getEntryPoint() != null) {
                location = area.getEntryPoint();
            } else {
                location = area.randomLocation();
            }
            sender.sendMessage("Spawning " + quantity + " villagers into area \"" + area.getAreaID() + "\"");
            instance.spawnEntity(location, EntityType.VILLAGER, quantity);
            return true;
        } else {
            return false;
        }
    }

    // /env remove
    private boolean executeRemove(CommandSender sender, String[] args) { //村人を削除（うまくいかない）
        int quantity;
        if (args.length >= 2) {
            try {
                quantity = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                quantity = TDUCEnvMirror.DEFAULT_REMOVE_QUANTITY;
                sender.sendMessage("Set spawn quantity to default value: " + quantity);
            }
        } else {
            quantity = TDUCEnvMirror.DEFAULT_REMOVE_QUANTITY;
            sender.sendMessage("Set spawn quantity to default value: " + quantity);
        }
        if (quantity == 1) {
            sender.sendMessage("Removing the oldest " + quantity + " villager...");
        } else {
            sender.sendMessage("Removing the oldest " + quantity + " villagers...");
        }

        if (instance.removeEntity(quantity)) { // 正常に削除した場合
            sender.sendMessage("done");
        } else { // quantity が小さすぎてfalseが返ってきた場合
            sender.sendMessage("No entities to remove (too small quantity provided)!");
        }
        return true;
    }

    // /env area
    private boolean executeArea(CommandSender sender, String[] args) {
        if (args.length < 2) {
            return false;
        } else {
            switch (args[1].toLowerCase()) {
                // /envbridge area add
                case "add" -> {
                    if (args.length < 9) {
                        sender.sendMessage("Not enough arguments! Please check the usage. For example:");
                        sender.sendMessage("/envbridge area add areaID x1 y1 z1 x2 y2 z2");
                    }
                    var argsToPass = new String[8];
                    System.arraycopy(args, 1, argsToPass, 0, args.length - 1);
                    return executeAddArea(sender, argsToPass);
                }
                // /envbridge area remove areaID
                case "remove", "delete" -> {
                    if (args.length < 3) {
                        sender.sendMessage("Not enough arguments! Please check the usage. For example:");
                        sender.sendMessage("/envbridge area remove areaID");
                    }
                    var argsToPass = new String[2];
                    System.arraycopy(args, 1, argsToPass, 0, args.length - 1);
                    return executeRemoveArea(sender, argsToPass);
                }
                // /envbridge area list
                case "list" -> {
                    return executeListArea(sender, null);
                }
                // /envbridge area status areaID
                case "status" -> {
                    if (args.length > 3) {
                        sender.sendMessage("Not enough arguments! Please check the usage. For example:");
                        sender.sendMessage("/envbridge area status");
                    }
                    var argsToPass = new String[2];
                    System.arraycopy(args, 1, argsToPass, 0, args.length - 1);
                    return executeStatus(sender, argsToPass);
                }
                default -> {
                    sender.sendMessage("You can add, remove, update, check status or list the area(s). Please check the usage");
                    return false;
                }
            }
        }
    }

    // /env addarea areaID x1 y1 z1 x2 y2 z2
    private boolean executeAddArea(CommandSender sender, String[] args) {
        if (args.length < 8) {
            sender.sendMessage("Not enough arguments! Please check the usage:");
            sender.sendMessage("Ex: /envmirror addarea areaID x1 y1 z1 x2 y2 z2");
            return true;
        }

        Player player;
        String areaID;
        World world;
        Location vertex1;
        Location vertex2;
        var v1 = new int[3];
        var v2 = new int[3];

        // データ準備
        if (sender instanceof Player) {
            player = (Player) sender;
        } else {
            sender.sendMessage("This command muse be issued by in-game player!");
            sender.sendMessage("Ex: /envmirror addarea areaID x1 y1 z1 x2 y2 z2");
            return true;
        }
        areaID = args[1];
        for (int i = 0; i < 3; i++) {
            try {
                v1[i] = Integer.parseInt(args[i + 2]);
                v2[i] = Integer.parseInt(args[i + 5]);
            } catch (NumberFormatException e) {
                return false;
            }
        }
        world = player.getWorld();
        vertex1 = new Location(world, v1[0], v1[1], v1[2]);
        vertex2 = new Location(world, v2[0], v2[1], v2[2]);

        // Area作成
        try {
            Area area = new Area(areaID, vertex1, vertex2);
            instance.areaManager.add(area);
            sender.sendMessage("New area \"" + area.getAreaID() + "\" has been created");
            return true;
        } catch (WorldsMismatchException e) {
            // 2頂点のワールドが一致しなかった場合のエラー（このメソッドでは発生しない）
            sender.sendMessage("There was an error creating new area (worlds of provided vertexes mismatched)");
            e.printStackTrace();
            return false;
        }
    }

    // /env removearea [areaID or areaName]
    private boolean executeRemoveArea(CommandSender sender, String[] args) {
        String query = args[1];
        Area toRemove = instance.areaManager.getArea(query);

        if (toRemove == null) { // 見つからなかった場合nullが返ってくる
            sender.sendMessage("Area \"" + query + "\" not found!");
            return true;
        }

        String areaID = toRemove.areaID;
        if (instance.areaManager.removeByID(areaID)) { // 処理できるとtrueが返ってくる
            sender.sendMessage("Area \"" + areaID + "\" was successfully removed");
        } else {
            sender.sendMessage("Something went wrong when removing the area!");
        }
        return true;
    }

    // /env listarea
    private boolean executeListArea(CommandSender sender, String[] args) {
        if (instance.areaManager.getArrayList().size() == 0) {
            sender.sendMessage("There are no area currently.");
            return true;
        }

        int sizeOfList = instance.areaManager.getArrayList().size();
        if (sizeOfList == 1) {
            sender.sendMessage("There are " + instance.areaManager.getArrayList().size() + " area: -----");
        } else {
            sender.sendMessage("There are " + instance.areaManager.getArrayList().size() + " areas: -----");
        }

        for (Area area : instance.areaManager.getArrayList()) {
            sender.sendMessage(area.getAreaID());
        }
        sender.sendMessage("--------------------");
        return true;
    }

    // /env setentry areaID x y z
    private boolean executeSetEntryPoint(CommandSender sender, String[] args) {
        String areaID = args[1];
        double x = Double.parseDouble(args[2]);
        double y = Double.parseDouble(args[3]);
        double z = Double.parseDouble(args[4]);
        Area area = instance.areaManager.getArea(areaID);
        if (area == null) {
            sender.sendMessage("No area with ID \"" + areaID + "\" found. Check the Area ID and try again.");
            return true;
        }

        boolean result = area.setEntryPoint(x, y, z);
        if (result) {
            sender.sendMessage("Set entry point for \"" + areaID + "\": " + x + ", " + y + ", " + z);
        } else {
            sender.sendMessage("Something went wrong!");
        }
        return result;
    }

    // /env update areaID
    private boolean executeUpdate(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage("Please specify an AreaID! ex. /env refresh areaID");
            return true;
        }
        String areaID = args[1];
        sender.sendMessage("Getting the latest data of " + areaID + " from the database...");
        if (!instance.dbAccessor.readAndSet(areaID)) {
            sender.sendMessage("Failed! Does your AreaID exists? Check the DB or try: /env list ");
        }
        sender.sendMessage("Done");
        return true;
    }

    // /env status [areaID]
    private boolean executeStatus(CommandSender sender, String[] args) {
        if (args.length <= 1) {
            return executeListArea(sender, null);
        } else {
            String areaID = args[1];
            Area area = instance.areaManager.getArea(areaID);
            sender.sendMessage(area.toString());
            return true;
        }
    }

    // /env starttask areaID
    private boolean executeStartTask(CommandSender sender, String[] args) {
        String areaID = args[1];
        if (!instance.startUpdateAreaTask(areaID)) {
            sender.sendMessage("The area with that AreaID not found!");
        } else {
            sender.sendMessage("Started update task for " + areaID);
        }
        return true;
    }

    // /env canceltask areaID
    private boolean executeCancelTask(CommandSender sender, String[] args) {
        String areaID = args[1];
        instance.cancelUpdateAreaTask(areaID);
        sender.sendMessage("Cancel order sent");
        return true;
    }

    // /env task start/cancel areaID
    private boolean executeTask(CommandSender sender, String[] args) {//タスクコマンド関連のコード
        if (args.length < 2) {
            return false;
        } else {
            switch (args[1].toLowerCase()) {
                case "start" -> {
                    if (args.length < 3) {
                        sender.sendMessage("Please provide the area ID!");
                        return false;
                    } else {
                        String areaID = args[2];
                        if (!instance.startUpdateAreaTask(areaID)) {
                            sender.sendMessage("The area with that AreaID not found!");
                        } else {
                            sender.sendMessage("Started update task for " + areaID);
                        }
                        return true;
                    }
                }
                case "cancel", "stop" -> {
                    if (args.length < 3) {
                        sender.sendMessage("Please provide the area ID!");
                        return false;
                    } else {
                        String areaID = args[2];
                        instance.cancelUpdateAreaTask(areaID);
                        sender.sendMessage("Stopped update task for \" + areaID");
                        return true;
                    }
                }
                default -> {
                    sender.sendMessage("start or cancel ?");
                    return false;
                }
            }
        }
    }

    // /env statustext area bool
    private boolean executeShowStatusText(CommandSender sender, String[] args) {
        boolean toSet;
        if (args.length < 2) {
            sender.sendMessage("Not enough arguments. Please provide the areaID and then true/false.");
            return false;
        } else {
            String areaID = args[1];
            Area area = instance.areaManager.getArea(areaID);
            if (area == null) {
                sender.sendMessage("Something went wrong (area does not found)");
                return true;
            } else if (args.length > 2){
                if (args[2].equals("true")) {
                    toSet = true;
                } else if (args[2].equals("false")) {
                    toSet = false;
                } else {
                    sender.sendMessage("\"true\" or \"false\" for the last argument. Nothing done.");
                    return true;
                }
            } else {
                toSet = true;
                sender.sendMessage("Set true for area " + areaID + " to display status text");
            }

            if (toSet) {
                area.setTextLocation(area.getCenterLocation());
                area.setShowText(true);
            } else {
                area.setShowText(false);
            }
            return true;
        }
    }

    // /env heateffect start/cancel areaID
    private boolean executeHeatEffect(CommandSender sender, String[] args) {
        switch (args[1].toLowerCase()) {
            case "start" -> {
                if (!instance.startHeatEffectTask(args[2])) {
                    sender.sendMessage("Something went wrong");
                    return false;
                } else {
                    sender.sendMessage("Started heat-effect task for " + args[2]);
                    return true;
                }
            }
            case "cancel" -> {
                instance.cancelHeatEffectTask(args[2]);
                sender.sendMessage("Cancel order sent");
                return true;
            }
            default -> {
                return false;
            }
        }
    }

    private boolean executeFireEffect(CommandSender sender, String[] args) {

        List<Entity> entities = instance.entityManager.getAllEntities();
        entities.removeIf(e -> e.getType() != EntityType.VILLAGER);

        boolean value;

        switch (args[1].toLowerCase()) {
            case "b", "block" -> {
                boolean mode;
                switch (args[2].toLowerCase()) {
                    case "on", "true" -> {
                        mode = true;
                    }
                    default -> {
                        mode = false;
                    }
                }
                Area area = instance.areaManager.getArea(args[3]);
                if (area == null) {
                    break;
                }
                if (mode) {
                    area.setFireBlocks();
                } else {
                    area.removeFireBlocks();
                }
            }
            case "e", "entitiy" -> {
                switch (args[2].toLowerCase()) {
                    case "on", "true" -> {
                        value = true;
                        sender.sendMessage("Setting visual fire on villagers");
                    }
                    case "off", "false" -> {
                        value = false;
                        sender.sendMessage("Removing visual fire on villagers");
                    }
                    default -> {
                        return false;
                    }
                }

                for (Entity e: entities) {
                    e.setVisualFire(value);
                }
            }
        }

        return true;
    }

    // /envbridge entity
    private boolean executeEntity(CommandSender sender, String[] args) {
        if (args.length < 2) {
            return false;
        } else {
            switch (args[1].toLowerCase()) {
                // /envbridge entity effect on <effect>
                case "effect" -> {
                    if (args.length < 4) {
                        return false;
                    } else {
                        switch (args[2].toLowerCase()) {
                            case "on", "play" -> {
                                List<Entity> entities = instance.entityManager.getAllEntities();
//                                sender.sendMessage("Playing" + args[3] + " effect for " + entities.size() + " entities");
                                for (Entity e: entities) {
//                                    sender.sendMessage("playing");
                                    if (EntityEffect.valueOf(args[3].toUpperCase()) == EntityEffect.HURT) {
//                                        if (e instanceof LivingEntity) {
//                                            var livingEntity = (LivingEntity) e;
//                                            livingEntity.playHurtAnimation(10);
//                                        }
                                    }
                                    e.playEffect(EntityEffect.valueOf(args[3].toUpperCase()));
                                }
                                return true;
                            }
                            default -> {
                                return false;
                            }
                        }
                    }
                }
                // /envbridge entity summon here [quantity]
                case "summon" -> {
                    if (args.length < 4) {
                        return false;
                    } else {
                        switch (args[2].toLowerCase()) {
                            case "here" -> {
                                var argsToPass = new String[3];
                                System.arraycopy(args, 1, argsToPass, 0, args.length - 1);
                                executeSummonHere(sender, argsToPass);
                                return true;
                            }
                            default -> {
                                sender.sendMessage("Please try: /envbridge entity summon here");
                                return true;
                            }
                        }
                    }
                }
                case "noai" -> {
                    List<Entity> entities = instance.entityManager.getAllEntities();
                    sender.sendMessage("Revoking AI from LivingEntities");
                    setAI(entities, false);
                    return true;
                }
                case "ai" -> {
                    List<Entity> entities = instance.entityManager.getAllEntities();
                    sender.sendMessage("Setting AI from LivingEntities");
                    setAI(entities, true);
                    return true;
                }
                default -> {
                    return false;
                }
            }
        }
    }

    private void setAI(List<Entity> entities, boolean ai) {
        for (Entity e: entities) {
            if (e instanceof LivingEntity) {
                var livingEntity = (LivingEntity)e;
                livingEntity.setAI(ai);
            }
        }
    }


    private boolean executeWebSocket(CommandSender sender, String[] args) {
        if (args.length < 2) {
            return false;
        } else {
            switch (args[1].toLowerCase()) {
                case "open" -> {
                    if(args.length < 3){
                        sender.sendMessage("Please select env or pose!");
                        return false;
                    }
                    switch (args[2].toLowerCase()) {
                        case "env" -> {
                            if (args.length < 4) {
                                sender.sendMessage("Please provide the URI!");
                                return false;
                            } else {
                                try {
                                    instance.webSocketClientForEnv.open(args[3]);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                            return true;
                        }
                        case "pose" -> {
                            if (args.length < 4){
                                sender.sendMessage("Please provide the URI!");
                                return false;
                            } else {
                                try{
                                    WebSocketClientForPose webSocketClientForPose = (WebSocketClientForPose) instance.webSocketClientForPose;
                                    webSocketClientForPose.setPlayer((Player) sender);
                                    webSocketClientForPose.open(args[3]);
                                } catch (Exception e){
                                    e.printStackTrace();
                                }
                            }
                            return true;
                        }
                    }
                }
                case "close" -> {
                    if(args.length < 3){
                        sender.sendMessage("Please select env or pose!");
                        return false;
                    }
                    switch (args[2].toLowerCase()) {
                        case "env" -> {
                            instance.webSocketClientForEnv.close();
                            return true;
                        }
                        case "pose" -> {
                            instance.webSocketClientForPose.close();
                            return true;
                        }
                    }
                }
                case "request" -> {
                    if (args.length < 3) {
                        sender.sendMessage("Please specify areaID");
                        return true;
                    } else {
                        String areaID = args[2];
                        String request = "{\"request\": true, \"areaid\": \"" + areaID + "\"}";
                        if (instance.webSocketClientForEnv.send(request)) {
                            return true;
                        } else {
                            sender.sendMessage("Something went wrong. Check your input (or maybe there is other reason)");
                            return false;
                        }
                    }
                }
                default -> {
                    return false;
                }
            }
        }
        return false;
    }

    private boolean executeCounter(CommandSender sender, String[] args) {
        if (args.length < 2) {
            return false;
        } else {
            switch (args[1].toLowerCase()) {
                case "reset" -> {
                    instance.resetCounter();
                    sender.sendMessage("Counter: " + instance.getCounter());
                    return true;
                }
                case "show" -> {
                    sender.sendMessage("Counter: " + instance.getCounter());
                    return true;
                }
                default -> {
                    return false;
                }
            }
        }
    }
}

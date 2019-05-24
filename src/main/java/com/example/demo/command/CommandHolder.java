package com.example.demo.command;

import com.example.demo.command.commands.CUPHandler;

import java.util.HashMap;
import java.util.Map;

public class CommandHolder {

    private static Map<String, CommandHandler> COMMANDS = new HashMap<String, CommandHandler>();

    static {
        COMMANDS.put("CUP", new CUPHandler());//客户端上发
//        COMMANDS.put("SDY", new SDYHandler());//服务端下发
//        COMMANDS.put("ACC", new ACCHandler());
//        COMMANDS.put("QST", new QSTHandler());
    }

    public static CommandHandler getHandler(String cmd) {
        return COMMANDS.get(cmd);
    }

}

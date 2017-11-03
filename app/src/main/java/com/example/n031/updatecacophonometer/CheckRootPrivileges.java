package com.example.n031.updatecacophonometer;

import java.util.ArrayList;

/**
 * Created by student on 10/20/2017.
 */

public class CheckRootPrivileges extends  ExcuteAsRootBase{
    @Override
    protected ArrayList<String> getCommandsToExecute() {
        ArrayList<String> commands = new ArrayList<>();
        commands.add("ls");
        return commands;
    }
}

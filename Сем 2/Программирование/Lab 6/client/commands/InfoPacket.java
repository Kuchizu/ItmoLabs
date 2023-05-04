package commands;

import collections.Flat;

import java.io.Serial;
import java.io.Serializable;

/**
 * ...
 */
public class InfoPacket extends OptionDecorator implements Serializable {
    @Serial
    private static final long serialVersionUID = 1;

    private String cmd;
    private String arg;
    private Flat flat;

    public Flat getFlat() {
        return flat;
    }

    public void setFlat(Flat flat) {
        this.flat = flat;
    }

    public InfoPacket(String cmd, String arg) {
        super("2");
        this.cmd = cmd;
        this.arg = arg;
    }

    public String getCmd() {
        return cmd;
    }

    public String getArg() {
        return arg;
    }

    public void setCmd(String cmd) {
        this.cmd = cmd;
    }

    public void setArg(String arg) {
        this.arg = arg;
    }

    @Override
    public String toString() {
        return "InfoPacket{" +
                "cmd='" + cmd + '\'' +
                ", arg='" + arg + '\'' +
                '}';
    }
}

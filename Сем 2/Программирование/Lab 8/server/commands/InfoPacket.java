package commands;

import collections.Flat;

import java.io.Serial;
import java.io.Serializable;

/**
 * ...
 */
public class InfoPacket implements Serializable {
    @Serial
    private static final long serialVersionUID = 1;

    private String cmd;
    private String arg;
    private Flat flat;

    private String login;
    private String password;

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Flat getFlat() {
        return flat;
    }

    public void setFlat(Flat flat) {
        this.flat = flat;
    }

    public InfoPacket(String cmd, String arg) {
        this.cmd = cmd;
        this.arg = arg;
    }

    public String getCmd() {
        return cmd;
    }

    public void setCmd(String cmd) {
        this.cmd = cmd;
    }

    public String getArg() {
        return arg;
    }

    public void setArg(String arg) {
        this.arg = arg;
    }

    @Override
    public String toString() {
        return "InfoPacket{" +
                "cmd = '" + cmd + '\'' +
                ", arg = '" + arg + '\'' +
                ", login = '" + login + '\'' +
                ", password = '" + password + '\'' +
                '}';
    }
}

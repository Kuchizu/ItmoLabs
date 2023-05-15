package commands;

// Для защиты я хз что это

public class OptionDecorator implements Service {
    private String info;
    private InfoPacket p;

    public OptionDecorator(String info) {
        this.info = info;
    }

    @Override
    public String getLabel() {
        return this.info + p.getCmd();
    }

}

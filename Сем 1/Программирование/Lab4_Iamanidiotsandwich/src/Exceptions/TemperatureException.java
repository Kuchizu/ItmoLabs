package Exceptions;

public class TemperatureException extends Exception{
    private int thisval;
    private int minval;
    private int maxval;

    public TemperatureException(int thisval, int minval, int maxval) {
        super();
        this.thisval = thisval;
        this.minval = minval;
        this.maxval = maxval;
    }

    @Override
    public String getMessage(){
        return "TemperatureException throwed, your value is " + thisval + " range of value for this object is " + minval + " " + maxval;
    }
}

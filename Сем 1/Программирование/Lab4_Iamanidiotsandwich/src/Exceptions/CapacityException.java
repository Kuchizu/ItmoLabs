package Exceptions;

public class CapacityException extends RuntimeException{
    private int thisval;
    private int minval;
    private int maxval;

    public CapacityException(int thisval, int minval, int maxval) {
        super();
        this.thisval = thisval;
        this.minval = minval;
        this.maxval = maxval;
    }

    @Override
    public String getMessage(){
        return "CapacityException throwed, your value is " + thisval + " range of value for this object is " + minval + " " + maxval;
    }

}

public abstract class AbsPlace implements PlaceAble{
    public String placeName;
    public String typeName;

    public String getPlace() {
        return placeName;
    }
    @Override
    public int hashCode() {
        return super.hashCode() + this.getPlace().hashCode();
    }
    @Override
    public boolean equals(Object obj) {
        return obj.hashCode() == this.hashCode();
    }
    @Override
    public String toString() {
        return "Место " + this.getPlace();
    }

}

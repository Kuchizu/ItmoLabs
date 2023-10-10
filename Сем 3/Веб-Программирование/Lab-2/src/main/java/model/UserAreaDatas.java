package model;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class UserAreaDatas implements Serializable {
    private LinkedList<AreaData> areaDataList;

    public UserAreaDatas() {
        super();
        this.areaDataList = new LinkedList<>();
    }

    public LinkedList<AreaData> getAreaDataList() {
        return areaDataList;
    }
    public List <AreaData> getData() {
        return areaDataList;
    }
    public void setAreaDataList(LinkedList<AreaData> areaDataList) {
        this.areaDataList = areaDataList;
    }
    public AreaData getLastResult() {
        if (areaDataList != null && !areaDataList.isEmpty()) {
            return areaDataList.get(areaDataList.size() - 1);
        }
        return null;
    }

    public void addResult(AreaData data){
        this.areaDataList.add(data);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserAreaDatas)) return false;
        UserAreaDatas that = (UserAreaDatas) o;
        return Objects.equals(getAreaDataList(), that.getAreaDataList());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getAreaDataList());
    }

    @Override
    public String toString() {
        return "UserAreaDatas{" +
                "areaDataList=" + areaDataList +
                '}';
    }
}

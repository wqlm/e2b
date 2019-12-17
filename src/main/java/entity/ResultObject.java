package entity;

public class ResultObject<O> {
    /**
     * excel行号
     */
    private int row;

    /**
     * 解析出来的对象
     */
    private O object;

    public ResultObject(O object, int row) {
        this.row = row;
        this.object = object;
    }

    public int getRow() {
        return row;
    }

    public void setRow(int row) {
        this.row = row;
    }

    public O getObject() {
        return object;
    }

    public void setObject(O object) {
        this.object = object;
    }
}

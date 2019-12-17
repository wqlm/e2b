package entity;

/**
 * 封装excel转换过程中的错误的对象
 */
public class ExcelToObjectError {

    public static final String DATA_ILLEGALITY = "数据不合法";

    public static final String TYPE_ILLEGALITY = "类型不合法";

    public static final String ILLEGAL_ACCESS = "反射赋值出错";

    public static final String FORMAT = "格式不合法";

    public static final String NOT_NULL = "不能为空";

    /**
     * excel的行号
     */
    private int row;

    /**
     * excel的列号
     */
    private String column;

    /**
     * 错误信息
     */
    private String msg;

    public ExcelToObjectError(int row, String column, String msg) {
        this.row = row;
        this.column = column;
        this.msg = msg;
    }

    public ExcelToObjectError(String msg) {
        this.msg = msg;
    }

    public ExcelToObjectError(int row, String column) {
        this.row = row;
        this.column = column;
        this.msg = DATA_ILLEGALITY;
    }

    public int getRow() {
        return row;
    }

    public void setRow(int row) {
        this.row = row;
    }

    public String getColumn() {
        return column;
    }

    public void setColumn(String column) {
        this.column = column;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}

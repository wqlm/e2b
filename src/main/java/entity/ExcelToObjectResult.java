package entity;

import java.util.List;

public class ExcelToObjectResult<O> {

    /**
     * 错误列表
     */
    private List<ExcelToObjectError> errorlList;

    /**
     * 解析结果对象列表，包含解析出来的对象和 excel对应的行号
     */
    private List<ResultObject<O>> resultObjectList;


    public ExcelToObjectResult(List<ExcelToObjectError> errorlList, List<ResultObject<O>> resultObjectList) {
        this.errorlList = errorlList;
        this.resultObjectList = resultObjectList;
    }

    public List<ExcelToObjectError> getErrorlList() {
        return errorlList;
    }

    public void setErrorlList(List<ExcelToObjectError> errorlList) {
        this.errorlList = errorlList;
    }

    public List<ResultObject<O>> getResultObjectList() {
        return resultObjectList;
    }

    public void setResultObjectList(List<ResultObject<O>> resultObjectList) {
        this.resultObjectList = resultObjectList;
    }
}

package entity;

import java.util.ArrayList;
import java.util.List;

/**
 * 该类是最终解析结果的一个实现，你也可以自定义自己的解析结果
 */
public class ExcelImportResult {
    /**
     * 导入成功的条数
     */
    private int success;

    /**
     * 成功的行号
     */
    private List<Integer> successRow;

    /**
     * 导入失败的条数
     */
    private int fail;

    /**
     * 失败的行号
     */
    private List<Integer> failRow;

    /**
     * 重复数据的条数
     */
    private int repetition;

    /**
     * 重复数据的行号
     */
    private List<Integer> repetitionRow;

    /**
     * 错误列表
     */
    private List<ExcelToObjectError> errorList;

    public ExcelImportResult(List<ExcelToObjectError> errorlList) {
        this.success = 0;
        this.successRow = new ArrayList<>();
        this.fail = 0;
        this.failRow = new ArrayList<>();
        this.repetition = 0;
        this.repetitionRow = new ArrayList<>();
        this.errorList = errorlList;
    }

    public ExcelImportResult(int success, List<Integer> successRow, int fail, List<Integer> failRow, int repetition,
                             List<Integer> repetitionRow) {
        this.success = success;
        this.successRow = successRow;
        this.fail = fail;
        this.failRow = failRow;
        this.repetition = repetition;
        this.repetitionRow = repetitionRow;
        this.errorList = new ArrayList<>();
    }

    public ExcelImportResult() {
        this.success = 0;
        this.successRow = new ArrayList<>();
        this.fail = 0;
        this.failRow = new ArrayList<>();
        this.repetition = 0;
        this.repetitionRow = new ArrayList<>();
        this.errorList = new ArrayList<>();
    }

    public int getSuccess() {
        return success;
    }

    public void setSuccess(int success) {
        this.success = success;
    }

    public int getFail() {
        return fail;
    }

    public void setFail(int fail) {
        this.fail = fail;
    }

    public List<Integer> getFailRow() {
        return failRow;
    }

    public void setFailRow(List<Integer> failRow) {
        this.failRow = failRow;
    }

    public List<Integer> getRepetitionRow() {
        return repetitionRow;
    }

    public void setRepetitionRow(List<Integer> repetitionRow) {
        this.repetitionRow = repetitionRow;
    }

    public List<ExcelToObjectError> getErrorList() {
        return errorList;
    }

    public void setErrorList(List<ExcelToObjectError> errorList) {
        this.errorList = errorList;
    }

    public List<Integer> getSuccessRow() {
        return successRow;
    }

    public void setSuccessRow(List<Integer> successRow) {
        this.successRow = successRow;
    }

    public int getRepetition() {
        return repetition;
    }

    public void setRepetition(int repetition) {
        this.repetition = repetition;
    }
}

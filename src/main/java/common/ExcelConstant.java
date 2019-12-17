package common;

public class ExcelConstant {
    /**
     * 该常量用于@excel注解的optionSchema，表示对下拉框使用转换模式
     * 转换模式下，会认为打上@excel注解的属性，对应的excel列名，在excel上填写的是下拉框的name值
     * 解析过程中，会将excel上填写下拉框的的name值，转换成对应的value值。
     */
    public static final String SWITCH = "switch";

    /**
     * 该常量用于@excel注解的optionSchema，表示对下拉框使用校验模式
     * 校验模式下，会认为打上@excel注解的属性，对应的excel列名，在excel上填写的是下拉框的value值
     * 解析过程中，会校验value值在下拉框的下拉列表中是否存在
     */
    public static final String CHECK = "check";
}

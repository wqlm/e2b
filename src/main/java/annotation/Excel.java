package annotation;

import common.ExcelConstant;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;


@Target({FIELD})   //设定注解使用范围,作用于字段上
@Retention(RUNTIME) //注解有效范围,运行时有效
public @interface Excel {
    /**
     * 该属性对应的excel的列名
     */
    String excelColumnName();

    /**
     * 该字段是否为空，默认不能为空
     */
    boolean notNull() default true;

    /**
     * 下拉框的名称
     */
    String optionName() default "";

    /**
     * 对下拉框的处理模式
     * SWITCH : 将下拉框的 name值转换成value
     * CHECK : 校验 value值是否合法
     */
    String optionSchema() default ExcelConstant.CHECK;
}

package core;

import annotation.Excel;
import common.ExcelConstant;
import entity.ExcelToObjectError;
import entity.ExcelToObjectResult;
import entity.ResultObject;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.ss.usermodel.Cell;
import util.utils;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ExcelToObject {

    private Map<String, Map<String, String>> optionMap;

    public ExcelToObject(Map<String, Map<String, String>> optionMap) {
        this.optionMap = optionMap;
    }

    public void setOptionMap(Map<String, Map<String, String>> optionMap) {
        this.optionMap = optionMap;
    }

    /**
     * 解析excel,将一行数据转换成一个对象
     *
     * @param inputStream   excel 文件流
     * @param objectClass   实体类 class
     * @param columnNameRow 列名所在的行号，从0开始计数
     * @param startDataRow  数据开始的行号，从0开始计数
     * @param <O>           任意类型
     * @return ExcelToObjectResult 包含对象列表 和 错误列表
     * @throws IOException
     * @throws IllegalAccessException
     * @throws InstantiationException
     */
    public <O> ExcelToObjectResult<O> getExcelToObjectResult(InputStream inputStream, Class<O> objectClass,
                                                             int columnNameRow, int startDataRow)
            throws IllegalAccessException, InstantiationException, IOException {
        if (inputStream == null || objectClass == null) {
            return null;
        }

        //结果对象列表
        List<ResultObject<O>> resultObjectList = new ArrayList<>();
        //解析过程中的错误列表
        List<ExcelToObjectError> errorList = new ArrayList<>();

        // 1 从文件流中获取 HSSFSheet 实体
        HSSFSheet hssfSheet = getHSSFSheetByFile(inputStream);
        if (hssfSheet == null) {
            return null;
        }

        // 2 得到 excel列名 与 实体属性名 的映射map
        Map<String, String> columnAndPropertyMap = getColumnAndPropertyMap(objectClass);
        if (columnAndPropertyMap == null || columnAndPropertyMap.size() == 0) {
            return null;
        }

        // 3 得到excel 数据列名和 下标 的映射map
        Map<String, Integer> columnAndIndexMap = getColumnAndIndexMap(hssfSheet, columnNameRow);
        if (columnAndIndexMap == null || columnAndIndexMap.size() == 0) {
            return null;
        }

        // 4 得到 类属性 与 列下标 的映射map
        Map<String, Integer> propertyAndIndexMap = getPropertyAndIndexMap(columnAndPropertyMap, columnAndIndexMap);
        if (propertyAndIndexMap == null || propertyAndIndexMap.size() == 0) {
            return null;
        }

        // 5 读取数据行中的数据,并封装成实体
        for (int i = startDataRow; i <= hssfSheet.getLastRowNum(); i++) {
            HSSFRow hssfRow = hssfSheet.getRow(i);
            if (hssfRow == null || hssfRowIsNull(hssfRow, propertyAndIndexMap)) {
                continue;
            }
            //创建一个实体
            O object = objectClass.newInstance();
            //将 excel 表中的一行数据封装成一个对象,
            List<ExcelToObjectError> excelToObjectErrorList = hssfRowToObject(object, hssfRow, propertyAndIndexMap);
            //如果错误列表为空
            if (excelToObjectErrorList == null || excelToObjectErrorList.size() == 0) {
                resultObjectList.add(new ResultObject<>(object, hssfRow.getRowNum() + 1));
            } else {
                //收集错误信息
                errorList.addAll(excelToObjectErrorList);
            }
        }

        // 6 返回解析结果
        return new ExcelToObjectResult<>(errorList, resultObjectList);

    }


    /**
     * 检查该单元行是否为空行
     *
     * @param hssfRow
     * @param propertyAndIndexMap
     * @return
     */
    private boolean hssfRowIsNull(HSSFRow hssfRow, Map<String, Integer> propertyAndIndexMap) {
        Iterator<String> iter = propertyAndIndexMap.keySet().iterator();
        while (iter.hasNext()) {
            String key = iter.next();
            int index = propertyAndIndexMap.get(key);
            //获取单位格中的值，并除去空格和特殊符号
            String cellValue = trimColumnName(getCellValue(hssfRow.getCell(index)));
            if (!utils.stringIsBlank(cellValue)) {
                //只要有一个值不为空，就认为该行不为空
                return false;
            }
        }
        return true;
    }

    /**
     * 遍历一行excel，并将每个单元格的值赋值给实体中对应的属性
     *
     * @param propertyAndIndexMap 实体的属性 与 数据列的下标 的映射map
     * @param hssfRow             单元行
     * @param object              要填充属性的实体
     * @return
     */
    private <O> List<ExcelToObjectError> hssfRowToObject(O object, HSSFRow hssfRow, Map<String, Integer> propertyAndIndexMap) {
        List<ExcelToObjectError> list = new ArrayList<>();
        Field[] fields = object.getClass().getDeclaredFields();
        for (Field field : fields) {
            //获取属性名
            String name = field.getName();
            //根据属性名获取 属性值所在的下标
            Integer index = propertyAndIndexMap.get(name);
            if (index == null) {
                continue;
            }
            //根据下标获取单元
            HSSFCell hssfCell = hssfRow.getCell(index);
            //取出单元格的值，赋给对应的属性
            ExcelToObjectError error = getCellValueAndSetField(object, field, hssfCell);
            if (error != null) {
                error.setRow(hssfRow.getRowNum() + 1);
                error.setColumn(intToAlphabet(index + 1));
                list.add(error);
            }
        }
        return list;
    }

    /**
     * 将指定的自然数转换为26进制表示。映射关系：[1-26] ->[A-Z]。
     *
     * @param n
     * @return
     */
    private String intToAlphabet(int n) {
        String s = "";
        while (n > 0) {
            int m = n % 26;
            if (m == 0) {
                m = 26;
            }
            s = (char) (64 + m) + s;
            n = (n - m) / 26;
        }
        return s;
    }

    /**
     * 获取并处理单元格的值
     *
     * @param field
     * @param object
     * @param hssfCell
     * @throws IllegalAccessException
     */
    private <O> ExcelToObjectError getCellValueAndSetField(O object, Field field, HSSFCell hssfCell) {
        ExcelToObjectError excelToObjectError = null;

        if (object == null || field == null) {
            return null;
        }
        //获取单元格的值,并去掉特殊符号和空格
        String cellValue = trimColumnName(getCellValue(hssfCell));

        Excel excel = field.getAnnotation(Excel.class);
        if (excel != null) {
            if (utils.stringIsBlank(cellValue) && excel.notNull()) {
                //如果excel单元格的值为空，并且属性不允许为空
                return new ExcelToObjectError(ExcelToObjectError.NOT_NULL);
            }

            String optionName = excel.optionName();
            String optionSchema = excel.optionSchema();

            //下拉框名称不为空
            if (!utils.stringIsBlank(optionName)) {
                if (ExcelConstant.SWITCH.equals(optionSchema)) {
                    //将单元格中的值换成对应下拉框的value
                    String optionValue = getCellOptionValue(optionName, cellValue);
                    if (utils.stringIsBlank(optionValue)) {
                        return new ExcelToObjectError(ExcelToObjectError.DATA_ILLEGALITY);
                    }
                    //替换成下拉框的value值
                    cellValue = optionValue;
                }

                if (ExcelConstant.CHECK.equals(optionSchema)) {
                    //校验 value值是否合法
                    if (!cellValueVerify(optionName, cellValue)) {
                        //校验不通过
                        return new ExcelToObjectError(ExcelToObjectError.DATA_ILLEGALITY);
                    }
                }
            }
        }

        //获取正则校验注解
        javax.validation.constraints.Pattern pattern = field.getAnnotation(javax.validation.constraints.Pattern.class);
        if (pattern != null && !utils.stringIsBlank(pattern.regexp())) {
            Matcher matcher = Pattern.compile(pattern.regexp()).matcher(cellValue);
            if (!matcher.matches()) {
                //完全匹配模式,匹配失败
                return new ExcelToObjectError(ExcelToObjectError.FORMAT);
            }
        }

        try {
            setField(object, field, hssfCell, cellValue);
        } catch (IllegalAccessException e) {
            excelToObjectError = new ExcelToObjectError(ExcelToObjectError.ILLEGAL_ACCESS);
        } catch (NumberFormatException e) {
            excelToObjectError = new ExcelToObjectError(ExcelToObjectError.TYPE_ILLEGALITY);
        }
        return excelToObjectError;
    }

    /**
     * 设置 属性值
     *
     * @param object   类
     * @param field    字段
     * @param hssfCell 单元格
     * @param value    值
     * @param <O>
     * @throws IllegalAccessException
     */
    private <O> void setField(O object, Field field, HSSFCell hssfCell, String value)
            throws IllegalAccessException, NumberFormatException {
        //设置属性可以修改
        field.setAccessible(true);

        //获取 属性 的类型
        String typeName = field.getType().getSimpleName();
        if ("String".equals(typeName)) {
            field.set(object, value);
            return;
        }
        if ("int".equals(typeName) || "Integer".equals(typeName)) {
            field.set(object, Integer.parseInt(value));
            return;
        }
        if ("float".equals(typeName) || "Float".equals(typeName)) {
            field.set(object, Float.parseFloat(value));
            return;
        }
        if ("double".equals(typeName) || "Double".equals(typeName)) {
            field.set(object, Double.parseDouble(value));
            return;
        }
        if ("long".equals(typeName) || "Long".equals(typeName)) {
            field.set(object, Long.parseLong(value));
            return;
        }
        if ("BigDecimal".equals(typeName)) {
            field.set(object, BigDecimal.valueOf(Double.parseDouble(value)));
            return;
        }
        if ("Date".equals(typeName)) {
            Date date = utils.autoDate(value);
            if (date == null) {
                // 如果Date为空则通过POI的API直接读取Date类型结果
                date = hssfCell.getDateCellValue();
            }
            field.set(object, date);
        }
    }

    /**
     * 得到 excel列名 与 实体属性名 的映射map
     *
     * @param objectClass
     * @return
     */
    private Map<String, String> getColumnAndPropertyMap(Class objectClass) {
        Map<String, String> map = new HashMap<>();

        if (objectClass == null) {
            return map;
        }
        Field[] fields = objectClass.getDeclaredFields();
        //遍历属性列表
        for (Field field : fields) {
            //从属性的注解中获取列名
            Excel excel = field.getAnnotation(Excel.class);
            if (excel == null) {
                continue;
            }
            String columnName = excel.excelColumnName();
            if (utils.stringIsBlank(columnName)) {
                continue;
            }
            //获取属性名
            String propertyName = field.getName();
            map.put(columnName, propertyName);
        }
        return map;
    }

    /**
     * 得到excel数据列的列名和对应的下标
     *
     * @param hssfSheet
     * @param row       列名所在的行号，行号从0开始
     * @return
     */
    private Map<String, Integer> getColumnAndIndexMap(HSSFSheet hssfSheet, int row) {
        if (hssfSheet == null) {
            return null;
        }
        Map<String, Integer> map = new HashMap<>();
        HSSFRow hssfRow = hssfSheet.getRow(row);

        for (int i = 0; i < hssfRow.getPhysicalNumberOfCells(); i++) {
            //获取列名并修剪
            String columnName = trimColumnName(getCellValue(hssfRow.getCell(i)));
            if (utils.stringIsBlank(columnName)) {
                //如果列名为null
                continue;
            }
            map.put(columnName, i);
        }
        return map;
    }

    /**
     * 获取 实体类中属性 与 excel表中 数据列 的下标 对应的map
     *
     * @param columnAndPropertyMap <Excel表中数据项的列名,列名对应的实体的属性名> ,
     * @param columnAndIndexMap    <Excel表中数据项的列名，列的下标>
     * @return <实体的属性名，excel列的下标>
     */
    private Map<String, Integer> getPropertyAndIndexMap(Map<String, String> columnAndPropertyMap, Map<String, Integer> columnAndIndexMap) {
        Map<String, Integer> map = new HashMap<>();
        Iterator<String> iter = columnAndIndexMap.keySet().iterator();
        while (iter.hasNext()) {
            String columnName = iter.next();
            String property = columnAndPropertyMap.get(columnName);
            if (utils.stringIsBlank(property)) {
                continue;
            }
            int index = columnAndIndexMap.get(columnName);
            //name 为实体的属性名， value是实体字段值所在excel列的下标
            map.put(property, index);
        }
        return map;
    }


    /**
     * 从文件流中获取 HSSFSheet 实体
     *
     * @param inputStream
     * @return
     * @throws IOException
     */
    private HSSFSheet getHSSFSheetByFile(InputStream inputStream) throws IOException {
        if (inputStream == null) {
            return null;
        }
        //获取 excel 文档实体类
        HSSFWorkbook hssfWorkbook = new HSSFWorkbook(inputStream);

        // 从第一个Sheet开始获取，获取到就返回
        for (int numSheet = 0; numSheet < hssfWorkbook.getNumberOfSheets(); numSheet++) {
            HSSFSheet hssfSheet = hssfWorkbook.getSheetAt(numSheet);
            if (hssfSheet != null) {
                return hssfSheet;
            }
        }
        return null;
    }


    /**
     * 去除 列名中的 空格、特殊符号 等
     *
     * @param columnName
     * @return
     */
    private String trimColumnName(String columnName) {
        if (columnName == null) {
            return null;
        }
        // 匹配任何空白字符，包括制表符、换页符等等
        Pattern p = Pattern.compile("\\f|\\n|\\r|\\t|\\v");
        Matcher m = p.matcher(columnName);
        return m.replaceAll("").trim();
    }


    /**
     * 获取单元格中的值,并转成 String 类型
     *
     * @param hssfCell
     * @return
     */
    private String getCellValue(HSSFCell hssfCell) {
        // 如果是空
        if (hssfCell == null || hssfCell.getCellType() == HSSFCell.CELL_TYPE_BLANK) {
            return "";
        }

        // 如果是布尔类型的值
        if (hssfCell.getCellType() == HSSFCell.CELL_TYPE_BOOLEAN) {
            return String.valueOf(hssfCell.getBooleanCellValue());
        }

        // 如果是数字类型的值
        if (hssfCell.getCellType() == HSSFCell.CELL_TYPE_NUMERIC) {
            // 如果是时间类型的值
            if (HSSFDateUtil.isCellDateFormatted(hssfCell)) {
                Date date = hssfCell.getDateCellValue();
                return new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(date);
            }
            // 将cell设置成 string 类型，防止获取 数值型 数据时出错
            hssfCell.setCellType(HSSFCell.CELL_TYPE_STRING);
            return String.valueOf(hssfCell.getStringCellValue());
        }

        //如果是公式
        if (hssfCell.getCellType() == Cell.CELL_TYPE_FORMULA) {
            String value;
            try {
                value = String.valueOf(hssfCell.getNumericCellValue());
            } catch (IllegalStateException e) {
                value = String.valueOf(hssfCell.getRichStringCellValue());
            }
            return value;
        }

        // 返回字符串类型的值
        return String.valueOf(hssfCell.getStringCellValue());
    }

    /**
     * 将单元格中的值换成对应下拉框的value
     */
    private String getCellOptionValue(String optionName, String name) {
        // 根据下列框名称获取下拉框
        Map<String, String> o = optionMap.get(optionName);
        if (o == null) {
            return null;
        }
        //根据名称获取值
        return o.get(name);
    }

    /**
     * 校验值是否合法
     */
    private boolean cellValueVerify(String optionName, String cellValue) {
        if (cellValue == null) {
            return false;
        }
        // 根据下列框名称获取下拉框
        Map<String, String> o = optionMap.get(optionName);
        if (o == null) {
            return false;
        }
        return o.containsValue(cellValue);
    }
}

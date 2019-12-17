package util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class utils {

    public static boolean stringIsBlank(String str) {
        return (str == null || "".equals(str));
    }

    /**
     * 自动分析字符串并尝试转换为日期对象<br>
     * 可转换示例
     *
     * <pre>
     * 2012-3-21, 2012-03-21
     * 2012/3/21, 2012/03/21
     * 2012.3.21, 2012.03.21
     * 2012-3-21 18:11:43, 2012-03-21 18:11:43
     * 2012-3-21 18:11:43.567, 2012-03-21 18:11:43.567
     * 2012/3/21 18:11:43, 2012/03/21 18:11:43
     * 2012/3/21 18:11:43.567, 2012/03/21 18:11:43.567
     * 2012.3.21 18:11:43, 2012.03.21 18:11:43
     * 2012.3.21 18:11:43.567, 2012.03.21 18:11:43.567
     * Wed Mar 21 18:11:43 CST 2012
     * 21/3/2012, 21/03/2012
     * 20120321
     * 1332324703000
     * </pre>
     *
     * @param dateTime 日期字符串
     * @return 一个 Date，如果无法分析dateTime，则为 null
     * @author Jesse Lu
     */
    public static Date autoDate(String dateTime) {
        if (stringIsBlank(dateTime)) {
            return null;
        }
        String[][] patterns = {{"\\d{4}(-\\d{1,2}){2}", "yyyy-M-d", "zh"}, // 2012-03-21, 2012-3-21
                {"\\d{4}(-\\d{1,2}){2} \\d{1,2}(:\\d{1,2}){2}", "yyyy-M-d H:m:s", "zh"}, // 2012-03-21 18:11:43
                {"\\d{4}(-\\d{1,2}){2} \\d{1,2}(:\\d{1,2}){2}\\.\\d{1,3}", "yyyy-M-d H:m:s.S", "zh"}, // 2012-03-21 18:11:43.567
                {"([A-Z][a-z]{2} ){2}\\d{1,2} \\d{1,2}(:\\d{1,2}){2} [A-Z]{3} \\d{4}", "EEE MMM d H:m:s z yyyy", "en"}, // Wed Mar 21 18:11:43 CST 2012
                {"\\d{4}(/\\d{1,2}){2}", "yyyy/M/d", "zh"}, // 2012/03/21, 2012/3/21
                {"\\d{4}(/\\d{1,2}){2} \\d{1,2}(:\\d{1,2}){2}", "yyyy/M/d H:m:s", "zh"}, // 2012/03/21 18:11:43
                {"\\d{4}(/\\d{1,2}){2} \\d{1,2}(:\\d{1,2}){2}\\.\\d{1,3}", "yyyy/M/d H:m:s.S", "zh"}, // 2012/03/21 18:11:43.567
                {"\\d{4}(\\.\\d{1,2}){2}", "yyyy.M.d", "zh"}, // 2012.03.21, 2012.3.21
                {"\\d{4}(\\.\\d{1,2}){2} \\d{1,2}(:\\d{1,2}){2}", "yyyy.M.d H:m:s", "zh"}, // 2012.03.21 18:11:43
                {"\\d{4}(\\.\\d{1,2}){2} \\d{1,2}(:\\d{1,2}){2}\\.\\d{1,3}", "yyyy.M.d H:m:s.S", "zh"}, // 2012.03.21 18:11:43.567
                {"(\\d{1,2}/){2}\\d{4}", "d/M/yyyy", "zh"}, // 21/03/2012, 21/3/2012
                {"\\d{8}", "yyyyMMdd", "zh"}, // 20120321
                {"\\d{4}(-\\d{1,2}){1}", "yyyy-M", "zh"},// 2012-03, 2012-3
                {"\\d{4}", "yyyy", "zh"},// 2012
        };
        for (int i = 0; i < patterns.length; i++) {
            if (dateTime.matches(patterns[i][0])) {
                SimpleDateFormat format = new SimpleDateFormat(patterns[i][1], new Locale(patterns[i][2]));
                try {
                    return format.parse(dateTime);
                } catch (ParseException e) {
                    // logger.warn("日期格式[" + dateTime + "]自动转换错误");
                    return null;
                }
            }
        }
        if (isNumeric(dateTime)) return new Date(Long.valueOf(dateTime)); // 1332324703000
        return null;
    }

    public static boolean isNumeric(final CharSequence cs) {
        if (isEmpty(cs)) {
            return false;
        }
        final int sz = cs.length();
        for (int i = 0; i < sz; i++) {
            if (!Character.isDigit(cs.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    public static boolean isEmpty(final CharSequence cs) {
        return cs == null || cs.length() == 0;
    }
}

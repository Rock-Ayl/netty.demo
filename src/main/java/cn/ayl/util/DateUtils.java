package cn.ayl.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * created by Rock-Ayl on 2019-6-28
 * 时间工具类(包括:取出本年，本月，本日的时间，时间戳，类型转换，精确到 00:00:00 和 23:59:59)
 */
public class DateUtils {

    protected static Logger logger = LoggerFactory.getLogger(DateUtils.class);

    //正常
    public final static SimpleDateFormat SDF_Normal = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    //年月日版
    public final static SimpleDateFormat SDF_Text = new SimpleDateFormat("yyyy年MM月dd日HH:mm:ss");
    //xls版
    public final static SimpleDateFormat SDF_Data = new SimpleDateFormat("yyyy/MM/dd");
    //小时
    public final static SimpleDateFormat SDF_Hour = new SimpleDateFormat("HH");
    //天
    public final static SimpleDateFormat SDF_Day = new SimpleDateFormat("dd");
    //月
    public final static SimpleDateFormat SDF_Month = new SimpleDateFormat("MM");
    //年
    public final static SimpleDateFormat SDF_Year = new SimpleDateFormat("yyyy");
    //年+月
    public final static SimpleDateFormat SDF_Year_Month = new SimpleDateFormat("yyyy-MM");
    //年+月+日 没有横
    public final static SimpleDateFormat SDF_NOT_Cross = new SimpleDateFormat("yyyyMMdd");
    //年+月+日
    public final static SimpleDateFormat SDF_Cross = new SimpleDateFormat("yyyy-MM-dd");

    //静态资源-文件最后修改时间格式
    public static final SimpleDateFormat SDF_HTTP_DATE_FORMATTER = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US);

    /**
     * 获取星期
     */
    public static String getWeek(Date date) {
        String[] weekDays = {"星期日", "星期一", "星期二", "星期三", "星期四", "星期五", "星期六"};
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        int w = cal.get(Calendar.DAY_OF_WEEK) - 1;
        if (w < 0) {
            w = 0;
        }
        return weekDays[w];
    }

    /**
     * 获取指定日期所在月份开始的时间戳
     *
     * @param date 指定日期 new Date()
     * @return eg: 2019-12-1 00:00:00 999
     */
    public static Long getMonthStart(Date date) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        //设置为1号,当前日期既为本月第一天
        c.set(Calendar.DAY_OF_MONTH, 1);
        //将小时至0
        c.set(Calendar.HOUR_OF_DAY, 0);
        //将分钟至0
        c.set(Calendar.MINUTE, 0);
        //将秒至0
        c.set(Calendar.SECOND, 0);
        //将毫秒至0
        c.set(Calendar.MILLISECOND, 0);
        // 获取本月第一天的时间戳
        return c.getTimeInMillis();
    }

    /**
     * 获取指定日期所在月份开始的时间戳
     *
     * @param date 指定日期 eg:  2019-04
     * @return eg: 2019-12-1 00:00:00 999
     */
    public static Long getMonthStart(String date) throws ParseException {
        return getMonthStart(getYear(date));
    }

    /**
     * 获取指定日期所在月份结束的时间戳
     *
     * @param date 指定日期 new Date()
     * @return eg: 2019-12-31 23:59:59  999
     */
    public static Long getMonthEnd(Date date) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        //设置为当月最后一天
        c.set(Calendar.DAY_OF_MONTH, c.getActualMaximum(Calendar.DAY_OF_MONTH));
        //将小时至23
        c.set(Calendar.HOUR_OF_DAY, 23);
        //将分钟至59
        c.set(Calendar.MINUTE, 59);
        //将秒至59
        c.set(Calendar.SECOND, 59);
        //将毫秒至999
        c.set(Calendar.MILLISECOND, 999);
        // 获取本月最后一天的时间戳
        return c.getTimeInMillis();
    }

    /**
     * 获取指定日期所在月份结束的时间戳
     *
     * @param date 指定日期 2019-04
     * @return eg: 2019-12-31 23:59:59  999
     */
    public static Long getMonthEnd(String date) throws ParseException {
        return getMonthEnd(getYear(date));
    }

    /**
     * 获取String文字版
     *
     * @return
     */
    public static String getDateString() {
        return SDF_Text.format(new Date());
    }

    /**
     * 获取String
     *
     * @return eg:1974-12-19 14:31:06
     */
    public static String getDateString(long time) {
        return SDF_Normal.format(new Date(time));
    }

    /**
     * 获取天
     *
     * @return
     */
    public static String getDay(long time) {
        return SDF_Day.format(new Date(time));
    }

    /**
     * 获取月
     *
     * @param time
     * @return
     */
    public static String getMonth(long time) {
        return SDF_Month.format(new Date(time));
    }

    /**
     * 获取月
     *
     * @param time
     * @return
     */
    public static String getHour(long time) {
        return SDF_Hour.format(new Date(time));
    }

    /**
     * 获取年
     *
     * @param time
     * @return
     */
    public static String getYear(long time) {
        return SDF_Year.format(new Date(time));
    }

    /**
     * String转时间戳
     *
     * @return
     * @throws ParseException
     */
    public static long toStamp(String s) throws ParseException {
        return SDF_Normal.parse(s).getTime();
    }

    /**
     * 获取本月开始日期
     *
     * @return String
     **/
    public static String getMonthStart() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MONTH, 0);
        cal.set(Calendar.DAY_OF_MONTH, 1);
        Date time = cal.getTime();
        return SDF_Normal.format(getStartByThisDay(time));
    }

    /**
     * 获取本月最后一天
     *
     * @return String
     **/
    public static String getMonthEnd() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
        Date time = cal.getTime();
        return SDF_Normal.format(getStartByThisDay(time));
    }

    /**
     * 获取本周的第一天
     *
     * @return String
     **/
    public static String getWeekStart() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.WEEK_OF_MONTH, 0);
        cal.set(Calendar.DAY_OF_WEEK, 2);
        Date time = cal.getTime();
        return SDF_Normal.format(getStartByThisDay(time));
    }

    /**
     * 获取本周的最后一天
     *
     * @return String
     **/
    public static String getWeekEnd() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_WEEK, cal.getActualMaximum(Calendar.DAY_OF_WEEK));
        cal.add(Calendar.DAY_OF_WEEK, 1);
        Date time = cal.getTime();
        return SDF_Normal.format(getStartByThisDay(time));
    }

    /**
     * 获取本年的第一天
     *
     * @return String
     **/
    public static String getYearStart() {
        Date now = new Date();
        Calendar c = Calendar.getInstance();
        c.setTime(now);
        c.set(Calendar.DAY_OF_YEAR, 1);
        return SDF_Normal.format(getStartByThisDay(c.getTime()));
    }

    /**
     * 获取本年的最后一天
     *
     * @return String
     **/
    public static String getYearEnd() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.MONTH, calendar.getActualMaximum(Calendar.MONTH));
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
        Date currYearLast = calendar.getTime();
        return SDF_Normal.format(getStartByThisDay(currYearLast));
    }

    /**
     * 获得某天最大时间 2017-10-15 23:59:59
     *
     * @param date
     * @return
     */
    public static Date getEndByThisDay(Date date) {
        LocalDateTime localDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(date.getTime()), ZoneId.systemDefault());
        LocalDateTime endOfDay = localDateTime.with(LocalTime.MAX);
        return Date.from(endOfDay.atZone(ZoneId.systemDefault()).toInstant());
    }

    /**
     * 获得某天最小时间 2017-10-15 00:00:00
     *
     * @param date
     * @return
     */
    public static Date getStartByThisDay(Date date) {
        LocalDateTime localDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(date.getTime()), ZoneId.systemDefault());
        LocalDateTime startOfDay = localDateTime.with(LocalTime.MIN);
        return Date.from(startOfDay.atZone(ZoneId.systemDefault()).toInstant());
    }

    /**
     * 获取年份时间戳
     * eg: 2019-04 -> 1554048000000
     *
     * @param value
     * @return
     */
    public static Date getYear(String value) throws ParseException {
        return new Date(SDF_Year_Month.parse(value).getTime());
    }

    /**
     * 解析中文为时间戳
     *
     * @param date   eg: 2020/12/31
     * @param format
     * @return 1554048000000
     * @throws ParseException
     */
    public static long parse(String date, SimpleDateFormat format) throws ParseException {
        return format.parse(date).getTime();
    }

}
/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package org.researchedc.core.form;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Pattern;


/**
 *
 * Help class for string usage
 *
 * @author jxu
 */
public class StringUtil {

    @Deprecated
    public static boolean isBlank(String s) {
        return s == null ? true : s.trim().equals("") ? true : false;

    }

    public static boolean isNumber(String s) {
        // To Do: whether we consider a blank string is still a number?
        return Pattern.matches("[0-9]*", s) ? true : false;

    }

    // isEmail removed — email delivery retired (0 active callers, run-66)

    public static String join(String glue, ArrayList a) {
        String answer = "";
        String join = "";

        for (int i = 0; i < a.size(); i++) {
            String s = (String) a.get(i);
            answer += join + s;
            join = glue;
        }

        return answer;
    }

    /**
     * Invoked method that uses the default locale.
     * @param s
     * @param dateFormat
     * @return
     */
    // ywang (Nov., 2008)
    public static boolean isFormatDate(String s, String dateFormat) { 
        String dateformat = dateFormat;
        while (dateformat.contains("Y")) { dateformat = dateformat.replace("Y", "y"); }
        while (dateformat.contains("D")) { dateformat = dateformat.replace("D", "d"); }
        SimpleDateFormat sdf1 = new SimpleDateFormat(dateformat);
        sdf1.setLenient(false);
        SimpleDateFormat sdf2 = new SimpleDateFormat(dateformat);
        sdf2.setLenient(false);
        try {
            Date d1 = sdf1.parse(s);
            try {
                String temp = sdf2.format(d1);
                if (temp.equals(s)) {
                    Calendar c = Calendar.getInstance();
                    c.setTime(d1);
                    int year = c.get(Calendar.YEAR);
                    return year <= 9999 && year >= 1000;
                } else {
                    return false;
                }
            } catch (Exception e) {
                return false;
            }
        } catch (Exception e) {
            return false;
        }
    }
    
    public static boolean isFormatDate(String s, String dateFormat, Locale locale) {
        String dateformat = dateFormat;
        while (dateformat.contains("Y")) { dateformat = dateformat.replace("Y", "y"); }
        while (dateformat.contains("D")) { dateformat = dateformat.replace("D", "d"); }
        SimpleDateFormat sdf1 = new SimpleDateFormat(dateformat, locale);
        sdf1.setLenient(false);
        SimpleDateFormat sdf2 = new SimpleDateFormat(dateformat, locale);
        sdf2.setLenient(false);
        try {
            Date d1 = sdf1.parse(s);
            try {
                String temp = sdf2.format(d1);
                if (temp.equals(s)) {
                    Calendar c = Calendar.getInstance();
                    c.setTime(d1);
                    int year = c.get(Calendar.YEAR);
                    return year <= 9999 && year >= 1000;
                } else {
                    return false;
                }
            } catch (Exception e) {
                return false;
            }
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Return true if a string can be parsed by the dateFormat with locale.
     *
     * @param s
     * @param dateFormat
     * @param locale
     * @return
     */
    //ywang (Oct., 2011)
    public static boolean isDateFormatString(String s, String dateFormat, Locale locale) {
        String dateformat = dateFormat;
        while (dateformat.contains("Y")) { dateformat = dateformat.replace("Y", "y"); }
        while (dateformat.contains("D")) { dateformat = dateformat.replace("D", "d"); }
        SimpleDateFormat f = new SimpleDateFormat(dateformat, locale);
        f.setLenient(false);
        try {
            f.parse(s);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    /**
     * Allow only 4 digits, no more, no less. SimpleDateFormat uses the default locale.
     *
     * @param s
     * @param yearFormat
     * @return
     */
    //ywang (Nov., 2008)
    public static boolean isPartialYear(String s, String yearFormat) {
        return partialYear(s, yearFormat, null);
    }

    public static boolean isPartialYear(String s, String yearFormat, Locale locale) {
        return partialYear(s, yearFormat, locale);
    }

    private static boolean partialYear(String s, String yearFormat, Locale locale) {
        int dn = 0;
        char[] cyear = s.toCharArray();
        for (char c : cyear) {
            if (!Character.isDigit(c)) {
                return false;
            }
            ++dn;
        }
        if(dn != 4) {
            return false;
        }
        String yearformat = yearFormat;
        while (yearformat.contains("Y")) { yearformat = yearformat.replace("Y", "y"); }
        while (yearformat.contains("D")) { yearformat = yearformat.replace("D", "d"); }
        yearformat = yearformat + "-MM-dd";
        SimpleDateFormat sdf_y;
        if(locale == null) {
            sdf_y = new SimpleDateFormat(yearformat);
        }else {
            sdf_y = new SimpleDateFormat(yearformat, locale);
        }
        sdf_y.setLenient(false);
        String sy = s + "-01-18";
        try {
            sdf_y.parse(sy);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * The year can only between 1000 and 9999.
     *
     * @param s
     * @param yearMonthFormat
     * @return
     */
    //ywang (Nov., 2008)
    public static boolean isPartialYearMonth(String s, String yearMonthFormat) {
        String yearmonthformat = yearMonthFormat;
        while (yearmonthformat.contains("Y")) { yearmonthformat = yearmonthformat.replace("Y", "y"); }
        while (yearmonthformat.contains("D")) { yearmonthformat = yearmonthformat.replace("D", "d"); }
        yearmonthformat = yearmonthformat + "-dd";
        String sym = s + "-18";
        SimpleDateFormat sdf1 = new SimpleDateFormat(yearmonthformat);
        sdf1.setLenient(false);
        SimpleDateFormat sdf2 = new SimpleDateFormat(yearmonthformat);
        sdf2.setLenient(false);
        try {
            Date d1 = sdf1.parse(sym);
            try {
                String temp = sdf2.format(d1);
                if (temp.equals(sym)) {
                    Calendar c = Calendar.getInstance();
                    c.setTime(d1);
                    int year = c.get(Calendar.YEAR);
                    return year <= 9999 && year >= 1000;
                } else {
                    return false;
                }
            } catch (Exception e) {
                return false;
            }
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean isPartialYearMonth(String s, String yearMonthFormat, Locale locale) {
        String yearmonthformat = yearMonthFormat;
        while (yearmonthformat.contains("Y")) { yearmonthformat = yearmonthformat.replace("Y", "y"); }
        while (yearmonthformat.contains("D")) { yearmonthformat = yearmonthformat.replace("D", "d"); }
        yearmonthformat = yearmonthformat + "-dd";
        String sym = s + "-18";
        SimpleDateFormat sdf1 = new SimpleDateFormat(yearmonthformat, locale);
        sdf1.setLenient(false);
        SimpleDateFormat sdf2 = new SimpleDateFormat(yearmonthformat, locale);
        sdf2.setLenient(false);
        try {
            Date d1 = sdf1.parse(sym);
            try {
                String temp = sdf2.format(d1);
                if (temp.equals(sym)) {
                    Calendar c = Calendar.getInstance();
                    c.setTime(d1);
                    int year = c.get(Calendar.YEAR);
                    return year <= 9999 && year >= 1000;
                } else {
                    return false;
                }
            } catch (Exception e) {
                return false;
            }
        } catch (Exception e) {
            return false;
        }
    }

    // /**
    // * @param s A string of words, which are substrings separated
    // * by non-word characters (reg ex "\W").
    // * @param numWords The number of words to cut-off at.
    // * @return A string composed of the first <code>numWords</code> words of
    // <code>s</code>.
    // */
    // public static String firstWords(String s, int numWords) {
    // Pattern p = Pattern.compile("\\W");
    // String[] pieces = p.split(s, numWords);
    // ArrayList a = new ArrayList(Arrays.asList(pieces));
    // return join(" ", a);
    // }
}

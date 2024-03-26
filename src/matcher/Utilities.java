package matcher;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * Created on Sep 29, 2013
 * @author Derek Worth
 */
public class Utilities {
    // used in getDatesByInterval method
    public static final int INTERVAL_TYPE_MONTHLY = 0;
    public static final int INTERVAL_TYPE_WEEKLY  = 1;
    public static final int INTERVAL_TYPE_DAILY   = 2;
    
    public static String toFraction(String number) throws Exception {
        
        // limits expression input to the following characters: 0123456789/*+-()
        for(int i = 0; i < number.length(); i++) {
            if ((number.charAt(i)<'0' || number.charAt(i)>'9')
                    && number.charAt(i)!='.') {
                throw new Exception();
            }
        }
        
        // removes decimals
        String result = "";
        String whole = "";
        String numer = "";
        String denom = "1";
        char curr;
        boolean afterDec = false;
        boolean beforeDec = false;
        
        // adds decimal to end if missing
        if(!number.contains(".")) {
            number += ".0";
        }
        // extract whole number and fraction
        for(int i = 0; i<number.length(); i++) {
            curr = number.charAt(i);
            if(!afterDec) {
                if(!beforeDec) {
                    if(curr=='.') {
                        afterDec = true;
                        beforeDec = false;
                    } else if(curr>='0' && curr<='9') {
                        beforeDec = true;
                        whole += curr;
                    } else {
                        result += curr;
                    }
                } else {
                    if(curr=='.') {
                        afterDec = true;
                        beforeDec = false;
                    } else if(curr>='0' && curr<='9') {
                        whole += curr;
                    } else {
                        beforeDec = false;
                        result += whole + curr;
                        whole = "";
                    }
                }
            } else {
                if(curr>='0' && curr<='9') {
                    numer += curr;
                    denom += "0";
                } else {
                    afterDec = false;
                    if(whole.length()>0) {
                        if(numer.length()>0) {
                            result += "(" + whole + "+" + numer + "/" + denom + ")";
                        } else {
                            result += whole;
                        }
                    } else {
                        if(numer.length()>0) {
                            result += numer + "/" + denom;
                        }
                    }
                    whole = "";
                    numer = "";
                    denom = "1";
                    result += curr;
                }
            }
            
            // after all characters have been processed, convert to format: whole+numer/denom
            if(i+1==number.length()) {
                if(whole.length()>0) {
                    if(numer.length()>0) {
                        result += "(" + whole + "+" + numer + "/" + denom + ")";
                    } else {
                        result += "(" + whole + "+0/10)";
                    }
                } else {
                    if(numer.length()>0) {
                        result += "(" + numer + "/" + denom + ")";
                    }
                }
            }
        }
        return result;
    }
    
    public static String getDuration(long sec) {
        long remainder = sec;
        long y, d, h, m, s;
        String duration = "";
        
        y = remainder / 31536000;
        remainder %= 31536000;
        if(y>0)
            duration += y + " yr(s) : ";
        
        d = remainder / 86400;
        remainder %= 86400;
        if(d>0)
            duration += d + " day(s) : ";
        
        h = remainder / 3600;
        remainder %= 3600;
        if(h>0)
            duration += h + " hr(s) : ";
        
        m = remainder / 60;
        remainder %= 60;
        if(m>0)
            duration += m + " min(s) : ";
        
        s = remainder / 1;
        if(s>0) 
            duration += s + " sec(s)";
        
        if(duration.endsWith(" : ")) 
            duration = duration.substring(0, duration.length()-3);
        
        return duration;
    }
    
    /**
     * Provides the current getTimestamp
     * @return getTimestamp in format: YYYY-MM-DD hh:mm:ss
     */
    public static String getTimestamp() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime());
    }
    
    public static String getDatestamp(int fromToday) {
        Calendar c = Calendar.getInstance();
        c.add(Calendar.DAY_OF_MONTH, fromToday);
        return new SimpleDateFormat("yyyy-MM-dd").format(c.getTime());
    }
    
    public static String getNewDate(String dateString, int delta) {
        
        if(!isDate(dateString)) {
            return "";
        }
        
        int yr  = Integer.parseInt(dateString.substring(0, 4));
        int mth = Integer.parseInt(dateString.substring(5, 7));
        int day = Integer.parseInt(dateString.substring(8, 10));
        
        if(delta==0) {
            return dateString;
        } else if (delta < 0) {
            day += delta;
            while(day<1) {
                mth--; // move to next prior month
                if(mth<1) {
                    yr--; // move to next prior year
                    mth = 12;
                }
                day = daysInMonth(yr, mth) + day;
            }
        } else {
            day += delta;
            while(day>daysInMonth(yr, mth)) {
                day -= daysInMonth(yr, mth);
                mth++; // move to next month
                if(mth>12) {
                    yr++; // move to next year
                    mth = 1;
                }
            }
        }
        
        String newDate = yr + "-";
        if(mth<10) {
            newDate += "0" + mth + "-";
        } else {
            newDate += mth + "-";
        }
        if(day<10) {
            newDate += "0" + day;
        } else {
            newDate += day;
        }
        return newDate;
    }
    
    public static int daysInMonth(int yr, int mth) {
        // gets max days in month
        Calendar mycal = new GregorianCalendar(yr, mth-1 , 1);
        // Get the number of days in that month
        return mycal.getActualMaximum(Calendar.DAY_OF_MONTH);
    }
    
    public static String getAddress(String addr) {
        int a = addr.indexOf("<");
        int b = addr.indexOf(">");
        if(a==-1 || b == -1)
            return addr;
        return addr.substring(a+1, b);
    }
    
    public static String validateDate(String date) {
        String newDate = "";
        // remove non-digit characters from date
        for(int i = 0; i < date.length(); i++) {
            if(date.charAt(i)>='0' && date.charAt(i)<='9') {
                newDate += date.charAt(i);
            }
        }
        // add dashes to date
        if(newDate.length()==8) {
            newDate = newDate.substring(0, 4) + "-" + newDate.substring(4, 6) + "-" + newDate.substring(6, 8);
            if(Utilities.isDate(newDate)) {
                return newDate;
            }
        }
        return "";
    }
    
    /**
     * Produces an array of dates, starting with today, for specified interval and quantity
     * @param intervalType INTERVAL_TYPE_MONTLY = 0, INTERVAL_TYPE_WEEKLY = 1, INTERVAL_TYPE_DAILY = 2; monthly is every 30 days, weekly 7 days, and daily self-explanatory
     * @param intervalCount number dates requested
     * @return String array containing specified number of dates (format 'yyyy-mm-dd') at given interval
     */
    public static String[] getDatesByInterval(int intervalType, int intervalCount) {
        String date = Utilities.getDatestamp(0); // sets initial date for today
        String [] dates = new String[intervalCount];
        
        for(int i = 1; i<=intervalCount; i++) {
            dates[i-1] = date;
            if(intervalType==INTERVAL_TYPE_MONTHLY) {       // monthly
                date = Utilities.getNewDate(date, -30);
            } else if(intervalType==INTERVAL_TYPE_WEEKLY) { // weekly
                date = Utilities.getNewDate(date, -7);
            } else {                                        // daily
                date = Utilities.getNewDate(date, -1);
            }
        }
        
        return dates;
    }
    
    /**
     * Checks that string representation of date is in the format: YYYY-MM-DD
     * @param dateString String representation of a date
     * @return true if successful, false otherwise
     */
    public static boolean isDate(String dateString) {
        // checks for correct length
        if (dateString.length()!=10) {
            return false;
        }
        // checks that date values are numerical
        int yr, mth, day;
        try{
            yr  = Integer.parseInt(dateString.substring(0, 4));
            mth = Integer.parseInt(dateString.substring(5, 7));
            day = Integer.parseInt(dateString.substring(8, 10));
        } catch(NumberFormatException e) {
            return false;
        }
        
        // gets max days in month
        Calendar mycal = new GregorianCalendar(yr, mth-1 , 1);
        // Get the number of days in that month
        int daysInMonth = mycal.getActualMaximum(Calendar.DAY_OF_MONTH);
        
        if (    yr<1884 || yr>2189 ||           // checks year
                mth<1 || mth>12 ||              // checks month
                day<1 || day>daysInMonth ||     // checks day
                dateString.charAt(4)!='-' ||    // checks first dash
                dateString.charAt(7)!='-'       // checks second dash
                ){
            return false;
        }
        return true;
    }
    
    /**
     * Checks that string representation of getTimestamp is in the format: 
     * YYYY-MM-DD hh:mm:ss
     * @param timestampString String representation of a getTimestamp
     * @return true if successful, false otherwise
     */
    public static boolean isTimestamp(String timestampString) {
        // checks for correct length
        if (timestampString.length()!=19) {
            return false;
        } else if (!isDate(timestampString.substring(0,10))) { // year/month/day
            return false;
        }
        // checks the time
        int hr, min, sec;
        try{
            hr  = Integer.parseInt(timestampString.substring(11, 13));
            min = Integer.parseInt(timestampString.substring(14, 16));
            sec = Integer.parseInt(timestampString.substring(17, 19));
        } catch(NumberFormatException e) {
            return false;
        }
        
        if (hr<0  || hr>23 ||                  // checks hour
           min<0 || min>59 ||                 // checks minute
           sec<0 || sec>59 ||                 // checks second
           timestampString.charAt(13)!=':' || // checks first colon
           timestampString.charAt(16)!=':'){  // checks second colon
            return false;
        }
        return true;
    }
    
    public static boolean isFirstCharacterALetter(String text) {
        // check for null/empty text
        if(text==null || text.length()==0)
            return false;
        // get first character
        char firstChar = text.charAt(0);
        // check character for letter
        if((firstChar>='a' && firstChar <='z') || (firstChar>='A' && firstChar <='Z'))
            return true;
        // return false if not a letter
        return false;
    }
    
    // validates name contains only numbers, letters, or the following characters: '-'  '('  ')'
    public static boolean isValidContainerName(String name) {
        if(name==null || name.length()==0 || !isFirstCharacterALetter(name)) {
            return false;
        }
        name = name.toLowerCase();
        // check each letter
        for(int i = 0; i< name.length(); i++) {
            char c = name.charAt(i);
            if( c=='-' || 
                c=='(' || 
                c==')' || 
                (c>='a' && c<='z') ||
                (c>='0' && c<='9')) {
                // do nothing
            } else {
                return false;
            }
        }
        return true;
    }
    
    public static boolean isValidDescription(String desc) {
        if(desc.length()>100) {
            return false;
        }
        // check each letter
        // valid characters include A-Z a-z 0-9 - ( ) @ # $ & '
        for(int i = 0; i< desc.length(); i++) {
            char c = desc.charAt(i);
            if( c=='-' || 
                c=='<' || 
                c=='>' || 
                c=='(' || 
                c==')' || 
                c=='@' || 
                c=='#' || 
                c=='$' || 
                c=='&' || 
                c=='.' || 
                c==' ' || 
                c=='\'' ||  
                c=='/' || 
                c=='\t' || 
                (c>='a' && c<='z') ||
                (c>='A' && c<='Z') ||
                (c>='0' && c<='9')) {
                // do nothing
            } else {
                return false;
            }
        }
        return true;
    }
    
    public static boolean isValidAmount(String amt) {
        try {
            Double.parseDouble(amt);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }
    
    public static boolean isValidUsername(String un) {
        un = un.toLowerCase();
        if(un.length()>0) { // must contain at least 1 letter
            if((un.charAt(0)>='a' && un.charAt(0)<='z')) { // first character must be a letter
                for(int i = 1; i < un.length(); i++) {
                    if((un.charAt(i)<'a' || un.charAt(i)>'z') && (un.charAt(i)<'0' || un.charAt(i)>'9')) { // must contain letters and numbers only
                        return false;
                    }
                }
                return true;
            }
        }
        return false;
    }
    
    public static boolean isValidPassword(String pw) {
        if(pw.length()>3) { // must contain at least 4 characters
            for(int i = 1; i < pw.length(); i++) {
                if(pw.charAt(i)==' ' || pw.charAt(i)=='\n' || pw.charAt(i)=='\t' || pw.charAt(i)=='\r') { // must not contain white spaces 
                    return false;
                }
//                if((pw.charAt(i)<'a' || pw.charAt(i)>'z') && (pw.charAt(i)<'0' || pw.charAt(i)>'9')) { // must contain letters, numbers, and/or the following special characters 
//                    return false;
//                }
            }
            return true;
        }
        return false;
    }
    
    public static String shortenString(String text, int shortenTo) {
        if(text.length()>shortenTo) {
            text = text.substring(0,shortenTo) + "...";
        }
        return text;
    }
    
    public static String doubleApostrophes(String txt) {
        String tmp = "";
        for(int i = 0; i < txt.length(); i++) {
            if(txt.charAt(i)=='\'') {
                tmp += "'";
            }
            tmp += txt.charAt(i);
        }
        return tmp;
    }
    
    public static String removeDoubleApostrophes(String txt) {
        while(txt.contains("''")) {
            txt = txt.replace("''", "'");
        }
        return txt;
    }
    
    public static String removeCommas(String txt) {
        while(txt.contains(",")) {
            txt = txt.replace(",", "");
        }
        return txt;
    }
    
    public static String trimInvalidCharacters(String desc) {
        String tmp = "";
        for(int i = 0; i< desc.length(); i++) {
            char c = desc.charAt(i);
            if( c=='-' || 
                c=='<' || 
                c=='>' || 
                c=='(' || 
                c==')' || 
                c=='.' || 
                c=='*' || 
                c=='@' || 
                c=='#' || 
                c=='$' || 
                c=='&' || 
                c==' ' || 
                c=='\'' ||  
                c=='/' || 
                c=='\t' || 
                (c>='a' && c<='z') ||
                (c>='A' && c<='Z') ||
                (c>='0' && c<='9')) {
                tmp += c;
            }
        }
        return tmp;
    }
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package matcher;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 *
 * @author derek
 */
public class Main {
    public static int envCnt = 0;
    public static int usaaCnt = 0;
    public static String[][] env;
    public static String[][] usaa;
    public static boolean[] split;
    public static boolean[] envToUsaa; // sized to match usaa (signifies match to envelope transaction)
    
    public static void main(String args[]) {
        if(setEnvelopeCount() && setUsaaCount()) {
            if(setEnvelope() && setUsaa()) {
                printReport();
            }
        }
    }
    
    public static void printReport() {
        String outputFile = "balanced-ledger.csv";
        try (Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputFile), "utf-8"))) {
            writer.write("Date,Desc,Amt,Split,,Date,Desc,Amt,,Matched\n");
            String eDate, eDesc, eAmt, eSplit, uDate, uDesc, uAmt;
            for(int i = 0; i < envCnt; i++) {
                eDate  = env[i][0];
                eDesc  = env[i][1];
                eAmt   = env[i][2];
                eSplit = env[i][3];
                uDate  = "";
                uDesc  = "";
                uAmt   = "";
                
                for(int j = 0; j < usaaCnt; j++) {
                    if(!envToUsaa[j] && eSplit.equalsIgnoreCase(usaa[j][2]) && (daysBetweenDates(eDate, usaa[j][0]) <= 2)) {
                        uDate  = usaa[j][0];
                        uDesc  = usaa[j][1];
                        uAmt   = usaa[j][2];
                        envToUsaa[j] = true;
                        break;
                    }
                }
                if(uDate.length()==0 && !eSplit.equals("--"))
                    writer.write(eDate + "," + eDesc + "," + eAmt + "," + eSplit + ",," + uDate + "," + uDesc + "," + uAmt + "\n");
                else
                    writer.write(eDate + "," + eDesc + "," + eAmt + "," + eSplit + ",," + uDate + "," + uDesc + "," + uAmt + ",,X\n");
            }
            
            writer.write("\n,,,,,,USAA Transactions not accounted for:\n");
            writer.write(",,,,,Date,Desc,Amt\n");
            for(int i = 0; i < usaaCnt; i++) {
                if(!envToUsaa[i]) {
                    writer.write(",,,,," + usaa[i][0] + "," + usaa[i][1] + "," + usaa[i][2] + "\n");
                }
            }
            System.out.println("Success!");
        } catch (IOException e) {
            System.out.println("ERROR: could not write to file '" + outputFile + "'");
        }
    }
    
    public static boolean setEnvelope() {
        String file = "envs.csv";
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            int curr = 0;
            String splitAmt = "";
            while ((line = br.readLine()) != null) {
                String[] cols = line.split(",");
                if(!cols[0].equals("Date")) { //skip first row of headers
                    env[curr][0] = cols[0];            //date
                    env[curr][1] = cols[1];            //desc
                    env[curr][2] = formatAmt(cols[2]); //amt
                    String[] desc = cols[1].split(" ");
                    if(desc[0].equalsIgnoreCase("SPLIT")) {
                        if(splitAmt.equalsIgnoreCase(desc[1])) {
                            env[curr][3] = "--";
                            split[curr] = true;
                        } else {
                            String amtStr = desc[1].substring(1).substring(0, desc[1].length()-2);
                            env[curr][3] = formatAmt(amtStr);
                            splitAmt = desc[1];
                            split[curr] = false;
                        }
                    } else {
                        env[curr][3] = formatAmt(cols[2]);
                        splitAmt = "";
                        split[curr] = false;
                    }
                    curr++;
                }                
            }
        } catch (Exception e) {
            System.out.println("ERROR: could not set envlopes");
            return false;
        }
        return true;
    }
    
    public static boolean setEnvelopeCount() {
        String file = "envs.csv";
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] cols = line.split(",");
                if(!cols[0].equals("Date")) {
                    envCnt++;
                }                
            }
        } catch (Exception e) {
            return false;
        }
        env = new String[envCnt][4];
        split = new boolean[envCnt];
        return true;
    }
    
    public static boolean setUsaa() {
        String file = "bk_download.csv";
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            int curr = 0;
            br.readLine(); // skip header
            while ((line = br.readLine()) != null) {
                line = formateUsaaLine(line);
                String[] cols = line.split(",");
                if(cols.length >= 6) {
                    usaa[curr][0] = cols[0];
                    usaa[curr][1] = cols[1];
                    usaa[curr][2] = formatAmt(cols[4]);
                    curr++;
                }
            }
        } catch (Exception e) {
            System.out.println("ERROR: could not set usaa");
            return false;
        }
        return true;
    }
    
    public static boolean setUsaaCount() {
        String file = "bk_download.csv";
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                if(line.length() > 0)
                    usaaCnt++;
            }
        } catch (Exception e) {
            return false;
        }
        usaaCnt--; //remove top row (containing column headers)
        usaa = new String[usaaCnt][3];
        envToUsaa = new boolean[usaaCnt];
        return true;
    }
    
    public static int daysBetweenDates(String date1, String date2) {
        String[] tmp;
        tmp = date1.split("-");
        int yr1  = Integer.parseInt(tmp[0]);
        int mth1 = Integer.parseInt(tmp[1]);
        int day1 = Integer.parseInt(tmp[2]);
        Calendar cal1 = new GregorianCalendar(yr1, mth1-1, day1);
        
        tmp = date2.split("-");
        int yr2  = Integer.parseInt(tmp[0]);
        int mth2 = Integer.parseInt(tmp[1]);
        int day2 = Integer.parseInt(tmp[2]);
        Calendar cal2 = new GregorianCalendar(yr2, mth2-1, day2);
        
        int dur = (int)((cal1.getTime().getTime() - cal2.getTime().getTime()) / (1000 * 60 * 60 *24));
        if(dur < 0) {
            dur *= -1;
        }
        return dur;
    }
    
    public static String formatAmt(String amt) {
        try {
            return roundAmount(evaluate(amt));
        } catch (Exception ex) {
            return "";
        }
    }
    
    /**
     * Rounds money amounts by cutting off all digits past two decimal places
     * @param amount The amount requiring money formating
     * @return true if successful, false otherwise
     */
    public static String roundAmount(double amount) {
        String amt = Double.toString(amount);
        boolean hasExpon = amt.contains("E");
        int expon;
        if(hasExpon) {
            // pulls out exponent
            expon = Integer.parseInt(amt.substring(amt.indexOf("E") + 1));
            // negative expressions get rounded down to zero
            if(expon<0) {
                return "0.00";
            }
            int exponIndex = amt.indexOf("E");
            int decimalIndex = amt.indexOf(".");
            String beforeDecimalA = amt.substring(0, decimalIndex);
            String beforeDecimalB = amt.substring(decimalIndex+1, decimalIndex+expon+1) + ".";
            String afterdecimal = amt.substring(decimalIndex+expon+1, exponIndex);
            amt = beforeDecimalA + beforeDecimalB + afterdecimal;
        }
        amt += "00";
        int decimalIndex = amt.indexOf(".");
        if(amt.charAt(decimalIndex+3)=='9') {
            if(amt.charAt(0)=='-') {
                return roundAmount(amount-0.001);
            }
            return roundAmount(amount+0.001);
        } else {
            String roundedAmt = amt.substring(0, decimalIndex+3);
            if(roundedAmt.equalsIgnoreCase("-0.00")) {
                return "0.00";
            }                
            return roundedAmt;
        }
    }
        
    public static double evaluate(String expression) throws Exception {
        double solution = Double.parseDouble(expression);
        solution = Double.parseDouble(roundAmount(solution));
        return solution;
    }
    
    public static String formateUsaaLine(String line) {
        // remove quotes and commas between quotes
        boolean leftQuote = false;
        String tmp = "";
        for(int i = 0; i < line.length(); i++) {
            if(line.charAt(i)=='"' && !leftQuote) {
                leftQuote = true;
            } else if(line.charAt(i)=='"' && leftQuote){
                leftQuote = false;
            } else if(line.charAt(i)==',' && leftQuote) {
                // do nothing (omit comma)
            } else {
                tmp = tmp + line.charAt(i);
            }
        }
        line = tmp;
        tmp = "";
        boolean prevDash = false;
        for(int i = 0; i < line.length(); i++) {
            if(line.charAt(i)=='-') {
                if(prevDash) {
                    prevDash = false; // double dash has been found and omitted
                } else {
                    prevDash = true; // single dash found
                }
            } else {
                if(prevDash) {
                    tmp = tmp + "-" + line.charAt(i); // single dash added before current character
                    prevDash = false;
                } else {
                    tmp = tmp + line.charAt(i);
                }
            }
        }
        return tmp;
    }
    
    public static String convertSlashToDashDate(String slashDate) {
        String[] date = slashDate.split("/");
        if(date.length!=3) return "";
        
        String mth, day, yr;
        mth = date[0];
        day = date[1];
        yr = date[2];
        if(mth.length()==1) {
            mth = "0" + mth;
        }
        if(day.length()==1) {
            day = "0" + day;
        }
        return yr + "-" + mth + "-" + day;
    }
    
    /**
     * Performs an MD5 hash function on given string and returns the hash
     * @param password String to be MD5 hashed
     * @return an MD5 hash of the given string
     */
    public static String getHash(String password) {
        MessageDigest md;
        try {
            md = MessageDigest.getInstance("MD5");
            md.update(password.getBytes());
            byte byteData[] = md.digest();
            //convert the byte to hex format method 1
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < byteData.length; i++) {
                sb.append(Integer.toString((byteData[i] & 0xff) + 0x100, 16).substring(1));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException ex) {
            return "";
        }
    }
    
    public static String removeCharacters(String rem, String fromText) {
        String tmp = "";
        for(int i = 0; i < fromText.length(); i++) {
            boolean removeChar = false;
            for(int j = 0; j < rem.length(); j++) {
                if(fromText.charAt(i) == rem.charAt(j)) {
                    removeChar = true;
                    break;
                }
            }
            if(!removeChar) {
                tmp = tmp + fromText.charAt(i);
            }
        }
        return tmp;
    }
}

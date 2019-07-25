
package com.dave.invertedindex.util;


public class Config {

    public static Boolean ENABLE_SYMBOL_FILTER = false;
    public static Boolean ENABLE_LOWERCASE_FILTER = true;
    public static Boolean ENABLE_STOPWORD_FILTER = true;
    public static Boolean ENABLE_ALPHANUMERIC_FILTER = true;
    public static Boolean ENABLE_LENGTH_FILTER = true;

    public static Integer MIN_LENGTH_FILTER = 3;
    public static Integer MAX_LENGTH_FILTER = 50;

    public static String INDEX_PATH = "";
    public static String INDEX_NAME = "index";


}

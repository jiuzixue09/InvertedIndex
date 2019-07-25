package com.dave.invertedindex.conf;


import com.dave.invertedindex.util.Config;

import java.util.HashMap;
import java.util.Map;


public class Configuration{

    public static final String KEY_ENABLE_LOWERCASE_FILTER = "ENABLE_LOWERCASE_FILTER";
    public static final String KEY_ENABLE_STOPWORD_FILTER = "ENABLE_STOPWORD_FILTER";


    public static final String KEY_ENABLE_ALPHANUMERIC_FILTER = "ENABLE_ALPHANUMERIC_FILTER";
    public static final String KEY_ENABLE_SYMBOL_FILTER = "ENABLE_SYMBOL_FILTER";
    public static final String KEY_ENABLE_LENGTH_FILTER = "ENABLE_LENGTH_FILTER";
    public static final String KEY_MIN_LENGTH_FILTER = "MIN_LENGTH_FILTER";
    public static final String KEY_MAX_LENGTH_FILTER = "MAX_LENGTH_FILTER";
    public static final String KEY_INDEX_PATH = "INDEX_PATH";
    public static final String KEY_INDEX_NAME = "INDEX_NAME";


    protected HashMap<String, Object> data = new HashMap<String, Object>();

    public Configuration set(String key, Object value){
        data.put(key, value);
        return this;
    }

    public String getString(String key){
        return get(key);
    }
    public Boolean getBoolean(String key){ return get(key); }
    public Integer getInteger(String key){
        return get(key);
    }
    public Long getLong(String key){
        return get(key);
    }
    public Double getDouble(String key){
        return get(key);
    }
    public Map<String,Integer> getMap(String key){
        return get(key);
    }


    public <T> T get(String key){
        return (T)data.get(key);
    }

    public <T> T getOrDefault(String key, T defaultValue){
        Object value = data.get(key);
        if(value == null){
            return defaultValue;
        }else{
            return (T)value;
        }
    }

    public HashMap<String, Object> getData() {
        return data;
    }

    public void setData(HashMap<String, Object> data) {
        this.data = data;
    }


    protected Configuration copy(){
        Configuration conf = new Configuration();
        conf.data = (HashMap<String, Object>) data.clone();
        return conf;
    }

    public Boolean getEnableLowercaseFilter(){
        return get(KEY_ENABLE_LOWERCASE_FILTER);
    }
    public Configuration setEnableLowercaseFilter(Boolean enableLowercaseFilter){
        return set(KEY_ENABLE_LOWERCASE_FILTER, enableLowercaseFilter);
    }

    public Boolean getEnableStopwordFilter(){
        return get(KEY_ENABLE_STOPWORD_FILTER);
    }
    public Configuration setEnableStopwordFilter(Boolean enableStopwordFilter){
        return set(KEY_ENABLE_STOPWORD_FILTER, enableStopwordFilter);
    }

    public Boolean getEnableAlphanumericFilter(){
        return get(KEY_ENABLE_ALPHANUMERIC_FILTER);
    }
    public Configuration setEnableAlphanumericFilter(Boolean enableAlphanumericFilter){
        return set(KEY_ENABLE_ALPHANUMERIC_FILTER, enableAlphanumericFilter);
    }


    public Boolean getEnableSymbolFilter(){
        return get(KEY_ENABLE_SYMBOL_FILTER);
    }
    public Configuration setEnableSymbolFilter(Boolean enableSymbolFilter){
        return set(KEY_ENABLE_SYMBOL_FILTER, enableSymbolFilter);
    }

    public Boolean getEnableLengthFilter(){
        return get(KEY_ENABLE_LENGTH_FILTER);
    }
    public Configuration setEnableLengthFilter(Boolean enableLengthFilter){
        return set(KEY_ENABLE_LENGTH_FILTER, enableLengthFilter);
    }

    public Integer getMinLengthFilter(){
        return get(KEY_MIN_LENGTH_FILTER);
    }
    public Configuration setMinLengthFilter(Integer redisPort){
        return set(KEY_MIN_LENGTH_FILTER, redisPort);
    }


    public Configuration setMaxLengthFilter(Integer maxLengthFilter){
        return set(KEY_MAX_LENGTH_FILTER, maxLengthFilter);
    }
    public Integer getMaxLengthFilter(){
        return get(KEY_MAX_LENGTH_FILTER);
    }

    public Configuration setIndexPath(String indexPath){
        return set(KEY_INDEX_PATH, indexPath);
    }

    public String getIndexPath() {
        return get(KEY_INDEX_PATH);
    }

    public Configuration setIndexName(String indexName){
        return set(KEY_INDEX_NAME, indexName);
    }

    public String getIndexName() {
        return get(KEY_INDEX_NAME);
    }

    private static Configuration defaultConf = null;

    private static Object configLock = new Object();

    public static Configuration getDefault(){
        if(defaultConf == null){
            synchronized (configLock){
                if(defaultConf == null){
                    defaultConf = new Configuration();
                    defaultConf.set(KEY_ENABLE_ALPHANUMERIC_FILTER, Config.ENABLE_ALPHANUMERIC_FILTER);
                    defaultConf.set(KEY_ENABLE_SYMBOL_FILTER, Config.ENABLE_SYMBOL_FILTER);
                    defaultConf.set(KEY_ENABLE_LENGTH_FILTER, Config.ENABLE_LENGTH_FILTER);
                    defaultConf.set(KEY_ENABLE_LOWERCASE_FILTER, Config.ENABLE_LOWERCASE_FILTER);
                    defaultConf.set(KEY_ENABLE_STOPWORD_FILTER, Config.ENABLE_STOPWORD_FILTER);
                    defaultConf.set(KEY_MIN_LENGTH_FILTER, Config.MIN_LENGTH_FILTER);
                    defaultConf.set(KEY_MAX_LENGTH_FILTER, Config.MAX_LENGTH_FILTER);
                    defaultConf.set(KEY_INDEX_PATH, Config.INDEX_PATH);
                    defaultConf.set(KEY_INDEX_NAME, Config.INDEX_NAME);

                }
            }
        }
        return defaultConf;
    }

    public static Configuration copyDefault(){
        return getDefault().copy();
    }


    @Override
    public String toString() {
        StringBuilder sb= new StringBuilder();
        sb.append("Configuration:\n");
        for(Map.Entry<String, Object> entry:data.entrySet()){
            sb.append("\t").append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
        }
        return sb.toString();
    }
}

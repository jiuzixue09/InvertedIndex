package com.dave.invertedindex.conf;


import com.dave.util.ConfigUtil;

public class FileConfigured extends CommonConfigured {

    public FileConfigured() {
        Configuration configuration = new Configuration();
        configuration.setEnableLowercaseFilter(ConfigUtil.getBooleanParameterWithDefault("ENABLE_LOWERCASE_FILTER",false));
        configuration.setEnableStopwordFilter(ConfigUtil.getBooleanParameterWithDefault("ENABLE_STOPWORD_FILTER",false));
        configuration.setEnableAlphanumericFilter(ConfigUtil.getBooleanParameterWithDefault("ENABLE_ALPHANUMERIC_FILTER",false));
        configuration.setEnableSymbolFilter(ConfigUtil.getBooleanParameterWithDefault("ENABLE_SYMBOL_FILTER",false));
        configuration.setEnableLengthFilter(ConfigUtil.getBooleanParameterWithDefault("ENABLE_LENGTH_FILTER",false));
        configuration.setSupportChineseParser(ConfigUtil.getBooleanParameterWithDefault("SUPPORT_CHINESE_PARSER",false));

        configuration.setMinLengthFilter(ConfigUtil.getIntParameterWithDefault("MIN_LENGTH_FILTER",1));
        configuration.setMaxLengthFilter(ConfigUtil.getIntParameterWithDefault("MAX_LENGTH_FILTER",50)) ;

        setConf(configuration);

    }
}

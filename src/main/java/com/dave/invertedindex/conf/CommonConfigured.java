package com.dave.invertedindex.conf;


public class CommonConfigured implements Configured {
    protected Configuration conf = null;

    @Override
    public Configuration getConf() {
        return conf;
    }

    @Override
    public void setConf(Configuration conf) {
        this.conf = conf;
    }
}

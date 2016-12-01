package org.aliangliang.tanetroamer;

import java.util.concurrent.Callable;

public class Callback implements Callable<String> {

    public String call(String param) throws Exception {
        return param;
    }

    @Override
    public String call() throws Exception {
        return null;
    }

}

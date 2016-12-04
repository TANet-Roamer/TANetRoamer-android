package org.aliangliang.tanetroamer;

import java.util.concurrent.Callable;

@FunctionalInterface
public interface Callback {
    public String call(String param) throws Exception;
}

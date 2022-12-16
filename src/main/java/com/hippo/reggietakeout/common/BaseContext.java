package com.hippo.reggietakeout.common;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class BaseContext {
    public static ThreadLocal<Long> threadLocal = new ThreadLocal<>();

    public static void set(Long id){
        threadLocal.set(id);
    }

    public static Long get(){
        return threadLocal.get();
    }
}

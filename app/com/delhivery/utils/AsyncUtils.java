package com.delhivery.utils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AsyncUtils {

  private static ExecutorService workStealingPool = Executors
          .newWorkStealingPool();

  private static ExecutorService epThreadPoolExecutor = Executors.newFixedThreadPool(100);
  public static ExecutorService getAsyncExecutor() {
    return workStealingPool;
  }
  
  public static ExecutorService getEPExecutor(){
    return epThreadPoolExecutor;
  }
}

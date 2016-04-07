package com.delhivery.cache;
import java.io.IOException;
import java.net.InetSocketAddress;

import com.google.inject.Singleton;

import net.spy.memcached.MemcachedClient;

@Singleton
public class ECMemCache {

  private static final String CONFIG_ENDPOINT = "serviceabilitypoc.kooykx.cfg.apse1.cache.amazonaws.com";
  private static Integer clusterPort = 11211;
  
  private static MemcachedClient memClient = null;
  static{
    try {
      InetSocketAddress socketAddr = new InetSocketAddress(CONFIG_ENDPOINT,clusterPort);
      System.out.println("socketAddr = " + socketAddr);
      if (socketAddr.isUnresolved()){
        System.out.println("Address is not resolved");
      }
      memClient = new MemcachedClient(socketAddr);
      
    } catch (IOException e) {
      System.out.println("== Exception while connecting to memcache client");
      e.printStackTrace();
    }
  }
  
  public void set(String key, Object data, Integer ttl){
    memClient.set(key,ttl, data);
  }
  
  public Object get(String key){
   return memClient.get(key);
  }
}

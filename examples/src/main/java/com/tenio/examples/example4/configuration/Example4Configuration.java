package com.tenio.examples.example4.configuration;

import com.tenio.core.bootstrap.annotation.Bean;
import com.tenio.core.bootstrap.annotation.BeanFactory;
import com.tenio.engine.heartbeat.HeartBeatManager;
import com.tenio.engine.heartbeat.HeartBeatManagerImpl;

@BeanFactory
public class Example4Configuration {

  @Bean
  public HeartBeatManager getHeartBeatManager() {
    return new HeartBeatManagerImpl();
  }
}

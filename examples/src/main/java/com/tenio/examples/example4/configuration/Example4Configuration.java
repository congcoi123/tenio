package com.tenio.examples.example4.configuration;

import com.tenio.common.bootstrap.annotation.Bean;
import com.tenio.common.bootstrap.annotation.Configuration;
import com.tenio.engine.heartbeat.HeartBeatManager;
import com.tenio.engine.heartbeat.HeartBeatManagerImpl;

@Configuration
public class Example4Configuration {

  @Bean
  public HeartBeatManager getHeartBeatManager() {
    return new HeartBeatManagerImpl();
  }
}

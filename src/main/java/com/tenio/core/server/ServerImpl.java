/*
The MIT License

Copyright (c) 2016-2026 kong <congcoi123@gmail.com>

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
*/

package com.tenio.core.server;

import com.tenio.common.configuration.Configuration;
import com.tenio.common.logger.SystemLogger;
import com.tenio.common.utility.TimeUtility;
import com.tenio.core.api.ServerApi;
import com.tenio.core.api.implement.ServerApiImpl;
import com.tenio.core.bootstrap.BootstrapHandler;
import com.tenio.core.command.client.ClientCommandManager;
import com.tenio.core.command.system.SystemCommandManager;
import com.tenio.core.configuration.DefaultCoreConfiguration;
import com.tenio.core.configuration.constant.CoreConstant;
import com.tenio.core.configuration.define.CoreConfigurationType;
import com.tenio.core.configuration.define.ServerEvent;
import com.tenio.core.entity.manager.ChannelManager;
import com.tenio.core.entity.manager.PlayerManager;
import com.tenio.core.entity.manager.RoomManager;
import com.tenio.core.entity.manager.implement.ChannelManagerImpl;
import com.tenio.core.entity.manager.implement.PlayerManagerImpl;
import com.tenio.core.entity.manager.implement.RoomManagerImpl;
import com.tenio.core.event.implement.EventManager;
import com.tenio.core.network.Network;
import com.tenio.core.network.NetworkImpl;
import com.tenio.core.network.codec.compression.BinaryPacketCompressor;
import com.tenio.core.network.codec.decoder.BinaryPacketDecoder;
import com.tenio.core.network.codec.decoder.BinaryPacketDecoderImpl;
import com.tenio.core.network.codec.encoder.BinaryPacketEncoder;
import com.tenio.core.network.codec.encoder.BinaryPacketEncoderImpl;
import com.tenio.core.network.codec.encryption.BinaryPacketEncryptor;
import com.tenio.core.network.configuration.SocketConfiguration;
import com.tenio.core.network.entity.outbound.packet.policy.DefaultOutboundQueuePolicy;
import com.tenio.core.network.entity.outbound.packet.policy.OutboundQueuePolicy;
import com.tenio.core.network.entity.outbound.Response;
import com.tenio.core.network.entity.inbound.policy.RequestPolicy;
import com.tenio.core.network.security.filter.ConnectionFilter;
import com.tenio.core.network.security.filter.DefaultConnectionFilter;
import com.tenio.core.network.zero.engine.manager.DatagramChannelManager;
import com.tenio.core.network.zero.engine.reader.policy.DatagramPacketPolicy;
import com.tenio.core.network.zero.engine.reader.policy.DefaultDatagramPacketPolicy;
import com.tenio.core.scheduler.Scheduler;
import com.tenio.core.scheduler.SchedulerImpl;
import com.tenio.core.server.core.ZeroProcessor;
import com.tenio.core.server.core.ZeroProcessorImpl;
import com.tenio.core.server.setting.ConfigurationAssessment;
import com.tenio.core.utility.CommandUtility;
import java.io.IOError;
import org.jline.reader.EndOfFileException;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.UserInterruptException;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

/**
 * This class manages the workflow of the current server. The instruction's
 * orders are important, event subscribes must be set last and all configuration
 * values should be confirmed.
 */
public final class ServerImpl extends SystemLogger implements Server {

  private static Server instance;
  private final EventManager eventManager;
  private final RoomManager roomManager;
  private final PlayerManager playerManager;
  private final ChannelManager channelManager;
  private final DatagramChannelManager datagramChannelManager;
  private final ZeroProcessor zeroProcessor;
  private final Scheduler scheduler;
  private final Network network;
  private final ServerApi serverApi;
  private ClientCommandManager clientCommandManager;
  private Configuration configuration;
  private long startedTime;
  private String serverName;

  private ServerImpl() {
    eventManager = EventManager.newInstance();
    roomManager = RoomManagerImpl.newInstance(eventManager);
    playerManager = PlayerManagerImpl.newInstance(eventManager);
    channelManager = ChannelManagerImpl.newInstance(eventManager);
    datagramChannelManager = DatagramChannelManager.newInstance();
    network = NetworkImpl.newInstance(eventManager);
    serverApi = ServerApiImpl.newInstance(this);
    zeroProcessor = ZeroProcessorImpl.newInstance(eventManager, serverApi, datagramChannelManager);
    scheduler = SchedulerImpl.newInstance(eventManager);
  } // prevent creation manually

  /**
   * Preventing Singleton object instantiation from outside and creates multiple instance if two
   * thread access this method simultaneously.
   *
   * @return a new instance
   */
  public static Server getInstance() {
    if (instance == null) {
      instance = new ServerImpl();
    }
    return instance;
  }

  @Override
  public void start(BootstrapHandler bootstrapHandler, String[] params) throws Exception {
    // record the started time
    startedTime = TimeUtility.currentTimeMillis();

    // create a new configuration instance
    Configuration configuration = bootstrapHandler.getConfigurationHandler().getConfiguration();
    if (configuration == null) {
      if (isInfoEnabled()) {
        info("CONFIGURATION", "No custom configuration class found. Use default configuration (DefaultCoreConfiguration).");
      }

      configuration = new DefaultCoreConfiguration();
    }

    // get the file path
    var file = params == null ? null : (params.length == 0 ? null : params[0]);

    // load the file
    configuration.load(file);

    // Put the current configurations to the logger
    if (isInfoEnabled()) {
      info("CONFIGURATION", configuration.toString());
    }

    this.configuration = configuration;

    serverName = configuration.getString(CoreConfigurationType.SERVER_NAME);

    if (isInfoEnabled()) {
      info(serverName, "STATE", "STARTING");
    }

    // subscribing for processes and handlers
    zeroProcessor.subscribe(configuration.get(CoreConfigurationType.NETWORK_TCP) != null ||
            configuration.get(CoreConfigurationType.NETWORK_WEBSOCKET) != null,
            configuration.get(CoreConfigurationType.NETWORK_UDP) != null);

    bootstrapHandler.getEventHandler().initialize(eventManager);

    // collect all subscribers, listen all the events
    eventManager.subscribe();

    var assessment = ConfigurationAssessment.newInstance(eventManager, configuration);
    assessment.assess();

    setupClientCommands(bootstrapHandler.getClientCommandManager());
    setupEntitiesManagementService(configuration);
    setupNetworkService(configuration, bootstrapHandler);
    setupInternalProcessorService(configuration, bootstrapHandler);
    setupScheduleService(configuration);

    initializeServices();
    startServices();

    // now it can be able to accept connections
    network.activate();
    zeroProcessor.activate();

    // it should wait for a while to let everything settles down
    int servicesTakeTime = Math.max(Math.max(network.getMaximumStartingTimeInMilliseconds(),
                    zeroProcessor.getMaximumStartingTimeInMilliseconds()),
            scheduler.getMaximumStartingTimeInMilliseconds());
    int totalWaitingTime = servicesTakeTime + CoreConstant.DELAY_BEFORE_SERVER_IS_READY_IN_MILLISECONDS;
    Thread.sleep(totalWaitingTime);

    if (isInfoEnabled()) {
      info(serverName, "STATE", "RUNNING");
    }

    // emit "server initialization" event
    eventManager.emit(ServerEvent.SERVER_INITIALIZATION, serverName);

    if (configuration.getBoolean(CoreConfigurationType.ENABLE_TERMINAL_COMMAND)) {
      startConsole(bootstrapHandler.getSystemCommandManager());
    }
  }

  private void initializeServices() {
    network.initialize();
    zeroProcessor.initialize();
    scheduler.initialize();
  }

  private void startServices() {
    network.start();
    zeroProcessor.start();
    scheduler.start();
  }

  private void setupClientCommands(ClientCommandManager clientCommandManager) {
    this.clientCommandManager = clientCommandManager;
  }

  private void setupEntitiesManagementService(Configuration configuration) {
    playerManager.configureMaxIdleTimeInSeconds(
        configuration.getInt(CoreConfigurationType.PROP_MAX_PLAYER_IDLE_TIME));
    playerManager.configureMaxIdleTimeNeverDeportedInSeconds(
        configuration.getInt(CoreConfigurationType.PROP_MAX_PLAYER_IDLE_TIME_NEVER_DEPORTED));
    roomManager.configureMaxRooms(
        configuration.getInt(CoreConfigurationType.PROP_MAX_NUMBER_ROOMS));
  }

  private void setupScheduleService(Configuration configuration) {
    scheduler.setCcuReportInterval(
        configuration.getInt(CoreConfigurationType.INTERVAL_CCU_SCAN));
    scheduler.setDeadlockScanInterval(
        configuration.getInt(CoreConfigurationType.INTERVAL_DEADLOCK_SCAN));
    scheduler.setDisconnectedPlayerScanInterval(
        configuration.getInt(CoreConfigurationType.INTERVAL_DISCONNECTED_PLAYER_SCAN));
    scheduler
        .setRemovedRoomScanInterval(
            configuration.getInt(CoreConfigurationType.INTERVAL_REMOVED_ROOM_SCAN));
    scheduler
        .setSystemMonitoringInterval(
            configuration.getInt(CoreConfigurationType.INTERVAL_SYSTEM_MONITORING));
    scheduler
        .setTrafficCounterInterval(
            configuration.getInt(CoreConfigurationType.INTERVAL_TRAFFIC_COUNTER));

    scheduler.setSessionManager(network.getSessionManager());
    scheduler.setPlayerManager(playerManager);
    scheduler.setRoomManager(roomManager);
    scheduler.setNetworkReaderStatistic(network.getNetworkReaderStatistic());
    scheduler.setNetworkWriterStatistic(network.getNetworkWriterStatistic());
  }

  private void setupNetworkService(Configuration configuration, BootstrapHandler bootstrapHandler)
      throws IllegalArgumentException, SecurityException {

    ConnectionFilter connectionFilter = bootstrapHandler.getBeanByClazz(ConnectionFilter.class);
    if (connectionFilter == null) {
      connectionFilter = new DefaultConnectionFilter();
    }
    network.setConnectionFilterClass(
        connectionFilter,
        configuration.getInt(CoreConfigurationType.NETWORK_PROP_MAX_CONNECTIONS_PER_IP));

    var servletMap = bootstrapHandler.getServletMap();
    var httpConfiguration = configuration.get(CoreConfigurationType.NETWORK_HTTP);
    network.setHttpConfiguration(
        CoreConstant.DEFAULT_NUMBER_HTTP_WORKERS,
        httpConfiguration != null ? ((SocketConfiguration) httpConfiguration).port() : 0,
        httpConfiguration != null ? servletMap : null);

    var serverAddress = configuration.getString(CoreConfigurationType.SERVER_ADDRESS);

    network.setSocketAcceptorServerAddress(serverAddress);

    network.setSocketAcceptorBufferSize(
        configuration.getInt(CoreConfigurationType.NETWORK_PROP_SOCKET_ACCEPTOR_BUFFER_SIZE));
    network.setSocketAcceptorWorkers(CoreConstant.DEFAULT_ENGINE_THREAD_POOL_SIZE);

    var tcpSocketConfiguration = configuration.get(CoreConfigurationType.NETWORK_TCP) != null ?
            (SocketConfiguration) configuration.get(CoreConfigurationType.NETWORK_TCP) : null;
    var udpChannelConfiguration = configuration.get(CoreConfigurationType.NETWORK_UDP) != null ?
            (SocketConfiguration) configuration.get(CoreConfigurationType.NETWORK_UDP) : null;
    var webSocketConfiguration = configuration.get(CoreConfigurationType.NETWORK_WEBSOCKET) != null ?
            (SocketConfiguration) configuration.get(CoreConfigurationType.NETWORK_WEBSOCKET) : null;
    network.setSocketConfigurations(tcpSocketConfiguration, udpChannelConfiguration, webSocketConfiguration);

    if (udpChannelConfiguration != null) {
      datagramChannelManager.configureUdpPort(udpChannelConfiguration.port());
    }

    network.setSocketReaderBufferSize(
        configuration.getInt(CoreConfigurationType.NETWORK_PROP_SOCKET_READER_BUFFER_SIZE));
    network.setSocketReaderWorkers(
        configuration.getInt(CoreConfigurationType.WORKER_SOCKET_READER));

    network.setSocketWriterBufferSize(
        configuration.getInt(CoreConfigurationType.NETWORK_PROP_SOCKET_WRITER_BUFFER_SIZE));
    network.setSocketWriterWorkers(
        configuration.getInt(CoreConfigurationType.WORKER_SOCKET_WRITER));

    network
        .setWebSocketConsumerWorkers(
            configuration.getInt(CoreConfigurationType.WORKER_WEBSOCKET_CONSUMER));
    network
        .setWebSocketProducerWorkers(
            configuration.getInt(CoreConfigurationType.WORKER_WEBSOCKET_PRODUCER));

    network.setWebSocketReceiverBufferSize(
        configuration.getInt(CoreConfigurationType.NETWORK_PROP_WEBSOCKET_RECEIVER_BUFFER_SIZE));
    network.setWebSocketSenderBufferSize(
        configuration.getInt(CoreConfigurationType.NETWORK_PROP_WEBSOCKET_SENDER_BUFFER_SIZE));
    network
        .setWebSocketUsingSsl(
            configuration.getBoolean(CoreConfigurationType.NETWORK_PROP_WEBSOCKET_USING_SSL));

    OutboundQueuePolicy outboundQueuePolicy = bootstrapHandler.getBeanByClazz(OutboundQueuePolicy.class);
    if (outboundQueuePolicy == null) {
      outboundQueuePolicy = new DefaultOutboundQueuePolicy();
    }
    network.setSessionOutboundQueuePolicy(outboundQueuePolicy);
    network.setSessionInboundQueueSize(
            configuration.getInt(CoreConfigurationType.PROP_MAX_SESSION_REQUEST_QUEUE_SIZE));
    network.setSessionOutboundQueueSize(
        configuration.getInt(CoreConfigurationType.PROP_MAX_SESSION_RESPONSE_QUEUE_SIZE));
    network.setSessionSlowConsumingInboundQueueWarningThreshold(
        configuration.getInt(CoreConfigurationType.PROP_SLOW_CONSUMING_WARNING_SESSION_REQUEST_THRESHOLD));
    network.setSessionSlowConsumingOutboundQueueWarningThreshold(
        configuration.getInt(CoreConfigurationType.PROP_SLOW_CONSUMING_WARNING_SESSION_RESPONSE_THRESHOLD));

    DatagramPacketPolicy datagramPacketPolicy =
        bootstrapHandler.getBeanByClazz(DatagramPacketPolicy.class);
    if (datagramPacketPolicy == null) {
      datagramPacketPolicy = new DefaultDatagramPacketPolicy();
    }
    network.setDatagramPacketPolicy(datagramPacketPolicy);

    network.setSessionMaxIdleTimeInSeconds(
        configuration.getInt(CoreConfigurationType.PROP_MAX_PLAYER_IDLE_TIME));

    BinaryPacketCompressor binaryPacketCompressor =
        bootstrapHandler.getBeanByClazz(BinaryPacketCompressor.class);
    BinaryPacketEncryptor binaryPacketEncryptor =
        bootstrapHandler.getBeanByClazz(BinaryPacketEncryptor.class);
    BinaryPacketEncoder binaryPacketEncoder = new BinaryPacketEncoderImpl();
    BinaryPacketDecoder binaryPacketDecoder = new BinaryPacketDecoderImpl();

    binaryPacketEncoder.setCompressionThresholdBytes(
        configuration.getInt(
            CoreConfigurationType.NETWORK_PROP_PACKET_COMPRESSION_THRESHOLD_BYTES));
    binaryPacketEncoder.setCompressor(binaryPacketCompressor);
    binaryPacketEncoder.setEncryptor(binaryPacketEncryptor);

    binaryPacketDecoder.setCompressor(binaryPacketCompressor);
    binaryPacketDecoder.setEncryptor(binaryPacketEncryptor);

    network.setPacketDecoder(binaryPacketDecoder);
    network.setPacketEncoder(binaryPacketEncoder);
  }

  private void setupInternalProcessorService(Configuration configuration,
                                             BootstrapHandler bootstrapHandler) {
    RequestPolicy requestPolicy = bootstrapHandler.getBeanByClazz(RequestPolicy.class);
    zeroProcessor.setRequestPolicy(requestPolicy);
    zeroProcessor
        .setMaxNumberPlayers(configuration.getInt(CoreConfigurationType.PROP_MAX_NUMBER_PLAYERS));
    zeroProcessor.setSessionManager(network.getSessionManager());
    zeroProcessor.setPlayerManager(playerManager);
    // Since v0.7.0, set fixed value for the thread pool size
    zeroProcessor.setThreadPoolSize(CoreConstant.DEFAULT_PROCESSOR_THREAD_POOL_SIZE);
    zeroProcessor.setKeepPlayerOnDisconnection(
        configuration.getBoolean(CoreConfigurationType.PROP_KEEP_PLAYER_ON_DISCONNECTION));

    zeroProcessor.setNetworkReaderStatistic(network.getNetworkReaderStatistic());
    zeroProcessor.setNetworkWriterStatistic(network.getNetworkWriterStatistic());
  }

  private void startConsole(SystemCommandManager systemCommandManager) {
    Terminal terminal = null;
    try {
      terminal = TerminalBuilder.builder().jna(true).build();
    } catch (Exception exception) {
      try {
        // fallback to a dumb jLine terminal
        terminal = TerminalBuilder.builder().dumb(true).build();
      } catch (Exception exception1) {
        // when dumb is true, build() never throws, ignore it
      }
    }
    var consoleLineReader = LineReaderBuilder.builder().terminal(terminal).build();

    String input = null;
    boolean isLastInterrupted = false;
    while (true) {
      try {
        input = consoleLineReader.readLine("$ ");
      } catch (UserInterruptException exception) {
        if (!isLastInterrupted) {
          isLastInterrupted = true;
          CommandUtility.INSTANCE.showConsoleMessage("Press Ctrl-C again to shutdown.");
          continue;
        } else {
          Runtime.getRuntime().exit(0);
        }
      } catch (EndOfFileException exception) {
        CommandUtility.INSTANCE.showConsoleMessage("EOF detected.");
        continue;
      } catch (IOError exception) {
        CommandUtility.INSTANCE.showConsoleMessage("An IO error occurred.");
        continue;
      }

      isLastInterrupted = false;
      try {
        systemCommandManager.invoke(input);
      } catch (Exception exception) {
        CommandUtility.INSTANCE.showConsoleMessage("Exception > " + exception.getMessage());
      }
    }
  }

  @Override
  public void shutdown() {
    if (isInfoEnabled()) {
      info(serverName, "STATE", "STOPPING");
    }
    // emit "server shutdown" event
    eventManager.emit(ServerEvent.SERVER_TEARDOWN, serverName);
    shutdownServices();
    if (isInfoEnabled()) {
      info(serverName, "STATE", "STOPPED");
    }
    // real stop
    Runtime.getRuntime().halt(0);
  }

  private void shutdownServices() {
    zeroProcessor.shutdown();
    network.shutdown();
    scheduler.shutdown();
  }

  @Override
  public ServerApi getApi() {
    return serverApi;
  }

  @Override
  public ClientCommandManager getClientCommandManager() {
    return clientCommandManager;
  }

  @Override
  public EventManager getEventManager() {
    return eventManager;
  }

  @Override
  public PlayerManager getPlayerManager() {
    return playerManager;
  }

  @Override
  public RoomManager getRoomManager() {
    return roomManager;
  }

  @Override
  public ChannelManager getChannelManager() {
    return channelManager;
  }

  @Override
  public DatagramChannelManager getDatagramChannelManager() {
    return datagramChannelManager;
  }

  @Override
  public Configuration getConfiguration() {
    return configuration;
  }

  @Override
  public long getStartedTime() {
    return startedTime;
  }

  @Override
  public long getUptime() {
    return TimeUtility.currentTimeMillis() - startedTime;
  }

  @Override
  public void write(Response response, boolean markedAsLast) {
    network.write(response, markedAsLast);
  }
}

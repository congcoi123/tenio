/*
The MIT License

Copyright (c) 2016-2025 kong <congcoi123@gmail.com>

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
import com.tenio.core.configuration.constant.CoreConstant;
import com.tenio.core.configuration.define.CoreConfigurationType;
import com.tenio.core.configuration.define.ServerEvent;
import com.tenio.core.configuration.setting.Setting;
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
import com.tenio.core.network.entity.packet.policy.DefaultPacketQueuePolicy;
import com.tenio.core.network.entity.packet.policy.PacketQueuePolicy;
import com.tenio.core.network.entity.protocol.Response;
import com.tenio.core.network.entity.protocol.policy.RequestPolicy;
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
import javax.annotation.concurrent.ThreadSafe;
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
@ThreadSafe
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
    zeroProcessor =
        ZeroProcessorImpl.newInstance(eventManager, serverApi, datagramChannelManager);
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

    // get the file path
    var file = params.length == 0 ? null : params[0];
    if (file == null) {
      file = CoreConstant.DEFAULT_CONFIGURATION_FILE;
    }

    // load configuration file
    var configuration = bootstrapHandler.getConfigurationHandler().getConfiguration();
    configuration.load(file);

    // Put the current configurations to the logger
    if (isInfoEnabled()) {
      info("CONFIGURATION", configuration.toString());
    }

    this.configuration = configuration;

    serverName = configuration.getString(CoreConfigurationType.SERVER_NAME);

    if (isInfoEnabled()) {
      info("SERVER", serverName, "Starting ...");
    }

    // subscribing for processes and handlers
    zeroProcessor.subscribe();

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

    // it should wait for a while to let everything settles down
    int servicesTakeTime = Math.max(Math.max(network.getMaximumStartingTimeInMilliseconds(),
        zeroProcessor.getMaximumStartingTimeInMilliseconds()),
        scheduler.getMaximumStartingTimeInMilliseconds());
    int totalWaitingTime =
        servicesTakeTime + CoreConstant.DELAY_BEFORE_SERVER_IS_READY_IN_MILLISECONDS;
    Thread.sleep(totalWaitingTime);

    if (isInfoEnabled()) {
      info("SERVER", serverName, buildgen("Started after ", totalWaitingTime, " milliseconds"));
    }

    // emit "server initialization" event
    eventManager.emit(ServerEvent.SERVER_INITIALIZATION, serverName, configuration);

    // now it can be able to accept connections
    network.activate();
    zeroProcessor.activate();

    if (((Setting) configuration.get(CoreConfigurationType.SERVER_SETTING)).getCommand()
        .isEnabled()) {
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
        httpConfiguration != null ?
            configuration.getInt(CoreConfigurationType.WORKER_HTTP_WORKER) : 0,
        httpConfiguration != null ?
            ((SocketConfiguration) httpConfiguration).port() : 0,
        httpConfiguration != null ? servletMap : null);

    var serverAddress = configuration.getString(CoreConfigurationType.SERVER_ADDRESS);

    network.setSocketAcceptorServerAddress(serverAddress);

    network.setSocketAcceptorBufferSize(
        configuration.getInt(CoreConfigurationType.NETWORK_PROP_SOCKET_ACCEPTOR_BUFFER_SIZE));
    network.setSocketAcceptorWorkers(
        configuration.getInt(CoreConfigurationType.WORKER_SOCKET_ACCEPTOR));

    var udpChannelConfiguration = configuration.get(CoreConfigurationType.NETWORK_UDP) != null ?
        (SocketConfiguration) configuration.get(CoreConfigurationType.NETWORK_UDP) : null;
    var kcpSocketConfiguration = configuration.get(CoreConfigurationType.NETWORK_KCP) != null ?
        (SocketConfiguration) configuration.get(CoreConfigurationType.NETWORK_KCP) : null;
    network.setSocketConfigurations(
        (configuration.get(CoreConfigurationType.NETWORK_TCP) != null ?
            (SocketConfiguration) configuration.get(CoreConfigurationType.NETWORK_TCP) : null),
        udpChannelConfiguration,
        (configuration.get(CoreConfigurationType.NETWORK_WEBSOCKET) != null ?
            (SocketConfiguration) configuration.get(CoreConfigurationType.NETWORK_WEBSOCKET) :
            null),
        kcpSocketConfiguration);

    if (udpChannelConfiguration != null) {
      datagramChannelManager.configureUdpPort(udpChannelConfiguration.port());
    }
    if (kcpSocketConfiguration != null) {
      datagramChannelManager.configureKcpPort(kcpSocketConfiguration.port());
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

    PacketQueuePolicy packetQueuePolicy = bootstrapHandler.getBeanByClazz(PacketQueuePolicy.class);
    if (packetQueuePolicy == null) {
      packetQueuePolicy = new DefaultPacketQueuePolicy();
    }
    network.setPacketQueuePolicy(packetQueuePolicy);
    network.setPacketQueueSize(
        configuration.getInt(CoreConfigurationType.PROP_MAX_RESPONSE_QUEUE_SIZE_PER_SESSION));

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
    zeroProcessor
        .setMaxRequestQueueSize(
            configuration.getInt(CoreConfigurationType.PROP_MAX_REQUEST_QUEUE_SIZE));
    zeroProcessor
        .setThreadPoolSize(configuration.getInt(CoreConfigurationType.WORKER_INTERNAL_PROCESSOR));
    zeroProcessor.setKeepPlayerOnDisconnection(
        configuration.getBoolean(CoreConfigurationType.PROP_KEEP_PLAYER_ON_DISCONNECTION));

    zeroProcessor.setNetworkReaderStatistic(network.getNetworkReaderStatistic());
    zeroProcessor.setNetworkWriterStatistic(network.getNetworkWriterStatistic());
  }

  private void startConsole(SystemCommandManager systemCommandManager) {
    Terminal terminal = null;
    try {
      terminal = TerminalBuilder.builder().jna(true).build();
    } catch (Exception e) {
      try {
        // fallback to a dumb jLine terminal
        terminal = TerminalBuilder.builder().dumb(true).build();
      } catch (Exception exception) {
        // when dumb is true, build() never throws, ignore it
      }
    }
    var consoleLineReader = LineReaderBuilder.builder().terminal(terminal).build();

    String input = null;
    boolean isLastInterrupted = false;
    while (true) {
      try {
        input = consoleLineReader.readLine("$ ");
      } catch (UserInterruptException e) {
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
      info("SERVER", serverName, "Stopping ...");
    }
    // emit "server shutdown" event
    eventManager.emit(ServerEvent.SERVER_TEARDOWN, serverName);
    shutdownServices();
    if (isInfoEnabled()) {
      info("SERVER", serverName, "Stopped");
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

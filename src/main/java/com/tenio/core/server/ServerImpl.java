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
import com.tenio.common.data.DataType;
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
import com.tenio.core.network.NetworkService;
import com.tenio.core.network.NetworkServiceImpl;
import com.tenio.core.network.configuration.SocketConfiguration;
import com.tenio.core.network.entity.packet.policy.DefaultPacketQueuePolicy;
import com.tenio.core.network.entity.packet.policy.PacketQueuePolicy;
import com.tenio.core.network.entity.protocol.Response;
import com.tenio.core.network.security.filter.ConnectionFilter;
import com.tenio.core.network.security.filter.DefaultConnectionFilter;
import com.tenio.core.network.zero.codec.compression.BinaryPacketCompressor;
import com.tenio.core.network.zero.codec.compression.DefaultBinaryPacketCompressor;
import com.tenio.core.network.zero.codec.decoder.BinaryPacketDecoder;
import com.tenio.core.network.zero.codec.decoder.DefaultBinaryPacketDecoder;
import com.tenio.core.network.zero.codec.encoder.BinaryPacketEncoder;
import com.tenio.core.network.zero.codec.encoder.DefaultBinaryPacketEncoder;
import com.tenio.core.network.zero.codec.encryption.BinaryPacketEncryptor;
import com.tenio.core.network.zero.codec.encryption.DefaultBinaryPacketEncryptor;
import com.tenio.core.network.zero.engine.manager.DatagramChannelManager;
import com.tenio.core.schedule.ScheduleService;
import com.tenio.core.schedule.ScheduleServiceImpl;
import com.tenio.core.server.service.InternalProcessorService;
import com.tenio.core.server.service.InternalProcessorServiceImpl;
import com.tenio.core.server.setting.ConfigurationAssessment;
import com.tenio.core.utility.CommandUtility;
import java.io.IOError;
import java.util.Objects;
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
  private final InternalProcessorService internalProcessorService;
  private final ScheduleService scheduleService;
  private final NetworkService networkService;
  private final ServerApi serverApi;
  private ClientCommandManager clientCommandManager;
  private Configuration configuration;
  private DataType dataType;
  private long startedTime;
  private String serverName;

  private ServerImpl() {
    eventManager = EventManager.newInstance();
    roomManager = RoomManagerImpl.newInstance(eventManager);
    playerManager = PlayerManagerImpl.newInstance(eventManager);
    channelManager = ChannelManagerImpl.newInstance(eventManager);
    datagramChannelManager = DatagramChannelManager.newInstance();
    networkService = NetworkServiceImpl.newInstance(eventManager, datagramChannelManager);
    serverApi = ServerApiImpl.newInstance(this);
    internalProcessorService = InternalProcessorServiceImpl.newInstance(eventManager, serverApi, datagramChannelManager);
    scheduleService = ScheduleServiceImpl.newInstance(eventManager);
  } // prevent creation manually

  /**
   * Preventing Singleton object instantiation from outside and creates multiple instance if two
   * thread access this method simultaneously.
   *
   * @return a new instance
   */
  public static Server getInstance() {
    if (Objects.isNull(instance)) {
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
    if (Objects.isNull(file)) {
      file = CoreConstant.DEFAULT_CONFIGURATION_FILE;
    }

    // load configuration file
    var configuration = bootstrapHandler.getConfigurationHandler().getConfiguration();
    configuration.load(file);

    // Put the current configurations to the logger
    info("CONFIGURATION", configuration.toString());

    this.configuration = configuration;

    dataType =
        DataType.getByValue(configuration.getString(CoreConfigurationType.DATA_SERIALIZATION));
    serverName = configuration.getString(CoreConfigurationType.SERVER_NAME);

    info("SERVER", serverName, "Starting ...");

    // subscribing for processes and handlers
    internalProcessorService.subscribe();

    bootstrapHandler.getEventHandler().initialize(eventManager);

    // collect all subscribers, listen all the events
    eventManager.subscribe();

    var assessment = ConfigurationAssessment.newInstance(eventManager, configuration);
    assessment.assess();

    setupClientCommands(bootstrapHandler.getClientCommandManager());
    setupEntitiesManagementService(configuration);
    setupNetworkService(configuration, bootstrapHandler);
    setupInternalProcessorService(configuration);
    setupScheduleService(configuration);

    initializeServices();
    startServices();

    // it should wait for a while to let everything settles down
    Thread.sleep(1000);

    info("SERVER", serverName, "Started");

    // emit "server started" event
    eventManager.emit(ServerEvent.SERVER_INITIALIZATION, serverName, configuration);

    if (((Setting) configuration.get(CoreConfigurationType.SERVER_SETTING)).getCommand()
        .isEnabled()) {
      startConsole(bootstrapHandler.getSystemCommandManager());
    }
  }

  private void initializeServices() {
    networkService.initialize();
    internalProcessorService.initialize();
    scheduleService.initialize();
  }

  private void startServices() {
    networkService.start();
    internalProcessorService.start();
    scheduleService.start();
  }

  private void setupClientCommands(ClientCommandManager clientCommandManager) {
    this.clientCommandManager = clientCommandManager;
  }

  private void setupEntitiesManagementService(Configuration configuration) {
    playerManager.configureMaxIdleTimeInSeconds(configuration.getInt(CoreConfigurationType.PROP_MAX_PLAYER_IDLE_TIME));
    playerManager.configureMaxIdleTimeNeverDeportedInSeconds(configuration.getInt(CoreConfigurationType.PROP_MAX_PLAYER_IDLE_TIME_NEVER_DEPORTED));
    roomManager.configureMaxRooms(configuration.getInt(CoreConfigurationType.PROP_MAX_NUMBER_ROOMS));
  }

  private void setupScheduleService(Configuration configuration) {
    scheduleService.setCcuReportInterval(
        configuration.getInt(CoreConfigurationType.INTERVAL_CCU_SCAN));
    scheduleService.setDeadlockScanInterval(
        configuration.getInt(CoreConfigurationType.INTERVAL_DEADLOCK_SCAN));
    scheduleService.setDisconnectedPlayerScanInterval(
        configuration.getInt(CoreConfigurationType.INTERVAL_DISCONNECTED_PLAYER_SCAN));
    scheduleService
        .setRemovedRoomScanInterval(
            configuration.getInt(CoreConfigurationType.INTERVAL_REMOVED_ROOM_SCAN));
    scheduleService
        .setSystemMonitoringInterval(
            configuration.getInt(CoreConfigurationType.INTERVAL_SYSTEM_MONITORING));
    scheduleService
        .setTrafficCounterInterval(
            configuration.getInt(CoreConfigurationType.INTERVAL_TRAFFIC_COUNTER));

    scheduleService.setSessionManager(networkService.getSessionManager());
    scheduleService.setPlayerManager(playerManager);
    scheduleService.setRoomManager(roomManager);
    scheduleService.setNetworkReaderStatistic(networkService.getNetworkReaderStatistic());
    scheduleService.setNetworkWriterStatistic(networkService.getNetworkWriterStatistic());
  }

  private void setupNetworkService(Configuration configuration, BootstrapHandler bootstrapHandler)
      throws IllegalArgumentException, SecurityException {

    ConnectionFilter connectionFilter = bootstrapHandler.getBeanByClazz(ConnectionFilter.class);
    if (Objects.isNull(connectionFilter)) {
      connectionFilter = new DefaultConnectionFilter();
    }
    networkService.setConnectionFilterClass(
        connectionFilter,
        configuration.getInt(CoreConfigurationType.NETWORK_PROP_MAX_CONNECTIONS_PER_IP));

    var servletMap = bootstrapHandler.getServletMap();
    var httpConfiguration = configuration.get(CoreConfigurationType.NETWORK_HTTP);
    networkService.setHttpConfiguration(
        Objects.nonNull(httpConfiguration) ?
            configuration.getInt(CoreConfigurationType.WORKER_HTTP_WORKER) : 0,
        Objects.nonNull(httpConfiguration) ?
            ((SocketConfiguration) httpConfiguration).port() : 0,
        Objects.nonNull(httpConfiguration) ? servletMap : null);

    networkService.setSocketAcceptorServerAddress(
        configuration.getString(CoreConfigurationType.SERVER_ADDRESS));

    networkService.setSocketAcceptorBufferSize(
        configuration.getInt(CoreConfigurationType.NETWORK_PROP_SOCKET_ACCEPTOR_BUFFER_SIZE));
    networkService.setSocketAcceptorWorkers(
        configuration.getInt(CoreConfigurationType.WORKER_SOCKET_ACCEPTOR));

    var udpSocketConfiguration = Objects.nonNull(configuration.get(CoreConfigurationType.NETWORK_UDP)) ?
        (SocketConfiguration) configuration.get(CoreConfigurationType.NETWORK_UDP) : null;
    var kcpSocketConfiguration = Objects.nonNull(configuration.get(CoreConfigurationType.NETWORK_KCP)) ?
        (SocketConfiguration) configuration.get(CoreConfigurationType.NETWORK_KCP) : null;
    networkService.setSocketConfiguration(
        (Objects.nonNull(configuration.get(CoreConfigurationType.NETWORK_TCP)) ?
            (SocketConfiguration) configuration.get(CoreConfigurationType.NETWORK_TCP) : null),
        udpSocketConfiguration,
        (Objects.nonNull(configuration.get(CoreConfigurationType.NETWORK_WEBSOCKET)) ?
            (SocketConfiguration) configuration.get(CoreConfigurationType.NETWORK_WEBSOCKET) : null),
        kcpSocketConfiguration);

    if (Objects.nonNull(udpSocketConfiguration)) {
      datagramChannelManager.configureUdpPort(udpSocketConfiguration.port());
    }
    if (Objects.nonNull(kcpSocketConfiguration)) {
      datagramChannelManager.configureKcpPort(kcpSocketConfiguration.port());
    }

    networkService.setSocketReaderBufferSize(
        configuration.getInt(CoreConfigurationType.NETWORK_PROP_SOCKET_READER_BUFFER_SIZE));
    networkService.setSocketReaderWorkers(
        configuration.getInt(CoreConfigurationType.WORKER_SOCKET_READER));

    networkService.setSocketWriterBufferSize(
        configuration.getInt(CoreConfigurationType.NETWORK_PROP_SOCKET_WRITER_BUFFER_SIZE));
    networkService.setSocketWriterWorkers(
        configuration.getInt(CoreConfigurationType.WORKER_SOCKET_WRITER));

    networkService
        .setWebSocketConsumerWorkers(
            configuration.getInt(CoreConfigurationType.WORKER_WEBSOCKET_CONSUMER));
    networkService
        .setWebSocketProducerWorkers(
            configuration.getInt(CoreConfigurationType.WORKER_WEBSOCKET_PRODUCER));

    networkService.setDataType(
        DataType.getByValue(configuration.getString(CoreConfigurationType.DATA_SERIALIZATION)));

    networkService.setWebSocketReceiverBufferSize(
        configuration.getInt(CoreConfigurationType.NETWORK_PROP_WEBSOCKET_RECEIVER_BUFFER_SIZE));
    networkService.setWebSocketSenderBufferSize(
        configuration.getInt(CoreConfigurationType.NETWORK_PROP_WEBSOCKET_SENDER_BUFFER_SIZE));
    networkService
        .setWebSocketUsingSsl(
            configuration.getBoolean(CoreConfigurationType.NETWORK_PROP_WEBSOCKET_USING_SSL));

    PacketQueuePolicy packetQueuePolicy = bootstrapHandler.getBeanByClazz(PacketQueuePolicy.class);
    if (Objects.isNull(packetQueuePolicy)) {
      packetQueuePolicy = new DefaultPacketQueuePolicy();
    }
    networkService.setPacketQueuePolicy(packetQueuePolicy);
    networkService.setPacketQueueSize(
        configuration.getInt(CoreConfigurationType.PROP_MAX_PACKET_QUEUE_SIZE));

    networkService.setSessionMaxIdleTimeInSeconds(
        configuration.getInt(CoreConfigurationType.PROP_MAX_PLAYER_IDLE_TIME));

    BinaryPacketCompressor binaryPacketCompressor =
        bootstrapHandler.getBeanByClazz(BinaryPacketCompressor.class);
    if (Objects.isNull(binaryPacketCompressor)) {
      binaryPacketCompressor = new DefaultBinaryPacketCompressor();
    }
    BinaryPacketEncryptor binaryPacketEncryptor =
        bootstrapHandler.getBeanByClazz(BinaryPacketEncryptor.class);
    if (Objects.isNull(binaryPacketEncryptor)) {
      binaryPacketEncryptor = new DefaultBinaryPacketEncryptor();
    }
    BinaryPacketEncoder binaryPacketEncoder =
        bootstrapHandler.getBeanByClazz(BinaryPacketEncoder.class);
    if (Objects.isNull(binaryPacketEncoder)) {
      binaryPacketEncoder = new DefaultBinaryPacketEncoder();
    }
    BinaryPacketDecoder binaryPacketDecoder =
        bootstrapHandler.getBeanByClazz(BinaryPacketDecoder.class);
    if (Objects.isNull(binaryPacketDecoder)) {
      binaryPacketDecoder = new DefaultBinaryPacketDecoder();
    }

    binaryPacketEncoder.setCompressionThresholdBytes(
        configuration.getInt(
            CoreConfigurationType.NETWORK_PROP_PACKET_COMPRESSION_THRESHOLD_BYTES));
    binaryPacketEncoder.setCompressor(binaryPacketCompressor);
    binaryPacketEncoder.setEncryptor(binaryPacketEncryptor);

    binaryPacketDecoder.setCompressor(binaryPacketCompressor);
    binaryPacketDecoder.setEncryptor(binaryPacketEncryptor);

    networkService.setPacketDecoder(binaryPacketDecoder);
    networkService.setPacketEncoder(binaryPacketEncoder);
  }

  private void setupInternalProcessorService(Configuration configuration) {
    internalProcessorService.setDataType(
        DataType.getByValue(configuration.getString(CoreConfigurationType.DATA_SERIALIZATION)));
    internalProcessorService
        .setMaxNumberPlayers(configuration.getInt(CoreConfigurationType.PROP_MAX_NUMBER_PLAYERS));
    internalProcessorService.setSessionManager(networkService.getSessionManager());
    internalProcessorService.setPlayerManager(playerManager);
    internalProcessorService
        .setMaxRequestQueueSize(
            configuration.getInt(CoreConfigurationType.PROP_MAX_REQUEST_QUEUE_SIZE));
    internalProcessorService
        .setThreadPoolSize(configuration.getInt(CoreConfigurationType.WORKER_INTERNAL_PROCESSOR));
    internalProcessorService.setKeepPlayerOnDisconnection(
        configuration.getBoolean(CoreConfigurationType.PROP_KEEP_PLAYER_ON_DISCONNECTION));

    internalProcessorService.setNetworkReaderStatistic(networkService.getNetworkReaderStatistic());
    internalProcessorService.setNetworkWriterStatistic(networkService.getNetworkWriterStatistic());
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
    info("SERVER", serverName, "Stopping ...");
    // emit "server shutdown" event
    eventManager.emit(ServerEvent.SERVER_TEARDOWN, serverName);
    shutdownServices();
    info("SERVER", serverName, "Stopped");
    // real stop
    Runtime.getRuntime().halt(0);
  }

  private void shutdownServices() {
    internalProcessorService.shutdown();
    networkService.shutdown();
    scheduleService.shutdown();
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
  public DataType getDataType() {
    return dataType;
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
    networkService.write(response, markedAsLast);
  }
}

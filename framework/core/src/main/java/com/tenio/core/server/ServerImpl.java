/*
The MIT License

Copyright (c) 2016-2022 kong <congcoi123@gmail.com>

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
import com.tenio.common.constant.Trademark;
import com.tenio.common.logger.SystemLogger;
import com.tenio.core.api.ServerApi;
import com.tenio.core.api.implement.ServerApiImpl;
import com.tenio.core.bootstrap.BootstrapHandler;
import com.tenio.core.configuration.constant.CoreConstant;
import com.tenio.core.configuration.define.CoreConfigurationType;
import com.tenio.core.configuration.define.ServerEvent;
import com.tenio.core.entity.manager.PlayerManager;
import com.tenio.core.entity.manager.RoomManager;
import com.tenio.core.entity.manager.implement.PlayerManagerImpl;
import com.tenio.core.entity.manager.implement.RoomManagerImpl;
import com.tenio.core.event.implement.EventManager;
import com.tenio.core.monitoring.system.SystemInfo;
import com.tenio.core.network.NetworkService;
import com.tenio.core.network.NetworkServiceImpl;
import com.tenio.core.network.define.data.HttpConfig;
import com.tenio.core.network.define.data.SocketConfig;
import com.tenio.core.network.entity.packet.policy.PacketQueuePolicy;
import com.tenio.core.network.entity.protocol.Response;
import com.tenio.core.network.security.filter.ConnectionFilter;
import com.tenio.core.network.zero.codec.compression.BinaryPacketCompressor;
import com.tenio.core.network.zero.codec.decoder.BinaryPacketDecoder;
import com.tenio.core.network.zero.codec.encoder.BinaryPacketEncoder;
import com.tenio.core.network.zero.codec.encryption.BinaryPacketEncryptor;
import com.tenio.core.schedule.ScheduleService;
import com.tenio.core.schedule.ScheduleServiceImpl;
import com.tenio.core.server.service.InternalProcessorService;
import com.tenio.core.server.service.InternalProcessorServiceImpl;
import com.tenio.core.server.setting.ConfigurationAssessment;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import javax.annotation.concurrent.ThreadSafe;
import org.apache.logging.log4j.util.Strings;

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
  private final InternalProcessorService internalProcessorService;
  private final ScheduleService scheduleService;
  private final NetworkService networkService;
  private final ServerApi serverApi;
  private String serverName;

  private ServerImpl() {
    eventManager = EventManager.newInstance();
    roomManager = RoomManagerImpl.newInstance(eventManager);
    playerManager = PlayerManagerImpl.newInstance(eventManager);
    networkService = NetworkServiceImpl.newInstance(eventManager);
    internalProcessorService = InternalProcessorServiceImpl.newInstance(eventManager);
    scheduleService = ScheduleServiceImpl.newInstance(eventManager);
    serverApi = ServerApiImpl.newInstance(this);

    // print out the framework's preface
    var trademark = String.format("\n\n%s\n", Strings.join(Arrays.asList(Trademark.CONTENT), '\n'));
    info("HAPPY CODING", trademark);
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

    // get the file path
    var file = params.length == 0 ? null : params[0];
    if (Objects.isNull(file)) {
      file = CoreConstant.DEFAULT_CONFIGURATION_FILE;
    }

    // load configuration file
    var configuration = bootstrapHandler.getConfigurationHandler().getConfiguration();
    configuration.load(file);

    serverName = configuration.getString(CoreConfigurationType.SERVER_NAME);

    // show system information
    var systemInfo = new SystemInfo();
    systemInfo.logSystemInfo();
    systemInfo.logNetCardsInfo();
    systemInfo.logDiskInfo();

    info("SERVER", serverName, "Starting ...");

    // subscribing for processes and handlers
    internalProcessorService.subscribe();

    bootstrapHandler.getEventHandler().initialize(eventManager);

    // collect all subscribers, listen all the events
    eventManager.subscribe();

    var assessment = ConfigurationAssessment.newInstance(eventManager, configuration);
    assessment.assess();

    // Put the current configurations to the logger
    info("CONFIGURATION", configuration.toString());

    setupNetworkService(configuration);
    setupInternalProcessorService(configuration);
    setupScheduleService(configuration);

    initializeServices();
    startServices();

    // emit "server started" event
    eventManager.emit(ServerEvent.SERVER_INITIALIZATION, serverName, configuration);

    info("SERVER", serverName, "Started");
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

    scheduleService.setPlayerManager(playerManager);
    scheduleService.setRoomManager(roomManager);
    scheduleService.setNetworkReaderStatistic(networkService.getNetworkReaderStatistic());
    scheduleService.setNetworkWriterStatistic(networkService.getNetworkWriterStatistic());
  }

  @SuppressWarnings("unchecked")
  private void setupNetworkService(Configuration configuration)
      throws ClassNotFoundException, InstantiationException, IllegalAccessException,
      IllegalArgumentException,
      InvocationTargetException, NoSuchMethodException, SecurityException {

    final var connectionFilterClazz = Class
        .forName(configuration.getString(CoreConfigurationType.CLASS_CONNECTION_FILTER).strip());
    networkService.setConnectionFilterClass(
        (Class<? extends ConnectionFilter>) connectionFilterClazz,
        configuration.getInt(CoreConfigurationType.NETWORK_PROP_MAX_CONNECTIONS_PER_IP));

    final var httpConfig = (List<HttpConfig>) configuration.get(CoreConfigurationType.HTTP_CONFIGS);
    networkService.setHttpPort(!httpConfig.isEmpty() ? httpConfig.get(0).getPort() : 0);
    networkService.setHttpPathConfigs(!httpConfig.isEmpty() ? httpConfig.get(0).getPaths() : null);

    networkService.setSocketAcceptorBufferSize(
        configuration.getInt(CoreConfigurationType.NETWORK_PROP_SOCKET_ACCEPTOR_BUFFER_SIZE));
    networkService.setSocketAcceptorWorkers(
        configuration.getInt(CoreConfigurationType.THREADS_SOCKET_ACCEPTOR));

    networkService.setSocketConfigs(
        (List<SocketConfig>) configuration.get(CoreConfigurationType.SOCKET_CONFIGS));

    networkService.setSocketReaderBufferSize(
        configuration.getInt(CoreConfigurationType.NETWORK_PROP_SOCKET_READER_BUFFER_SIZE));
    networkService.setSocketReaderWorkers(
        configuration.getInt(CoreConfigurationType.THREADS_SOCKET_READER));

    networkService.setSocketWriterBufferSize(
        configuration.getInt(CoreConfigurationType.NETWORK_PROP_SOCKET_WRITER_BUFFER_SIZE));
    networkService.setSocketWriterWorkers(
        configuration.getInt(CoreConfigurationType.THREADS_SOCKET_WRITER));

    networkService
        .setWebSocketConsumerWorkers(
            configuration.getInt(CoreConfigurationType.THREADS_WEBSOCKET_CONSUMER));
    networkService
        .setWebSocketProducerWorkers(
            configuration.getInt(CoreConfigurationType.THREADS_WEBSOCKET_PRODUCER));

    networkService.setWebSocketReceiverBufferSize(
        configuration.getInt(CoreConfigurationType.NETWORK_PROP_WEBSOCKET_RECEIVER_BUFFER_SIZE));
    networkService.setWebSocketSenderBufferSize(
        configuration.getInt(CoreConfigurationType.NETWORK_PROP_WEBSOCKET_SENDER_BUFFER_SIZE));
    networkService
        .setWebSocketUsingSsl(
            configuration.getBoolean(CoreConfigurationType.NETWORK_PROP_WEBSOCKET_USING_SSL));

    final var packetQueuePolicyClazz = Class
        .forName(configuration.getString(CoreConfigurationType.CLASS_PACKET_QUEUE_POLICY).strip());
    networkService.setPacketQueuePolicy(
        (Class<? extends PacketQueuePolicy>) packetQueuePolicyClazz);
    networkService.setPacketQueueSize(
        configuration.getInt(CoreConfigurationType.PROP_MAX_PACKET_QUEUE_SIZE));

    final var binaryPacketCompressorClazz = Class
        .forName(configuration.getString(CoreConfigurationType.CLASS_PACKET_COMPRESSOR).strip());
    final var binaryPacketCompressor =
        (BinaryPacketCompressor) binaryPacketCompressorClazz.getDeclaredConstructor()
            .newInstance();
    final var binaryPacketEncryptorClazz = Class
        .forName(configuration.getString(CoreConfigurationType.CLASS_PACKET_ENCRYPTOR).strip());
    final var binaryPacketEncryptor =
        (BinaryPacketEncryptor) binaryPacketEncryptorClazz.getDeclaredConstructor()
            .newInstance();
    final var binaryPacketEncoderClazz = Class
        .forName(configuration.getString(CoreConfigurationType.CLASS_PACKET_ENCODER).strip());
    final var binaryPacketEncoder =
        (BinaryPacketEncoder) binaryPacketEncoderClazz.getDeclaredConstructor().newInstance();
    final var binaryPacketDecoderClazz = Class
        .forName(configuration.getString(CoreConfigurationType.CLASS_PACKET_DECODER).strip());
    final var binaryPacketDecoder =
        (BinaryPacketDecoder) binaryPacketDecoderClazz.getDeclaredConstructor().newInstance();

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
    internalProcessorService
        .setMaxNumberPlayers(configuration.getInt(CoreConfigurationType.PROP_MAX_NUMBER_PLAYERS));
    internalProcessorService.setPlayerManager(playerManager);
    internalProcessorService
        .setMaxRequestQueueSize(
            configuration.getInt(CoreConfigurationType.PROP_MAX_REQUEST_QUEUE_SIZE));
    internalProcessorService
        .setThreadPoolSize(configuration.getInt(CoreConfigurationType.THREADS_INTERNAL_PROCESSOR));
  }

  @Override
  public void shutdown() {
    info("SERVER", serverName, "Stopping ...");
    // emit "server shutdown" event
    eventManager.emit(ServerEvent.SERVER_TEARDOWN, serverName);
    shutdownServices();
    info("SERVER", serverName, "Stopped");
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
  public void write(Response response) {
    networkService.write(response);
  }
}

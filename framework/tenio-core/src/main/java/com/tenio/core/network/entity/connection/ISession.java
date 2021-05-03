package com.tenio.core.network.entity.connection;

public interface ISession {
	int getId();

    void setId(int var1);

    String getHashId();

    void setHashId(String var1);

    SessionType getType();

    void setType(SessionType var1);

    String getNodeId();

    void setNodeId(String var1);

    boolean isLocal();

    boolean isLoggedIn();

    void setLoggedIn(boolean var1);

    IPacketQueue getPacketQueue();

    void setPacketQueue(IPacketQueue var1);

    SocketChannel getConnection();

    void setConnection(SocketChannel var1);

    DatagramChannel getDatagramChannel();

    void setDatagrmChannel(DatagramChannel var1);

    long getCreationTime();

    void setCreationTime(long var1);

    boolean isConnected();

    void setConnected(boolean var1);

    long getLastActivityTime();

    void setLastActivityTime(long var1);

    long getLastLoggedInActivityTime();

    void setLastLoggedInActivityTime(long var1);

    long getLastReadTime();

    void setLastReadTime(long var1);

    long getLastWriteTime();

    void setLastWriteTime(long var1);

    long getReadBytes();

    void addReadBytes(long var1);

    long getWrittenBytes();

    void addWrittenBytes(long var1);

    int getDroppedMessages();

    void addDroppedMessages(int var1);

    int getMaxIdleTime();

    void setMaxIdleTime(int var1);

    int getMaxLoggedInIdleTime();

    void setMaxLoggedInIdleTime(int var1);

    boolean isMarkedForEviction();

    void setMarkedForEviction();

    boolean isIdle();

    boolean isFrozen();

    void freeze();

    void unfreeze();

    long getFreezeTime();

    boolean isReconnectionTimeExpired();

    Object getSystemProperty(String var1);

    void setSystemProperty(String var1, Object var2);

    void removeSystemProperty(String var1);

    Object getProperty(String var1);

    void setProperty(String var1, Object var2);

    void removeProperty(String var1);

    String getFullIpAddress();

    String getAddress();

    int getClientPort();

    String getServerAddress();

    int getServerPort();

    String getFullServerIpAddress();

    ISessionManager getSessionManager();

    void setSessionManager(ISessionManager var1);

    void close() throws IOException;

    int getReconnectionSeconds();

    void setReconnectionSeconds(int var1);

    boolean isMobile();

    boolean isWebsocket();

    void setMobile(boolean var1);
}

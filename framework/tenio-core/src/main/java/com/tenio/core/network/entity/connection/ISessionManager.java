package com.tenio.core.network.entity.connection;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.List;

import com.tenio.core.manager.IService;

public interface ISessionManager extends IService {
    void addSession(ISession var1);

    int[] getReconnectStatus();

    void removeSession(ISession var1);

    ISession removeSession(int var1);

    void removeChannel(SocketChannel var1);

    ISession removeSession(String var1);

    ISession removeSession(SocketChannel var1);

    boolean containsSession(ISession var1);

    void shutDownLocalSessions();

    List getAllSessions();

    ISession getSessionById(int var1);

    ISession getSessionByHash(String var1);

    int getNodeSessionCount(String var1);

    List getAllSessionsAtNode(String var1);

    List getAllLocalSessions();

    ISession getLocalSessionById(int var1);

    ISession getLocalSessionByHash(String var1);

    ISession getLocalSessionByConnection(SocketChannel var1);

    int getLocalSessionCount();

    ISession createSession(SocketChannel var1);

    ISession createConnectionlessSession();

    ISession createBlueBoxSession();

    void publishLocalNode(String var1);

    void clearClusterData();

    void onNodeLost(String var1);

    int getHighestCCS();

    void addSessionToken(ISession var1);

    ISession getSessionbyToken(String var1);

    void onSocketDisconnected(SocketChannel var1) throws IOException;

    void onSocketDisconnected(ISession var1) throws IOException;

    ISession reconnectSession(ISession var1, String var2) throws SessionReconnectionException, IOException;

    ISession createWebSocketSession(Object var1);
}

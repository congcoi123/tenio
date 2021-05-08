package com.tenio.core.network.entity.connection;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.List;

import com.tenio.core.manager.IService;

public interface SessionManager extends IService {
    void addSession(Session var1);

    int[] getReconnectStatus();

    void removeSession(Session var1);

    Session removeSession(int var1);

    void removeChannel(SocketChannel var1);

    Session removeSession(String var1);

    Session removeSession(SocketChannel var1);

    boolean containsSession(Session var1);

    void shutDownLocalSessions();

    List getAllSessions();

    Session getSessionById(int var1);

    Session getSessionByHash(String var1);

    int getNodeSessionCount(String var1);

    List getAllSessionsAtNode(String var1);

    List getAllLocalSessions();

    Session getLocalSessionById(int var1);

    Session getLocalSessionByHash(String var1);

    Session getLocalSessionByConnection(SocketChannel var1);

    int getLocalSessionCount();

    Session createSession(SocketChannel var1);

    Session createConnectionlessSession();

    Session createBlueBoxSession();

    void publishLocalNode(String var1);

    void clearClusterData();

    void onNodeLost(String var1);

    int getHighestCCS();

    void addSessionToken(Session var1);

    Session getSessionbyToken(String var1);

    void onSocketDisconnected(SocketChannel var1) throws IOException;

    void onSocketDisconnected(Session var1) throws IOException;

    Session reconnectSession(Session var1, String var2) throws SessionReconnectionException, IOException;

    Session createWebSocketSession(Object var1);
}

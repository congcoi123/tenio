package com.tenio.core.network;

public class WriterManager {

	public void write(IResponse response) {
        if (this.configuration.getWebSocketEngineConfig().isActive()) {
             List webSocketRecipients = new ArrayList();
             List socketRecipients = new ArrayList();
             Iterator it = response.getRecipients().iterator();

             while(it.hasNext()) {
                  ISession session = (ISession)it.next();
                  if (session.isWebsocket()) {
                       webSocketRecipients.add(session);
                  } else {
                       socketRecipients.add(session);
                  }
             }

             this.bootLogger.debug("Web size: " + webSocketRecipients.size());
             this.bootLogger.debug("Socket size: " + socketRecipients.size());
             if (webSocketRecipients.size() > 0) {
                  response.setRecipients((Collection)socketRecipients);
                  IResponse webSocketResponse = Response.clone(response);
                  webSocketResponse.setRecipients((Collection)webSocketRecipients);
                  this.writeToWebSocket(webSocketResponse);
             }
        }

        this.writeToSocket(response);
   }

   private void writeToSocket(IResponse res) {
        this.bootLogger.debug("Write to socket");
        this.engineWriter.getIOHandler().getCodec().onPacketWrite(res);
   }

   private void writeToWebSocket(IResponse res) {
        this.bootLogger.debug("Write to websocket");
        this.webSocketService.getProtocolCodec().onPacketWrite(res);
   }
	
}

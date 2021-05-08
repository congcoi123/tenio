package com.tenio.core.network;

import com.tenio.core.network.entity.protocol.Response;
import com.tenio.core.network.zero.engine.ZeroWriter;

public class WriterManager {
	
	// create zero writer and netty websocket server here
	private ZeroWriter writer;
	private boolean isWebSocketEnabled;

	public void write(Response response) {
		
        if (isWebSocketEnabled) {
        	writeToWebSocket(response);
        }

        writeToSocket(response);
   }

   private void writeToSocket(Response res) {
	   writer.getProtocolCode().onPacketWrite(res);
   }

   private void writeToWebSocket(Response res) {
        // this.webSocketService.getProtocolCodec().onPacketWrite(res);
   }
	
}

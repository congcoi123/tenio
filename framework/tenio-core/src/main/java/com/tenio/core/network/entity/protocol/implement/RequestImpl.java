package com.tenio.core.network.entity.protocol.implement;

import com.tenio.core.network.define.ResponsePriority;
import com.tenio.core.network.define.TransportType;
import com.tenio.core.network.entity.protocol.Request;
import com.tenio.core.network.entity.session.SessionType;

public final class RequestImpl extends AbstractMessage implements Request {
     private ISession sender;
     private TransportType type;
     private ResponsePriority priority;
     private long timeStamp;

     public RequestImpl() {
          this.type = TransportType.TCP;
          this.priority = ResponsePriority.NORMAL;
          this.timeStamp = System.nanoTime();
     }

     public ISession getSender() {
          return this.sender;
     }

     public TransportType getTransportType() {
          return this.type;
     }

     public void setSender(ISession session) {
          this.sender = session;
     }

     public void setTransportType(TransportType type) {
          this.type = type;
     }

     public ResponsePriority getPriority() {
          return this.priority;
     }

     public void setPriority(ResponsePriority priority) {
          this.priority = priority;
     }

     public long getTimeStamp() {
          return this.timeStamp;
     }

     public void setTimeStamp(long timeStamp) {
          this.timeStamp = timeStamp;
     }

     public boolean isTcp() {
          return this.type == TransportType.TCP;
     }

     public boolean isUdp() {
          return this.type == TransportType.UDP;
     }

     public boolean isWebsocket() {
          return this.sender.getType() == SessionType.WEBSOCKET;
     }

     public String toString() {
     }
}

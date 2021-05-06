package com.tenio.core.network.entity.protocol.implement;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.tenio.core.network.define.TransportType;
import com.tenio.core.network.entity.connection.Session;
import com.tenio.core.network.entity.protocol.Response;

public class ResponseImpl extends AbstractMessage implements Response {
     private Collection recipients;
     private TransportType type;
     private Object targetController;

     public ResponseImpl() {
          this.type = TransportType.TCP;
     }

     public Collection getRecipients() {
          return this.recipients;
     }

     public TransportType getTransportType() {
          return this.type;
     }

     public boolean isTCP() {
          return this.type == TransportType.TCP;
     }

     public boolean isUDP() {
          return this.type == TransportType.UDP;
     }

     public void setRecipients(Collection recipents) {
          this.recipients = recipents;
     }

     public void setRecipients(Session session) {
          List recipients = new ArrayList();
          recipients.add(session);
          this.setRecipients((Collection)recipients);
     }

     public void setTransportType(TransportType type) {
          this.type = type;
     }

     public void write() {
          // engine write this
     }

     public void write(int delay) {
          // write delay
     }

     public Object getTargetController() {
          return this.targetController;
     }

     public void setTargetController(Object o) {
          this.targetController = o;
     }

     public static Response clone(Response original) {
          Response newResponse = new ResponseImpl();
          newResponse.setContent(original.getContent());
          newResponse.setTargetController(original.getTargetController());
          newResponse.setId(original.getId());
          newResponse.setTransportType(original.getTransportType());
          return newResponse;
     }
}

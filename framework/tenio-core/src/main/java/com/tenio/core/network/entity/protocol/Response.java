package com.tenio.core.network.entity.protocol;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.tenio.core.network.define.TransportType;
import com.tenio.core.network.entity.connection.ISession;

public class Response extends AbstractMessage implements IResponse {
     private Collection recipients;
     private TransportType type;
     private Object targetController;

     public Response() {
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

     public void setRecipients(ISession session) {
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

     public static IResponse clone(IResponse original) {
          IResponse newResponse = new Response();
          newResponse.setContent(original.getContent());
          newResponse.setTargetController(original.getTargetController());
          newResponse.setId(original.getId());
          newResponse.setTransportType(original.getTransportType());
          return newResponse;
     }
}

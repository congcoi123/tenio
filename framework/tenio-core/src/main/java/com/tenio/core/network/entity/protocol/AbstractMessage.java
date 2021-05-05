package com.tenio.core.network.entity.protocol;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class AbstractMessage implements ZeroMessage {
     protected Object id;
     protected Object content;
     protected Map attributes;

     public Object getId() {
          return this.id;
     }

     public void setId(Object id) {
          this.id = id;
     }

     public Object getContent() {
          return this.content;
     }

     public void setContent(Object content) {
          this.content = content;
     }

     public Object getAttribute(String key) {
          Object attr = null;
          if (this.attributes != null) {
               attr = this.attributes.get(key);
          }

          return attr;
     }

     public void setAttribute(String key, Object attribute) {
          if (this.attributes == null) {
               this.attributes = new ConcurrentHashMap();
          }

          this.attributes.put(key, attribute);
     }
}

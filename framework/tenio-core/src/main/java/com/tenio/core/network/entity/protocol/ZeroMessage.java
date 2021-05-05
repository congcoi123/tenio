package com.tenio.core.network.entity.protocol;

public interface ZeroMessage {
     Object getId();

     void setId(Object id);

     Object getContent();

     void setContent(Object content);

     Object getAttribute(String key);

     void setAttribute(String key, Object value);
}

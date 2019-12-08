package com.tenio.engine.entitas.api.entitas;

/**
 * @author Rubentxu
 */
public interface IAERC {

    int retainCount();

    void retain(Object owner);

    void release(Object owner);

}

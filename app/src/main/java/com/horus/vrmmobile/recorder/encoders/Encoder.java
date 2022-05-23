package com.horus.vrmmobile.recorder.encoders;

public interface Encoder {
    public void encode(short[] buf);

    public void close();
}

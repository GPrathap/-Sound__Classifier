package org.sound.classifier.features;

import be.tarsos.dsp.AudioEvent;
import org.apache.commons.collections.Buffer;
import org.apache.commons.collections.BufferUtils;
import org.apache.commons.collections.buffer.CircularFifoBuffer;

import java.util.HashMap;

public class FeatureProperties {

    private HashMap featureValues;
    //Buffer fifo = BufferUtils.synchronizedBuffer(new CircularFifoBuffer());

    public double getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(double timeStamp) {
        this.timeStamp = timeStamp;
    }

    public void setRms(double rms) {
        this.rms = rms;
    }

    public void setPressureLevel(double pressureLevel) {
        this.pressureLevel = pressureLevel;
    }

    public double getRms() {
        return rms;

    }

    public double getPressureLevel() {
        return pressureLevel;
    }

    public AudioEvent getAudioEvent() {
        return audioEvent;
    }

    public void setAudioEvent(AudioEvent audioEvent) {
        this.audioEvent = audioEvent;
    }

    double timeStamp;
    double rms;
    double pressureLevel;
    AudioEvent audioEvent;

    public void setFeatureValues(HashMap featureValues) {
        this.featureValues = featureValues;
    }

    public HashMap getFeatureValues() {
        return this.featureValues;
    }
}

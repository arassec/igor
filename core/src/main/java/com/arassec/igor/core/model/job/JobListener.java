package com.arassec.igor.core.model.job;

public interface JobListener {

    void notifyStarted(Job job);

    void notifyFinished(Job job);

}

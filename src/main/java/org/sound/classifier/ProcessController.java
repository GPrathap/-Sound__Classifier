/*
  ~ Copyright (c) 2016  WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
  ~
  ~ WSO2 Inc. licenses this file to you under the Apache License,
  ~ Version 2.0 (the "License"); you may not use this file except
  ~ in compliance with the License.
  ~ You may obtain a copy of the License at
  ~
  ~ http://www.apache.org/licenses/LICENSE-2.0
  ~
  ~ Unless required by applicable law or agreed to in writing,
  ~ software distributed under the License is distributed on an
  ~ "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  ~ KIND, either express or implied. See the License for the
  ~ specific language governing permissions and limitations
  ~ under the License.
*/

package org.sound.classifier;

import be.tarsos.dsp.AudioDispatcher;
import org.apache.commons.collections.Buffer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;


public class ProcessController implements Runnable {
    private static final Log log = LogFactory.getLog(ProcessController.class);
    private final CountDownLatch countDownLatch;
    private FeatureProcessor featureProcessor;
    private PropertyList propertyList;
    private Buffer audioFeaturesBuffer;
    private AudioDispatcher dispatcher;


    public ProcessController(CountDownLatch countDownLatch, FeatureProcessor featureProcessor, AudioDispatcher dispatcher, PropertyList propertyList, Buffer audioFeaturesBuffer) {
        this.countDownLatch = countDownLatch;
        this.featureProcessor = featureProcessor;
        this.dispatcher=dispatcher;
        this.propertyList = propertyList;
        this.audioFeaturesBuffer = audioFeaturesBuffer;
    }

    public void run() {
        try {
            featureProcessor.extractFeatures(dispatcher, propertyList, audioFeaturesBuffer);
        } catch (UnsupportedAudioFileException e) {
            SharedCommandLineUtilities.printLine();
            log.error("\tThe audio file is not supported!");
        } catch (IOException e) {
            SharedCommandLineUtilities.printLine();
            log.error("Current error:");
            log.error("\tIO error, maybe the audio file is not found or not supported!");
        } catch (Exception e) {
            log.error(e);
        } finally {
            countDownLatch.countDown();
        }
    }
}

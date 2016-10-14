/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.60 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy
 * of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.60
 *
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations under the License.
 */
package org.sound.classifier.constants;


public final class Constants {

    public static final int ADAPTER_MIN_THREAD_POOL_SIZE = 8;
    public static final int ADAPTER_MAX_THREAD_POOL_SIZE = 100;
    public static final int ADAPTER_EXECUTOR_JOB_QUEUE_SIZE = 10000;
    public static final long DEFAULT_KEEP_ALIVE_TIME_IN_MILLS = 20000;
    public static final int NUMNER_OF_INPUT_STREAM = 1;
    public static final String SAMPLING_RATE = "samplingRate";
    public static final String BEATEXTRACTOR_SIZE = "size";
    public static final int NUMNER_OF_CLASSES = 10;

    public static final int NUMNER_OF_CEPSTRA = 12;
    public static final int WINDOW_SIZE = 1024;
    public static final int OVERLAP_SIZE = 512;
}

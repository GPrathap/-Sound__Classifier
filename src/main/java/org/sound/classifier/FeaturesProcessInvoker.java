/*
*      _______                       _____   _____ _____  
*     |__   __|                     |  __ \ / ____|  __ \ 
*        | | __ _ _ __ ___  ___  ___| |  | | (___ | |__) |
*        | |/ _` | '__/ __|/ _ \/ __| |  | |\___ \|  ___/ 
*        | | (_| | |  \__ \ (_) \__ \ |__| |____) | |     
*        |_|\__,_|_|  |___/\___/|___/_____/|_____/|_|     
*                                                         
* -------------------------------------------------------------
*
* TarsosDSP is developed by Joren Six at IPEM, University Ghent
*  
* -------------------------------------------------------------
*
*  Info: http://0110.be/tag/TarsosDSP
*  Github: https://github.com/JorenSix/TarsosDSP
*  Releases: http://0110.be/releases/TarsosDSP/
*  
*  TarsosDSP includes modified source code by various authors,
*  for credits and info, see README.
* 
*/

package org.sound.classifier;

import be.tarsos.dsp.AudioDispatcher;
import org.apache.commons.collections.Buffer;
import org.apache.commons.collections.BufferUtils;
import org.apache.commons.collections.buffer.CircularFifoBuffer;
import org.sound.classifier.constants.Constants;
import org.sound.classifier.features.FeatureExtractor;
import org.sound.classifier.features.FeatureProperties;
import be.tarsos.dsp.io.jvm.AudioDispatcherFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.*;
import java.net.URL;
import java.util.*;
import java.util.concurrent.*;

// this.getClass().getResource(File.separator + in.nextLine()).getPath();
public class FeaturesProcessInvoker {
    private static  ArrayList<Buffer> audioFeaturesBuffer;
    private static ArrayList<AudioDispatcher> audioDispatcheres;
    private static List<FeatureProcessor> featureExtractors;
	public static volatile ExecutorService executorService;
	private static final Log log = LogFactory.getLog(FeaturesProcessInvoker.class);
    int numberOfInputStrems = Constants.NUMNER_OF_INPUT_STREAM;
    String[] soundFilesNames;
    int numberOfClasses = Constants.NUMNER_OF_CLASSES;
	int numberOfThreads = numberOfInputStrems;
	CountDownLatch countDownLatch;
    HashMap classVectors;
    PropertyList propertyList;
	static DataModel dataModel;
    static StringBuffer featureNames;

	public FeaturesProcessInvoker(){
        initialization();
		dataModel =  new DataModel();
        propertyList = new PropertyList();
        classVectors = initializeClassVector("classNames.txt");
        soundFilesNames = getAllSoundFilesNames("soundFilesNames.txt");
        featureNames = new StringBuffer();
    }

    public void extractFeaturePerSoundClass() throws InterruptedException {
        PrintWriter finalResultX = null;
        PrintWriter finalResultY = null;
        try{
            FileWriter writerX = new FileWriter("resultX.txt", true);
            BufferedWriter bw = new BufferedWriter(writerX);
            finalResultX = new PrintWriter(bw);

            FileWriter writerY = new FileWriter("resultY.txt", true);
            BufferedWriter bwY = new BufferedWriter(writerY);
            finalResultY = new PrintWriter(bwY);

            for(String fileName : soundFilesNames){
                countDownLatch = new CountDownLatch(numberOfThreads);
                int windowSize =  Constants.WINDOW_SIZE;
                int overlapSize = Constants.OVERLAP_SIZE;
                log.info("SOUND EXTRACTION PROCESS: "+ fileName + " WITH WINDOW SIZE "+ windowSize + " AND OVERLAP SIZE "+ overlapSize);
                extractFeaturePerSoundClass(fileName, finalResultX, finalResultY, countDownLatch, windowSize, overlapSize);
                countDownLatch.await();
            }
        } catch (IOException e) {
            log.error(e);
        } finally {
            executorService.shutdown();
            if(finalResultX != null){
                finalResultX.close();
            }
            if(finalResultY != null){
                finalResultY.close();
            }
        }
    }

    private String[] getAllSoundFilesNames(String fileName) {
        Scanner in = null;
        List<String> fileList = new ArrayList<String>();
        try{
            in = new Scanner(new File(fileName));
            while (in.hasNext()) {
                String file = in.nextLine();
                fileList.add(file);
            }
        } catch (FileNotFoundException e) {
            log.error("There is no no file names ...", e);
            return null;
        } finally {
            if(in != null){
                in.close();
            }
        }
        return fileList.toArray(new String[0]);
    }

    public static AudioDispatcher getAudioDispatcher(int dispatcherNumber){
	    return audioDispatcheres.get(dispatcherNumber);
    }

    public static void setAudioDispatcher(File[] audioFiles, int windowSize, int windowOverlap, int sizeOfAudioDispatches, double samplingRate)
            throws Exception {
        audioDispatcheres = new ArrayList<AudioDispatcher>();
        featureExtractors = new ArrayList<FeatureProcessor>();
    	for(int i=0; i<sizeOfAudioDispatches; i++){
            AudioDispatcher audioDispatcher = AudioDispatcherFactory.fromFile(audioFiles[i], windowSize, windowOverlap);
            audioDispatcheres.add(audioDispatcher);
            FeatureExtractor[] featuresList = dataModel.featuresList();
            featureExtractors.add(new FeatureProcessor(windowSize, windowOverlap, samplingRate, false, featuresList));
		}
    }

    public static Buffer getAudioFeaturesBuffer(int bufferNumber){
        return audioFeaturesBuffer.get(bufferNumber);
    }

    public static void setAudioFeaturesBuffers(int numberOfBuffers, int sizeOfTheBuffer) {
        audioFeaturesBuffer = new ArrayList<Buffer>();
        for(int i=0; i<numberOfBuffers; i++){
            Buffer buffer = BufferUtils.synchronizedBuffer(new CircularFifoBuffer(sizeOfTheBuffer));
            audioFeaturesBuffer.add(buffer);
        }

    }

    private void extractFeaturePerSoundClass(String soundFiles,  PrintWriter finalResultX, PrintWriter finalResultY, CountDownLatch countDownLatch, int windowSize, int windowOverlap) {

        try {
            String inputFile = soundFiles;
            URL filePath = this.getClass().getResource(File.separator + "sounds"+ File.separator + soundFiles);
            if ( filePath == null){
                log.info("File doesn't exist.."+ inputFile);
            }else{
                File audioFile = new File(filePath.getPath());
                File[] audioFiles = new File[numberOfInputStrems];
                String className = audioFile.getName().split("__")[0];
                audioFiles[0] = audioFile;
                double samplingRate = AudioSystem.getAudioFileFormat(audioFile).getFormat().getSampleRate();
                propertyList.addProperties(Constants.SAMPLING_RATE, String.valueOf(AudioSystem.getAudioFileFormat(audioFile).getFormat().getSampleRate()));
                propertyList.addProperties(Constants.BEATEXTRACTOR_SIZE, String.valueOf(windowSize));

                setAudioFeaturesBuffers(numberOfInputStrems, 100);
                setAudioDispatcher(audioFiles, windowSize, windowOverlap, numberOfInputStrems, samplingRate);

                //featureExtractors.add(new FeatureProcessor(windowSize, windowOverlap, samplingRate, false, featuresList));
                //AudioDispatcher dispatcher = AudioDispatcherFactory.fromPipe(audioFile.getAbsolutePath(),(int)AudioSystem.getAudioFileFormat(audioFile).getFormat().getSampleRate(), size, overlap);
                for (int i = 0; i < numberOfInputStrems; i++) {
                    executorService.submit(new ProcessController(countDownLatch, featureExtractors.get(i), getAudioDispatcher(i), propertyList, getAudioFeaturesBuffer(i)));
                }
                boolean i = true;
                //log.info("-----------------------------"+ Integer.parseInt((String)classVectors.get(className)));
                int[] classVector = getClassVector(numberOfClasses, Integer.parseInt((String)classVectors.get(className)));
                //log.info(Arrays.toString(classVector)+ "----------------------------------");
                //finalResult.println("class-name:" + className + "||vector:" + Arrays.toString(classVector).replace("[", "").replace("]", ""));
           /* while (i) {
                try {
                    log.info(((FeatureProperties) getAudioFeaturesBuffer(0).get()).getTimeStamp());
                        *//*log.info(((FeatureProperties)getAudioFeaturesBuffer(1).get()).getTimeStamp());
                        log.info(((FeatureProperties)getAudioFeaturesBuffer(2).get()).getTimeStamp());*//*
                    HashMap dataset = ((FeatureProperties) getAudioFeaturesBuffer(0).get()).getFeatureValues();
                    getFeautreValues(dataset, finalResult);
                    getAudioFeaturesBuffer(0).remove();
                       *//* getAudioFeaturesBuffer(1).remove();
                        getAudioFeaturesBuffer(2).remove();*//*
                    //Thread.sleep(100);
                } catch (Exception e) {

                }
            }*/
                while (i) {
                    try {
                        if(getAudioFeaturesBuffer(0).size() > 0){
                            ///log.info(((FeatureProperties)getAudioFeaturesBuffer(0).get()).getTimeStamp()+"-----");
                            HashMap dataset = ((FeatureProperties) getAudioFeaturesBuffer(0).get()).getFeatureValues();
                            getFeautreValues(dataset, finalResultX, finalResultY, classVector);
                            getAudioFeaturesBuffer(0).remove();
                        }else{
                            Thread.sleep(2000);
                            if(getAudioFeaturesBuffer(0).size() == 0){
                                break;
                            }
                        }
                    } finally {

                    }
                }
                //finalResult.println("END_OF_FILE");

                //log.info(audioFeaturesBuffer.getAudioFeaturesBuffer());
                //traversal Throu
                // ghMap(audioFeaturesBuffer.);
                /*audioFeaturesBuffer.get();
                for(Object j: audioFeaturesBuffer){
                    Iterator it = ((FeatureProperties)audioFeaturesBuffer.get()).getFeatureValues().entrySet().iterator();
                    //Map.Entry pair = (Map.Entry)it.next();
                    //log.info(Arrays.toString((double[])map.get("Root Mean Square")));
                    //Iterator it = map.entrySet().iterator();
                    while (it.hasNext()) {
                        Map.Entry pair = (Map.Entry)it.next();
                        log.info(pair.getKey());
                        it.remove(); // avoids a ConcurrentModificationException
                    }
                }*/
            }

        } catch (InterruptedException e) {
            log.error("Error while waiting on org.sound.classifier.thread" + e);
        } catch (NullPointerException e) {
            log.error("Error while starting service..." + e);
        } catch (UnsupportedAudioFileException e) {
            log.error(e);
        } catch (IOException e) {
            log.error(e);
        } catch (Exception e) {
            log.error(e);
        } finally {

        }
    }


    public static void getFeautreValues(Map mp, PrintWriter writerX, PrintWriter writerY, int[] classVector) {
        final List<Object> features = new ArrayList<Object>();
        /*try{

            Iterator it = mp.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry pair = (Map.Entry)it.next();
                out.println(pair.getKey() + "=>" + Arrays.toString((double[])pair.getValue()).replace("[","").replace("]",""));
                it.remove(); // avoids a ConcurrentModificationException
            }
            out.println("WINDOW_END");

        }catch(Exception e){

            log.error(e);
        }*/
        try{
            StringBuffer featuresList=new StringBuffer();
            Iterator it = mp.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry pair = (Map.Entry)it.next();
                featuresList.append(Arrays.toString((double[])pair.getValue()).replace("[","").replace("]",""));
                featuresList.append(",");
                it.remove(); // avoids a ConcurrentModificationException
            }
            writerX.println(featuresList.substring(0,featuresList.length() - 1));
            writerY.println(Arrays.toString(classVector).replace("[","").replace("]",""));
        }catch(Exception e){
            log.error(e);
        }
    }

    public int[] getClassVector(int vectorLength, int position){
        int[] vector = new int[vectorLength];
        for(int i=1; i<=vectorLength; i++){
                vector[position-1] =0;
        }
        vector[position-1] =1;
        return vector;
    }

    public HashMap initializeClassVector(String fileName){
        Scanner in = null;
        HashMap classFeatureVector = new HashMap();
        int numberOfCount = 0;
        try{
            in = new Scanner(new File(fileName));
            while (in.hasNext() && numberOfCount<numberOfClasses) {
                String[] classInfo = in.nextLine().split(",");
                classFeatureVector.put(classInfo[0], classInfo[1]);
                numberOfCount++;
            }
            return classFeatureVector;
        } catch (FileNotFoundException e) {
            log.error(e);
        } finally {
            if(in != null){
                in.close();
            }
        }
        return classFeatureVector;
    }

	/**
	 * @param arguments
	 */
	public static void main(String... arguments) {
		FeaturesProcessInvoker featureProcessInvoker =   new FeaturesProcessInvoker();
        try {
            featureProcessInvoker.extractFeaturePerSoundClass();
        } catch (InterruptedException e) {
            log.error("Error occurred while feautre extracting...", e);
        }
    }

	private static void initialization() {
		try {
			if (executorService == null) {
				RejectedExecutionHandler rejectedExecutionHandler = new RejectedExecutionHandler() {
					public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
						try {
							executor.getQueue().put(r);
						} catch (InterruptedException e) {
							log.error("Exception while adding event to executor queue : " + e.getMessage(), e);
						}
					}
				};
				executorService = new ThreadPoolExecutor(Constants.ADAPTER_MIN_THREAD_POOL_SIZE
						, Constants.ADAPTER_MAX_THREAD_POOL_SIZE, Constants.DEFAULT_KEEP_ALIVE_TIME_IN_MILLS
						, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(Constants
						.ADAPTER_EXECUTOR_JOB_QUEUE_SIZE), rejectedExecutionHandler);
			}
		} catch (Exception e){
			log.error(e);
		}
	}

	public static void traversalThroughMap(Map map) {
		Iterator it = map.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry pair = (Map.Entry)it.next();
			FeatureProperties featureProperties = (FeatureProperties) pair.getValue();
            Iterator featureValues = featureProperties.getFeatureValues().entrySet().iterator();
            while (featureValues.hasNext()) {
                Map.Entry featureValue = (Map.Entry)featureValues.next();
                //log.info(featureValue.getKey() + " values: " + Arrays.toString(((double[])featureValue.getValue()).length));
                //log.info(featureValue.getKey() + " values: " +((double[])featureValue.getValue()).length);
                //writeMatrix("result.txt", Arrays.deepToString());
                featureValues.remove();
            }
			it.remove(); // avoids a ConcurrentModificationException
		}
	}

	static void writeMatrix(String filename, String values) {
		FileWriter fileWriter;
		try {
			fileWriter = new FileWriter(filename,true);
			BufferedWriter bufferFileWriter = new BufferedWriter(fileWriter);
			bufferFileWriter.append(values);
			bufferFileWriter.newLine();
			bufferFileWriter.close();
		} catch (IOException ex) {
			log.error("error"+ ex);
		}
	}
}

package org.sound.classifier.features;

import jAudioFeatureExtractor.ACE.DataTypes.FeatureDefinition;
import org.sound.classifier.constants.Constants;
import org.sound.classifier.features.mfcc.Delta;
import org.sound.classifier.features.mfcc.Energy;
import org.sound.classifier.features.mfcc.FeatureVector;
import org.sound.classifier.features.mfcc.MFCC;

public class MFCCFeatures  extends FeatureExtractor {

    private float[][] framedSignal;
    private int samplePerFrame;
    private int noOfFrames;
    /**
     * how many mfcc coefficients per frame
     */
    private int numCepstra = Constants.NUMNER_OF_CEPSTRA;

    private double[][] featureVector;
    private double[][] mfccFeature;
    private double[][] deltaMfcc;
    private double[][] deltaDeltaMfcc;
    private double[] energyVal;
    private double[] deltaEnergy;
    private double[] deltaDeltaEnergy;
    private FeatureVector fv;
    private MFCC mfcc;
    private Delta delta;
    private Energy en;


    public String name() {
        return null;
    }

    public MFCCFeatures(){
        String name = "MFCCFeatures";
        String description = "TMFCCFeatures  mfcc, mfcc delta, mfcc delta delta";
        boolean is_sequential = true;
        int dimensions = 1;
        definition = new FeatureDefinition( name, description, is_sequential, dimensions );
        dependencies = null;
    }

    public Object clone(){
        return new MFCCFeatures();
    }

    public void featureExtract(float[][] framedSignal, int samplingRate, int samplePerFrame) {
        this.framedSignal = framedSignal;
        this.noOfFrames = framedSignal.length;
        this.samplePerFrame = samplePerFrame;
        mfcc = new MFCC(samplePerFrame, samplingRate, numCepstra);
        en = new Energy(samplePerFrame);
        fv = new FeatureVector();
        mfccFeature = new double[noOfFrames][numCepstra];
        deltaMfcc = new double[noOfFrames][numCepstra];
        deltaDeltaMfcc = new double[noOfFrames][numCepstra];
        energyVal = new double[noOfFrames];
        deltaEnergy = new double[noOfFrames];
        deltaDeltaEnergy = new double[noOfFrames];
        featureVector = new double[noOfFrames][3 * numCepstra + 3];
        delta = new Delta();
    }

    public double[] extractFeature( double[] samples, double sampling_rate, double[][] other_feature_values )
            throws Exception {
        float[] floatValues = toFloatArray(samples);
        float[][] audioSample = new float[1][samples.length];
        audioSample[0] = floatValues;
        featureExtract(audioSample, (int)sampling_rate, samples.length);
        makeMfccFeatureVector();
        double[] result = new double[mfccFeature[0].length + deltaMfcc[0].length+ deltaDeltaMfcc[0].length];
        System.arraycopy(mfccFeature[0], 0, result, 0, mfccFeature[0].length);
        System.arraycopy(deltaMfcc[0], 0, result, mfccFeature[0].length, deltaMfcc[0].length);
        System.arraycopy(deltaDeltaMfcc[0], 0, result, deltaMfcc[0].length, deltaDeltaMfcc[0].length);
        return result;
    }

    float[] toFloatArray(double[] arr) {
        if (arr == null) return null;
        int n = arr.length;
        float[] ret = new float[n];
        for (int i = 0; i < n; i++) {
            ret[i] = (float)arr[i];
        }
        return ret;
    }

    public void makeMfccFeatureVector() {
        calculateMFCC();
        doCepstralMeanNormalization();
        // delta
        delta.setRegressionWindow(1);// 2 for delta
        deltaMfcc = delta.performDelta2D(mfccFeature);
        // delta delta
        delta.setRegressionWindow(1);// 1 for delta delta
        deltaDeltaMfcc = delta.performDelta2D(deltaMfcc);
        // energy
        energyVal = en.calcEnergy(framedSignal);

        delta.setRegressionWindow(1);
        // energy delta
        deltaEnergy = delta.performDelta1D(energyVal);
        delta.setRegressionWindow(1);
        // energy delta delta
        deltaDeltaEnergy = delta.performDelta1D(deltaEnergy);
        for (int i = 0; i < framedSignal.length; i++) {
            for (int j = 0; j < numCepstra; j++) {
                featureVector[i][j] = mfccFeature[i][j];
            }
            for (int j = numCepstra; j < 2 * numCepstra; j++) {
                featureVector[i][j] = deltaMfcc[i][j - numCepstra];
            }
            for (int j = 2 * numCepstra; j < 3 * numCepstra; j++) {
                featureVector[i][j] = deltaDeltaMfcc[i][j - 2 * numCepstra];
            }
            featureVector[i][3 * numCepstra] = energyVal[i];
            featureVector[i][3 * numCepstra + 1] = deltaEnergy[i];
            featureVector[i][3 * numCepstra + 2] = deltaDeltaEnergy[i];
        }
        fv.setMfccFeature(mfccFeature);
        fv.setFeatureVector(featureVector);
        //System.gc();
    }

    /**
     * calculates MFCC coefficients of each frame
     */
    private void calculateMFCC() {
        for (int i = 0; i < noOfFrames; i++) {
            // for each frame i, make mfcc from current framed signal
            mfccFeature[i] = mfcc.doMFCC(framedSignal[i]);// 2D data
        }
    }

    /**
     * performs cepstral mean substraction. <br>
     * it removes channel effect...
     */
    private void doCepstralMeanNormalization() {
        double sum;
        double mean;
        double mCeps[][] = new double[noOfFrames][numCepstra - 1];// same size
        // as mfcc
        // 1.loop through each mfcc coeff
        for (int i = 0; i < numCepstra - 1; i++) {
            // calculate mean
            sum = 0.0;
            for (int j = 0; j < noOfFrames; j++) {
                sum += mfccFeature[j][i];// ith coeff of all frame
            }
            mean = sum / noOfFrames;
            // subtract
            for (int j = 0; j < noOfFrames; j++) {
                mCeps[j][i] = mfccFeature[j][i] - mean;
            }
        }
    }
}


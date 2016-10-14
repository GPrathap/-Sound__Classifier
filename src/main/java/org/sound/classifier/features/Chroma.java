package org.sound.classifier.features;

import jAudioFeatureExtractor.ACE.DataTypes.FeatureDefinition;
import jAudioFeatureExtractor.DataModel;

import java.util.Arrays;

/**
 * Created with IntelliJ IDEA.
 * User: dmcennis
 * Date: 9/23/13
 * Time: 11:10 AM
 * To change this template use File | Settings | File Templates.
 */
public class Chroma extends FeatureExtractor {

    public Chroma(){
        String name = "Chroma";
        String description = "Basic chroma mfcc derived from constant q function output";
        boolean is_sequential = true;
        int dimensions = 12;
        String[] attributes = new String[]{};
        definition = new FeatureDefinition( name,
                description,
                is_sequential,
                dimensions,
                attributes );

        dependencies = new String[]{"ConstantQ"};

        offsets = new int[]{0};

    }

    public String name() {
        return null;
    }

    /**
     * The prototype function that classes extending this class will override in
     * order to extract their mfcc from a window of audio.
     *
     * @param samples              The samples to extract the mfcc from.
     * @param sampling_rate        The sampling rate that the samples are encoded with.
     * @param other_feature_values The values of other features that are needed to calculate this
     *                             value. The order and offsets of these features must be the
     *                             same as those returned by this class's getDependencies and
     *                             getDependencyOffsets methods respectively. The first indice
     *                             indicates the mfcc/window and the second indicates the
     *                             value.
     * @return The extracted mfcc value(s).
     * @throws Exception Throws an informative exception if the mfcc cannot be
     *                   calculated.
     */
    @Override
    public double[] extractFeature(double[] samples, double sampling_rate, double[][] other_feature_values) throws Exception {
        double[] ret = new double[12];
        Arrays.fill(ret,0.0);
        for(int i=0;i<other_feature_values[0].length;++i){
           ret[i%12] += other_feature_values[0][i];
        }
        return ret;
    }



    /**
     * Gives features a reference to the container frame to notify it that
     * features have changed state and need to be redrawn.
     *
     * @param parent container frame which holds the model for displaying features
     *               in the mfcc display panel.
     */
    @Override
    public void setParent(DataModel parent) {
        super.setParent(parent);    //To change body of overridden methods use File | Settings | File Templates.
    }

    /**
     * Create an identical copy of this mfcc. This permits FeaturesProcessInvoker
     * to use the prototype pattern to create new composite features using
     * metafeatures.
     */
    @Override
    public Object clone() {
        return new Chroma();  //To change body of implemented methods use File | Settings | File Templates.
    }
}

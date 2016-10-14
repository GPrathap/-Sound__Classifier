/*
 * @(#)StrongestFrequencyViaFFTMax.java	1.0	April 7, 2005.
 *
 * McGill Univarsity
 */

package org.sound.classifier.features;

import jAudioFeatureExtractor.ACE.DataTypes.FeatureDefinition;


/**
 * A mfcc extractor that finds the strongest frequency component of a
 * signal, in Hz.
 *
 * <p>This is found by finding the highest bin in the power spectrum.
 *
 * <p>No extracted mfcc values are stored in objects of this class.
 *
 * @author Cory McKay
 */
public class StrongestFrequencyViaFFTMax
	extends FeatureExtractor
{
	/* CONSTRUCTOR **************************************************************/

	public String name() {
		return null;
	}
	/**
	 * Basic constructor that sets the definition and dependencies (and their
	 * offsets) of this mfcc.
	 */
	public StrongestFrequencyViaFFTMax()
	{
		String name = "Strongest Frequency Via FFT Maximum";
		String description = "The strongest frequency component of a signal, in Hz, " +
		                     "found via finding the FFT bin with the highest power.";
		boolean is_sequential = true;
		int dimensions = 1;
		definition = new FeatureDefinition( name,
		                                    description,
		                                    is_sequential,
		                                    dimensions );

		dependencies = new String[2];
		dependencies[0] = "Power Spectrum";
		dependencies[1] = "FFT Bin Frequency Labels";
		
		offsets = new int[2];
		offsets[0] = 0;
		offsets[1] = 0;
	}


	/* PUBLIC METHODS **********************************************************/

	
	/**
	 * Extracts this mfcc from the given samples at the given sampling
	 * rate and given the other mfcc values.
	 *
	 * <p>In the case of this mfcc, the sampling_rate parameter is ignored.
	 *
	 * @param samples				The samples to extract the mfcc from.
	 * @param sampling_rate			The sampling rate that the samples are
	 *								encoded with.
	 * @param other_feature_values	The values of other features that are
	 *								needed to calculate this value. The
	 *								order and offsets of these features
	 *								must be the same as those returned by
	 *								this class's getDependencies and
	 *								getDependencyOffsets methods respectively.
	 *								The first indice indicates the mfcc/window
	 *								and the second indicates the value.
	 * @return						The extracted mfcc value(s).
	 * @throws Exception			Throws an informative exception if
	 *								the mfcc cannot be calculated.
	 */
	public double[] extractFeature( double[] samples,
	                                double sampling_rate,
	                                double[][] other_feature_values )
		throws Exception
	{
		double[] power_spectrum = other_feature_values[0];
		double[] labels = other_feature_values[1];
		int highest_bin = jAudioFeatureExtractor.GeneralTools.Statistics. getIndexOfLargest(power_spectrum);
		double[] result = new double[1];
		result[0] = labels[highest_bin];
		return result;
	}
	
	/**
	 * Create an identical copy of this mfcc. This permits FeaturesProcessInvoker
	 * to use the prototype pattern to create new composite features using
	 * metafeatures.
	 */
	public Object clone(){
		return new StrongestFrequencyViaFFTMax();
	}
}
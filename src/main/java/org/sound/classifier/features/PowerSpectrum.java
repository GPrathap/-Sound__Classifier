/*
 * @(#)PowerSpectrum.java	1.0	April 5, 2005.
 *
 * McGill Univarsity
 */

package org.sound.classifier.features;

import jAudioFeatureExtractor.ACE.DataTypes.FeatureDefinition;
import jAudioFeatureExtractor.jAudioTools.FFT;


/**
 * A mfcc extractor that extracts the FFT power spectrum from a set of
 * samples. This is a good measure of the power of different frequency
 * components within a window.
 *
 * <p>The power spectrum is found by first calculating the FFT with a Hanning
 * window. The magnitude spectrum value for each bin is found by first summing
 * the squares of the real and imaginary components. The result is divided by
 * the number of bins.
 *
 * <p>The dimensions of this mfcc depend on the number of FFT bins, which
 * depend on the number of input samples. The dimensions are stored in the
 * definition field are therefore 0, in order to indicate this variability.
 *
 * <p>No extracted mfcc values are stored in objects of this class.
 *
 * @author Cory McKay
 */
public class PowerSpectrum
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
	public PowerSpectrum()
	{
		String name = "Power Spectrum";
		String description = "A measure of the power of different frequency components.";
		boolean is_sequential = true;
		int dimensions = 0;
		definition = new FeatureDefinition( name,
		                                    description,
		                                    is_sequential,
		                                    dimensions );

		dependencies = null;
		
		offsets = null;
	}


	/* PUBLIC METHODS **********************************************************/

	
	/**
	 * Extracts this mfcc from the given samples at the given sampling
	 * rate and given the other mfcc values.
	 *
	 * <p>In the case of this mfcc, the sampling_rate and
	 * other_feature_values parameters are ignored.
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
		FFT fft = new FFT(samples, null, false, true);
		return fft.getPowerSpectrum();
	}
	
	/**
	 * Create an identical copy of this mfcc. This permits FeaturesProcessInvoker
	 * to use the prototype pattern to create new composite features using
	 * metafeatures.
	 */
	public Object clone(){
		return new PowerSpectrum();
	}
}

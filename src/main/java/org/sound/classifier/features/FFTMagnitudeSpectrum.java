/*
 * @(#)MagnitudeSpectrum.java	1.0	April 5, 2005.
 *
 * McGill Univarsity
 */

package org.sound.classifier.features;

import jAudioFeatureExtractor.ACE.DataTypes.FeatureDefinition;
import jAudioFeatureExtractor.jAudioTools.*;
import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.transform.DftNormalization;
import org.apache.commons.math3.transform.FastFourierTransformer;
import org.apache.commons.math3.transform.TransformType;
import org.sound.classifier.constants.Constants;

import java.util.Arrays;


/**
 * A mfcc extractor that extracts the FFT magnitude spectrum from a set of
 * samples. This is a good measure of the magnitude of different frequency
 * components within a window.
 *
 * <p>The magnitude spectrum is found by first calculating the FFT with a Hanning
 * window. The magnitude spectrum value for each bin is found by first summing
 * the squares of the real and imaginary components. The square root of this is
 * then found and the result is divided by the number of bins.
 *
 * <p>The dimensions of this mfcc depend on the number of FFT bins, which
 * depend on the number of input samples. The dimensions are stored in the
 * definition field are therefore 0, in order to indicate this variability.
 *
 * <p>No extracted mfcc values are stored in objects of this class.
 *
 * @author Cory McKay
 */
public class FFTMagnitudeSpectrum extends FeatureExtractor {
	/* CONSTRUCTOR **************************************************************/

	public String name() {
		return null;
	}

	/**
	 * Basic constructor that sets the definition and dependencies (and their
	 * offsets) of this mfcc.
	 */
	public FFTMagnitudeSpectrum()
	{
		String name = "Magnitude Spectrum";
		String description = "A measure of the strength of different frequency components.";
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
		int windowSize = 128;
		int count = Constants.WINDOW_SIZE/windowSize;
		int destPos = 0;
		int numberOfBins = windowSize/2;
		double[] tempResult = new double[Constants.WINDOW_SIZE];
		double[] result = new double[windowSize/2];
		while(count >0){
			jAudioFeatureExtractor.jAudioTools.FFT fft = new jAudioFeatureExtractor.jAudioTools.FFT(copyOfRange(samples, destPos, destPos+windowSize), null, false, true);
			double [] holder =fft.getMagnitudeSpectrum();
			System.arraycopy(holder, 0, tempResult, destPos, holder.length );
			destPos = destPos +windowSize;
			count--;
		}
		count = Constants.WINDOW_SIZE/windowSize;
		for (int h=0; h<count; h++){
			int bin =h;
			double sum =0;
			for(int l=0; l< count; l++){
				sum+=tempResult[bin];
				bin+=numberOfBins;
			}
			result[h] = sum/numberOfBins;
		}
		return result;
	}

	public static double[] copyOfRange(double[] myarray, int from, int to) {
		double[] newarray = new double[to - from];
		for (int i = 0 ; i < to - from ; i++) newarray[i] = myarray[i + from];
		return newarray;
	}

	private double[] applyFFT(double input[]) {

		//fft works on data length = some power of two
		int fftLength;
		int length = input.length;  //initialized with input's length
		int power = 0;
		while (true) {
			int powOfTwo = (int) Math.pow(2, power);  //maximum no. of values to be applied fft on

			if (powOfTwo == length) {
				fftLength = powOfTwo;
				break;
			}
			if (powOfTwo > length) {
				fftLength = (int) Math.pow(2, (power - 1));
				break;
			}
			power++;
		}
		double[] tempInput = Arrays.copyOf(input, fftLength);
		FastFourierTransformer fft = new FastFourierTransformer(DftNormalization.STANDARD);
		//apply fft on input
		Complex[] complexTransInput = fft.transform(tempInput, TransformType.FORWARD);

		for (int i = 0; i < complexTransInput.length; i++) {
			double real = (complexTransInput[i].getReal());
			double img = (complexTransInput[i].getImaginary());

			tempInput[i] = Math.sqrt((real * real) + (img * img));
		}
		return tempInput;
	}

	/**
	 * Create an identical copy of this mfcc. This permits FeaturesProcessInvoker
	 * to use the prototype pattern to create new composite features using
	 * metafeatures.
	 */
	public Object clone(){
		return new FFTMagnitudeSpectrum();
	}
}

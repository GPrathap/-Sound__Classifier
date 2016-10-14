//
//  Log ConstantQ.java
//  jAudio
//
//  Created by Daniel McEnnis on August 18, 2010.
//  Published under the LGPL license.  See most recent LGPL license on www.fsf.org
//  a copy of this license.
//

package org.sound.classifier.features;

import jAudioFeatureExtractor.ACE.DataTypes.FeatureDefinition;

/**
 * Log Constant Q
 *
 * Performs the log linear transform of the bins of the constant q transform to produce a representation whose linear
 * content better represents how the human ear hears differences in amplitude.
 *
 * @author Daniel McEnnis
 */
public class LogConstantQ extends FeatureExtractor
{


	public String name() {
		return null;
	}
	/* CONSTRUCTOR **************************************************************/
	
	
	/**
	 * Basic constructor that sets the definition and dependencies (and their
	 * offsets) of this mfcc.
	 */
	public LogConstantQ()
	{
		String name = "Log of ConstantQ";
		String description = "logarithm of each bin of exponentially-spaced frequency bins.";
		boolean is_sequential = true;
		int dimensions = 0;
//		String[] attributes = new String[]{"Percent of a semitone per bin"};
		definition = new FeatureDefinition( name,
		                                    description,
		                                    is_sequential,
		                                    dimensions);

		dependencies = new String[]{"ConstantQ"};
		
		offsets = new int[]{0};
		
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
		double[] ret = new double[other_feature_values[0].length];
		for(int i=0;i<ret.length;++i)
		{
			if(other_feature_values[0][i] <= 0.0){
				ret[i] = -50.0;
			}else{
				ret[i] = Math.log(other_feature_values[0][i]);
				if(ret[i] < -50.0){
					ret[i]=-50.0;
				}
			}
		}
		return ret;
	}
	
	/**
	 * Create an identical copy of this mfcc. This permits FeaturesProcessInvoker
	 * to use the prototype pattern to create new composite features using
	 * metafeatures.
	 */
	public Object clone(){
		return new LogConstantQ();
	}

}

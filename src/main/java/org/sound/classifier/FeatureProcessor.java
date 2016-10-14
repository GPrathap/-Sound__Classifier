/*
 * @(#)FeatureProcessor.java	1.01	April 9, 2005.
 *
 * McGill Univarsity
 */

package org.sound.classifier;

import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.AudioProcessor;
import be.tarsos.dsp.SilenceDetector;
import be.tarsos.dsp.io.TarsosDSPAudioFormat;
import org.apache.commons.collections.Buffer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sound.classifier.features.FeatureExtractor;
import org.sound.classifier.features.FeatureProperties;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.io.DataOutputStream;
import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;


public class FeatureProcessor {
	/* FIELDS ***************************************************************** */

	private static final Log log = LogFactory.getLog(FeatureProcessor.class);
	// The window size used for dividing up the recordings to classify.
	private int window_size;

	// The dependencies of the features in the feature_extractors field.
	// The first indice corresponds to the feature_extractors indice
	// and the second identifies the number of the dependent mfcc.
	// The entry identifies the indice of the mfcc in feature_extractors
	// that corresponds to a dependant mfcc. The first dimension will be
	// null if there are no dependent features.
	private int[][] feature_extractor_dependencies;

	// The number of samples that windows are offset by. A value of zero
	// means that there is no window overlap.
	private int window_overlap_offset;

	// The sampling rate that all recordings are to be converted to before
	// mfcc extraction.
	private double sampling_rate;

	// Whether or not to normalise recordings before mfcc extraction.
	private boolean normalise;

	// The features that are to be extracted.
	private FeatureExtractor[]  feature_extractors;


	// The longest number of windows of previous features that each mfcc must
	// have before it can be extracted. The indice corresponds to that of
	// feature_extractors.
	private int[] max_feature_offsets;

	// Which features are to be saved after processing. Entries correspond to
	// the
	// feature_extractors field.
	private boolean[] features_to_save;

	// Whether or not to save features individually for each window
	private boolean save_features_for_each_window;

	// Whetehr or not to save the average and standard deviation of each
	// mfcc accross all windows.
	private boolean save_overall_recording_features;

	// Used to write to the feature_vector_file file to save mfcc values to.
	private DataOutputStream values_writer;

	// Used to write to the feature_key_file file to save mfcc definitions
	// to.
	private DataOutputStream definitions_writer;

	// Indicates whether the mfcc definitions have been written by the
	// definitions_writer yet.
	private boolean definitions_written;

	// Indicates what the type of the output format is
	private int outputType;

	// Since Overall features are not recorded until after the header is
	// written, the main body needs to know if it needs to write the header or
	// not.
	private boolean isARFFOverallHeaderWritten = false;


	double[][] results;


	/* CONSTRUCTOR ************************************************************ */

	/**
	 * Validates and stores the configuration to use for extracting features
	 * from audio recordings. Prepares the feature_vector_file and
	 * feature_key_file XML files for saving.
	 *
	 * @param window_size
	 *            The size of the windows that the audio recordings are to be
	 *            broken into.
	 * @param window_overlap
	 *            The fraction of overlap between adjacent windows. Must be
	 *            between 0.0 and less than 1.0, with a value of 0.0 meaning no
	 *            overlap.
	 * @param sampling_rate
	 *            The sampling rate that all recordings are to be converted to
	 *            before mfcc extraction
	 * @param normalise
	 *            Whether or not to normalise recordings before mfcc
	 *            extraction.
	 * @param all_feature_extractors
	 *            All features that can be extracted.
	 * @throws Exception
	 *             Throws an informative exception if the input parameters are
	 *             invalid.
	 */
	public FeatureProcessor(int window_size, double window_overlap, double sampling_rate, boolean normalise, FeatureExtractor[] all_feature_extractors) throws Exception {

		boolean one_selected = false;
		definitions_written = false;

		// Save parameters as fields
		this.window_size = window_size;
		this.sampling_rate = sampling_rate;
		this.normalise = normalise;

		// Calculate the window offset
		window_overlap_offset = (int) (window_overlap * (double) window_size);

		findAndOrderFeaturesToExtract(all_feature_extractors);

		this.feature_extractors = all_feature_extractors;
		results = new double[feature_extractors.length][];
	}

	/* PUBLIC METHODS ********************************************************* */

	/**
	 * Extract the features from the provided audio file. This includes
	 * pre-processing involving sample rate conversion, windowing and, possibly,
	 * normalisation. The mfcc values are automatically saved to the
	 * feature_vector_file XML file referred to by the values_writer field. The
	 * definitions of the features that are saved are also saved to the
	 * feature_key_file XML file referred to by the definitions_writer field.
	 *
	 */
	public boolean extractFeatures(AudioDispatcher dispatcher, PropertyList propertyList, final Buffer audioFeaturesBuffer) throws Exception {

		//TODO do some pre-processing here...
		final SilenceDetector silenceDetecor = new SilenceDetector();
		dispatcher.addAudioProcessor(silenceDetecor);
		dispatcher.addAudioProcessor(new AudioProcessor() {
			public void processingFinished() {

			}
			public boolean process(AudioEvent audioEvent) {
				try {
					double[] samples = convertFloatsToDoubles(audioEvent.getFloatBuffer());
					HashMap window_feature_values = getFeatures(samples);
					FeatureProperties featureProperties = new FeatureProperties();
                    featureProperties.setTimeStamp(audioEvent.getTimeStamp());
					featureProperties.setFeatureValues(window_feature_values);
					audioFeaturesBuffer.add(featureProperties);
				} catch (Exception e) {
					log.error("Unexpected error has occurred while processing current window...but continue.."+ e);
				} finally {
					return true;
				}
			}
		});
		dispatcher.run();
		return true;
	}

	/**
	 * Fills the feature_extractors, feature_extractor_dependencies,
	 * max_feature_offsets and features_to_save fields. This involves finding
	 * which features need to be extracted and in what order and finding the
	 * indices of dependencies and the maximum offsets for each mfcc.
	 * <p>
	 * Daniel McEnnis 05-07-05 added mfcc offset of dependancies to
	 * max_offset
	 *
	 * @param all_feature_extractors
	 *            All features that can be extracted.
	 */
	private void findAndOrderFeaturesToExtract(FeatureExtractor[] all_feature_extractors) {
		// Find the names of all features
		String[] all_feature_names = new String[all_feature_extractors.length];
		for (int feat = 0; feat < all_feature_extractors.length; feat++)
			all_feature_names[feat] = all_feature_extractors[feat].getFeatureDefinition().name;

		// Find dependencies of all features marked to be extracted.
		// Mark as null if features are not to be extracted. Note that will also
		// be null if there are no dependencies.
		String[][] dependencies = new String[all_feature_extractors.length][];
		for (int feat = 0; feat < all_feature_extractors.length; feat++) {
				dependencies[feat] = all_feature_extractors[feat].getDepenedencies();
		}

		// Add dependencies to dependencies and if any features are not marked
		// for
		// saving but are marked as a dependency of a mfcc that is marked to
		// be
		// saved. Also fill features_to_extract in order to know what features
		// to
		// extract(but not necessarily save).
		boolean done = false;
		boolean[] features_to_extract = new boolean[dependencies.length];
		for (int feat = 0; feat < features_to_extract.length; feat++) {
				features_to_extract[feat] = true;
		}
		while (!done) {
			done = true;
			for (int feat = 0; feat < dependencies.length; feat++)
				if (dependencies[feat] != null)
					for (int i = 0; i < dependencies[feat].length; i++) {
						String name = dependencies[feat][i];
						for (int j = 0; j < all_feature_names.length; j++) {
							if (name.equals(all_feature_names[j])) {
								if (!features_to_extract[j]) {
									features_to_extract[j] = true;
									dependencies[j] = all_feature_extractors[j]
											.getDepenedencies();
									if (dependencies[j] != null)
										done = false;
								}
								j = all_feature_names.length;
							}
						}
					}
		}

		// Find the correct order to extract features in by filling the
		// feature_extractors field
		int number_features_to_extract = 0;
		for (int i = 0; i < features_to_extract.length; i++)
			if (features_to_extract[i])
				number_features_to_extract++;
		feature_extractors = new FeatureExtractor[number_features_to_extract];
		features_to_save = new boolean[number_features_to_extract];
		for (int i = 0; i < features_to_save.length; i++)
			features_to_save[i] = false;
		boolean[] feature_added = new boolean[dependencies.length];
		for (int i = 0; i < feature_added.length; i++)
			feature_added[i] = false;
		int current_position = 0;
		done = false;
		while (!done) {
			done = true;

			// Add all features that have no remaining dependencies and remove
			// their dependencies from all unadded features
			for (int feat = 0; feat < dependencies.length; feat++) {
				if (features_to_extract[feat] && !feature_added[feat])
					if (dependencies[feat] == null) // add mfcc if it has no
					// dependencies
					{
						feature_added[feat] = true;
						feature_extractors[current_position] = all_feature_extractors[feat];
						current_position++;
						done = false;

						// Remove this dependency from all features that have
						// it as a dependency and are marked to be extracted
						for (int i = 0; i < dependencies.length; i++)
							if (features_to_extract[i]
									&& dependencies[i] != null) {
								int num_defs = dependencies[i].length;
								for (int j = 0; j < num_defs; j++) {
									if (dependencies[i][j]
											.equals(all_feature_names[feat])) {
										if (dependencies[i].length == 1) {
											dependencies[i] = null;
											j = num_defs;
										} else {
											String[] temp = new String[dependencies[i].length - 1];
											int m = 0;
											for (int k = 0; k < dependencies[i].length; k++) {
												if (k != j) {
													temp[m] = dependencies[i][k];
													m++;
												}
											}
											dependencies[i] = temp;
											j--;
											num_defs--;
										}
									}
								}
							}
					}
			}
		}
		// Find the indices of the mfcc extractor dependencies for each
		// mfcc
		// extractor
		feature_extractor_dependencies = new int[feature_extractors.length][];
		String[] feature_names = new String[feature_extractors.length];
		for (int feat = 0; feat < feature_names.length; feat++) {
			feature_names[feat] = feature_extractors[feat]
					.getFeatureDefinition().name;
		}
		String[][] feature_dependencies_str = new String[feature_extractors.length][];
		for (int feat = 0; feat < feature_dependencies_str.length; feat++)
			feature_dependencies_str[feat] = feature_extractors[feat]
					.getDepenedencies();
		for (int i = 0; i < feature_dependencies_str.length; i++)
			if (feature_dependencies_str[i] != null) {
				feature_extractor_dependencies[i] = new int[feature_dependencies_str[i].length];
				for (int j = 0; j < feature_dependencies_str[i].length; j++)
					for (int k = 0; k < feature_names.length; k++)
						if (feature_dependencies_str[i][j]
								.equals(feature_names[k]))
							feature_extractor_dependencies[i][j] = k;
			}

		// Find the maximum offset for each mfcc
		// Daniel McEnnis 5-07-05 added mfcc offset of dependancies to
		// max_offset
		max_feature_offsets = new int[feature_extractors.length];
		for (int i = 0; i < max_feature_offsets.length; i++) {
			if (feature_extractors[i].getDepenedencyOffsets() == null)
				max_feature_offsets[i] = 0;
			else {
				int[] these_offsets = feature_extractors[i]
						.getDepenedencyOffsets();
				max_feature_offsets[i] = Math
						.abs(these_offsets[0]
								+ max_feature_offsets[feature_extractor_dependencies[i][0]]);
				for (int k = 0; k < these_offsets.length; k++) {
					int val = Math.abs(these_offsets[k])
							+ max_feature_offsets[feature_extractor_dependencies[i][k]];
					if (val > max_feature_offsets[i]) {
						max_feature_offsets[i] = val;
					}
				}
			}
		}
	}

	/**
	 * Write the ending tags to the feature_vector_file XML file. Close the
	 * DataOutputStreams that were used to write it.
	 * <p>
	 * This method should be called when all features have been extracted.
	 *
	 * @throws Exception
	 *             Throws an exception if cannot write or close the output
	 *             streams.
	 */
	public void finalize() throws Exception {
		if (outputType == 0) {
			values_writer.writeBytes("</feature_vector_file>");
		}
		values_writer.close();
	}

	private static double[] convertFloatsToDoubles(float[] input) {
		if (input == null)
		{
			return null; // Or throw an exception - your choice
		}
		double[] output = new double[input.length];
		for (int i = 0; i < input.length; i++)
		{
			output[i] = input[i];
		}
		return output;
	}


	/**
     * @param window
	 *            The samples to extract features from. Sample values should
	 *            generally be between -1 and +1.
	 * @return The extracted mfcc values for this recording. The first indice
	 *         identifies the window, the second identifies the mfcc and the
	 *         third identifies the mfcc value. The third dimension will be
	 *         null if the given mfcc could not be extracted for the given
	 *         window.
	 * @throws Exception
	 *             Throws an exception if a problem occurs.
	 */
	private HashMap getFeatures(double[] window) throws Exception {
            HashMap perWindowFeatures = new HashMap();
			for (int feat = 0; feat < feature_extractors.length; feat++) {
					FeatureExtractor feature = feature_extractors[feat];
					double[][] other_feature_values = null;
					if (feature_extractor_dependencies[feat] != null) {
						other_feature_values = new double[feature_extractor_dependencies[feat].length][];
						for (int i = 0; i < feature_extractor_dependencies[feat].length; i++) {
							int feature_indice = feature_extractor_dependencies[feat][i];
							other_feature_values[i] = results[feature_indice];
						}
					}
					// Store the extracted mfcc values
					results[feat] = feature.extractFeature(window, sampling_rate, other_feature_values);
//                    perWindowFeatures[feat] = results[feat];
                    perWindowFeatures.put(feature.getFeatureDefinition().name,results[feat]);
			}
		return perWindowFeatures;
	}
}
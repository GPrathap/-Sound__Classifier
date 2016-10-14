package org.sound.classifier;;
import org.sound.classifier.features.*;

import java.util.LinkedList;


public class DataModel {
	/**
	 * list of which features are enabled by default
	 */
	public boolean[] defaults;

	/**
	 * list of all features available
	 */
    LinkedList<FeatureExtractor> extractors;

	public DataModel() {
         extractors = new LinkedList<FeatureExtractor>();
		 LinkedList<Boolean> def = new LinkedList<Boolean>();
		 extractors.add(new FFTMagnitudeSpectrum());
		 def.add(true);
		 extractors.add(new MagnitudeSpectrum());
		 def.add(true);
		 extractors.add(new MFCCFeatures());
		 def.add(true);


         /*extractors.add(new RMS());
         def.add(true);
         extractors.add(new MagnitudeSpectrum());
         def.add(true);
		 extractors.add(new AreaMoments());
		 def.add(true);
		 extractors.add(new BeatHistogram());
		 def.add(true);
		 extractors.add(new BeatHistogramLabels());
		 def.add(true);
		 extractors.add( new BeatSum());
		 def.add(true);
		 extractors.add(new Compactness());
		 def.add(true);*/
		 /*extractors.add(new FFTBinFrequencies());
		 def.add(false);*/
		/* extractors.add(new FractionOfLowEnergyWindows());
		 def.add(true);
		 extractors.add(new LPC());
		 def.add(false);

		 extractors.add(new Moments());
		 def.add(true);*/
		 /*extractors.add(new PeakFinder());
		 def.add(false);*/
		 /*extractors.add(new PowerSpectrum());
		 def.add(false);
		 extractors.add(new RelativeDifferenceFunction());
		 def.add(false);
		 extractors.add(new SpectralCentroid());
		 def.add(true);
		 extractors.add(new SpectralFlux());
		 def.add(true);
		 extractors.add(new SpectralRolloffPoint());
		 def.add(true);
		 extractors.add(new SpectralVariability());
		 def.add(false);
		 extractors.add(new StrengthOfStrongestBeat());
		 def.add(false);
		 extractors.add(new StrongestBeat());
		 def.add(false);
         extractors.add(new StrongestFrequencyViaFFTMax());
         def.add(false);
         extractors.add(new StrongestFrequencyViaSpectralCentroid());
         def.add(false);
         extractors.add(new ZeroCrossings());
         def.add(true);*/
         /*extractors.add(new HarmonicSpectralCentroid());
		 def.add(false);*/
		 /*extractors.add(new HarmonicSpectralFlux());
		 def.add(false);*/
		 /*extractors.add(new HarmonicSpectralSmoothness());
		 def.add(false);*/
		/* extractors.add(new StrongestFrequencyVariability());
		 def.add(false);
         extractors.add(new StrongestFrequencyViaZeroCrossings());
		 def.add(false);*/
	}

	public FeatureExtractor[] featuresList(){
        return extractors.toArray(new FeatureExtractor[extractors.size()]);
    }

}

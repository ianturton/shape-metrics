package edu.psu.geovista.ian.measures;


import org.opengis.feature.Feature;

public interface ShapeClassifier{
	/**
	 * calculate a compactness measure for the feature
	 * 0 is a circle -> 1 is most uncompact 
	 * @param feature
	 * @return the compactness score
	 */
	public double score(Feature feature);
	public String getName();
}

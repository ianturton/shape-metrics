package org.ianturton.shapes.measures;

import org.opengis.feature.Feature;

import com.vividsolutions.jts.geom.Geometry;

/**
 * calculate the Ritter shape compactness score for the feature
 * ratio of area / perimeter.
 * @author ijt1
 *
 */
public class Ritter implements ShapeClassifier {

	
	
	public double score(Feature f) {
		double score = 0;
		Geometry geom = (Geometry) f.getDefaultGeometryProperty().getValue();
        if (geom != null && geom.isValid()) {
        	score = calcScore(geom);
        }
        return score;
	}

	protected double calcScore(Geometry geom) {
		double score;
		double area = geom.getArea();
		double perimeter = geom.getLength();
		final double adjusment = Math.sqrt(1.0/(Math.PI*4.0));//.282;
		score = Math.sqrt(area)/(adjusment*perimeter);
		return score;
	}

	public String getName() {
		// TODO Auto-generated method stub
		return "Ritter";
	}
}

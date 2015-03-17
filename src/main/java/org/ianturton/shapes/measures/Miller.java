package org.ianturton.shapes.measures;

import org.opengis.feature.Feature;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Polygon;

/**
 * calculate the Miller shape compactness score for the feature
 * this is a ratio of area to perimeter.
 * @author ijt1
 *
 */
public class Miller implements ShapeClassifier {

	
	public double score(Feature f) {
		double score = 0;
		Geometry geom = (Geometry) f.getDefaultGeometryProperty().getValue();
        if (geom != null && geom.isValid()) {
        	score = calcScore(geom);
        }
        return score;
	}

	
	public String getName() {
		// TODO Auto-generated method stub
		return "Miller";
	}

	double calcScore(Geometry geom) {
		double area = geom.getArea();
    	double perimeter = geom.getLength();
    	//System.out.println("area = "+area+" perimeter "+perimeter);
    	final double adjusment = Math.sqrt(1.0/(Math.PI*4.0));//.282;
    	//System.out.println(adjusment);
		final double d = adjusment*perimeter;
		double score = area/(d*d);
		return score;
	}
}

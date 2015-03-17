package org.ianturton.shapes.measures;

import org.opengis.feature.Feature;

import com.vividsolutions.jts.geom.Geometry;

public class BlairAndBiss implements ShapeClassifier {
	private boolean DEBUG = false;
	

	public String getName() {
		// TODO Auto-generated method stub
		return "Blair and Biss";
	}

	public double score(Feature feature) {
		double score = 0;
		Geometry geom = (Geometry) feature.getDefaultGeometryProperty()
				.getValue();
		if (geom != null && geom.isValid()) {
			score = calcScore(geom);
		}
		return score;
	}

	double calcScore(Geometry geom) {
		// triangulate the polygon

		double I = MomentOfInertia.calculateMomentOfInertia(geom);
		double trueArea = geom.getArea();
		// MacEachren, 1985 gives this as A/2*PI*I
		// while Massam and Goodchild, 1971 give this:
		final double score = Math.sqrt((trueArea * trueArea) / (2.0 * Math.PI * I));
		return score;
	}

	

	public boolean isDEBUG() {
		return DEBUG;
	}

	public void setDEBUG(boolean dEBUG) {
		DEBUG = dEBUG;
	}

}

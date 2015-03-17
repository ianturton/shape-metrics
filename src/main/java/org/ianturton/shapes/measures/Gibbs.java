package org.ianturton.shapes.measures;

import org.ianturton.shapes.utils.CircumscribedCircle;
import org.opengis.feature.Feature;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;

public class Gibbs implements ShapeClassifier {
	/**
	 * compactness measure based on ratio circumscribing circle to polygon area.
	 */
	
	public String getName() {

		return "Gibbs";
	}

	
	public double score(Feature feature) {
		double score = 0;
		Geometry geom = (Geometry) feature.getDefaultGeometryProperty()
				.getValue();
		if (geom != null && geom.isValid()) {
			score = calcScore(geom);
			// System.err.println("axis "+axis+" cArea "+cArea+" area "+area
			// +" -> "+score);

		}
		return score;
	}

	double calcScore(Geometry geom) {
		double score;
		final double area = geom.getArea();

		// longest axis
		double axis = 1.0;
		double cArea = 1.0;
		if (geom instanceof Polygon) {
			CircumscribedCircle cc = new CircumscribedCircle((Polygon) geom);
			axis = cc.getRadius() * 2.0;
			cArea = cc.getArea();
		} else if (geom instanceof MultiPolygon) {
			MultiPolygon mp = (MultiPolygon) geom;
			axis = 0.0;
			cArea = 0.0;
			for (int i = 0; i < mp.getNumGeometries(); i++) {
				CircumscribedCircle cc = new CircumscribedCircle((Polygon) mp
						.getGeometryN(i));
				axis += cc.getRadius() * 2.0;
				cArea += cc.getArea();
			}
		}

		score = area / cArea;
		return score;
	}

}

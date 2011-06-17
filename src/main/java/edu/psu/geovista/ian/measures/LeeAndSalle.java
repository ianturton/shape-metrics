package edu.psu.geovista.ian.measures;

import org.opengis.feature.Feature;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.util.GeometricShapeFactory;

public class LeeAndSalle implements ShapeClassifier {
	/**
	 * calculate the Lee-Salle index of the geometry this is the ratio of the
	 * area of intersection of circle and shape/area of union of circle and
	 * shape
	 */
	static final private GeometricShapeFactory fac = new GeometricShapeFactory();
	
	public String getName() {
		// TODO Auto-generated method stub
		return "Lee and Salle";
	}

	
	public double score(Feature feature) {
		double score = 0;
		Geometry geom = (Geometry) feature.getDefaultGeometryProperty().getValue();
		if (geom != null && geom.isValid()) {
			score = calcScore(geom);
		}
		return score;
	}

	double calcScore(Geometry geom) {
		double score;
		double polyArea = geom.getArea();
		double radius = Math.sqrt(polyArea/Math.PI);
		fac.setSize(radius*2.0);
		fac.setNumPoints(360);
		fac.setCentre(geom.getCentroid().getCoordinate());
		Polygon circle = fac.createCircle();
		Geometry union = circle.union(geom);
		Geometry intersection = circle.intersection(geom);
		/*System.out.println("union "+union.getArea());
		System.out.println("intersection "+intersection.getArea());*/
		score = intersection.getArea()/union.getArea();
		return score;
	}

}

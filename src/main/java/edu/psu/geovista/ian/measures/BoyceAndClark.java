package edu.psu.geovista.ian.measures;

import org.opengis.feature.Feature;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;

public class BoyceAndClark implements ShapeClassifier {
	private int numRadials = 100;
	GeometryFactory gf = new GeometryFactory();
	/**
	 * calculate the Boyce and Clark compactness measure a summation of the
	 * length of the radials from a point in the polygon.
	 * 
	 */
	
	public String getName() {
		// TODO Auto-generated method stub
		return "Boyce and Clark";
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
		double score = 0;
		Point centroid = geom.getCentroid();
		Point center = centroid;
		double longLine = 10000000;
		
		final double cX = center.getX();
		final double cY = center.getY();
		double[] lengths = new double[numRadials];
		double sum = 0;
		for(int i=0;i<numRadials;i++) {
			//calc length of radial from centroid to Poly
			double angle = i*(2*Math.PI/numRadials);
			//System.out.println(angle);
			//first a point a long way away 
			double x = cX+Math.cos(angle)*longLine*2.0;
			double y = cY+Math.sin(angle)*longLine*2.0;
			Coordinate c = new Coordinate(x,y);
			Coordinate[] coordinates = new Coordinate[] {center.getCoordinate(),c};
			LineString l = gf.createLineString(coordinates);
			
			//intersection of line and geom
			Geometry g = geom.intersection(l);
			double length=g.getLength();
			
			
			//System.out.println(l+" "+g+" -> "+length);
			lengths[i]=length;
			sum+=length;
		}
		final double n = 100.0/numRadials;
		for(int i=0;i<lengths.length;i++) {
			final double ri = (lengths[i]/sum)*100.0;
			//System.out.println(i+":"+lengths[i]+" "+ri+" score "+score);
			score += Math.abs(ri - n);
		}
		score = score/200.0;
		return 1-score;
	}

	public int getNumRadials() {
		return numRadials;
	}

	public void setNumRadials(int numRadials) {
		this.numRadials = numRadials;
	}

}

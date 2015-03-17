package org.ianturton.shapes.utils;

import org.ianturton.shapes.utils.Circle;

import com.vividsolutions.jts.algorithm.Angle;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.LineSegment;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

import junit.framework.TestCase;

public class AngleTest extends TestCase {
	
	public void testAngle() {
		Circle c = new Circle(1.0,1.0, 1.0);
		Polygon poly = c.toPolygon(360);
		Point p = c.getCentre();
		
		Coordinate start = new Coordinate(1.0,2.0);
		Coordinate[] coords = poly.getCoordinates();
		System.out.println(p.getCoordinate()+","+start);
		for(Coordinate co:coords) {
			double angleBetween = Angle.angleBetweenOriented(start,p.getCoordinate() , co);
			double angleInterior = Angle.interiorAngle(start, p.getCoordinate(), co);
			double norm = Angle.normalizePositive(angleBetween);
			System.out.println(co+" "+Angle.toDegrees(angleBetween)+" "+Angle.toDegrees(angleInterior)+" "+Angle.toDegrees(norm));
		}
		
	}
	public void testLines() {
		LineSegment l1 = new LineSegment(90.0,370.0,130.0,410.0);
		LineSegment l2 = new LineSegment(90.0,370.0,130.0,410.0);
		System.out.println(l1.equals(l2));
		System.out.println(l1.hashCode()+" "+l2.hashCode()+" "+(l1.hashCode()==l2.hashCode()));
	}
}

package edu.psu.geovista.ian.utils;

import com.vividsolutions.jts.algorithm.MinimumDiameter;
import com.vividsolutions.jts.geom.Geometry;

public class InscribedCircle {
	/**
	 * calculate the inscribed circle of a polygon
	 */
	double radius = Double.NaN;
	double area = Double.NaN;
	double perimeter = Double.NaN;
	public InscribedCircle(Geometry geom) {
		MinimumDiameter md = new MinimumDiameter(geom);
		final double axis = md.getLength();
		setRadius(axis/2.0);
	}

	public double getRadius() {
		return radius;
	}

	public void setRadius(double radius) {
		this.radius = radius;
		setArea();
		setPerimeter();
	}

	public double getArea() {
		return area;
	}

	private void setArea() {
		area = Math.PI*radius*radius;
	}

	public double getPerimeter() {
		return perimeter;
	}

	private void setPerimeter() {
		this.perimeter = 2.0 * Math.PI * radius ;
	}
}

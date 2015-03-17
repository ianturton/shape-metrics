package org.ianturton.shapes.utils;

import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.util.GeometricShapeFactory;

public class Square{
	GeometryFactory gf = new GeometryFactory();
	static final private GeometricShapeFactory fac = new GeometricShapeFactory();
	private Polygon poly;
	public Polygon getPoly() {
		return poly;
	}
	
	public Square() {
		this(1.0);
	}
	public Square(double len) {
		fac.setHeight(len);
		fac.setWidth(len);
		fac.setNumPoints(4);
		poly=fac.createRectangle();
	}
}

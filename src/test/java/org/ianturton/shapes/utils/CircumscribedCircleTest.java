package org.ianturton.shapes.utils;

import org.ianturton.shapes.utils.CircumscribedCircle;

import junit.framework.TestCase;

import com.vividsolutions.jts.algorithm.MinimumBoundingCircle;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.util.GeometricShapeFactory;

public class CircumscribedCircleTest extends TestCase {
	static final private GeometricShapeFactory fac = new GeometricShapeFactory();
	public void testCircumscribedCircle() {
		//System.out.println(JTSVersion.CURRENT_VERSION);
		fac.setHeight(1.0);
		fac.setWidth(1.0);
		fac.setNumPoints(20);
		Polygon sq = fac.createRectangle();
		
		System.out.println(sq.isValid()+" "+sq);
		MinimumBoundingCircle mbc = new MinimumBoundingCircle(sq);
		System.out.println(mbc.getRadius());
		System.out.println(mbc.getCentre());
		CircumscribedCircle cc = new CircumscribedCircle(sq);
		System.out.println(cc.radius);
		cc.welzl(sq);
		System.out.println(cc.radius);
		
	}

}

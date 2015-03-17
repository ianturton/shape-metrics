package org.ianturton.shapes.measures;

import org.ianturton.shapes.measures.LeeAndSalle;
import org.ianturton.shapes.utils.Circle;
import org.ianturton.shapes.utils.Square;

import com.vividsolutions.jts.geom.Polygon;

import junit.framework.TestCase;

public class LeeAndSalleTest extends TestCase {

	public void testCalcScore() {
		Square sq = new Square();
		Polygon geom = sq.getPoly();
		LeeAndSalle ls = new LeeAndSalle();
		double score = ls.calcScore(geom);
		System.out.println("square = "+score);
		Circle c = new Circle(1.0,1.0, 1.0);
		score = ls.calcScore(c.toPolygon(720));
		System.out.println("Circle = "+score);
		assertEquals("Circle score is wrong",1.0, score, 1.0e-4);
	}

}

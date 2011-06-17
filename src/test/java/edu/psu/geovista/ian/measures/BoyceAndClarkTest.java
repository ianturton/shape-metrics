package edu.psu.geovista.ian.measures;

import com.vividsolutions.jts.geom.Polygon;

import edu.psu.geovista.ian.utils.Circle;
import edu.psu.geovista.ian.utils.Square;
import junit.framework.TestCase;

public class BoyceAndClarkTest extends TestCase {

	public void testScore() {
		Square sq = new Square();
		Polygon geom = sq.getPoly();
		BoyceAndClark bc = new BoyceAndClark();
		double score = bc.calcScore(geom);
		System.out.println("square = "+score);
		Circle c = new Circle(1.0,1.0, 1.0);
		score = bc.calcScore(c.toPolygon(720));
		System.out.println("Circle = "+score);
		assertEquals("Circle score is wrong",1.0, score, 1.0e-4);
	}

}

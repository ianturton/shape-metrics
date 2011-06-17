package edu.psu.geovista.ian.utils;

import com.vividsolutions.jts.geom.Polygon;

import junit.framework.TestCase;

public class InscribedCircleTest extends TestCase {

	public void testInscribedCircle() {
		Polygon sq = new Square().getPoly();
		InscribedCircle ic = new InscribedCircle(sq);
		System.out.println(ic.getRadius());
	}

}

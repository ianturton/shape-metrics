package org.ianturton.shapes.measures;

import java.util.List;

import mxb.jts.triangulate.EarClipper;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.Triangle;
import com.vividsolutions.jts.geom.util.PolygonExtracter;
import com.vividsolutions.jts.simplify.TopologyPreservingSimplifier;

public class MomentOfInertia {
	static boolean DEBUG=false;
	static public double calculateMomentOfInertia(Geometry geom) {
		List<Polygon> list = PolygonExtracter.getPolygons(geom);
		final int size = list.size();
		double[] polyI = new double[size];
		double[] polyArea = new double[size];
		Coordinate[] polyCog = new Coordinate[size];
		for (int i = 0; i < size; i++) {
			Polygon p = list.get(i);
			if (DEBUG)
				System.out.println(p);
			double d = p.getCentroid().getCoordinate().distance(
					p.getCoordinate()) / 1000;
			//d = Math.max(d,0.00001);
			if (DEBUG)
				System.out.println("simplefy distance is " + d+"\n"+p.isValid()+" "+p);
			
			Polygon p2 = (Polygon) TopologyPreservingSimplifier.simplify(p, d);
			if (DEBUG)
				System.out.println(p2.isValid()+" "+p2);
			if(p2.isValid()) {
				/*
				 * in principal p2 should be valid if p1 is 
				 * but I'm seeing cases where that doesn't happen
				 */
				p=p2;
			}
			EarClipper ec = new EarClipper(p);

			Geometry gm = ec.getResult(false);
			final int N = gm.getNumGeometries();
			if (gm.isEmpty() || N == 0) {
				continue;
			}
			if (DEBUG)
				System.out.println("Zusne - got " + N + " triangles");
			double[] areas = new double[N];
			double[] m = new double[N];
			Coordinate[] centers = new Coordinate[N];
			double[] l = new double[3];
			for (int i1 = 0; i1 < N; i1++) {
				Geometry ipoly = gm.getGeometryN(i1);
				if (DEBUG)
					System.out.println("processing " + i1 + ": " + ipoly);
				Coordinate[] coords = ipoly.getCoordinates();

				l[0] = coords[0].distance(coords[1]);
				l[1] = coords[1].distance(coords[2]);
				l[2] = coords[2].distance(coords[0]);

				final double x = (coords[0].x + coords[1].x + coords[2].x) / 3.0;
				final double y = (coords[0].y + coords[1].y + coords[2].y) / 3.0;

				centers[i1] = new Coordinate(x, y);

				areas[i1] = Triangle.area(coords[0], coords[1], coords[2]);
				if (DEBUG)
					System.out.println("-> " + centers[i1] + areas[i1]);
				m[i1] = areas[i1] * (l[0] * l[0] + l[1] * l[1] + l[2] * l[2])
						/ 36.0;

			}

			final double trueArea = gm.getArea();
			double areaSum = 0.0;
			double x = 0, y = 0;
			for (int i1 = 0; i1 < N; i1++) {
				areaSum += areas[i1];
				x += centers[i1].x * areas[i1];
				y += centers[i1].y * areas[i1];
			}
			if (DEBUG)
				System.out.println(x + "," + y + " areas " + trueArea + " "
						+ areaSum);
			x = x / trueArea;
			y = y / trueArea;
			Coordinate cog = new Coordinate(x, y);
			if (DEBUG)
				System.out.println("cog " + cog);
			double I = 0;
			for (int i1 = 0; i1 < N; i1++) {
				final double distance = cog.distance(centers[i1]);
				// if(DEBUG)System.out.println(distance+" "+areas[i]+" "+(m[i] +
				// areas[i] * distance*distance));
				I += (m[i1] + areas[i1] * distance * distance);
			}

			polyI[i] = I;
			polyArea[i] = trueArea;
			polyCog[i] = cog;
		}
		if (size > 1) {
			double x = 0, y = 0;

			for (int i1 = 0; i1 < size; i1++) {

				x += polyCog[i1].x * polyArea[i1];
				y += polyCog[i1].y * polyArea[i1];
			}
			double trueArea = geom.getArea();
			x = x / trueArea;
			y = y / trueArea;
			Coordinate cog = new Coordinate(x, y);
			double I = 0;
			for (int i = 0; i < size; i++) {
				final double distance = cog.distance(polyCog[i]);
				I += (polyI[i] + polyArea[i] * distance * distance);
			}
			return I;
		} else {
			return polyI[0];
		}
	}
}
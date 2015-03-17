package org.ianturton.shapes.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.PriorityQueue;
import java.util.TreeSet;

import javax.management.RuntimeErrorException;

import com.vividsolutions.jts.algorithm.Angle;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineSegment;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;

public class MonotonePolygon {
	/**
	 * convert a polygon (possibly including holes) to a set of monotonic
	 * polygons Based on Computational Geometry, De Berg, M., van Kreveld, M.,
	 * Overmars, M., Schwarzkopf, O. Springer, 1997
	 */
	private static final int STARTVERTEX = 1;
	private static final int ENDVERTEX = 2;
	private static final int SPLITVERTEX = 3;
	private static final int MERGEVERTEX = 4;
	private static final int REGULARVERTEX = 5;

	public class Vertex implements Comparable<Vertex> {
		Coordinate next;
		Coordinate prev;
		LineSegment eIMinusOne, eI;
		Coordinate c;
		int type;
		int index;

		public Vertex(Coordinate[] coord, int pos) {
			c = coord[pos];
			index = pos;
			next = coord[(index + 1) % coord.length];
			eIMinusOne = new LineSegment(c, next);
			prev = coord[(coord.length + index - 1) % coord.length];
			eI = new LineSegment(prev, c);
			type = classifyVertex();
		}

		private int classifyVertex() {
			/*
			 * System.out.println("C " + c); System.out.println("Prev " + prev);
			 * System.out.println("Next " + next);
			 */
			final double interiorAngle = Angle.normalizePositive(Angle
					.angleBetweenOriented(prev, c, next));
			// System.out.println(interiorAngle);
			if (c.y > next.y && c.y > prev.y) {
				// System.out.println("above");
				if (interiorAngle < Math.PI) {
					// System.out.println("\tstart");
					return STARTVERTEX;
				} else {
					// System.out.println("\tsplit");
					return SPLITVERTEX;
				}
			} else if (c.y < next.y && c.y < prev.y) {
				// System.out.println("\tbelow");
				if (interiorAngle < Math.PI) {
					// System.out.println("end");
					return ENDVERTEX;
				} else {
					// System.out.println("\tmerge");
					return MERGEVERTEX;
				}
			}
			// System.out.println("\tregular");
			return REGULARVERTEX;
		}

		public String toString() {
			return (index + ": " + c + " " + type+" next "+next+" prev "+prev);
		}

		public int compareTo(Vertex o) {
			if (c.y < o.c.y) {
				return 1;
			} else if (c.y > o.c.y) {
				return -1;
			}
			if (c.x < o.c.x) {
				return 1;
			} else if (c.x > o.c.x) {
				return -1;
			}
			return 0;
		}
	}

	class LineComparator implements Comparator<LineSegment> {

		public int ycoord;

		private double xcoord(LineSegment e) {

			double x1 = (e.p0.x);
			double y1 = e.p0.y;
			double x2 = e.p1.x;
			double y2 = e.p1.y;
			if (x1 == x2)
				return x1;
			if (y1 == y2)
				return (x2 + x1) / 2;
			/* supposing ycoord \in [y1,y2] */
			return x1 + (double) (x2 - x1) / (y2 - y1) * (ycoord - y1);
		}

		public int compare(LineSegment o1, LineSegment o2) {
			double x1 = xcoord(o1);
			double x2 = xcoord(o2);
			if (x1 < x2)
				return -1;
			else if (x1 > x2)
				return 1;
			else
				return 0; /* Wrong?! */
		}

	}

	private static final GeometryFactory gf = new GeometryFactory();
	PriorityQueue<Vertex> pq;
	TreeSet<LineSegment> T = new TreeSet<LineSegment>(new LineComparator());
	ArrayList<LineSegment> D = new ArrayList<LineSegment>();
	HashMap<LineSegment, Vertex> helpers = new HashMap<LineSegment, Vertex>();
	Polygon p;
	ArrayList<Point> splits = new ArrayList<Point>();
	ArrayList<Point> merges = new ArrayList<Point>();
	public MonotonePolygon(Polygon poly) {
		p = poly;
		p.normalize();
		poly.normalize();
		pq = new PriorityQueue<Vertex>(poly.getNumPoints());

		Coordinate[] c = poly.getExteriorRing().getCoordinates();
		c = Arrays.copyOf(c, c.length - 1);
		int length = c.length;

		for (int i = 0; i < length ; i++) {
			Vertex v = new Vertex(c, i);
			System.out.println(v);
			if(v.type==SPLITVERTEX) {
				splits.add(gf.createPoint(v.c));
			}
			if(v.type==MERGEVERTEX) {
				merges.add(gf.createPoint(v.c));
			}
			pq.add(v);
		}
		int rings = poly.getNumInteriorRing();
		for (int r = 0; r < rings; r++) {
			final LineString ring = poly.getInteriorRingN(r);
			//ring.normalize();
			Coordinate[] rc = ring.getCoordinates();
			System.out.println(rc.length);
			for(int k=0;k<rc.length;k++)System.out.println(rc[k]);
			rc = Arrays.copyOf(rc, rc.length - 1);
			length = rc.length;
			for (int i = 0; i < length ; i++) {
				Vertex v = new Vertex(rc, i);
				System.out.println(v);
				if(v.type==SPLITVERTEX) {
					splits.add(gf.createPoint(v.c));
				}
				if(v.type==MERGEVERTEX) {
					merges.add(gf.createPoint(v.c));
				}
				pq.add(v);
			}
		}
		while (!pq.isEmpty()) {
			Vertex v = pq.remove();
			System.out.println("processing " + v);
			switch (v.type) {
			case STARTVERTEX:
				handleStart(v);
				break;
			case SPLITVERTEX:
				handleSplit(v);
				break;
			case MERGEVERTEX:
				handleMerge(v);
				break;
			case ENDVERTEX:
				handleEnd(v);
				break;
			case REGULARVERTEX:
				handleRegular(v);
				break;
			}
		}

	}

	private void handleRegular(Vertex v) {
		// TODO Auto-generated method stub
		System.out.println("Regular vertex");

		Coordinate c = new Coordinate(v.c.x + 0.0001, v.c.y);
		Point pt = gf.createPoint(c);
		boolean flag = p.contains(pt);

		if (flag) {// polygon is right of v
			System.out.println("poly is right");
			final LineSegment eim1 = v.eIMinusOne;
			Vertex vertex = getHelper(eim1);
			if (vertex.type == MERGEVERTEX) {
				final LineSegment lineSegment = new LineSegment(v.c, vertex.c);
				lineSegment.normalize();
				System.out.println("adding " + lineSegment + " to diagonals");
				D.add(lineSegment);
			}
			removeFromT(eim1);
			final LineSegment ei = v.eI;
			addToT(ei);
			addHelper(ei, v);

		} else {// polygon is left
			LineSegment edgeJ = T.lower(new LineSegment(v.c, v.c));
			if(edgeJ==null) {
				edgeJ=v.eIMinusOne;
			}
			final Vertex vertex = helpers.get(edgeJ);
			if (vertex.type == MERGEVERTEX) {
				final LineSegment lineSegment = new LineSegment(v.c, vertex.c);
				lineSegment.normalize();
				System.out.println("adding " + lineSegment + " to diagonals");
				D.add(lineSegment);
			}

			addHelper(edgeJ, v);

		}
	}

	private void handleMerge(Vertex v) {
		// 
		System.out.println("merge");

		Vertex vertex = getHelper(v.eIMinusOne);
		if (vertex.type == MERGEVERTEX) {
			final LineSegment lineSegment = new LineSegment(v.c, vertex.c);
			lineSegment.normalize();
			System.out.println(lineSegment);
			D.add(lineSegment);
		}
		T.remove(v.eIMinusOne);
		System.out.println(T);
		LineSegment edgeJ = T.lower(new LineSegment(v.c, v.c));
		if (edgeJ == null) {
			System.out.println(T.first() + " is left?");
			edgeJ = T.first();
		}
		System.out.println(helpers.keySet());
		vertex = getHelper(edgeJ);

		if (vertex.type == MERGEVERTEX) {
			final LineSegment lineSegment = new LineSegment(v.c, vertex.c);
			lineSegment.normalize();
			System.out.println("adding " + lineSegment + " to diagonals");
			D.add(lineSegment);
		}
		
		addHelper(edgeJ, v);
		// System.out.println("T " + T.size() + " helpers " + helpers.size());
	}

	private void handleSplit(Vertex v) {
		// find edge to left of v
		System.out.println("split");

		LineSegment edgeJ = T.lower(new LineSegment(v.c, v.c));
		if (edgeJ == null) {
			System.out.println(v.eI + "is left?");
			edgeJ = v.eI;
		}
		final Vertex vertex = getHelper(edgeJ);

		final LineSegment lineSegment = new LineSegment(v.c, vertex.c);
		lineSegment.normalize();
		System.out.println("adding " + lineSegment + " to diagonals");
		D.add(lineSegment);
		addHelper(edgeJ, v);
		final LineSegment ei = v.eI;
		ei.normalize();
		addToT(ei);
		addHelper(ei, v);

	}

	private void handleEnd(Vertex v) {
		// 
		System.out.println("end");

		Vertex vert = getHelper(v.eIMinusOne);
		if (vert.type == MERGEVERTEX) {
			final LineSegment lineSegment = new LineSegment(vert.c, v.c);
			lineSegment.normalize();
			System.out.println("adding " + lineSegment + " to diagonals");
			D.add(lineSegment);
		}
		removeFromT(v.eIMinusOne);
	}

	private void handleStart(Vertex v) {
		// 
		System.out.println("start");
		final LineSegment prevEdge = v.eI;
		prevEdge.normalize();
		addHelper(prevEdge, v);
		addToT(prevEdge);

	}

	private void addHelper(LineSegment lineSegment, Vertex v) {
		lineSegment.normalize();
		System.out.println("adding " + lineSegment + "(" + v + ") to helpers");
		helpers.put(lineSegment, v);
	}

	private Vertex getHelper(final LineSegment nextEdge) {
		Vertex vertex = null;
		nextEdge.normalize();
		vertex = helpers.get(nextEdge);
		System.out.println("Helper (" + nextEdge + ") is " + vertex);
		return vertex;
	}

	private void addToT(LineSegment l) {
		l.normalize();
		System.out.println("adding " + l + " to T");
		T.add(l);
	}

	private void removeFromT(LineSegment l) {
		l.normalize();
		System.out.println("removing " + l + " from T");
		T.remove(l);

	}

	public static void main(String[] args) throws ParseException {
		WKTReader reader = new WKTReader(gf);
		String wellKnownText = "POLYGON ((180 360, 200 250, 280 350, 290 230, 390 340, 410 150, 290 90, 100 60, 10 170, 40 350, 90 190, 180 360),"
				+ " (170 220, 260 220, 210 120, 110 160, 110 180, 170 220))";
		if (args.length > 0) {
			wellKnownText = args[0];
		}
		Polygon poly = (Polygon) reader.read(wellKnownText);
		MonotonePolygon mp = new MonotonePolygon(poly);
		System.out.println(poly);
		System.out.println(mp.getDiagonals());
		System.out.println("split "+gf.createGeometryCollection(mp.splits.toArray(new Point[] {})));
		System.out.println("merge "+gf.createGeometryCollection(mp.merges.toArray(new Point[] {})));
		
	}

	private GeometryCollection getDiagonals() {
		Geometry[] geoms = new Geometry[D.size()];
		int i = 0;
		for (LineSegment l : D) {
			LineString ls = l.toGeometry(gf);
			geoms[i++] = (Geometry) ls;
		}
		return gf.createGeometryCollection(geoms);
		// return null;
	}
}

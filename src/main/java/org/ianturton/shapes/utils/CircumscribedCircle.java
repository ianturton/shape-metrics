package org.ianturton.shapes.utils;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import com.vividsolutions.jts.algorithm.MinimumBoundingCircle;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.Triangle;

public class CircumscribedCircle {
	/**
	 * calculate the Circumscribing circle of the polygon
	 * 
	 */
	double radius = Double.NaN;
	double area = Double.NaN;
	double perimeter = Double.NaN;
	Point center = null;
	GeometryFactory gf = new GeometryFactory();
	public CircumscribedCircle(Polygon geom) {
		// oldMethod(geom);
		//welzl(geom);
		MinimumBoundingCircle mbc = new MinimumBoundingCircle(geom);
		setRadius(mbc.getRadius());
		this.center = gf.createPoint(mbc.getCentre());
	}

	void welzl(Polygon geom) {
		
		
		// Using the method from
		// Welzl, E., 1991. Smallest enclosing disks (balls and ellipsoids).
		// In: Maurer, H. (Ed.), New Results and New Trends in Computer Science.
		// Vol. 555 of Lecture Notes in Computer Science. Springer-Verlag,
		// Berlin/Heidelberg, Ch. 24, pp. 359-370.
		// this is allegedely linear!

		// step one - how many points?
		int n = geom.getExteriorRing().getNumPoints();
		if (n <= 1) {
			setRadius(0);
			return;
		}else if (n == 2) {
			LineString perim = ((Polygon) geom.convexHull()).getExteriorRing();
			Point p1 = perim.getPointN(0);
			Point p2 = perim.getPointN(1);
			LineString line = gf.createLineString(new Coordinate[] {p1.getCoordinate(),p2.getCoordinate()});
			double dist = Math.abs(p1.distance(p2));
			setRadius(dist/2.0);
			setCenter(line.getCentroid());
			return;
		}else {
			LineString perim = ((Polygon) geom.convexHull()).getExteriorRing();
			Set<Point> s = new HashSet<Point>();
			for(int i=0;i<perim.getNumPoints();i++) {
				final Point pointN = perim.getPointN(i);
				Iterator<Point> it = s.iterator();
				boolean bad = false;
				while(it.hasNext()) {
					if(pointN.compareTo(it.next())==0) {//I don't trust equals
						bad= true;
					}
				}
				if(!bad)s.add(pointN);
				//System.out.println(pointN+" -> "+s);
			}
			Circle c = miniDisk(s);
			setRadius(c.getRadius());
			setCenter(c.getCentre());//crap!
			
		}
	}
	/**
	 * Calculate the circumscribing circle of S 
	 * @param s - set of points to circumscribe
	 * @return - the circle 
	 */
	private Circle miniDisk(Set<Point> s) {
		if(s.size()==0) {
			//System.out.println("empty set in miniDisk");
			return new Circle();
		}
		Iterator<Point> it =s.iterator();
		Point p = it.next();
		Set<Point> s2 = new HashSet<Point>();
		s2.addAll(s);
		s2.remove(p);
		//System.out.println("proceeding with s2 "+s2);
		Circle c = miniDisk(s2);
		//System.out.println("s2: "+s2+" ->"+c);
		if(!c.contains(p)) {
			//System.out.println(c+" doesn't contain "+p+" expanding");
			Set<Point> x = new HashSet<Point>();
			x.add(p);
			c=miniDisk(s2,x);
		}
		return c;
	}
	/**
	 * Calculate the circumscribing circle of P 
	 * @param p - set of points to circumscribe
	 * @param r - set of points on the circle
	 * @return - the circle 
	 */
	private Circle miniDisk(Set<Point> p,Set<Point> r) {
		if(p.size()==0) {
			return calcCircle(r);
		}
		Iterator<Point> it = p.iterator();
		int rand = (int) (Math.random()*(double)p.size());
		Point p1 = it.next();
		for(int i=0;i<rand;i++) {
			p1 =it.next();
		}
		Set<Point> s2 = new HashSet<Point>();
		s2.addAll(p);
		s2.remove(p1);
		Circle c = miniDisk(s2, r);
		if(!c.contains(p1)) {
			Set<Point> r2 = new HashSet<Point>();
			r2.addAll(r);
			r2.add(p1);
			c = miniDisk(s2,r2);
		}
		return c;
		
	}
	/**
	 * based on http://en.wikipedia.org/wiki/Circumscribed_circle#Circumcircle_equations
	 * @param r - a set of points 
	 * @return the circuscribing circle (if it exists)
	 */
	private Circle calcCircle(Set<Point> r) {
		//System.out.println(r);
		Circle circle = new Circle();
		/*if(r.size()>3) {
			System.out.println("Whoops - r is too big "+r);
		}*/
		if(r.size()>=3) {
			Iterator<Point> it = r.iterator();
			Point a = it.next();
			Point b = it.next();
			Point c = it.next();
			/*
			double d = 2*(a.getX()*(b.getY()-c.getY())+b.getX()*(c.getY()-a.getY())+c.getX()*(a.getY()-b.getY()));
			
			double x1 = ((a.getY()*a.getY()+a.getX()*a.getX())*(b.getY()-c.getY())
					+ (b.getY()*b.getY()+b.getX()*b.getX())*(c.getY()-a.getY())
					+ (c.getY()*c.getY()+c.getX()*c.getX())*(a.getY()-b.getY()))/d; 
			double y1 = ((a.getY()*a.getY()+a.getX()*a.getX())*(c.getX()-b.getX())
					+ (b.getY()*b.getY()+b.getX()*b.getX())*(a.getX()-c.getX())
					+ (c.getY()*c.getY()+c.getX()*c.getX())*(b.getX()-a.getX()))/d;
			Point p = gf.createPoint(new Coordinate(x1,y1));*/
			
			//System.out.println("center = "+p);
			Point p = gf.createPoint(Triangle.circumcentre(a.getCoordinate(),b.getCoordinate(),c.getCoordinate()));
			Double distA = p.distance(a);
			Double distB = p.distance(b);
			Double distC = p.distance(c);
			assert(Math.abs(distA-distB)<0.000001);
			assert(Math.abs(distC-distB)<0.000001);
			assert(Math.abs(distA-distC)<0.000001);
			//System.out.println(distA+" "+distB+" "+distC);
			circle = new Circle(p, distA);
			//System.out.println("created new circle "+circle);
			
		}
		return circle;
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
		area = Math.PI * radius * radius;
	}

	public double getPerimeter() {
		return perimeter;
	}

	private void setPerimeter() {
		this.perimeter = 2.0 * Math.PI * radius;
	}

	public Point getCenter() {
		return center;
	}

	public void setCenter(Point center) {
		this.center = center;
	}
}

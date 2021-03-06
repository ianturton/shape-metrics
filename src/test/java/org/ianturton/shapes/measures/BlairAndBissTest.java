package org.ianturton.shapes.measures;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.ianturton.shapes.measures.BlairAndBiss;
import org.ianturton.shapes.utils.Circle;
import org.ianturton.shapes.utils.Square;
import org.junit.Test;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;

public class BlairAndBissTest {

	@Test
	public void testCalcScore() {
		Square sq = new Square(2.0);
		Polygon geom = sq.getPoly();
		BlairAndBiss z = new BlairAndBiss();
		//z.setDEBUG(true);
		double score = z.calcScore(geom);
		System.out.println("square = "+score);
		//assertTrue("Square is less than 1",score<=1.0);
		Circle c = new Circle(1.0,1.0, 1.0);
		score = z.calcScore(c.toPolygon(720));
		System.out.println("Circle = "+score);
		assertEquals("Circle score is wrong",1.0, score, 1.0e-3);
		
		try {
			URL url = this.getClass().getResource("resources/testpoly.txt");
			System.out.println(url);
			File file = new File(url.toURI());
		
			LineIterator it = FileUtils.lineIterator(file);
			int id=0;
			while(it.hasNext()) {
				
				WKTReader reader = new WKTReader();
				Geometry g = reader.read(it.nextLine());
				System.out.println(g);
				score = z.calcScore(g);
				System.out.println("poly "+id+" score ="+score);
				id++;
			}
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}

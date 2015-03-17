package org.ianturton.shapes;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import org.apache.commons.lang.StringUtils;
import org.geotools.data.FeatureSource;
import org.geotools.data.FileDataStore;
import org.geotools.data.FileDataStoreFinder;
import org.geotools.feature.FeatureIterator;
import org.geotools.swing.data.JFileDataStoreChooser;
import org.hsqldb.lib.StringUtil;
import org.ianturton.shapes.measures.MomentOfInertia;
import org.ianturton.shapes.measures.ShapeClassifier;
import org.ianturton.shapes.measures.Zusne;
import org.opengis.feature.Feature;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.simplify.TopologyPreservingSimplifier;

public class ShapeToWKT {

	public ShapeToWKT(File file) {

	}

	public static void main(String[] args) {

		File file;
		if (args.length == 0) {
			file = JFileDataStoreChooser.showOpenFile("shp", null);
		} else {
			file = new File(args[0]);
			if (!file.exists()) {
				System.err.println("could not find " + args[0]);
				return;
			}
		}
		if (file == null) {
			return;
		}

		FileDataStore store;
		FeatureSource<SimpleFeatureType, SimpleFeature> featureSource = null;
		FeatureIterator<SimpleFeature> it = null;
		try {
			store = FileDataStoreFinder.getDataStore(file);
			Zusne z = new Zusne();
			featureSource = store.getFeatureSource();
			it = featureSource.getFeatures().features();
			while (it.hasNext()) {
				final Feature f = it.next();
				//System.out.print(f.getProperty("LEG_DISTRI").getValue().toString().trim() + "|");
				Geometry geom = (Geometry) f.getDefaultGeometryProperty().getValue();
				if(geom instanceof MultiPolygon) {
					//System.out.println(geom.toText());
				}
					int n = geom.getNumGeometries();
					for(int i=0;i<n;i++) {
						Geometry g = geom.getGeometryN(i);
						System.out.println("valid:"+g.isValid());
						System.out.println(g.toText());
						MomentOfInertia.calculateMomentOfInertia(g);
						
						
					}
				//}
				//final String wkt = geom.toText();
				//System.out.println(wkt);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			it.close();
		}
	}
}

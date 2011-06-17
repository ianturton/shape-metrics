package edu.psu.geovista.ian;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFactorySpi;
import org.geotools.data.DefaultTransaction;
import org.geotools.data.FeatureSource;
import org.geotools.data.FeatureStore;
import org.geotools.data.FileDataStore;
import org.geotools.data.FileDataStoreFinder;
import org.geotools.data.Transaction;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureCollections;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.swing.data.JFileDataStoreChooser;
import org.opengis.feature.Feature;
import org.opengis.feature.Property;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import edu.psu.geovista.ian.measures.BlairAndBiss;
import edu.psu.geovista.ian.measures.BoyceAndClark;
import edu.psu.geovista.ian.measures.Gibbs;
import edu.psu.geovista.ian.measures.LeeAndSalle;
import edu.psu.geovista.ian.measures.Miller;
import edu.psu.geovista.ian.measures.Ritter;
import edu.psu.geovista.ian.measures.ShapeClassifier;
import edu.psu.geovista.ian.measures.Zusne;

/**
 * Hello world!
 * 
 */
public class ProcessShapes {
	static final String[] extensions = { ".dbf", ".fix", ".qix", ".shx",
			".prj", ".shp" };
	static final ShapeClassifier[] methods = { new Ritter(), new Miller(),
			new Gibbs(), new LeeAndSalle(), new BoyceAndClark(), new Zusne(),
			new BlairAndBiss() };

	@SuppressWarnings("unchecked")
	public static void main(String[] args) throws IOException {

		// display a data store file chooser dialog for shapefiles
		ArrayList<File> files = new ArrayList<File>();

		int index = 0;
		boolean force = false;
		if (args.length > 0) {
			if (args[0].equalsIgnoreCase("-f")) {
				force = true;
				index = 1;
			}
		}
		String fileName = "";
		if (args.length > index) {
			fileName = args[index];
		}
		files = processFile(fileName);

		for (File file1 : files) {
			System.out.println("about to process " + file1);
			String outFileName = StringUtils.removeEnd(file1.getAbsolutePath(),
					".shp")
					+ "_measure.shp";
			// System.out.println("output to " + outFileName);
			File outfile = new File(outFileName);

			if (outfile.exists()) {
				if (force) {
					String basename = StringUtils.removeEnd(file1
							.getAbsolutePath(), ".shp");
					for (String ext : extensions) {
						File f = new File(basename + ext);
						f.delete();
					}
					outfile = new File(outFileName);
				} else {
					continue;
				}
			}
			System.out.println("Processing to " + outfile);

			FileDataStore inputStore = FileDataStoreFinder.getDataStore(file1);
			FeatureSource inputFeatureSource = inputStore.getFeatureSource();

			SimpleFeatureType schema = (SimpleFeatureType) inputFeatureSource
					.getSchema();
			DataStore newDataStore = buildOutput(schema, outfile);
			SimpleFeatureType FLAG = newDataStore.getSchema(newDataStore
					.getNames().get(0));
			FeatureCollection collection = FeatureCollections.newCollection();
			Iterator<Feature> it = inputFeatureSource.getFeatures().iterator();
			try {
				SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder(
						FLAG);

				while (it.hasNext()) {
					final Feature f = it.next();
					// copy the existing attributes
					Collection<Property> props = f.getProperties();
					for (Property p : props) {
						featureBuilder.set(p.getName(), p.getValue());
					}
					for (ShapeClassifier sc : methods) {
						double s = sc.score(f);
						featureBuilder.set(StringUtils.abbreviate(sc.getName(),
								10), s);
					}

					SimpleFeature feature = featureBuilder.buildFeature(null);

					collection.add(feature);
				}
			} finally {
				inputFeatureSource.getFeatures().close(it);
			}

			// write out the new shapefile
			Transaction transaction = new DefaultTransaction("create");
			String typeName = newDataStore.getTypeNames()[0];

			FeatureStore featureStore = (FeatureStore) newDataStore
					.getFeatureSource(typeName);

			featureStore.setTransaction(transaction);
			try {
				featureStore.addFeatures(collection);
				transaction.commit();

			} catch (Exception problem) {
				problem.printStackTrace();
				transaction.rollback();

			} finally {
				transaction.close();
				newDataStore.dispose();
				outfile = null;

			}

		}

	}

	private static DataStore buildOutput(SimpleFeatureType schema, File outfile) {
		SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
		builder.setName("Flag");
		builder.setNamespaceURI("http://localhost/");
		final CoordinateReferenceSystem crs = schema
				.getCoordinateReferenceSystem();

		builder.setCRS(crs);
		List<AttributeDescriptor> descriptors = schema
				.getAttributeDescriptors();
		for (AttributeDescriptor descriptor : descriptors) {
			// System.out.println(descriptor.getName()+" "+descriptor.getType().getBinding());
			builder.add(descriptor);
		}
		for (ShapeClassifier sc1 : methods) {
			// System.out.print(sc1.getName() + "\t");
			builder.add(StringUtils.abbreviate(sc1.getName(), 10),
					java.lang.Double.class);
		}

		// build the type
		final SimpleFeatureType FLAG = builder.buildFeatureType();

		DataStoreFactorySpi factory = new ShapefileDataStoreFactory();
		Map<String, Serializable> create = new HashMap<String, Serializable>();
		DataStore newDataStore = null;
		try {
			create.put("url", outfile.toURI().toURL());

			newDataStore = factory.createNewDataStore(create);

			newDataStore.createSchema(FLAG);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return newDataStore;
	}

	private static ArrayList<File> processFile(String fileName) {
		File file;
		ArrayList<File> files = new ArrayList<File>();
		if (fileName.isEmpty()) {
			file = JFileDataStoreChooser.showOpenFile("shp", null);
			files.add(file);
		} else {
			file = new File(fileName);

			if (!file.exists()) {
				System.err.println("could not find " + fileName);
				return files;
			}
			if (file.isDirectory()) {
				String[] l = file.list(new FilenameFilter() {

					public boolean accept(File dir, String name) {
						// TODO Auto-generated method stub
						if (name.endsWith(".shp")
								&& !name.contains("_measure.")) {
							return true;
						}
						return false;
					}
				});
				for (String f : l) {
					File dd = new File(file.getAbsolutePath()
							+ file.separatorChar + f);
					files.add(dd);
				}
			} else {
				files.add(file);
			}
		}
		if (files.size() == 0) {
			System.out.println("no files found in " + file);

		}
		return files;
	}
}
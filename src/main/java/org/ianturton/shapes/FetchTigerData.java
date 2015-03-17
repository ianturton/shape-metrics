package org.ianturton.shapes;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Iterator;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.lang.StringUtils;
import org.geotools.data.FeatureSource;
import org.geotools.data.FileDataStore;
import org.geotools.data.FileDataStoreFinder;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.opengis.feature.Feature;

public class FetchTigerData {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String baseDir="";
		String type = "county";
		// read through the states list and fetch the congressional districts
		if(args.length>0) {
			for(int i=0;i<args.length;i++) {
				if(args[i].equalsIgnoreCase("-b")) {
					baseDir = args[i+1];
					i++;
				}
				if(args[i].equalsIgnoreCase("-t")) {
					type = args[i+1];
					i++;
				}
			}
		}
		URL url = FetchTigerData.class.getResource("tl_2009_us_state.shp");
		FileDataStore store;
		try {
			store = FileDataStoreFinder.getDataStore(url);

			FeatureSource featureSource = store.getFeatureSource();
			FeatureIterator it = featureSource.getFeatures().features();
			 byte[] buffer = new  byte[2048];
			 
			 String outdir = type;
			 File outdirFile = null;
			 if(baseDir.isEmpty()) {
				 outdirFile = new File(outdir);
			 }else {
				 outdirFile = new File(baseDir+File.separatorChar+outdir);
			 }
			 System.out.println("processing to "+outdirFile);
			 if(!outdirFile.exists()) {
				 outdirFile.mkdir();
			 }
			while (it.hasNext()) {
				Feature f = it.next();
				String name = (String) f.getProperty("NAME").getValue();
				String newName = StringUtils.replaceChars(name.toUpperCase(),
						' ', '_');
				String fips = (String) f.getProperty("STATEFP").getValue();
				System.out.println(name + " " + fips);
				// http://www2.census.gov/geo/tiger/TIGER2009/11_DISTRICT_OF_COLUMBIA/tl_2009_11_cd108.zip
				// http://www2.census.gov/geo/tiger/TIGER2009/39_OHIO/tl_2009_39_cd111.zip
				//counties - http://www2.census.gov/geo/tiger/TIGER2009/01_ALABAMA/tl_2009_01_county.zip
				//http://www2.census.gov/geo/tiger/TIGER2009/60_AMERICAN_SAMOA/tl_2009_60_county.zip
				//http://www2.census.gov/geo/tiger/TIGER2009/01_ALABAMA/tl_2009_01_sldu.zip
				String u = "http://www2.census.gov/geo/tiger/TIGER2009/" + fips
						+ "_" + newName + "/tl_2009_" + fips + "_"+type+".zip";
				System.out.println(u);
				URL fetch = new URL(u);
				try {
				ZipInputStream zis = new ZipInputStream(fetch.openStream());
				ZipEntry z;
				while ((z = zis.getNextEntry()) != null) {
					String outpath = outdirFile.getAbsolutePath() + "/" + z.getName();
					FileOutputStream output = null;
					try {
						output = new FileOutputStream(outpath);
						int len = 0;
						while ((len = zis.read(buffer)) > 0) {
							output.write(buffer, 0, len);
						}
					} finally {
						// we must always close the output file
						if (output != null)
							output.close();
					}
				}
				}catch (IOException e) {
					e.printStackTrace();
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}

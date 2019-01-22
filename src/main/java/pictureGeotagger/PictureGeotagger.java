package pictureGeotagger;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.apache.commons.csv.CSVPrinter;
import org.casaca.gpx4j.core.data.GpxDocument;
import org.casaca.gpx4j.core.data.Waypoint;
import org.casaca.gpx4j.core.driver.GpxDriver;
import org.casaca.gpx4j.core.driver.IGpxReader;
import org.casaca.gpx4j.core.exception.GpxFileNotFoundException;
import org.casaca.gpx4j.core.exception.GpxIOException;
import org.casaca.gpx4j.core.exception.GpxPropertiesException;
import org.casaca.gpx4j.core.exception.GpxReaderException;
import org.casaca.gpx4j.core.exception.GpxValidationException;
import org.casaca.gpx4j.core.exception.GpxWriterException;

public class PictureGeotagger {

	public static void main(String[] args) throws GpxPropertiesException, GpxValidationException, GpxReaderException,
			IOException, InterruptedException, GpxWriterException {
		JFileChooser fChooser = new JFileChooser();
		fChooser.setDialogTitle("Please select the waypoint file");
		fChooser.setFileFilter(new FileNameExtensionFilter("GPX waypoint file", "gpx"));
		// int returnVal = fChooser.showOpenDialog(null);
		int returnVal = JFileChooser.APPROVE_OPTION;

		if (returnVal == JFileChooser.APPROVE_OPTION) {
			// List<Waypoint> wpts = readWaypoints(fChooser.getSelectedFile());
			List<Waypoint> wpts = readWaypoints(new File("C:\\Users\\GreenFlash1986\\Desktop\\Boofen\\BOOFENLISTE.GPX"));
			for (int i = 0; i < wpts.size(); ++i) { // first check, that every name is different
				for (int j = i + 1; j < wpts.size(); ++j) {
					if (wpts.get(i).getName().equalsIgnoreCase(wpts.get(j).getName())) {
						throw new IllegalStateException("There are two waypoints with the same name! Aborting");
					}
				}
			}

			// open the picture folder
			fChooser.setDialogTitle("Please select the folder with the correlative pictures");
			fChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			fChooser.setAcceptAllFileFilterUsed(false);
			// returnVal = fChooser.showOpenDialog(null);
			returnVal = JFileChooser.APPROVE_OPTION;
			if (returnVal != JFileChooser.APPROVE_OPTION) {
				return;
			}

			// File folder = fChooser.getSelectedFile();
			File folder = new File("C:\\Users\\GreenFlash1986\\Desktop\\Boofen");
			List<String> pictures = new ArrayList<String>(Arrays.asList(folder.list(new FilenameFilter() {
				@Override
				public boolean accept(File dir, String name) {
					return (name.toLowerCase().endsWith(".jpg") || name.toLowerCase().endsWith("jpeg"));
				}
			})));

			if (pictures.size() == 0) {
				return;
			}

			fChooser.setDialogTitle("Please select the folder for the output for osmand");

			// if (fChooser.showOpenDialog(null) != JFileChooser.APPROVE_OPTION) {
			// return;
			// }

			// OsmAndHandler osmAnd = new OsmAndHandler(fChooser.getSelectedFile());
			OsmAndHandler osmAnd = new OsmAndHandler(new File("C:\\Users\\GreenFlash1986\\Desktop\\Test"));

			String exifToolPath = System.getProperty("user.dir") + "\\lib\\exiftool";
			int picCnt = 0;
			List<File> picturesToAdd = new ArrayList<>();
			ExiftoolHandler exifTool = new ExiftoolHandler(folder);
			for (Waypoint wpt : wpts) {
				boolean found = false;
				int i = 0;
				picturesToAdd.clear();
				while (i < pictures.size()) {
					if (pictures.get(i).split("_|\\.jpg")[1].equalsIgnoreCase(wpt.getName())) {
						// geotagPicture(exifToolPath, folder, pictures.get(i), wpt.getLatitude(), wpt.getLongitude(),
						// wpt.getElevation(),
						// wpt.getDescription());
						exifTool.addPictureToTag(pictures.get(i), wpt.getLatitude(), wpt.getLongitude(), wpt.getElevation(),
								wpt.getName(), wpt.getDescription());

						++picCnt;
						found = true;
						picturesToAdd.add(new File(folder.getAbsolutePath() + File.separator + pictures.get(i)));
						pictures.remove(i);
					} else {
						++i;
					}
				}
				if (!found) {
					System.err.println("No picture for waypoint " + wpt.getName() + " found.");
				}

				osmAnd.addPicture(picturesToAdd, wpt);
			}

			if (pictures.size() > 0) {
				for (String filename : pictures) {
					System.err.println("Could not find the corrosponding waypoint for file " + filename);
				}
			}

			osmAnd.writeOut();
			exifTool.writeFiles(exifToolPath);
			System.out.println(picCnt + " pictures geotagged");
		} else {
			System.err.println("No pictures found. Abort");
		}
	}

	private static List<Waypoint> readWaypoints(File wptFile) throws GpxFileNotFoundException, GpxPropertiesException,
			GpxIOException, GpxValidationException, GpxReaderException {
		GpxDriver driver = GpxDriver.getGpxDriver();
		driver.loadDefaultDriverProperties();
		IGpxReader reader = driver.createReader();
		GpxDocument doc = reader.readGpxDocument(wptFile);
		List<Waypoint> wpts = doc.getWaypoints();
		return wpts;
	}

	private static void geotagPicture(String exifToolPath, File workingDir, String filenamePic, BigDecimal latitude,
			BigDecimal longitude, BigDecimal elevation, String desc) throws IOException, InterruptedException {
		DecimalFormatSymbols otherSymbols = new DecimalFormatSymbols(Locale.getDefault());
		otherSymbols.setDecimalSeparator('.');
		otherSymbols.setGroupingSeparator(',');
		DecimalFormat coordFormat = new DecimalFormat("#0.0#####", otherSymbols);

		String gpsLongitudeRef = longitude.signum() == -1 ? "W" : "E";
		String gpsLatitudeRef = latitude.signum() == -1 ? "S" : "N";
		List<String> cmd = new ArrayList<>();

		cmd.add(exifToolPath);
		cmd.add("-charset");
		cmd.add("filename=UTF8");
		cmd.add("-L");

		cmd.add("-GPSLatitudeRef=" + gpsLatitudeRef);
		cmd.add("-GPSLatitude=" + coordFormat.format(latitude));
		cmd.add("-GPSLongitudeRef=" + gpsLongitudeRef);
		cmd.add("-GPSLongitude=" + coordFormat.format(longitude));
		if (elevation != null) {
			cmd.add("-GPSAltitudeRef=Above");
			cmd.add("-GPSAltitude=" + coordFormat.format(elevation));
		}
		if (desc != null && !desc.equals("")) {
			String[] parts = desc.split(" ");
			if (parts.length > 1) {
				cmd.add("-Description=\"" + parts[0]);
				for (int i = 1; i < parts.length - 1; ++i) {
					cmd.add(parts[i]);
				}
				cmd.add(parts[parts.length - 1] + "\"");
			} else {
				cmd.add("-Description=\"" + parts[0] + "\"");
			}
		}
		// String test = new String(filenamePic.getBytes("UTF-8"), "ISO-8859-1");
		String test = new String(filenamePic.getBytes(), "UTF-8"); // Probleme mit Umlauten
		// String test = new String(filenamePic.getBytes(), "ISO-8859-1"); // problem mit ß
		// String test = new String(filenamePic.getBytes(), "UTF-16");
		// String test = filenamePic;
		cmd.add(test);
		if (filenamePic.contains("ß"))
			filenamePic.toString();
		// cmd.add(filenamePic);
		ProcessBuilder exifTool = new ProcessBuilder(cmd);
		exifTool.directory(workingDir);
		exifTool.inheritIO();
		Process proc = exifTool.start();
		proc.waitFor();
	}
}

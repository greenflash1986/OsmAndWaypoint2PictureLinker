package com.github.greenflash1986.osmand_waypoint2picture_linker;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

import org.casaca.gpx4j.core.data.GpxDocument;
import org.casaca.gpx4j.core.data.Link;
import org.casaca.gpx4j.core.data.Waypoint;
import org.casaca.gpx4j.core.driver.GpxDriver;
import org.casaca.gpx4j.core.driver.IGpxWriter;
import org.casaca.gpx4j.core.exception.GpxIOException;
import org.casaca.gpx4j.core.exception.GpxPropertiesException;
import org.casaca.gpx4j.core.exception.GpxWriterException;

public class OsmAndHandler {

	private File pictureDir;
	private File waypointFile;
	private GpxDocument doc;
	private List<Waypoint> waypoints;

	public OsmAndHandler(File outputDirectory) throws IOException {
		String folder = outputDirectory.getPath();
		waypointFile = new File(folder + File.separator + "osmand" + File.separator + "tracks" + File.separator
				+ "Boofenliste.gpx");
		pictureDir = new File(folder + File.separator + "osmand" + File.separator + "avnotes");
		waypointFile.mkdirs();
		if (waypointFile.exists()) {
			waypointFile.delete();
		}
		waypointFile.createNewFile();

		if (pictureDir.exists()) {
			for (String file : pictureDir.list()) {
				new File(pictureDir.getAbsolutePath() + File.separator + file).delete();
			}
			pictureDir.delete();
		}
		pictureDir.mkdirs();

		doc = new GpxDocument();
		waypoints = doc.getWaypoints();
	}

	public void addPicture(List<File> pictures, Waypoint wpt) throws IOException {
		for (File file : pictures) {
			String filename = getOsmAndFilename(pictureDir, wpt.getLatitude().doubleValue(), wpt.getLongitude().doubleValue());
			Files.copy(file.toPath(), pictureDir.toPath().resolve(filename));
			Link link = new Link();
			link.setHref(filename);
			wpt.getLinks().add(link);
		}

		waypoints.add(wpt);
	}

	private String getOsmAndFilename(File destFolder, double lat, double lon) {
		String fileBasename = createShortLinkString(lat, lon, 15);
		int k = 1;
		File fl;
		String filename;
		do {
			filename = fileBasename + "." + (k++) + ".jpg";
			// filename = fileBasename + "-" + (k++) + ".jpg";
			fl = new File(destFolder, filename);
		} while (fl.exists());
		return filename;
	}

	public void writeOut() throws GpxPropertiesException, GpxIOException, GpxWriterException, FileNotFoundException {
		GpxDriver driver = GpxDriver.getGpxDriver();
		driver.loadDefaultDriverProperties();
		IGpxWriter writer = driver.createWriter();
		writer.write(doc, new FileOutputStream(waypointFile));
	}

	private String createShortLinkString(double latitude, double longitude, int zoom) {
		long lat = (long) (((latitude + 90d) / 180d) * (1L << 32));
		long lon = (long) (((longitude + 180d) / 360d) * (1L << 32));
		long code = interleaveBits(lon, lat);
		String str = "";
		// add eight to the zoom level, which approximates an accuracy of one pixel in a tile.
		for (int i = 0; i < Math.ceil((zoom + 8) / 3d); i++) {
			str += intToBase64[(int) ((code >> (58 - 6 * i)) & 0x3f)];
		}
		// append characters onto the end of the string to represent
		// partial zoom levels (characters themselves have a granularity of 3 zoom levels).
		for (int j = 0; j < (zoom + 8) % 3; j++) {
			str += '-';
		}
		return str;
	}

	private long interleaveBits(long x, long y) {
		long c = 0;
		for (byte b = 31; b >= 0; b--) {
			c = (c << 1) | ((x >> b) & 1);
			c = (c << 1) | ((y >> b) & 1);
		}
		return c;
	}

	private static final char intToBase64[] = {
			'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M',
			'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
			'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm',
			'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
			'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '_', '~'
	};
}

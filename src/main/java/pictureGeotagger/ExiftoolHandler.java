package pictureGeotagger;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ExiftoolHandler {

	private List<String[]> lines = new ArrayList<String[]>();
	DecimalFormat coordFormat;
	File workingDir;

	public ExiftoolHandler(File workingDir) {
		lines.add(new String[] { "SourceFile", "GPSLatitude", "GPSLatitudeRef", "GPSLongitude", "GPSLongitudeRef",
				"GPSAltitude", "GPSAltitudeRef", 
				"desc", "IPTC:CodedCharacterSet" });
		DecimalFormatSymbols otherSymbols = new DecimalFormatSymbols(Locale.getDefault());
		otherSymbols.setDecimalSeparator('.');
		otherSymbols.setGroupingSeparator(',');
		coordFormat = new DecimalFormat("#0.0#####", otherSymbols);
		this.workingDir = workingDir;
	}

	public void addPictureToTag(String filenamePic, BigDecimal latitude, BigDecimal longitude, BigDecimal elevation,
			String desc) {
		String lat = coordFormat.format(latitude);
		String latRef = latitude.signum() == -1 ? "S" : "N";
		String lon = coordFormat.format(longitude);
		String lonRef = longitude.signum() == -1 ? "W" : "E";
		String ele = null;
		String eleRef = null;
		if (elevation != null) {
			ele = coordFormat.format(elevation);
			eleRef = "Above";
		}
		lines.add(new String[] { filenamePic, lat, latRef, lon, lonRef, ele, eleRef, desc });
	}

	private File writeCsv() throws UnsupportedEncodingException, IOException {
		File out = new File(workingDir, "out.csv");
		try (Writer writer = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream(out), "UTF-8"))) {
			for (String[] line : lines) {
				StringBuilder sb = new StringBuilder();
				for (int i = 0; i < line.length - 1; ++i) {
					if (line[i] != null) {
						sb.append(quoteField(line[i]));
					}
					sb.append(",");
				}
				if (line[line.length - 1] != null) {
					sb.append(line[line.length - 1]);
				}
				sb.append(System.getProperty("line.separator"));
				writer.write(sb.toString());
			}
		}
		return out;
	}

	private String quoteField(String value) {
		if (value.contains(",")) {
			return "\"" + value + "\"";
		} else {
			return value;
		}
	}

	public void writeFiles(String exifToolPath) throws IOException, InterruptedException {
		File out = writeCsv();
		List<String> cmd = new ArrayList<>();

		cmd.add(exifToolPath);
		cmd.add("-charset");
		cmd.add("filename=UTF8");
		cmd.add("-charset");
		cmd.add("iptc=UTF8");
		cmd.add("-csv=out.csv");
		cmd.add("-codedcharacterset=utf8");
		cmd.add("*.jpg");

		ProcessBuilder exifTool = new ProcessBuilder(cmd);
		exifTool.directory(workingDir);
		exifTool.inheritIO();
		Process proc = exifTool.start();
		proc.waitFor();
	}
}

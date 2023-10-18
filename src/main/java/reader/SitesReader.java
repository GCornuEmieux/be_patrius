package reader;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;

import utils.LogUtils;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;

import fr.cnes.sirius.patrius.bodies.GeodeticPoint;
import fr.cnes.sirius.patrius.math.util.MathLib;

/**
 * Read the reference sites.
 *
 * @author bonitt
 */
public class SitesReader {
	
	/**
	 * Logger for this class.
	 */
	private final static Logger LOGGER = LogUtils.GLOBAL_LOGGER;
	
	/** Path of the file containing our sites to read. */
	public static final String OBSERVATION_SITES_FILE = "src/test/resources/villes_france_metropolitaine.csv";

	/**
	 * [DO NOT MODIFY THIS METHOD]
	 * 
	 * Reads the input file containing the target sites for observation. The input
	 * file must be a csv respecting particular format constraints.
	 *
	 * @param filename Relative path of the input file to read
	 * @return sites data as a {@link List} of {@link Site}
	 * @throws CsvValidationException if the csv is not valid
	 * @throws NumberFormatException  if an NumberFormatException occurs
	 * @throws IOException            if an IOException occurs
	 */
	public static List<Site> readSites(final String filename)
			throws CsvValidationException, NumberFormatException, IOException {

		final List<Site> siteList = new ArrayList<>();
		final CSVReader reader = new CSVReader(new FileReader(OBSERVATION_SITES_FILE));
		String[] lineInArray;
		while ((lineInArray = reader.readNext()) != null) {
			if (!lineInArray[0].contains("ID")) {
				final String[] tab = lineInArray[0].trim().split(";");
				final String name = tab[2];
				final double score = Double.parseDouble(tab[1]);
				final double latitude = MathLib.toRadians(Double.parseDouble(tab[6]));
				final double longitude = MathLib.toRadians(Double.parseDouble(tab[5]));
				double altitude;
				try {
					altitude = Double.parseDouble(tab[9]);
				} catch (Exception e) {
					// For the sites without an altitude, we just set it to 0 m by default
					altitude = 0;
				}
				final GeodeticPoint point = new GeodeticPoint(latitude, longitude, altitude, name);
				siteList.add(new Site(name, score, point));
			}
		}
		
		reader.close();
		
		return siteList;
	}

	/**
	 * [DO NOT MODIFY THIS METHOD]
	 * 
	 * Read the reference sites (test application).
	 *
	 * @param args Nothing expected
	 * @throws CsvValidationException if the csv is not valid
	 * @throws NumberFormatException  if an NumberFormatException occurs
	 * @throws IOException            if an IOException occurs
	 */
	public static void main(final String[] args) throws CsvValidationException, NumberFormatException, IOException {

		final List<Site> siteList = readSites(OBSERVATION_SITES_FILE);
		LOGGER.info("Loaded sites: " + siteList.size());
	}
}

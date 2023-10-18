package utils;

import org.slf4j.Logger;

import fr.cnes.sirius.patrius.events.Phenomenon;
import fr.cnes.sirius.patrius.events.postprocessing.Timeline;
import fr.cnes.sirius.patrius.time.TimeScale;
import fr.cnes.sirius.patrius.time.TimeScalesFactory;
import fr.cnes.sirius.patrius.utils.exception.PatriusException;

/**
 * [DO NOT MODIFY THIS CLASS] Utility class.
 * 
 * @author herberl
 */
public class ProjectUtils {

	/**
	 * Logger for this class.
	 */
	private final static Logger LOGGER = LogUtils.GLOBAL_LOGGER;
	
	/**
	 * [DO NOT MODIFY THIS METHOD]
	 * 
	 * Print the input {@link Timeline} in the console.
	 * 
	 * @param timeline Input {@link Timeline} to print.
	 * @throws PatriusException If the UTC TimeScale instantiation fails because of missing UTC-TAI history data. 
	 */
	public static void printTimeline(final Timeline timeline) throws PatriusException {
		final TimeScale utc = TimeScalesFactory.getUTC();
		LOGGER.info("____ Printing Timeline ____");
		for (final Phenomenon phenom : timeline.getPhenomenaList()) {
			LOGGER.info(phenom.getCode()+" [ "
			+phenom.getStartingEvent().getDate().toString(utc) + " (UTC) ; "+phenom.getEndingEvent().getDate().toString(utc)+" (UTC) ]");
		}
		LOGGER.info("___________________________");
	}

}

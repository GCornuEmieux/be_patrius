package simulation;

import org.slf4j.Logger;

import fr.cnes.sirius.patrius.utils.exception.PatriusException;
import progmission.SimpleMission;
import utils.LogUtils;

/**
 * Main class for launching the simulation.
 *
 * @author herberl
 */
public class SimpleMissionMain {

	/**
	 * Main method.
	 * 
	 * @param args Nothing expected
	 * @throws PatriusException if a PatriusException occurs
	 */
	public static void main(final String[] args) throws PatriusException {

		final Logger logger = LogUtils.GLOBAL_LOGGER;

		logger.info("##################################################");

		// Instantiating our mission using the SimpleMission object.
		final SimpleMission mission = new SimpleMission("BE Supaero mission", 20);
		logger.info("Simple simulation starting ...");
		logger.info(mission.toString());

		// Creating simple VTS visualization by propagating our satellite with a simple
		// nadir pointing law
		mission.createSimpleVTSVisualization();

		logger.info("\n\nSimulation done");
		logger.info("##################################################");

	}
}

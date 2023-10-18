package simulation;

import java.util.Map;

import org.slf4j.Logger;

import fr.cnes.sirius.patrius.attitudes.AttitudeLawLeg;
import fr.cnes.sirius.patrius.attitudes.AttitudeLeg;
import fr.cnes.sirius.patrius.attitudes.StrictAttitudeLegsSequence;
import fr.cnes.sirius.patrius.events.postprocessing.Timeline;
import fr.cnes.sirius.patrius.utils.exception.PatriusException;
import progmission.CompleteMission;
import reader.Site;
import utils.LogUtils;

public class CompleteMissionMain {
	
	/**
	 * Logger for this class.
	 */
	public static final Logger LOGGER = LogUtils.GLOBAL_LOGGER;
	
	/**
	 * Main method to run your simulation.
	 * 
	 * @param args No arg
	 * @throws PatriusException If a {@link PatriusException} occurs.
	 */
	public static void main(String[] args) throws PatriusException {
		
		LOGGER.info("##################################################");
		double t0 = System.currentTimeMillis();

		// Instantiating our mission using the CompleteMission object.
		final CompleteMission mission = new CompleteMission("BE Supaero mission", 10);
		LOGGER.info("Complete simulation starting ...");
		LOGGER.info(mission.toString());

		// First step is to compute when the satellite can access the targets. Each
		// access is an observation opportunity to be consider in the later scheduling
		// process.
		Map<Site, Timeline> accessPlan = mission.computeAccessPlan();
		LOGGER.info(accessPlan.toString());

		// Then we compute the observation plan, that is to say we fill a plan with
		// Observation objects that can be achieved one after each other by the
		// satellite without breaking the cinematic constraints imposed by the
		// satellite agility.
		Map<Site, AttitudeLawLeg> observationPlan = mission.computeObservationPlan();
		LOGGER.info(observationPlan.toString());

		// Then, we compute the cinematic plan, which is the whole cinematic sequence of
		// attitude law legs for our satellite during the mission horizon
		StrictAttitudeLegsSequence<AttitudeLeg> cinematicPlan = mission.computeCinematicPlan();
		LOGGER.info(cinematicPlan.toPrettyString());

		// Checking our cinematic plan
		boolean validity = mission.checkCinematicPlan(cinematicPlan);
		LOGGER.info("Plan validity : " + validity);
		
		// Only if the cinematic plan is valid : we compute the score of our
		// observationPlan
		LOGGER.info("Number of cities visited : "+observationPlan.size());
		double beforeComputingScore = System.currentTimeMillis();
		LOGGER.info("Final score :"+ mission.computeFinalScore(observationPlan));
		double afterComputingScore = System.currentTimeMillis();
		LOGGER.info("Duration of the score computation : "+ 0.001 * (afterComputingScore - beforeComputingScore));
		
		// Computing the time of execution
		double t1 = System.currentTimeMillis();
		LOGGER.info("Total duration : " + 0.001 * (t1 - t0));

		// Finally, we write the VTS outputs to visualize and validate our plan
		mission.generateVTSVisualization(cinematicPlan);

		LOGGER.info("\n\nSimulation done");

		LOGGER.info("##################################################");

	
	}

}

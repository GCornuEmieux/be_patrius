package unittest;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import fr.cnes.sirius.patrius.attitudes.AttitudeLawLeg;
import fr.cnes.sirius.patrius.events.postprocessing.Timeline;
import fr.cnes.sirius.patrius.utils.exception.PatriusException;
import progmission.CompleteMission;

/**
 * This is the test class for the {@link CompleteMission} class. Here you can
 * define all the unit tests you want to test the methods of {@link CompleteMission}.
 * 
 * Before each method you want to write to test a particular aspect of your
 * code, you have to add a @Test tag. This @Test tag indicates to JUnit that the
 * method must be executed as a test. Usually, test methods are made with
 * assertions to check the validity of a condition. See the
 * {@link CompleteMissionTest#testMissionInstantiation()} method to learn how to
 * implement basic tests using assertions.
 * 
 * For the assertions, you can use all the org.junit.Assert methods, like the
 * ones used in the {@link CompleteMissionTest#testMissionInstantiation()}
 * method.
 * <p>
 * <b>Warning: make sure to add Javadoc and comments to your tests so that they are
 * easy to read and to understand. Tests that lack explanations may not be considered.</b>
 * 
 * @author herberl
 *
 */
public class CompleteMissionTest {

	/**
	 * Basic unit test to check the right instantiation of the
	 * {@link CompleteMission} object.
	 * 
	 * @throws PatriusException If a {@link PatriusException} occurs when
	 *                          instantiating the {@link CompleteMission}.
	 */
	@Test
	public void testMissionInstantiation() throws PatriusException {

		// Instantiating a CompleMission for test purposes
		final String input_name = "BE Supaero mission";
		final int siteNumber = 10;

		final CompleteMission mission = new CompleteMission(input_name, siteNumber);

		// Asserting that the number of Sites is the one given in input
		assertTrue("Site list size don't match !", siteNumber == mission.getSiteList().size());
		assertTrue("Names don't match !", input_name.equalsIgnoreCase(mission.getName()));

		// Testing the access plan
		assertTrue("The access plan is not valid !", testAccessPlan(mission));

		// Testing the observation plan
		assertTrue("The observation plan is not valid !", testObservationPlan(mission));

		// Testing the cinematic plan
		assertTrue("The cinematic plan is not valid !", testCinematicPlan(mission));
	}

	/**
	 * Here you can implement your own tests to check the access plan computation.
	 * For example, check that the access plan is not empty, that the
	 * {@link Timeline} objects it contains are realistic, etc.
	 * 
	 * @param mission {@link CompleteMission} containing the plan to validate.
	 * @return A boolean indicating the validity of the plan
	 */
	private boolean testAccessPlan(CompleteMission mission) {
		// Implement you own tests here and return the output as a boolean
		// Of course, the more tests you perform, and the more realistic and specific
		// are your tests, the more validated your code is !
		final boolean testResult = true;
		return testResult;
	}

	/**
	 * Here you can implement your own tests to check the observation plan
	 * computation. For example, you can check that the observation plan is not
	 * empty, that the {@link AttitudeLawLeg} it contains are valid, etc.
	 * 
	 * @param mission {@link CompleteMission} containing the plan to validate.
	 * @return A boolean indicating the validity of the plan
	 */
	private boolean testObservationPlan(CompleteMission mission) {
		// Implement you own tests here and return the output as a boolean
		// Of course, the more tests you perform, and the more realistic and specific
		// are your tests, the more validated your code is !
		final boolean testResult = true;
		return testResult;
	}

	/**
	 * Here you can implement your own tests to check the cinematic plan
	 * computation. For example, you can check that the cinematic plan is not empty,
	 * or that it is valid by using the provided method
	 * {@link SimpleMission#checkCinematicPlan(StrictAttitudeLegsSequence<AttitudeLeg>)}
	 * 
	 * @param mission {@link CompleteMission} containing the plan to validate.
	 * @return A boolean indicating the validity of the plan
	 */
	private boolean testCinematicPlan(CompleteMission mission) {
		// Implement you own tests here and return the output as a boolean
		// For example by using mission.checkCinematicPlan(mission.getCinematicPlan());
		// Of course, the more tests you perform, and the more realistic and specific
		// are your tests, the more validated your code is !
		final boolean testResult = true;
		return testResult;
	}

}

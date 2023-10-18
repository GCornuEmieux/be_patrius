package progmission;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;

import reader.Site;
import reader.SitesReader;
import utils.ConstantsBE;
import utils.LogUtils;
import utils.VTSTools;

import com.opencsv.exceptions.CsvValidationException;

import fr.cnes.sirius.addons.patriusdataset.PatriusDataset;
import fr.cnes.sirius.patrius.attitudes.Attitude;
import fr.cnes.sirius.patrius.attitudes.AttitudeLawLeg;
import fr.cnes.sirius.patrius.attitudes.AttitudeLeg;
import fr.cnes.sirius.patrius.attitudes.StrictAttitudeLegsSequence;
import fr.cnes.sirius.patrius.attitudes.TargetGroundPointing;
import fr.cnes.sirius.patrius.bodies.CelestialBody;
import fr.cnes.sirius.patrius.bodies.CelestialBodyFactory;
import fr.cnes.sirius.patrius.bodies.ExtendedOneAxisEllipsoid;
import fr.cnes.sirius.patrius.frames.Frame;
import fr.cnes.sirius.patrius.frames.FramesFactory;
import fr.cnes.sirius.patrius.frames.TopocentricFrame;
import fr.cnes.sirius.patrius.frames.transformations.Transform;
import fr.cnes.sirius.patrius.math.geometry.euclidean.threed.Vector3D;
import fr.cnes.sirius.patrius.math.util.FastMath;
import fr.cnes.sirius.patrius.math.util.MathLib;
import fr.cnes.sirius.patrius.orbits.CircularOrbit;
import fr.cnes.sirius.patrius.orbits.Orbit;
import fr.cnes.sirius.patrius.orbits.PositionAngle;
import fr.cnes.sirius.patrius.orbits.pvcoordinates.PVCoordinates;
import fr.cnes.sirius.patrius.propagation.BoundedPropagator;
import fr.cnes.sirius.patrius.propagation.analytical.KeplerianPropagator;
import fr.cnes.sirius.patrius.time.AbsoluteDate;
import fr.cnes.sirius.patrius.time.AbsoluteDateInterval;
import fr.cnes.sirius.patrius.time.TimeScale;
import fr.cnes.sirius.patrius.time.TimeScalesFactory;
import fr.cnes.sirius.patrius.utils.Constants;
import fr.cnes.sirius.patrius.utils.exception.PatriusException;
import fr.cnes.sirius.patrius.utils.exception.PropagationException;

/**
 * Simple mission class to simulate an Earth observation mission with one
 * satellite.
 * 
 * @author herberl
 *
 */
public class SimpleMission {

	/**
	 * Logger for this class.
	 */
	private final Logger logger = LogUtils.GLOBAL_LOGGER;

	/** Name of the mission. */
	private final String name;

	/**
	 * EME2000 {@link Frame} will be the reference frame for our
	 * {@link SimpleMission} context.
	 */
	private final Frame eme2000;

	/** Reference TimeScale for the {@link SimpleMission} context. */
	private final TimeScale tai;

	/**
	 * Earth model. See {@link ExtendedOneAxisEllipsoid} for more details about this
	 * model.
	 */
	private final ExtendedOneAxisEllipsoid earth;

	/** Default Patrius Sun model. */
	private final CelestialBody sun;

	/** Initial date of the mission. */
	private final AbsoluteDate startDate;

	/** Final date of the mission. */
	private final AbsoluteDate endDate;

	/**
	 * Mission satellite object. The {@link Satellite} is a custom class developped
	 * in the context of the BEProgrammationMission project.
	 */
	private final Satellite satellite;

	/**
	 * {@link List} of {@link Site} objects, representing the list of observation
	 * targets for our Earth observation {@link Satellite}. The {@link Site} object
	 * is a custom class developed in the context of the BEProgrammationMission
	 * project.
	 */
	private final List<Site> siteList;

	/**
	 * [DO NOT MODIFY THIS METHOD]
	 * 
	 * Basic constructor for the {@link SimpleMission} object.
	 * 
	 * @param missionName   Name of the mission
	 * @param numberOfSites Number of target {@link Site} to consider, please give a
	 *                      number between 1 and 100.
	 * @throws PatriusException If a {@link PatriusException} occurs
	 */
	public SimpleMission(final String missionName, int numberOfSites) throws PatriusException {
		// Naming our mission
		this.name = missionName;

		// Loading Patrius Dataset resources
		// Note that the PatriusDataset is necessary to provide all the physical context
		// (offsets bewteen Timescales, definition of the Frames, basic ephemeris of the
		// Celesital bodies, etc.
		PatriusDataset.addResourcesFromPatriusDataset();

		// Instantiating the space-time physical context
		this.eme2000 = FramesFactory.getEME2000();
		this.tai = TimeScalesFactory.getTAI();

		// Instantiating the Earth model as a one axis ellipsoid
		// Equatorial radius
		final double ae = Constants.WGS84_EARTH_EQUATORIAL_RADIUS;
		// Flattening: here we use a spherical Earth (f=0)
		final double f = 0.;
		// ITRF is the Frame attached to the Earth (International Terrestrial Reference
		// Frame)
		this.earth = new ExtendedOneAxisEllipsoid(ae, f, FramesFactory.getITRF(), "Earth");

		// Instantiating the Sun, using the default Sun body of Patrius
		this.sun = CelestialBodyFactory.getSun();

		// Instantiating the mission horizon
		this.startDate = ConstantsBE.START_DATE;
		this.endDate = ConstantsBE.END_DATE;

		// Keplerian orbital parameters for a sun-synchronous orbit like Pleiades orbit
		final double a = Constants.WGS84_EARTH_EQUATORIAL_RADIUS + ConstantsBE.ALTITUDE;
		final double i = FastMath.toRadians(ConstantsBE.INCLINATION);
		final double raan = FastMath.toRadians(ConstantsBE.ASCENDING_NODE_LONGITUDE);
		final double e = FastMath.toRadians(ConstantsBE.MEAN_ECCENTRICITY);

		// Initial orbit, we use a simple circular orbit
		final CircularOrbit initialOrbit = new CircularOrbit(a, e, e, i, raan, 0.0, PositionAngle.TRUE, this.eme2000,
				this.startDate, Constants.WGS84_EARTH_MU);

		// Instantiating the satellite (see the Satellite object for more details)
		this.satellite = new Satellite(this, "Pleiades 1A", initialOrbit);

		// Read the sites list and extract only the top N ranking elements
		// Number of sites to evaluate: a low number improves the performance,
		// might be interesting for testing
		this.siteList = extractTopRankingSites(numberOfSites);

	}

	/**
	 * [DO NOT MODIFY THIS METHOD]
	 * 
	 * Read the sites list and extract only the top N ranking elements.
	 * 
	 * @param n Number of elements to return
	 * @return top N ranking sites list
	 * @throws IllegalStateException if an error occurs during reading the sites
	 *                               list file if the original sites list hasn't
	 *                               enough elements to be resized
	 */
	private static List<Site> extractTopRankingSites(final int n) {
		// Read the original sites list
		final List<Site> fullSiteList;
		try {
			fullSiteList = SitesReader.readSites(SitesReader.OBSERVATION_SITES_FILE);
		} catch (CsvValidationException | NumberFormatException | IOException e) {
			throw new IllegalStateException(e);
		}

		// Check if the original sites list has enough elements to be resized
		if (fullSiteList.size() < n) {
			throw new IllegalStateException("The original sites list hasn't enough elements to be resized");
		}

		// Sort the list: the top rank elements come first
		Collections.sort(fullSiteList);

		// Resize the list with N elements
		return fullSiteList.subList(0, n);
	}

	/**
	 * [DO NOT MODIFY THIS METHOD]
	 * 
	 * Write the AEM and OEM CIC files to visualize a simple simulation with
	 * VTS.Also write the VTS ROI file to indicate to VTS to plot only the sites
	 * that have been selected for the mission. In this simulation we only have our
	 * satellite pointing nadir and its agility cone under it.
	 * 
	 * @throws PropagationException if the propagator fails
	 */
	public void createSimpleVTSVisualization() throws PropagationException {
		// Creating VTS outputs for basic trajectory with Nadir pointing with a
		// default propagator (pointing Nadir)
		final KeplerianPropagator vtsPropagator = createDefaultPropagator();

		// This object is used for the cinematic plan, don't bother understanding it at
		// this point, we use it as an AtittudeLaw object to write the orbital events
		// file in VTS simulation. In the Nadir case, this file is almost empty since we
		// have only on AtittudeLaw pointing only Nadir.
		final StrictAttitudeLegsSequence<AttitudeLeg> simpleSequence = new StrictAttitudeLegsSequence<>();
		simpleSequence.add(new AttitudeLawLeg(this.getSatellite().getDefaultAttitudeLaw(),
				new AbsoluteDateInterval(startDate, endDate)));

		// Writing the outputs using the VTSTools class

		// Declaring the paths of the output files
		final String pathPOI = ConstantsBE.PATH_VTS_DIRECTORY + File.separator + "BE_Supaero_Target_Sites_POI.txt";
		final String pathOEM = ConstantsBE.PATH_VTS_DIRECTORY + File.separator
				+ "BE_Supaero_Satellite_Trajectory_OEM.txt";
		final String pathAEMNadir = ConstantsBE.PATH_VTS_DIRECTORY + File.separator
				+ "BE_Supaero_Nadir_Pointing_AEM.txt";
		final String pathAEMCinematicPlan = ConstantsBE.PATH_VTS_DIRECTORY + File.separator
				+ "BE_Supaero_Cinematic_Plan_AEM.txt";
		final String pathMEMCinematicPlan = ConstantsBE.PATH_VTS_DIRECTORY + File.separator
				+ "BE_Supaero_Cinematic_Plan_Events_MEM.txt";

		logger.info("\n\nWriting VTS outputs, please wait...");

		// Calling the writing methods
		VTSTools.generatePOIFile(pathPOI, this.getSiteList());
		VTSTools.generateOEMFile(pathOEM, this.getStartDate(), this.getEndDate(), vtsPropagator);
		VTSTools.generateAEMFile(pathAEMNadir, this.getStartDate(), this.getEndDate(), vtsPropagator);
		VTSTools.generateAEMFile(pathAEMCinematicPlan, this.getStartDate(), this.getEndDate(), vtsPropagator);
		VTSTools.generateLegSequenceMEMFile(pathMEMCinematicPlan, simpleSequence);
		logger.info("VTS outputs written");
	}

	/**
	 * [DO NOT MODIFY THIS METHOD]
	 * 
	 * Creating a new {@link KeplerianPropagator} based on the satellite
	 * propagator's configuration.
	 * 
	 * We use this method because the satellite propagator might be saturated with
	 * all its events detectors and loggers which slows the computations.
	 * 
	 * @return the new {@link KeplerianPropagator}
	 * @throws PropagationException if initial attitude cannot be computed
	 */
	public KeplerianPropagator createDefaultPropagator() throws PropagationException {
		return new KeplerianPropagator(this.getSatellite().getInitialOrbit(),
				this.getSatellite().getDefaultAttitudeLaw());
	}

	/**
	 * [DO NOT MODIFY THIS METHOD]
	 * 
	 * Checks the cinematic plan and prints if it's ok or not.
	 * 
	 * We provide this method so that you can check if your cinematic plan doesn't
	 * violate a cinematic constraint. It returns a boolean saying if the plan is
	 * valid or not.
	 * 
	 * @param cinematicPlan Input cinematic plan to check.
	 * @return A boolean indicating the cinematic validity of the plan.
	 * @throws PatriusException if an error occurs during propagation
	 */
	public boolean checkCinematicPlan(StrictAttitudeLegsSequence<AttitudeLeg> cinematicPlan) throws PatriusException {

		final KeplerianPropagator propagator = createDefaultPropagator();

		// Checking the cinematic plan validity
		boolean valid = true;
		for (final AttitudeLeg slew : cinematicPlan) {
			logger.info(slew.getNature());

			final Attitude endAtt = slew.getAttitude(propagator, slew.getEnd(), this.getEme2000());
			final Attitude startAtt = slew.getAttitude(propagator, slew.getDate(), this.getEme2000());

			final boolean condition = slew.getTimeInterval().getDuration() > this.getSatellite()
					.computeSlewDuration(startAtt, endAtt);

			if (condition) {
				logger.info("Cinematic is ok");
			} else {
				valid = false;
				logger.warn("WARNING: cinematic is not realistic for this slew");
				logger.warn("Slew actual duration: " + slew.getTimeInterval().getDuration());
				logger.warn("Slew duration theory: " + this.getSatellite().computeSlewDuration(startAtt, endAtt));
			}
		}
		logger.info("==== Is the cinematic plan valid ? => " + valid + " ==== ");
		return valid;
	}

	/**
	 * [DO NOT MODIFY THIS METHOD]
	 * 
	 * Writes the VTS output files : one CIC-POI file to print the sites of
	 * interest, one CIC-OEM file giving the position and velocity ephemeris of the
	 * satellite, one CIC-AEM file giving the attitude ephemeris of the satellite
	 * pointing Nadir nadir only (to help visualize the access field of view of the
	 * satellite) and one CIC-AEM file giving the attitude ephemeris of the
	 * satellite cinematic plan. Also writes the cinematic plan as a sequence of
	 * pointing modes for the satellite in a CIC-MEM file.
	 * 
	 * @param cinematicPlan Input cinematic plan.
	 * 
	 * @throws PropagationException if an error happens during the {@link Orbit}
	 *                              propagation
	 */
	public void generateVTSVisualization(StrictAttitudeLegsSequence<AttitudeLeg> cinematicPlan)
			throws PropagationException {
		// First, create the propagator for the satellite's pointing capacity view (nadir law)
		final KeplerianPropagator vtsPropagatorNadir = createDefaultPropagator();
		vtsPropagatorNadir.setEphemerisMode();
		vtsPropagatorNadir.propagate(this.getEndDate());

		// Get generated ephemeris
		final BoundedPropagator ephemerisNadir = vtsPropagatorNadir.getGeneratedEphemeris();

		// Then, we create the propagator for the cinematic plan visualization
		final KeplerianPropagator vtsPropagator = new KeplerianPropagator(this.getSatellite().getInitialOrbit(),
				cinematicPlan);

		vtsPropagator.setEphemerisMode();
		vtsPropagator.propagate(this.getEndDate());

		// Get generated ephemeris
		final BoundedPropagator ephemeris = vtsPropagator.getGeneratedEphemeris();

		// Writing the outputs
		final String pathPOI = ConstantsBE.PATH_VTS_DIRECTORY + File.separator + "BE_Supaero_Target_Sites_POI.txt";
		final String pathOEM = ConstantsBE.PATH_VTS_DIRECTORY + File.separator
				+ "BE_Supaero_Satellite_Trajectory_OEM.txt";
		final String pathAEMNadir = ConstantsBE.PATH_VTS_DIRECTORY + File.separator
				+ "BE_Supaero_Nadir_Pointing_AEM.txt";
		final String pathAEMCinematicPlan = ConstantsBE.PATH_VTS_DIRECTORY + File.separator
				+ "BE_Supaero_Cinematic_Plan_AEM.txt";
		final String pathMEMCinematicPlan = ConstantsBE.PATH_VTS_DIRECTORY + File.separator
				+ "BE_Supaero_Cinematic_Plan_Events_MEM.txt";

		logger.info("\n\nWriting VTS outputs, please wait...");
		VTSTools.generatePOIFile(pathPOI, this.getSiteList());
		VTSTools.generateOEMFile(pathOEM, this.getStartDate(), this.getEndDate(), ephemeris);
		VTSTools.generateAEMFile(pathAEMNadir, this.getStartDate(), this.getEndDate(), ephemerisNadir);
		VTSTools.generateAEMFile(pathAEMCinematicPlan, this.getStartDate(), this.getEndDate(), ephemeris);
		VTSTools.generateLegSequenceMEMFile(pathMEMCinematicPlan, cinematicPlan);
		logger.info("VTS outputs written");
	}

	/**
	 * [DO NOT MODIFY THIS METHOD]
	 * 
	 * Computes and returns the angle of incidence (satellite->nadir ;
	 * satellite->target) at the middle date of the input observation.
	 * 
	 * @param site              Input {@link Site} associated with the
	 *                          {@link AttitudeLawLeg}.
	 * @param observationLawLeg Observation law leg to evaluate. It should be a
	 *                          {@link TargetGroundPointing} targeting a given
	 *                          {@link Site}, and we are going to measure the angle
	 *                          of incidence of the {@link Site}with respect to
	 *                          nadir direction at the middle date of the
	 *                          observation {@link AttitudeLawLeg}.
	 * @return The angle of incidence at the middle date of the input law, as a
	 *         double.
	 * @throws PatriusException If an error occurs during the propagation.
	 */
	private double getEffectiveIncidence(Site site, AttitudeLawLeg observationLawLeg) throws PatriusException {
		// Getting the middleDate, IE the date of the middle of the observation that we
		// are going to use to check the incidence angle.
		final AbsoluteDate middleDate = observationLawLeg.getTimeInterval().getMiddleDate();

		// Creating a new propagator to compute the satellite's pv coordinates
		final KeplerianPropagator propagator = createDefaultPropagator();

		// Calculating the satellite PVCoordinates at middleDate
		final PVCoordinates satPv = propagator.getPVCoordinates(middleDate, eme2000);

		// Calculating the Site PVCoordinates at middleDate
		final TopocentricFrame siteFrame = new TopocentricFrame(earth, site.getPoint(), site.getName());
		final PVCoordinates sitePv = siteFrame.getPVCoordinates(middleDate, eme2000);

		// Calculating the normalized site-sat vector at middleDate
		final Vector3D siteSatVectorEme2000 = satPv.getPosition().subtract(sitePv.getPosition()).normalize();

		// Calculating the vector normal to the surface at the Site at middleDate
		final Vector3D siteNormalVectorEarthFrame = siteFrame.getZenith();
		final Transform earth2Eme2000 = siteFrame.getParentShape().getBodyFrame().getTransformTo(eme2000, middleDate);
		final Vector3D siteNormalVectorEme2000 = earth2Eme2000.transformPosition(siteNormalVectorEarthFrame);

		// Finally, we can compute the incidence angle = angle between
		// siteNormalVectorEme2000 and siteSatVectorEme2000
		final double incidenceAngle = Vector3D.angle(siteNormalVectorEme2000, siteSatVectorEme2000);

		logger.info("Site : " + site.getName() + " " + site.getPoint().toString());
		logger.info("Incidence angle rad : " + incidenceAngle);
		logger.info("Incidence angle deg : " + MathLib.toDegrees(incidenceAngle));

		return incidenceAngle;

	}

	/**
	 * [DO NOT MODIFY THIS METHOD]
	 * 
	 * Compute the final score from the observation plan.
	 * 
	 * <p>
	 * Note : the observation plan should have unique sites.<br>
	 * The duplicated elements won't be considered for the score (target with no
	 * value = lost opportunity)
	 * </p>
	 * 
	 * @param observationPlan Input plan to evaluate.
	 * 
	 * @return the final score
	 * @throws PatriusException If an error occurs when calling
	 *                          getEffectiveIncidence().
	 */
	public double computeFinalScore(final Map<Site, AttitudeLawLeg> observationPlan) throws PatriusException {
		logger.info("==== Evaluation of the final score ====");
		// Extract all the sites from the observation plan
		final Set<Site> sitesSet = observationPlan.keySet();

		// Loop over each site to evaluate its quality factor and add it to the final
		// score
		double finalScore = 0.;
		for (final Site site : sitesSet) {
			final AttitudeLawLeg obsLawLeg = observationPlan.get(site);
			// The quality factor is the cosine of the effective incidence angle
			double qualityFactor = MathLib.cos(getEffectiveIncidence(site, obsLawLeg));
			finalScore += qualityFactor * site.getScore();
			logger.info(" => Contribution of " + site.getName() + " : " + String.valueOf(site.getScore()) + "*"
					+ String.valueOf(qualityFactor) + " = " + String.valueOf(site.getScore() * qualityFactor));
		}

		return finalScore;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * @return the eme2000
	 */
	public Frame getEme2000() {
		return this.eme2000;
	}

	/**
	 * @return the tai
	 */
	public TimeScale getTai() {
		return this.tai;
	}

	/**
	 * @return the earth
	 */
	public ExtendedOneAxisEllipsoid getEarth() {
		return this.earth;
	}

	/**
	 * @return the sun
	 */
	public CelestialBody getSun() {
		return this.sun;
	}

	/**
	 * @return the startDate
	 */
	public AbsoluteDate getStartDate() {
		return this.startDate;
	}

	/**
	 * @return the endDate
	 */
	public AbsoluteDate getEndDate() {
		return this.endDate;
	}

	/**
	 * @return the satellite
	 */
	public Satellite getSatellite() {
		return this.satellite;
	}

	/**
	 * @return the siteList
	 */
	public List<Site> getSiteList() {
		return this.siteList;
	}

	@Override
	public String toString() {
		return "CompleteMission [name=" + this.getName() + ", startDate=" + this.getStartDate() + ", endDate="
				+ this.getEndDate() + ", satellite=" + this.getSatellite() + "]";
	}

}

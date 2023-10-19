package progmission;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;

import fr.cnes.sirius.patrius.assembly.models.SensorModel;
import fr.cnes.sirius.patrius.assembly.Assembly;
import fr.cnes.sirius.patrius.attitudes.Attitude;
import fr.cnes.sirius.patrius.attitudes.AttitudeLaw;
import fr.cnes.sirius.patrius.attitudes.AttitudeLawLeg;
import fr.cnes.sirius.patrius.attitudes.AttitudeLeg;
import fr.cnes.sirius.patrius.attitudes.AttitudeProvider;
import fr.cnes.sirius.patrius.attitudes.ConstantSpinSlew;
import fr.cnes.sirius.patrius.attitudes.StrictAttitudeLegsSequence;
import fr.cnes.sirius.patrius.events.CodedEvent;
import fr.cnes.sirius.patrius.events.CodedEventsLogger;
import fr.cnes.sirius.patrius.events.GenericCodingEventDetector;
import fr.cnes.sirius.patrius.events.Phenomenon;
import fr.cnes.sirius.patrius.events.postprocessing.AndCriterion;
import fr.cnes.sirius.patrius.events.postprocessing.ElementTypeFilter;
import fr.cnes.sirius.patrius.events.postprocessing.Timeline;
import fr.cnes.sirius.patrius.events.sensor.SensorVisibilityDetector;
import fr.cnes.sirius.patrius.frames.FramesFactory;
import fr.cnes.sirius.patrius.frames.TopocentricFrame;
import fr.cnes.sirius.patrius.orbits.pvcoordinates.PVCoordinatesProvider;
import fr.cnes.sirius.patrius.propagation.analytical.KeplerianPropagator;
import fr.cnes.sirius.patrius.propagation.events.ConstantRadiusProvider;
import fr.cnes.sirius.patrius.propagation.events.EventDetector;
import fr.cnes.sirius.patrius.propagation.events.EventDetector.Action;
import fr.cnes.sirius.patrius.propagation.events.LocalRadiusProvider;
import fr.cnes.sirius.patrius.propagation.events.ThreeBodiesAngleDetector;
import fr.cnes.sirius.patrius.time.AbsoluteDate;
import fr.cnes.sirius.patrius.time.AbsoluteDateInterval;
import fr.cnes.sirius.patrius.time.AbsoluteDateIntervalsList;
import fr.cnes.sirius.patrius.utils.exception.PatriusException;
import reader.Site;
import utils.ConstantsBE;
import utils.LogUtils;
import utils.ProjectUtils;

/**
 * This class implements the context of an Earth Observation mission.
 *
 * @author herberl
 */
public class CompleteMission extends SimpleMission {

	/**
	 * Logger for this class.
	 */
	private final  Logger logger = LogUtils.GLOBAL_LOGGER;
	
	/**
	 * Maximum checking interval (s) for the event detection during the orbit
	 * propagation.
	 */
	public static final double MAXCHECK_EVENTS = 120.0;

	/**
	 * Default convergence threshold (s) for the event computation during the orbit
	 * propagation.
	 */
	public static final double TRESHOLD_EVENTS = 1.e-4;

	/**
	 * This {@link Map} will be used to enumerate each site access {@link Timeline},
	 * that is to say a {@link Timeline} with access windows respecting all
	 * observation conditions. This object corresponds to the access plan, which
	 * will be computed in the computeAccessPlan() method.
	 */
	private final Map<Site, Timeline> accessPlan;

	/**
	 * This {@link Map} will be used to enumerate each site's programmed
	 * observation. We suggest to use an {@link AttitudeLawLeg} to encapsulate the
	 * guidance law of each observation. This object corresponds to the observation
	 * plan, which will be computed in the computeObservationPlan() method.
	 */
	private final Map<Site, AttitudeLawLeg> observationPlan;

	/**
	 * {@link StrictAttitudeLegsSequence} representing the cinematic plan during the
	 * whole mission horizon. Each {@link AttitudeLeg} corresponds to a diffrent
	 * attitude law : either nadir pointing, target pointing or a slew between two
	 * laws. This object corresponds to the cinematic plan, which will be computed
	 * in the computeCinematicPlan() method.
	 */
	private final StrictAttitudeLegsSequence<AttitudeLeg> cinematicPlan;

	/**
	 * Constructor for the {@link CompleteMission} class.
	 *
	 * @param missionName   Name of the mission
	 * @param numberOfSites Number of target {@link Site} to consider, please give a
	 *                      number between 1 and 100.
	 * @throws PatriusException      Can be raised by Patrius when building
	 *                               particular objects. Here it comes from
	 *                               {@link FramesFactory}
	 * @throws IllegalStateException if the mission horizon is too short
	 */
	public CompleteMission(final String missionName, int numberOfSites) throws PatriusException {

		// Since this class extends the SimpleMission class, we need to use the super
		// constructor to instantiate our instance of CompleteMission. All the
		// attributes of the super class will be instantiated during this step.
		super(missionName, numberOfSites);

		// Initialize the mission plans with empty maps. You will fill those HashMaps in
		// the "compute****Plan()" methods.
		this.accessPlan = new HashMap<>();
		this.observationPlan = new HashMap<>();
		this.cinematicPlan = new StrictAttitudeLegsSequence<>();

	}

	/**
	 * [COMPLETE THIS METHOD TO ACHIEVE YOUR PROJECT]
	 * 
	 * Compute the access plan.
	 * 
	 * Reminder : the access plan corresponds to the object gathering all the
	 * opportunities of access for all the sites of interest during the mission
	 * horizon. One opportunity of access is defined by an access window (an
	 * interval of time during which the satellite can observe the target and during
	 * which all the observation conditions are achieved : visibility, incidence
	 * angle, illumination of the scene,etc.). Here, we suggest you use the Patrius
	 * class {@link Timeline} to encapsulate all the access windows of each site of
	 * interest. One access window will then be described by the {@link Phenomenon}
	 * object, itself defined by two {@link CodedEvent} objects giving the start and
	 * end of the access window. Please find more tips and help in the submethods of
	 * this method.
	 * 
	 * @return the sites access plan with one {@link Timeline} per {@link Site}
	 * @throws PatriusException If a {@link PatriusException} occurs during the
	 *                          computations
	 */
	public Map<Site, Timeline> computeAccessPlan() throws PatriusException {
		/**
		 * Here you need to compute one access Timeline per target Site. You can start
		 * with only one site and then try to compute all of them.
		 * 
		 * Note : when computing all the sites, try to make sure you don't decrease the
		 * performance of the code too much. You might have some modifications to do in
		 * order to ensure a reasonable time of execution.
		 */
		/*
		 * We give a very basic example of incomplete code computing the first target
		 * site access Timeline and adding it to the accessPlan.
		 * 
		 * Please complete the code below.
		 */
	    //final Site targetSite = this.getSiteList().get(0);
	    
	    for (Site targetSite : this.getSiteList())
	    {
		    final Timeline siteAccessTimeline = createSiteAccessTimeline(targetSite);
			this.accessPlan.put(targetSite, siteAccessTimeline);
			ProjectUtils.printTimeline(siteAccessTimeline);
	    }
		
		return this.accessPlan;
	}

	/**
	 * [COMPLETE THIS METHOD TO ACHIEVE YOUR PROJECT]
	 * 
	 * Compute the observation plan.
	 * 
	 * Reminder : the observation plan corresponds to the sequence of observations
	 * programmed for the satellite during the mission horizon. Each observation is
	 * defined by an observation window (start date; end date defining an
	 * {@link AbsoluteDateInterval}), a target (target {@link Site}) and an
	 * {@link AttitudeLawLeg} giving the attitude guidance to observe the target.
	 * 
	 * @return the sites observation plan with one {@link AttitudeLawLeg} per
	 *         {@link Site}
	 * @throws PatriusException If a {@link PatriusException} occurs during the
	 *                          computations
	 */
	public Map<Site, AttitudeLawLeg> computeObservationPlan() throws PatriusException {
		/**
		 * Here are the big constraints and informations you need to build an
		 * observation plan.
		 * 
		 * Reminder : we can perform only one observation per site of interest during
		 * the mission horizon.
		 * 
		 * Objective : Now we have our access plan, listing for each Site all the access
		 * windows. There might be up to one access window per orbit pass above each
		 * site, so we have to decide for each Site which access window will be used to
		 * achieve the observation of the Site. Then, during one access window, we have
		 * to decide when precisely we perform the observation, which lasts a constant
		 * duration which is much smaller than the access window itself (see
		 * ConstantsBE.INTEGRATION_TIME for the duration of one observation). Finally,
		 * we must respect the cinematic constraint : using the
		 * Satellite#computeSlewDuration() method, we need to ensure that the
		 * theoritical duration of the slew between two consecutive observations is
		 * always smaller than the actual duration between those consecutive
		 * observations. Same goes for the slew between a Nadir pointing law and another
		 * poiting law. Of course, we cannot point two targets at once, so we cannot
		 * perform two observations during the same AbsoluteDateInterval !
		 * 
		 * Tip 1 : Here you can use the greedy algorithm presented in class, or any
		 * method you want. You just have to ensure that all constraints are respected.
		 * This is a non linear, complex optimization problem (scheduling problem), so
		 * there is no universal answer. Even if you don't manage to build an optimal
		 * plan, try to code a suboptimal algorithm anyway, we will value any idea you
		 * have. For example, try with a plan where you have only one observation per
		 * satellite pass over France. With that kind of plan, you make sure all
		 * cinematic constraint are respected (no slew to fast for the satellite
		 * agility) and you have a basic plan to use to build your cinematic plan and
		 * validate with VTS visualization.
		 * 
		 * Tip 2 : We provide the observation plan format : a Map of AttitudeLawLeg. In
		 * doing so, we give you the structure that you must obtain in order to go
		 * further. If you check the Javadoc of the AttitudeLawLeg class, you see that
		 * you have two inputs. First, you must provide a specific interval of time that
		 * you have to chose inside one of the access windows of your access plan. Then,
		 * we give you which law to use for observation legs : TargetGroundPointing.
		 * 
		 */
		/*
		 * We provide a basic and incomplete code that you can use to compute the
		 * observation plan.
		 * 
		 * Here the only thing we do is printing all the access opportunities using the
		 * Timeline objects. We get a list of AbsoluteDateInterval from the Timelines,
		 * which is the basis of the creation of AttitudeLawLeg objects since you need
		 * an AbsoluteDateInterval or two AbsoluteDates to do it.
		 */
		for (final Entry<Site, Timeline> entry : this.accessPlan.entrySet()) {
			// Scrolling through the entries of the accessPlan
			// Getting the target Site
			final Site target = entry.getKey();
			logger.info("Current target site : " + target.getName());
			// Getting its access Timeline
			final Timeline timeline = entry.getValue();
			// Getting the access intervals
			final AbsoluteDateIntervalsList accessIntervals = new AbsoluteDateIntervalsList();
			for (final Phenomenon accessWindow : timeline.getPhenomenaList()) {
				// The Phenomena are sorted chronologically so the accessIntervals List is too
			    final AbsoluteDateInterval accessInterval = accessWindow.getTimespan();
				accessIntervals.add(accessInterval);
				logger.info(accessInterval.toString());

				// Use this method to create your observation leg, see more help inside the
				// method.
				final AttitudeLaw observationLaw = createObservationLaw(target);

				/**
				 * Now that you have your observation law, you can compute at any AbsoluteDate
				 * the Attitude of your Satellite pointing the target (using the getAttitude()
				 * method). You can use those Attitudes to compute the duration of a slew from
				 * one Attitude to another, for example the duration of the slew from the
				 * Attitude at the end of an observation to the Atittude at the start of the
				 * next one. That's how you will be able to choose a valid AbsoluteDateInterval
				 * during which the observation will actually be performed, lasting
				 * ConstantsBE.INTEGRATION_TIME seconds. When you have your observation
				 * interval, you can build an AttitudeLawLeg using the observationLaw and this
				 * interval and finally add this leg to the observation plan.
				 */
				/*
				 * Here is an example of how to compute an Attitude. You need a
				 * PVCoordinatePropagator (which we provide we the method
				 * SimpleMission#createDefaultPropagator()), an AbsoluteDate and a Frame (which
				 * we provide with this.getEME2000()).
				 */
				// Getting the begining/end of the accessIntervall as AbsoluteDate objects
				final AbsoluteDate date1 = accessInterval.getLowerData();
				final AbsoluteDate date2 = accessInterval.getUpperData();
				final Attitude attitude1 = observationLaw.getAttitude(this.createDefaultPropagator(), date1,
						this.getEme2000());
				final Attitude attitude2 = observationLaw.getAttitude(this.createDefaultPropagator(), date2,
						this.getEme2000());
				/*
				 * Now here is an example of code showing how to compute the duration of the
				 * slew from attitude1 to attitude2 Here we compare two Attitudes coming from
				 * the same AttitudeLaw which is a TargetGroundPointing so the
				 */
				final double slew12Duration = this.getSatellite().computeSlewDuration(attitude1, attitude2);
				logger.info("Maximum possible duration of the slew : " + slew12Duration);
				final double actualDuration = date2.durationFrom(date1);
				logger.info("Actual duration of the slew : " + actualDuration);
				/**
				 * Of course, here the actual duration is less than the maximum possible
				 * duration because the TargetGroundPointing mode is a very slow one and the
				 * Satellite is very agile. But sometimes when trying to perform a slew from one
				 * target to another, you will find that the Satellite doesn't have enough time,
				 * then you need to either translate one of the observations or just don't
				 * perform one of the observation.
				 */

				/**
				 * Let's say after comparing several observation slews, you find a valid couple
				 * of dates defining your observation window : {obsStart;obsEnd}, with
				 * obsEnd.durationFrom(obsStart) == ConstantsBE.INTEGRATION_TIME.
				 * 
				 * Then you can use those dates to create your AtittudeLawLeg that you will
				 * insert inside the observaiton pla, for this target. Reminder : only one
				 * observation in the observation plan per target !
				 * 
				 * WARNING : what we do here doesn't work, we didn't check that there wasn't
				 * another target observed while inserting this target observation, it's up to
				 * you to build your observation plan using the methods and tips we provide. You
				 * can also only insert one observation for each pass of the satellite and it's
				 * fine.
				 */
				// Here we use the middle of the accessInterval to define our dates of
				// observation
				final AbsoluteDate middleDate = accessInterval.getMiddleDate();
				final AbsoluteDate obsStart = middleDate.shiftedBy(-ConstantsBE.INTEGRATION_TIME / 2);
				final AbsoluteDate obsEnd = middleDate.shiftedBy(ConstantsBE.INTEGRATION_TIME / 2);
				final AbsoluteDateInterval obsInterval = new AbsoluteDateInterval(obsStart, obsEnd);
				// Then, we create our AttitudeLawLeg, that we name using the name of the target
				final String legName = "OBS_" + target.getName();
				final AttitudeLawLeg obsLeg = new AttitudeLawLeg(observationLaw, obsInterval, legName);

				// Finally, we add our leg to the plan
				this.observationPlan.put(target, obsLeg);

			}

		}

		return this.observationPlan;
	}

	/**
	 * [COMPLETE THIS METHOD TO ACHIEVE YOUR PROJECT]
	 * 
	 * Computes the cinematic plan.
	 * 
	 * Here you need to compute the cinematic plan, which is the cinematic chain of
	 * attitude law legs (observation, default law and slews) needed to perform the
	 * mission. Usually, we start and end the mission in default law and during the
	 * horizon, we alternate between default law, observation legs and slew legs.
	 * 
	 * @return a {@link StrictAttitudeLegsSequence} that gives all the cinematic
	 *         plan of the {@link Satellite}. It is a chronological sequence of all
	 *         the {@link AttitudeLawLeg} that are necessary to define the
	 *         {@link Attitude} of the {@link Satellite} during all the mission
	 *         horizon. Those legs can have 3 natures : pointing a target site,
	 *         pointing nadir and performing a slew between one of the two previous
	 *         kind of legs.
	 * @throws PatriusException
	 */
	public StrictAttitudeLegsSequence<AttitudeLeg> computeCinematicPlan() throws PatriusException {

		/**
		 * Now we want to assemble a continuous attitude law which is valid during all
		 * the mission horizon. For that, we will use to object
		 * StrictAttitudeLegsSequence<AttitudeLeg> which is a chronological sequence of
		 * AttitudeLeg. In our case, each AttitudeLeg will be an AttitudeLawLeg, either
		 * a leg of site observation, a slew, or the nadir pointing attitude law (see
		 * the Satellite constructor and the BodyCenterGroundPointing class, it's the
		 * Satellite default attitude law). For more help about the Attitude handling,
		 * use the module 11 of the patrius formation.
		 * 
		 * Tip 1 : Please give names to the different AttitudeLawLeg you build so that
		 * you can visualize them with VTS later on. For example "OBS_Paris" when
		 * observing Paris or "SlEW_Paris_Lyon" when adding a slew from Paris
		 * observation AttitudeLawLeg to Lyon observation AttitudeLawLeg.
		 * 
		 * Tip 2 : the sequence you want to obtain should look like this :
		 * [nadir-slew-obs1-slew-obs2-slew-obs3-slew-nadir] for the simple version where
		 * you don't try to fit nadir laws between observations or
		 * [nadir-slew-obs1-slew-nadir-selw-obs2-slew-obs3-slew-nadir] for the more
		 * complexe version with nadir laws if the slew during two observation is long
		 * enough.
		 * 
		 * Tip 3 : You can use the class ConstantSpinSlew(initialAttitude,
		 * finalAttitude, slewName) for the slews. This an AtittudeLeg so you will be
		 * able to add it to the StrictAttitudeLegsSequence as every other leg.
		 */

		/*
		 * Example of code using our observation plan, let's say we only have one obs
		 * pointing Paris.
		 * 
		 * Then we are going to create a very basic cinematic plan : nadir law => slew
		 * => obsParis => slew => nadir law
		 * 
		 * To do that, we need to compute the slew duration from the end of nadir law to
		 * the begining of Paris obs and then from the end of Paris obs to the begining
		 * of nadir law. For that, we use the Satellite#computeSlewDurationMethod() as
		 * before. We know we have to the time to perform the slew thanks to the
		 * cinematic checks we already did during the observation plan computation.
		 */
		// Getting the Paris Site
	    final Site paris = this.getSiteList().get(0);
		// Getting the associated observation leg defined previously
	    final AttitudeLeg parisObsLeg = observationPlan.get(paris);

		// Getting our nadir law
	    final AttitudeLaw nadirLaw = this.getSatellite().getDefaultAttitudeLaw();

		// Getting all the dates we need to compute our slews
	    final AbsoluteDate start = this.getStartDate();
	    final AbsoluteDate end = this.getEndDate();
	    final AbsoluteDate obsStart = parisObsLeg.getDate();
	    final AbsoluteDate obsEnd = parisObsLeg.getEnd();

		// For the slew nadir => paris and paris => nadir, we will use the maximum
		// duration because we have a lot of time here. In practice, you will use either
		// the maximum possible time if you have nothing else planned around or the
		// available time coming from the duration until next observation programmed.
	    final AbsoluteDate endNadirLaw1 = obsStart.shiftedBy(-getSatellite().getMaxSlewDuration());
	    final AbsoluteDate startNadirLaw2 = obsEnd.shiftedBy(+getSatellite().getMaxSlewDuration());

		// The propagator will be used to compute Attitudes
	    final KeplerianPropagator propagator = this.createDefaultPropagator();

		// Computing the Attitudes used to compute the slews
	    final Attitude startObsAttitude = parisObsLeg.getAttitude(propagator, obsStart, getEme2000());
	    final Attitude endObsAttitude = parisObsLeg.getAttitude(propagator, obsEnd, getEme2000());
	    final Attitude endNadir1Attitude = nadirLaw.getAttitude(propagator, endNadirLaw1, getEme2000());
	    final Attitude startNadir2Attitude = nadirLaw.getAttitude(propagator, startNadirLaw2, getEme2000());

		// Finally computing the slews
		// From nadir law 1 to Paris observation
	    final ConstantSpinSlew slew1 = new ConstantSpinSlew(endNadir1Attitude, startObsAttitude, "Slew_Nadir_to_Paris");
		// From Paris observation to nadir law 2
	    final ConstantSpinSlew slew2 = new ConstantSpinSlew(endObsAttitude, startNadir2Attitude, "Slew_Paris_to_Nadir");

		// We create our two Nadir legs using the dates we computed
	    final AttitudeLawLeg nadir1 = new AttitudeLawLeg(nadirLaw, start, endNadirLaw1, "Nadir_Law_1");
	    final AttitudeLawLeg nadir2 = new AttitudeLawLeg(nadirLaw, startNadirLaw2, end, "Nadir_Law_2");

		// Finally we can add all those legs to our cinametic plan, in the chronological
		// order
		this.cinematicPlan.add(nadir1);
		this.cinematicPlan.add(slew1);
		this.cinematicPlan.add(parisObsLeg);
		this.cinematicPlan.add(slew2);
		this.cinematicPlan.add(nadir2);

		/**
		 * Now your job is finished, the two following methods will finish the job for
		 * you : checkCinematicPlan() will check that each slew's duration is longer
		 * than the theoritical duration it takes to perform the same slew. Then, if the
		 * cinematic plan is valid, computeFinalScore() will compute the score of your
		 * observation plan. Finaly, generateVTSVisualization will write all the
		 * ephemeris (Position/Velocity + Attitude) and generate a VTS simulation that
		 * you will be able to play to visualize and validate your plans.
		 */
		return this.cinematicPlan;
	}

	/**
	 * This method should compute the input {@link Site}'s access {@link Timeline}.
	 * That is to say the {@link Timeline} which contains all the {@link Phenomenon}
	 * respecting the access conditions for this site : good visibility + corrrect
	 * illumination of the {@link Site}.
	 * 
	 * For that, we suggest you create as many {@link Timeline} as you need and
	 * combine them with logical gates to filter only the access windows phenomenon.
	 * 
	 * @param targetSite Input target {@link Site}
	 * @return The {@link Timeline} of all the access {@link Phenomenon} for the
	 *         input {@link Site}.
	 * @throws PatriusException If a {@link PatriusException} occurs.
	 */
	private Timeline createSiteAccessTimeline(Site targetSite) throws PatriusException {
		
		 // We create and add the THREE detectors to the propagator of the satellite
		CodedEventsLogger eventVisibilityLogger   = addVisibilityDetector(targetSite, this.getSatellite().getPropagator());
		CodedEventsLogger eventIlluminationLogger = addIlluminationDetector(targetSite, this.getSatellite().getPropagator());
		CodedEventsLogger eventDazzlingLogger     = addDazzlingDetector(targetSite, this.getSatellite().getPropagator());
		
		// Perform propagation
		this.getSatellite().getPropagator().propagate(this.getStartDate(), this.getEndDate());
		
		// Flush the logged events of the 3 detectors in a timeline
		final Timeline timelineV = createSiteConstraintTimeline(targetSite, eventVisibilityLogger);
		final Timeline timelineI = createSiteConstraintTimeline(targetSite, eventIlluminationLogger);
	    final Timeline timelineD = createSiteConstraintTimeline(targetSite, eventDazzlingLogger);

		/**
		 * Step 2 :
		 * 
		 * Combine the timelines with logical gates and retrieve only the access
		 * conditions through a refined Timeline object.
		 * 
		 * For that, you can use the classes in the events.postprocessing module : for
		 * example, the AndCriterion or the NotCriterion.
		 * 
		 * Finally, you can filter only the Phenomenon matching a certain condition
		 * using the ElementTypeFilter
		 */
		/*

		 * Code your logical operations on Timeline objects and filter only the access
		 * Phenomenon (gathering all constraints you need to define an access condition)
		 * below.
		 */
	    
		// Combining all Timelines
		// Creating a global Timeline containing all phenomena, this Timeline will be
		// filtered and processed to that only the access Phenomennon remain, this is
		// our siteAccessTimeline
	    
		final Timeline siteAccessTimeline = new Timeline(
				new AbsoluteDateInterval(this.getStartDate(), this.getEndDate()));
		// Adding the phenomena of all the considered timelines
		// Visibility
		for (final Phenomenon phenom : timelineV.getPhenomenaList()) {
			siteAccessTimeline.addPhenomenon(phenom);
		}
		// Sun Incidence
		for (final Phenomenon phenom : timelineI.getPhenomenaList()) {
			siteAccessTimeline.addPhenomenon(phenom);
		}
		// Dazzling
		for (final Phenomenon phenom : timelineD.getPhenomenaList()) {
			siteAccessTimeline.addPhenomenon(phenom);
		}

		// Define AND criteria to get phenomena where VIS, ILL and DAZ all occurs.
		// Those intersections are grouped under the label ACCESS
		final AndCriterion andCriterionVisIll = new AndCriterion("VIS", "ILL", "VISxILL", "When the site is visible and illuminated");
		final AndCriterion andCriterionAccess = new AndCriterion("VISxILL", "DAZ", "ACCESS", "When the site is accessible");
		// Applying our criterion adds all the new phenomena VILxILL and ACCESS inside the global timeline
		andCriterionVisIll.applyTo(siteAccessTimeline);
		andCriterionAccess.applyTo(siteAccessTimeline);

		// Then create an ElementTypeFilter that will filter all phenomenon not
		// respecting the input condition you gave it
		// Create a filter to keep only ACCESS phenomena in the timeline
		final ElementTypeFilter obsConditionFilter = new ElementTypeFilter("ACCESS", false);
		// Apply the filter to the timeline
		obsConditionFilter.applyTo(siteAccessTimeline);

		/*
		 * Now make sure your globalTimeline represents the access Timeline for the
		 * input target Site and it's done ! You can print the Timeline using the
		 * utility module of the BE as below
		 */

	    
		// Log the final access timeline associated to the current target
		logger.info("\n" + targetSite.getName());
//		ProjectUtils.printTimeline(siteAccessTimeline);

		return siteAccessTimeline;
	}

	/**
	 * This method should compute a {@link Timeline} object which encapsulates all
	 * the {@link Phenomenon} corresponding to the visibility of the input target {@link Site}.
	 * 
	 * @param targetSite Input target {@link Site}
	 * @param eventVisibilityLogger The event logger to flush into the timeline
	 * @return The {@link Timeline} containing all the {@link Phenomenon} relative
	 *         to the visibility.
	 * @throws PatriusException If a {@link PatriusException} occurs when creating
	 *                          the {@link Timeline}.
	 */
	private Timeline createSiteConstraintTimeline(Site targetSite, CodedEventsLogger eventLogger ) throws PatriusException {
		/**
		 * Here is a quick idea of how to compute a Timeline. A Timeline contains a
		 * PhenomenaList, which is list of Phenomenon objects. Each Phenomenon object
		 * represents an phenomenon in orbit which is defined between two AbsoluteDate
		 * objects and their associated CodedEvent which define the begin and the end of
		 * the Phenomenon. For example, the Sun visibility can be defined as a
		 * phenomenon beginning with the start of visibility and ending with the end of
		 * visibility, itself defined using geometrical rules.
		 * 
		 * Now, how to create a Phenomenon object matching the requirement of a given
		 * orbital phenomenon.
		 * 
		 * For that, you can use Patrius possibilities with the
		 * "fr.cnes.sirius.patrius.propagation.events", "fr.cnes.sirius.patrius.events",
		 * "fr.cnes.sirius.patrius.events.sensor" and the
		 * "fr.cnes.sirius.patrius.events.postprocessing" modules. See the modules 05
		 * and 09 of the Patrius formation for those aspects, you have examples of codes
		 * using those modules and how to build a Timeline derived from other objects in
		 * a representative case.
		 * 
		 * Below are some basic steps and tips to help you search for the right
		 * informations in Javadoc and in the Patrius formation in order to compute your
		 * Timeline.
		 * 
		 */

		/**
		 * Step 1 :
		 * 
		 * Here we deal with event detection. As explain in the module 05, this is done
		 * with EventDetector objects. If you look at the Javadoc, you'll find you all
		 * sorts of detectors. You need to translate the X input constraint (for example
		 * an incidence angle between the sensor and the target, sun incidence angle,
		 * masking of the target by the Earth, etc.) into an EventDetector object.
		 * Scroll through the event detection modules to find the one adapted to your
		 * problem (represented by the X constraint which describe the X phenomenon you
		 * want to detect) and then look at the inputs you need to build it.
		 * 
		 * Please note that in order to facilitate the task for you, we provide the
		 * object Satellite. If you look how the constructor build this object, you will
		 * find that our Satellite already has an Assembly filed with a lot of
		 * properties. Among those properties, there is a SensorProperty that you can
		 * use to your advantage when trying to build you detector (for example when
		 * trying to build a visibility detector). See the module 7 of the formation to
		 * learn more about the Assembly object. You can use the SensorProperty via the
		 * Assembly of the Satellite and its name to define appropriate detectors.
		 * 
		 */
		/*
		 * Complete the method below to build your detector. More indications are given
		 * in the method.
		 */
		
		// Moved to addXDetector() methods for optimization...
//	    final EventDetector constraintVisibilityDetector = createConstraintVisibilityDetector(targetSite);

		/**
		 * Step 2 :
		 * 
		 * When you have your detector, you can add it on an Orbit Propagator such as
		 * the KeplerianPropagator of your Satellite. If you give the detector the right
		 * parameters, you can then propagate the orbit (see the SimpleMission code and
		 * the module 03 from the Patrius formation) and the detector will automatically
		 * perform actions when a particular orbital event happens (you need to
		 * configure the right detector to detect the event you want to monitor).
		 * 
		 * You can add several detectors to the propagator (one per constraint per Site
		 * for example).
		 */
		/*
		 * This is how you add a detector to a propagator, feel free to add several
		 * detectors to the satellite propagator !
		 */
		
		// Moved to addXDetector() methods for optimization...
//		this.getSatellite().getPropagator().addEventDetector(constraintVisibilityDetector);

		/**
		 * Step 3 :
		 * 
		 * Now you need to use the detector's ability to create CodedEvent objects to
		 * actually detect the events and visualize them. You can obtain CodedEvents
		 * with a CodedEventsLogger that you plug on an EventDetector with the
		 * CodedEventsLogger.monitorDetector() method. For that, you will need the
		 * GenericCodingEventDetector class. See the module 09 to understand how to use
		 * those objects in order to detect events.
		 */
		/*
		 * Develop the code in which you create your GenericCodingEventDetector and use
		 * it to create a CodedEventsLogger here. You have some example code to help.
		 */
		
		// Moved to addXDetector() methods for optimization...
//		final GenericCodingEventDetector codingEventVisibilityDetector = new GenericCodingEventDetector(constraintVisibilityDetector,
//				"Event starting the visibility phenomenon", "Event ending the visibility phenomenon", true, "VIS");
//		final CodedEventsLogger eventVisibilityLogger = new CodedEventsLogger();
//		final EventDetector eventVisibilityDetector = eventVisibilityLogger.monitorDetector(codingEventVisibilityDetector);
//		// Then you add your logger to the propagator, it will monitor the event coded
//		// by the codingEventDetector
//		this.getSatellite().getPropagator().addEventDetector(eventVisibilityDetector);

		/**
		 * Step 4 :
		 * 
		 * Now you can propagate your orbit and the propagator will use the added
		 * detectors and loggers the way you defined them, detecting all events you
		 * wanted to monitor.
		 */
		// Finally propagating the orbit
		
		// Moved to createSiteAccessTimeline() methods for optimization...
//		this.getSatellite().getPropagator().propagate(this.getStartDate(), this.getEndDate());
		
		/**
		 * Remark : since you can add as many EventDetectors as you want to an instance
		 * of propagator, you might want to delay this step afterwards to propagate the
		 * orbit with all your detectors at once. Here we do it right now to provide a
		 * clear example but feel free to code your own more optimized version of it.
		 */

		/**
		 * Step 5 : WARNING : this can only be done after the propagation !
		 * 
		 * Now, you have to post process all your events. That's when you actually
		 * create your Timeline object which contains the Phenomenon you want to
		 * monitor.
		 * 
		 * Since you have propagated your orbit, the events that have been detected are
		 * stored inside the detector and logger. This mechanic is used to create a
		 * Timeline.
		 */
		/*
		 * See code below and create your own code to have your X Timeline describing
		 * all X phenomenon you want to detect.
		 */
		// Creating a Timeline to process the events : we are going to define one
		// visibility Phenomenon by couple of events "start -> end" (linked to the
		// increase and decrease of the g function of the visibility detector)
		final Timeline timeline = new Timeline(eventLogger,
				new AbsoluteDateInterval(this.getStartDate(), this.getEndDate()), null);
		
		return timeline;
	}

	/**
	 * Create an adapted instance of {@link EventDetector} matching the input need
	 * for monitoring the events defined by the visibility constraint.
	 * 
	 * @param targetSite Input target {@link Site}
	 * 
	 * @return An {@link EventDetector} answering the visibility constraint
	 */
	private EventDetector createConstraintVisibilityDetector(Site targetSite) {
		/**
		 * Here you build an EventDetector object that corresponds to the constraint X:
		 * visibility of the target from the satellite, target is in day time, whatever.
		 *
		 * Note that when you create a detector, you choose the actions that it will
		 * perform when the target event is detected. See the module 5 for more
		 * informations about this.
		 * 
		 * Visibility: For the visibility detector, you can use a SensorModel. You will
		 * have to add the Earth as a masking body with the method
		 * addMaskingCelestialBody and to set the main target of the SensorModel with
		 * the method setMainTarget. Then, you can use the class
		 * SensorVisibilityDetector with your SensorModel.
		 * 
		 * Sun incidence: For the sun incidence angle detector (illumination
		 * condition), you can use the class ThreeBodiesAngleDetector, the three bodies
		 * being the ground target, the Earth and the Sun. See the inputs of this class
		 * to build the object properly.
		 * 
		 * Dazzling: Your satellite needs to be protected from dazzling. As a good 
		 * approximation, dazzling is avoided if the angle satellite - target - the Sun is 
		 * below the maximum phase angle (90 degrees, see {@link ConstantsBE}). The class
		 * ThreeBodiesAngleDetector is suitable for this condition as well.
		 * 
		 * Tip 1 : When you create the detectors listed above, you can use the two
		 * public final static fields MAXCHECK_EVENTS and TRESHOLD_EVENTS to configure
		 * the detector (those values are often asked in input of the EventDectector
		 * classes. You will also indicate the Action to perform when the detection
		 * occurs, which is Action.CONTINUE.
		 * 
		 * Tip 2 : The Satellite uses the Assembly class to represent its model.
		 * To access this Assembly, you have a getter in the Satellite class. Then, to
		 * access any part of an Assembly, you can call Assembly#getPart(String
		 * partName). The parts name for our Satellite are declared in the Satellite
		 * class.
		 * 
		 * Tip 3 : when you need an object which is an interface (let's say for
		 * example a PVCoordinatesProvider) you have to find a class implementing this
		 * interface and which models what you want to do (here which models the
		 * target's position/coordinates). To find all the classes implementing an
		 * interface : "Right Clic", then "Open Type Hierarchy". For example for a
		 * PVCoordinatesProvider, you have a lot of classes : AbstractCelestialBody if
		 * your target is a planet for example, or any Propagator if you are propagating
		 * the PV of a Target like a satellite, or TopocentricFrame if the target is a
		 * location at the surface of a celestial body, etc.
		 * 
		 */
		/*
		 * Create your detector and return it.
		 */

		//Define the coordinates of the target
		//The target is a location at the surface of a celestial body, so we can use a topocentric frame to track it
		PVCoordinatesProvider targetCoord = new TopocentricFrame(getEarth(), targetSite.getPoint(),targetSite.getName());
		// No radius is provided for the site, so we consider it as a point. We then set it to zero by default
		LocalRadiusProvider siteLocalRadius = new ConstantRadiusProvider(0);
		
		// Retrieve and configure the sensor model of the satellite
		SensorModel sensorModel = new SensorModel(this.getSatellite().getAssembly(), Satellite.SENSOR_NAME);		
		// The sensor model is blinded by the Earth
		sensorModel.addMaskingCelestialBody(getEarth());
		// Set the sensor to point toward the target
		sensorModel.setMainTarget(targetCoord, siteLocalRadius);
		
		// Create the sensor visibility detector, with the sensor the satellite
		SensorVisibilityDetector detector = new SensorVisibilityDetector(
				sensorModel, MAXCHECK_EVENTS, TRESHOLD_EVENTS, Action.CONTINUE, Action.CONTINUE);
		
		return detector;
	}
	
	/**
	 * Create an adapted instance of {@link EventDetector} matching the input need
	 * for monitoring the events defined by the illumination constraint.
	 * 
	 * @param targetSite Input target {@link Site}
	 * 
	 * @return An {@link EventDetector} answering the illumination constraint
	 */
	private EventDetector createConstraintIlluminationDetector(Site targetSite) {

		// Define the coordinates of the target
		// The target is a location at the surface of a celestial body, so we can use a topocentric frame to track it
		PVCoordinatesProvider targetCoord = new TopocentricFrame(getEarth(), targetSite.getPoint(),targetSite.getName());	
		
		// Create a three body detector that triggers when vectors (targetCoord, earth) and (targetCoord, sun) forms an angle of
		// exactly 180 - MAX_SUN_INCIDENCE_ANGLE deg
		ThreeBodiesAngleDetector detector = new ThreeBodiesAngleDetector(
				getEarth(), targetCoord, getSun(),
				Math.toRadians(180 - ConstantsBE.MAX_SUN_INCIDENCE_ANGLE),
				MAXCHECK_EVENTS, TRESHOLD_EVENTS, Action.CONTINUE);	
		
		return detector;
	}
	
	/**
	 * Create an adapted instance of {@link EventDetector} matching the input need
	 * for monitoring the events defined by the dazzling constraint.
	 * 
	 * @param targetSite Input target {@link Site}
	 * 
	 * @return An {@link EventDetector} answering the dazzling constraint
	 */
	private EventDetector createConstraintDazzlingDetector(Site targetSite) {
		

		// Define the coordinates of the target
		// The target is a location at the surface of a celestial body, so we can use a topocentric frame to track it
		PVCoordinatesProvider targetCoord = new TopocentricFrame(getEarth(), targetSite.getPoint(),targetSite.getName());
		
		// Define the coordinates of the satellite
		PVCoordinatesProvider satelliteCoord = this.getSatellite().getInitialOrbit();		
		
		// Create a three body detector that triggers when vectors (targetCoord, satelliteCoord) and (targetCoord, sun) forms an angle of
		// MAX_SUN_PHASE_ANGLE deg
		ThreeBodiesAngleDetector detector = new ThreeBodiesAngleDetector(
				satelliteCoord, targetCoord, getSun(),
				Math.toRadians(ConstantsBE.MAX_SUN_PHASE_ANGLE),
				MAXCHECK_EVENTS, TRESHOLD_EVENTS, Action.CONTINUE);
		
		return detector;
	}
	
	/**
	 * Create the visibility event detector and logger. Add them to the given propagator.
	 *
	 * @param targetSite  The target to monitor
	 * @param propagator The propagator to which the event detector shall be added
	 * @return the CodedEventsLogger created in the process.
	 */
	private CodedEventsLogger addVisibilityDetector(Site targetSite, KeplerianPropagator propagator) {

	    final EventDetector constraintVisibilityDetector = createConstraintVisibilityDetector(targetSite);

	  	final GenericCodingEventDetector codingEventVisibilityDetector = new GenericCodingEventDetector(constraintVisibilityDetector,
	  				"Event starting the visibility phenomenon", "Event ending the visibility phenomenon", true, "VIS");
	  	final CodedEventsLogger eventVisibilityLogger = new CodedEventsLogger();
	  	final EventDetector eventVisibilityDetector = eventVisibilityLogger.monitorDetector(codingEventVisibilityDetector);
	  	this.getSatellite().getPropagator().addEventDetector(eventVisibilityDetector);
	  	
	  	return eventVisibilityLogger;
	}

	/**
	 * Create the illumination event detector and logger. Add them to the given propagator.
	 *
	 * @param targetSite   The target to monitor
	 * @param propagator The propagator to which the event detector shall be added
	 * @return the CodedEventsLogger created in the process.
	 */
	private CodedEventsLogger addIlluminationDetector(Site targetSite, KeplerianPropagator propagator) {

	    final EventDetector constraintIlluminationDetector = createConstraintIlluminationDetector(targetSite);

		final GenericCodingEventDetector codingEventIlluminationDetector = new GenericCodingEventDetector(constraintIlluminationDetector,
				"Event starting the illumination phenomenon", "Event ending the illumination phenomenon", true, "ILL");
		final CodedEventsLogger eventIlluminationLogger = new CodedEventsLogger();
		final EventDetector eventIlluminationDetector = eventIlluminationLogger.monitorDetector(codingEventIlluminationDetector);
		this.getSatellite().getPropagator().addEventDetector(eventIlluminationDetector);
		
		return eventIlluminationLogger;
	}

	/**
	 * Create the dazzling event detector and logger. Add them to the given propagator.
	 *
	 * @param targetSite   The target to monitor
	 * @param propagator The propagator to which the event detector shall be added
	 * @return the CodedEventsLogger created in the process.
	 */
	private CodedEventsLogger addDazzlingDetector(Site targetSite, KeplerianPropagator propagator) {

		final EventDetector constraintDazzlingDetector = createConstraintDazzlingDetector(targetSite);
    	
		final GenericCodingEventDetector codingEventDazzlingDetector = new GenericCodingEventDetector(constraintDazzlingDetector,
				"Event starting the dazzling phenomenon", "Event ending the dazzling phenomenon", false, "DAZ"); // false to invert the timeline
		final CodedEventsLogger eventDazzlingLogger = new CodedEventsLogger();
		final EventDetector eventDazzlingDetector = eventDazzlingLogger.monitorDetector(codingEventDazzlingDetector);
		this.getSatellite().getPropagator().addEventDetector(eventDazzlingDetector);
		
		return eventDazzlingLogger;
	}

	
	/**
	 * [COMPLETE THIS METHOD TO ACHIEVE YOUR PROJECT]
	 * 
	 * Create an observation leg, that is to say an {@link AttitudeLaw} that give
	 * the {@link Attitude} (pointing direction) of the {@link Satellite} in order
	 * to perform the observation of the input target {@link Site}.
	 * 
	 * An {@link AttitudeLaw} is an {@link AttitudeProvider} providing the method
	 * {@link AttitudeProvider#getAttitude()} which can be used to compute the
	 * {@link Attitude} of the {@link Satellite} at any given {@link AbsoluteDate}
	 * (instant) during the mission horizon.
	 * 
	 * An {@link AttitudeLaw} is valid at anu time in theory.
	 * 
	 * @param target Input target {@link Site}
	 * @return An {@link AttitudeLawLeg} adapted to the observation.
	 */
	private AttitudeLaw createObservationLaw(Site target) {
		/**
		 * To perform an observation, the satellite needs to point the target for a
		 * fixed duration.
		 * 
		 * Here, you will use the {@link TargetGroundPointing}. This law provides a the
		 * Attitude of a Satellite that only points one target at the surface of a
		 * BodyShape. The earth object from the SimpleMission is a BodyShape and we
		 * remind you that the Site object has an attribute which is a GeodeticPoint.
		 * Use those informations to your advantage to build a TargetGroundPointing.
		 * 
		 * Note : to avoid unusual behavior of the TargetGroundPointing law, we advise
		 * you use the following constructor : TargetGroundPointing(BodyShape, Vector3D,
		 * Vector3D, Vector3D) specifying the line of sight axis and the normal axis.
		 */
		/*
		 * Complete the code below to create your observation law and return it
		 */
		return null;
	}

	
	
	/**
	 * @return the accessPlan
	 */
	public Map<Site, Timeline> getAccessPlan() {
		return this.accessPlan;
	}

	/**
	 * @return the observationPlan
	 */
	public Map<Site, AttitudeLawLeg> getObservationPlan() {
		return this.observationPlan;
	}

	/**
	 * @return the cinematicPlan
	 */
	public StrictAttitudeLegsSequence<AttitudeLeg> getCinematicPlan() {
		return this.cinematicPlan;
	}

	@Override
	public String toString() {
		return "CompleteMission [name=" + this.getName() + ", startDate=" + this.getStartDate() + ", endDate="
				+ this.getEndDate() + ", satellite=" + this.getSatellite() + "]";
	}
}

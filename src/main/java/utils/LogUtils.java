package utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class handle logging for the BEProgrammationMission project.
 * 
 * Create your logger using : private final Logger myLogger = LogUtils.GLOBAL_LOGGER;
 * 
 * Then, to log something using a certain level of verbosity, you can use : 
 * - myLogger.debug(String msg) for debugging only.
 * - myLogger.info(String msg) to log informations about the execution of the code, results, etc.
 * - myLogger.warning(String msg) to log warning messages. 
 * - myLogger.error(String msg) to log error messages, exceptions.
 * 
 * 
 * @author herberl
 *
 */
public final class LogUtils {
	public static final Logger GLOBAL_LOGGER = LoggerFactory.getLogger("BEProgMissionLogger");
}

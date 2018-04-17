/**
 * 
 */
package za.co.indigocube.rtc.code.importer;

import java.util.concurrent.TimeUnit;

import org.eclipse.core.runtime.IProgressMonitor;

import com.ibm.team.repository.client.ITeamRepository;
import com.ibm.team.repository.common.IContributor;
import com.ibm.team.repository.common.TeamRepositoryException;

/**
 * @author Sudheer
 *
 */
public class RTCCodeImportUtils {
	
	public static IContributor findContributor(ITeamRepository teamRepository, String contributorName, 
			IProgressMonitor monitor) throws TeamRepositoryException {
        return teamRepository.contributorManager().fetchContributorByUserId(contributorName, monitor);
        
	}
	
	public static String formatExecutionTime(long executionTime) {
		long days = TimeUnit.MILLISECONDS.toDays(executionTime);
		executionTime -= TimeUnit.DAYS.toMillis(days);
		long hours = TimeUnit.MILLISECONDS.toHours(executionTime);
		executionTime -= TimeUnit.HOURS.toMillis(hours);
		long minutes = TimeUnit.MILLISECONDS.toMinutes(executionTime);
		executionTime -= TimeUnit.MINUTES.toMillis(minutes);
		long seconds = TimeUnit.MILLISECONDS.toSeconds(executionTime);
		executionTime -= TimeUnit.SECONDS.toMillis(seconds);
		long milliseconds = TimeUnit.MILLISECONDS.toMillis(executionTime);
		
		StringBuilder executionTimeString = new StringBuilder();
		
		if (days > 0) {
			executionTimeString.append(days + "d");
			executionTimeString.append(" ");
		}
		if (hours > 0) {
			executionTimeString.append(hours + "h");
			executionTimeString.append(" ");
		}
		if (minutes > 0) {
			executionTimeString.append(minutes + "m");
			executionTimeString.append(" ");		    	  
		}
		if (seconds > 0) {
			executionTimeString.append(seconds + "s");
			executionTimeString.append(" ");
		}
		if (milliseconds > 0) {
			executionTimeString.append(milliseconds + "ms");
		}
		
		return executionTimeString.toString();
	}

}

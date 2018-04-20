/**
 * 
 */
package za.co.indigocube.rtc.code.importer.workitem;

import java.sql.Timestamp;

import org.eclipse.core.runtime.IProgressMonitor;

import com.ibm.team.foundation.common.text.XMLString;
import com.ibm.team.links.client.ILinkManager;
import com.ibm.team.process.common.IDevelopmentLine;
import com.ibm.team.process.common.IDevelopmentLineHandle;
import com.ibm.team.process.common.IIterationHandle;
import com.ibm.team.process.common.IProjectArea;
import com.ibm.team.repository.client.IItemManager;
import com.ibm.team.repository.client.ITeamRepository;
import com.ibm.team.repository.common.IContributorHandle;
import com.ibm.team.repository.common.TeamRepositoryException;
import com.ibm.team.workitem.client.IAuditableClient;
import com.ibm.team.workitem.client.IWorkItemClient;
import com.ibm.team.workitem.client.WorkItemOperation;
import com.ibm.team.workitem.client.WorkItemWorkingCopy;
import com.ibm.team.workitem.common.IWorkItemCommon;
import com.ibm.team.workitem.common.model.ICategoryHandle;
import com.ibm.team.workitem.common.model.IWorkItem;

/**
 * @author Sudheer
 *
 */
public class WorkItemUtils {
	
	public static IWorkItemCommon getWorkItemCommon(ITeamRepository teamRepository) {
		return (IWorkItemCommon) teamRepository.getClientLibrary(IWorkItemCommon.class);
	}
	
	public static IWorkItemClient getWorkItemClient(ITeamRepository teamRepository) {
		return (IWorkItemClient) teamRepository.getClientLibrary(IWorkItemClient.class);
	}
	
	public static IAuditableClient getAuditableClient(ITeamRepository teamRepository) {
		return (IAuditableClient) teamRepository.getClientLibrary(IAuditableClient.class);
	}
	
	public static ILinkManager getLinkManager(ITeamRepository teamRepository) {
		return (ILinkManager) teamRepository.getClientLibrary(ILinkManager.class);
	}
	
	public static IDevelopmentLine findDevelopmentLine(ITeamRepository teamRepository, 
			IProjectArea projectArea, String devLineName, IProgressMonitor monitor) 
					throws TeamRepositoryException {
		
		IDevelopmentLineHandle[] devLineHandles = projectArea.getDevelopmentLines();
		for (IDevelopmentLineHandle devLineHandle : devLineHandles) {
			IDevelopmentLine devLine = (IDevelopmentLine) teamRepository.itemManager().
					fetchCompleteItem(devLineHandle, IItemManager.REFRESH, monitor);
			if (devLine.getName().equals(devLineName))
				return devLine;
		}
		
		return null;
	}

	
	static class WorkItemInitialization extends WorkItemOperation {
		
		private String fSummary;
		private ICategoryHandle fCategory;
		private Timestamp fCreationDate;
		private IContributorHandle fCreator;
		private IContributorHandle fOwner;
		private IIterationHandle fIteration;
		
		public WorkItemInitialization(String summary, ICategoryHandle category, 
				Timestamp creationDate, IContributorHandle creator,
				IContributorHandle owner, IIterationHandle iteration) {
			super("Initializing Work Item");
			fSummary= summary;
			fCategory= category;
			fCreationDate = creationDate;
			fCreator = creator;
			fOwner = owner;
			fIteration = iteration;
		}
		
		@Override
		protected void execute(WorkItemWorkingCopy workingCopy, IProgressMonitor monitor) 
				throws TeamRepositoryException {
			IWorkItem workItem= workingCopy.getWorkItem();
			workItem.setHTMLSummary(XMLString.createFromPlainText(fSummary));
			workItem.setHTMLDescription(XMLString.createFromPlainText(fSummary));
			workItem.setCategory(fCategory);
			workItem.setCreationDate(fCreationDate);
			workItem.setCreator(fCreator);
			workItem.setOwner(fOwner);
			workItem.setTarget(fIteration);
		}
	}

}

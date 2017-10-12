/**
 * 
 */
package za.co.indigocube.rtc.code.importer.workitem;

import org.eclipse.core.runtime.IProgressMonitor;

import com.ibm.team.foundation.common.text.XMLString;
import com.ibm.team.repository.client.ITeamRepository;
import com.ibm.team.repository.common.TeamRepositoryException;
import com.ibm.team.workitem.client.IAuditableClient;
import com.ibm.team.workitem.client.IWorkItemClient;
import com.ibm.team.workitem.client.WorkItemOperation;
import com.ibm.team.workitem.client.WorkItemWorkingCopy;
import com.ibm.team.workitem.common.IWorkItemCommon;
import com.ibm.team.workitem.common.model.ICategoryHandle;
import com.ibm.team.workitem.common.model.IWorkItem;
import com.ibm.team.workitem.common.model.IWorkItemHandle;
import com.ibm.team.workitem.common.model.IWorkItemType;

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
	
	public static IWorkItem createWorkItem(ITeamRepository teamRepository, IWorkItemType workItemType, 
			String summary, ICategoryHandle category) throws TeamRepositoryException {
		
		IWorkItem workItem = null;
		IAuditableClient auditableClient = getAuditableClient(teamRepository);
		
		WorkItemInitialization workItemInit = new WorkItemInitialization(summary, category);
		IWorkItemHandle handle = workItemInit.run(workItemType, null);
		workItem = auditableClient.resolveAuditable(handle, IWorkItem.FULL_PROFILE, null);
		System.out.println("Created work item " + workItem.getId() + ".");
		
		return workItem;
	}
	
	static class WorkItemInitialization extends WorkItemOperation {
		
		private String fSummary;
		private ICategoryHandle fCategory;
		
		public WorkItemInitialization(String summary, ICategoryHandle category) {
			super("Initializing Work Item");
			fSummary= summary;
			fCategory= category;
		}
		
		@Override
		protected void execute(WorkItemWorkingCopy workingCopy, IProgressMonitor monitor) throws TeamRepositoryException {
			IWorkItem workItem= workingCopy.getWorkItem();
			workItem.setHTMLSummary(XMLString.createFromPlainText(fSummary));
			workItem.setCategory(fCategory);
		}
	}

}

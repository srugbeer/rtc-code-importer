/**
 * 
 */
package za.co.indigocube.rtc.code.importer.workitem;

import org.eclipse.core.runtime.IProgressMonitor;

import za.co.indigocube.rtc.code.importer.workitem.WorkItemUtils.WorkItemInitialization;

import com.ibm.team.process.common.IProjectArea;
import com.ibm.team.repository.client.ITeamRepository;
import com.ibm.team.repository.common.TeamRepositoryException;
import com.ibm.team.workitem.client.IAuditableClient;
import com.ibm.team.workitem.common.IWorkItemCommon;
import com.ibm.team.workitem.common.internal.model.WorkItem;
import com.ibm.team.workitem.common.model.ICategory;
import com.ibm.team.workitem.common.model.IWorkItem;
import com.ibm.team.workitem.common.model.IWorkItemHandle;
import com.ibm.team.workitem.common.model.IWorkItemType;

/**
 * @author Sudheer
 *
 */
public class WorkItemClient {
	
	public WorkItemClient() {
		//Default Constructor
		super();
	}
	
	public IWorkItem findWorkItem(ITeamRepository teamRepository, int workItemId, IProgressMonitor monitor) 
			throws TeamRepositoryException {
		IWorkItem workItem = null;
		
		IWorkItemCommon wiCommon = WorkItemUtils.getWorkItemCommon(teamRepository);
		workItem = wiCommon.findWorkItemById(workItemId, WorkItem.FULL_PROFILE, monitor);
		
		return workItem;
	}
	
	public IWorkItem createWorkItem(ITeamRepository teamRepository, IProjectArea projectArea, 
			String workItemTypeId, String summary, ICategory category, IProgressMonitor monitor) 
					throws TeamRepositoryException {
		
		IWorkItem workItem = null;
		IAuditableClient auditableClient = WorkItemUtils.getAuditableClient(teamRepository);
		IWorkItemCommon wiCommon = WorkItemUtils.getWorkItemCommon(teamRepository);
		if (category == null) {
			category = wiCommon.findCategories(projectArea, ICategory.DEFAULT_PROFILE, monitor).get(0);
		}
		
		IWorkItemType workItemType = wiCommon.findWorkItemType(projectArea, workItemTypeId, monitor);
		
		WorkItemInitialization workItemInit = new WorkItemInitialization(summary, category);
		IWorkItemHandle handle = workItemInit.run(workItemType, null);
		workItem = auditableClient.resolveAuditable(handle, IWorkItem.FULL_PROFILE, null);
		System.out.println("Created work item " + workItem.getId() + ".");
		
		return workItem;
	}

}

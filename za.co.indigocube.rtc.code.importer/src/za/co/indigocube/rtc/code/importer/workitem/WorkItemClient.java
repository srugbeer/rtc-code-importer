/**
 * 
 */
package za.co.indigocube.rtc.code.importer.workitem;

import java.sql.Timestamp;

import org.eclipse.core.runtime.IProgressMonitor;

import za.co.indigocube.rtc.code.importer.workitem.WorkItemUtils.WorkItemInitialization;

import com.ibm.team.links.client.ILinkManager;
import com.ibm.team.links.common.ILink;
import com.ibm.team.links.common.IReference;
import com.ibm.team.process.common.IIterationHandle;
import com.ibm.team.process.common.IProjectArea;
import com.ibm.team.repository.client.ITeamRepository;
import com.ibm.team.repository.common.IContributorHandle;
import com.ibm.team.repository.common.TeamRepositoryException;
import com.ibm.team.workitem.client.IAuditableClient;
import com.ibm.team.workitem.common.IWorkItemCommon;
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
		workItem = wiCommon.findWorkItemById(workItemId, IWorkItem.FULL_PROFILE, monitor);
		
		return workItem;
	}
	
	public IWorkItem createWorkItem(ITeamRepository teamRepository, IProjectArea projectArea, IWorkItemType workItemType, 
			String summary, String description, ICategory category, Timestamp creationDate, IContributorHandle creator, 
			IContributorHandle owner, IIterationHandle iteration, String comment, IProgressMonitor monitor) 
					throws TeamRepositoryException {
		
		IWorkItem workItem = null;
		IAuditableClient auditableClient = WorkItemUtils.getAuditableClient(teamRepository);
		IWorkItemCommon wiCommon = WorkItemUtils.getWorkItemCommon(teamRepository);
		if (category == null) {
			category = wiCommon.findAllCategories(projectArea, ICategory.DEFAULT_PROFILE, monitor).get(0);
		}
		
		//IWorkItemType workItemType = wiCommon.findWorkItemType(projectArea, workItemTypeId, monitor);		
		
		WorkItemInitialization workItemInit = new WorkItemInitialization(summary, description, category, creationDate, 
				creator, owner, iteration, comment);
		IWorkItemHandle handle = workItemInit.run(workItemType, null);
		workItem = auditableClient.resolveAuditable(handle, IWorkItem.FULL_PROFILE, monitor);
		
		return workItem;
	}
	
	public void createWorkItemLink(ITeamRepository teamRepository, IWorkItem sourceWorkItem, 
			IWorkItem targetWorkItem, String linkType, IProgressMonitor monitor) throws TeamRepositoryException {
		
		ILinkManager linkManager = WorkItemUtils.getLinkManager(teamRepository);
		IReference sourceWorkItemRef =
		          linkManager.referenceFactory().createReferenceToItem(sourceWorkItem);
		IReference targetWorkItemRef = linkManager.referenceFactory().createReferenceToItem(targetWorkItem);
		ILink newLink = linkManager.createLink(
		          linkType, sourceWorkItemRef, targetWorkItemRef);
		linkManager.saveLink(newLink, monitor);
	}

}

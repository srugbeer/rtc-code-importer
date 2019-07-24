/**
 * 
 */
package za.co.indigocube.rtc.code.importer.workitem;

import java.sql.Timestamp;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;

import com.ibm.team.foundation.common.text.XMLString;
import com.ibm.team.links.client.ILinkManager;
import com.ibm.team.process.common.IDevelopmentLine;
import com.ibm.team.process.common.IDevelopmentLineHandle;
import com.ibm.team.process.common.IIteration;
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
import com.ibm.team.workitem.common.model.ICategory;
import com.ibm.team.workitem.common.model.ICategoryHandle;
import com.ibm.team.workitem.common.model.IComment;
import com.ibm.team.workitem.common.model.IComments;
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
	
	public static IIteration findIteration(ITeamRepository teamRepository, 
			IDevelopmentLine devLine, String iterationName, IProgressMonitor monitor) 
					throws TeamRepositoryException {
		
		IIterationHandle[] iterationHandles = devLine.getIterations();
		
		for (IIterationHandle iterationHandle : iterationHandles) {
			IIteration iteration = (IIteration) teamRepository.itemManager().
					fetchCompleteItem(iterationHandle, IItemManager.REFRESH, monitor);
			
			if (iteration.getName().equals(iterationName)) {
				return iteration;
			}
		}
		
		return null;
	}
	
	public static ICategory findCategory(ITeamRepository teamRepository, IProjectArea projectArea, 
			String categoryName, IProgressMonitor monitor) throws TeamRepositoryException {
		
		IWorkItemCommon workItemCommon = getWorkItemCommon(teamRepository);
		
		List<ICategory> categories = workItemCommon.
				findAllCategories(projectArea, ICategory.SMALL_PROFILE, monitor);
		
		for (ICategory category : categories) {
			if (category.getName().equals(categoryName)) {
				return category;
			}
		}
		
		return null;
	}

	
	static class WorkItemInitialization extends WorkItemOperation {
		
		private String fSummary;
		private String fDescription;
		private ICategoryHandle fCategory;
		private Timestamp fCreationDate;
		private IContributorHandle fCreator;
		private IContributorHandle fOwner;
		private IIterationHandle fIteration;
		private String fComment;
		
		public WorkItemInitialization(String summary, String description, ICategoryHandle category, 
				Timestamp creationDate, IContributorHandle creator, IContributorHandle owner, 
				IIterationHandle iteration, String comment) {
			super("Initializing Work Item");
			fSummary= summary;
			fDescription = description;
			fCategory= category;
			fCreationDate = creationDate;
			fCreator = creator;
			fOwner = owner;
			fIteration = iteration;
			fComment = comment;
		}
		
		public WorkItemInitialization(String summary, ICategoryHandle category, 
				Timestamp creationDate, IContributorHandle creator,
				IContributorHandle owner, IIterationHandle iteration) {
			this(summary, summary, category, creationDate, creator, owner, iteration, null);
			
		}
		
		@Override
		protected void execute(WorkItemWorkingCopy workingCopy, IProgressMonitor monitor) 
				throws TeamRepositoryException {
			IWorkItem workItem = workingCopy.getWorkItem();
			workItem.setHTMLSummary(XMLString.createFromPlainText(fSummary));
			workItem.setHTMLDescription(XMLString.createFromPlainText(fDescription));
			workItem.setCategory(fCategory);
			workItem.setCreationDate(fCreationDate);
			workItem.setCreator(fCreator);
			workItem.setOwner(fOwner);
			workItem.setTarget(fIteration);
			if (fComment != null && !fComment.equals("")) {
				IComments comments = workItem.getComments(); 
	            IComment newComment = comments.createComment(fCreator, XMLString.createFromPlainText(fComment)); 
	            comments.append(newComment); 
			}
		}
	}
	
	static class WorkItemSetWorkflowAction extends WorkItemOperation {
	    private String fWorkFlowAction;

	    public WorkItemSetWorkflowAction(String workFlowAction) {
	        super("Modifying Work Item State", IWorkItem.FULL_PROFILE);
	        fWorkFlowAction = workFlowAction;
	    }

	    @Override
	    protected void execute(WorkItemWorkingCopy workingCopy,
	            IProgressMonitor monitor) throws TeamRepositoryException {
	        workingCopy.setWorkflowAction(fWorkFlowAction);
	    }

	    public void setfWorkFlowAtion(String fWorkFlowAction) {
	        this.fWorkFlowAction = fWorkFlowAction;
	    }		
	}

}

/**
 * 
 */
package za.co.indigocube.rtc.code.importer.scm;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;

import com.ibm.team.filesystem.client.FileSystemCore;
import com.ibm.team.filesystem.client.IFileContentManager;
import com.ibm.team.filesystem.client.workitems.IFileSystemWorkItemManager;
import com.ibm.team.filesystem.common.FileLineDelimiter;
import com.ibm.team.filesystem.common.IFileContent;
import com.ibm.team.filesystem.common.IFileItem;
import com.ibm.team.links.common.ILink;
import com.ibm.team.repository.client.IItemManager;
import com.ibm.team.repository.client.ITeamRepository;
import com.ibm.team.repository.common.TeamRepositoryException;
import com.ibm.team.scm.client.IConfiguration;
import com.ibm.team.scm.client.IWorkspaceConnection;
import com.ibm.team.scm.client.content.util.VersionedContentManagerByteArrayInputStreamPovider;
import com.ibm.team.scm.common.IChangeSetHandle;
import com.ibm.team.scm.common.IComponent;
import com.ibm.team.scm.common.IComponentHandle;
import com.ibm.team.scm.common.IFolderHandle;
import com.ibm.team.scm.common.IWorkspaceHandle;
import com.ibm.team.scm.common.WorkspaceComparisonFlags;
import com.ibm.team.scm.common.dto.IChangeHistorySyncReport;
import com.ibm.team.workitem.common.model.IWorkItem;
import com.ibm.team.workitem.common.model.IWorkItemHandle;

/**
 * @author Sudheer
 *
 */
public class ScmClient {
	
	public ScmClient() {
		//Default Constructor
		super();
	}

	public IFileItem addFileToSourceControl(ITeamRepository teamRepository, File file, String fileName,
			IWorkspaceConnection workspaceConnection, IComponentHandle componentHandle, IConfiguration config,
			String comment, Date date, IWorkItem workItem, IProgressMonitor monitor) 
					throws TeamRepositoryException, IOException {
		
		IFileItem sourceFileItem = null;
		
		//Create Change Set
		IChangeSetHandle changeSetHandle = workspaceConnection.createChangeSet(componentHandle, 
				comment, true, monitor);
		
		//Link Change Set to Work Item
		System.out.println("Linking Change Set to Work Item: " + workItem.getHTMLSummary().getPlainText());
		this.linkChangeSetToWorkItem(workspaceConnection, changeSetHandle, workItem, monitor);
		
		IFileContentManager contentManager = FileSystemCore.getContentManager(teamRepository);
		
		IComponent component = (IComponent) teamRepository.itemManager().
				fetchCompleteItem(componentHandle, IItemManager.REFRESH, monitor);
		
		IFolderHandle parentFolderHandle = component.getRootFolder();
		
		sourceFileItem = ScmUtils.getFile(fileName, parentFolderHandle, config, monitor);
		
		if (sourceFileItem == null) {
			sourceFileItem = ScmUtils.createFileItem(fileName, parentFolderHandle, date);
		}
		
		FileInputStream fileInputStream = new FileInputStream(file);
		ByteArrayOutputStream byteArrayOutputStream = ScmUtils.copyFileData(fileInputStream);

		VersionedContentManagerByteArrayInputStreamPovider streamProvider = 
				new VersionedContentManagerByteArrayInputStreamPovider(byteArrayOutputStream.toByteArray());
		
		try {
			IFileContent sourceFileContent = contentManager.storeContent(
					IFileContent.ENCODING_UTF_8,
					FileLineDelimiter.LINE_DELIMITER_PLATFORM, streamProvider,
					null, monitor);
			IFileItem sourceFileItemWorkingCopy = (IFileItem) sourceFileItem
					.getWorkingCopy();
			sourceFileItemWorkingCopy.setContent(sourceFileContent);
			sourceFileItemWorkingCopy.setFileTimestamp(date);
			workspaceConnection.commit(changeSetHandle, Collections
					.singletonList(workspaceConnection.configurationOpFactory()
							.save(sourceFileItemWorkingCopy)), monitor);
			//return sourceFileItemWorkingCopy;
		} finally {
			byteArrayOutputStream.close();
		}
		return sourceFileItem;
	}
	
	public void deliverChangeSetsToStream(ITeamRepository teamRepo, IWorkspaceConnection sourceWorkspace,
			IWorkspaceConnection targetStream, IComponentHandle componentHandle, IProgressMonitor monitor) 
					throws TeamRepositoryException {
		
		IChangeHistorySyncReport syncReport = sourceWorkspace.compareTo(targetStream, 
				WorkspaceComparisonFlags.CHANGE_SET_COMPARISON_ONLY, Collections.EMPTY_LIST, monitor);
		
		sourceWorkspace.deliver(targetStream, syncReport, Collections.EMPTY_LIST, 
				syncReport.outgoingChangeSets(), monitor);
		
	}
	
	public List<ILink> linkChangeSetToWorkItem(IWorkspaceConnection workspaceConnection, 
			IChangeSetHandle changeSetHandle, IWorkItemHandle workItemHandle, IProgressMonitor monitor) 
					throws TeamRepositoryException {
		
		ITeamRepository teamRepository = workspaceConnection.teamRepository();
		
		IWorkspaceHandle workspaceHandle = workspaceConnection.getResolvedWorkspace();
		
		IFileSystemWorkItemManager fileSystemWorkItemManager = 	
				(IFileSystemWorkItemManager) teamRepository.getClientLibrary(IFileSystemWorkItemManager.class);
		
		IWorkItemHandle[] workItemHandles = {workItemHandle};
				
		return fileSystemWorkItemManager.createLink(workspaceHandle, changeSetHandle, workItemHandles, monitor);
	}
	
}

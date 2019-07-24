/**
 * @author Sudheer
 *
 */
package za.co.indigocube.rtc.code.importer.scm;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.util.Date;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;

import com.ibm.team.filesystem.common.IFileItem;
import com.ibm.team.process.client.IProcessItemService;
import com.ibm.team.process.common.IProjectArea;
import com.ibm.team.repository.client.ITeamRepository;
import com.ibm.team.repository.client.internal.TeamRepository;
import com.ibm.team.repository.common.ItemNotFoundException;
import com.ibm.team.repository.common.TeamRepositoryException;
import com.ibm.team.scm.client.IConfiguration;
import com.ibm.team.scm.client.IWorkspaceConnection;
import com.ibm.team.scm.client.IWorkspaceManager;
import com.ibm.team.scm.client.SCMPlatform;
import com.ibm.team.scm.client.interop.IWorkspaceConnectionInteropAdapter;
import com.ibm.team.scm.common.ComponentNotInWorkspaceException;
import com.ibm.team.scm.common.IComponentHandle;
import com.ibm.team.scm.common.IFolderHandle;
import com.ibm.team.scm.common.IScmService;
import com.ibm.team.scm.common.IVersionable;
import com.ibm.team.scm.common.IVersionableHandle;
import com.ibm.team.scm.common.IWorkspaceHandle;
import com.ibm.team.scm.common.dto.IComponentSearchCriteria;
import com.ibm.team.scm.common.dto.IWorkspaceSearchCriteria;

public class ScmUtils {
	/**
	 * Get the SCM Service in a client application
	 * 
	 * @param teamRepository
	 * @return
	 */
	public static IScmService getScmService(ITeamRepository teamRepository) {

		IScmService scmService = (IScmService) ((TeamRepository) teamRepository)
				.getServiceInterface(IScmService.class);
		return scmService;
	}
	
	public static IProjectArea getProjectArea(ITeamRepository teamRepository, 
			String projectAreaName) throws TeamRepositoryException {
		
	    IProjectArea projectArea = null;
		
	    IProcessItemService processItemService = (IProcessItemService) teamRepository.
	    		getClientLibrary(IProcessItemService.class);
	    
	    URI uri = URI.create(projectAreaName.replaceAll(" ", "%20").replaceAll("\\[", "%5B").replaceAll("\\]", "%5D"));
		projectArea = (IProjectArea) processItemService.findProcessArea(uri, null, null);
	
		return projectArea;
	}
	
	public static IWorkspaceManager getWorkspaceManager(ITeamRepository teamRepository) {
		return SCMPlatform.getWorkspaceManager(teamRepository);
	}
	
	public static IWorkspaceConnection getWorkspaceConnection(ITeamRepository teamRepository, String workspaceName,
			IProgressMonitor monitor) throws TeamRepositoryException {
		return getWorkspaceConnection(teamRepository, workspaceName, IWorkspaceSearchCriteria.WORKSPACES, monitor);
	}
	
	public static IWorkspaceConnection getStreamConnection(ITeamRepository teamRepository, String streamName, 
			IProgressMonitor monitor) throws TeamRepositoryException {
		return getWorkspaceConnection(teamRepository, streamName, IWorkspaceSearchCriteria.STREAMS, monitor);
	}
	
	public static IWorkspaceConnection getWorkspaceConnection(ITeamRepository teamRepository, String workspaceName,
			int searchCriteria, IProgressMonitor monitor) throws TeamRepositoryException {
		
		IWorkspaceConnection workspaceConnection = null;
		//Get Workspace Connection
        IWorkspaceManager workspaceManager = getWorkspaceManager(teamRepository);

        IWorkspaceSearchCriteria workspaceSearchCriteria = IWorkspaceSearchCriteria.FACTORY.newInstance().
        		setKind(searchCriteria).setExactName(workspaceName);       
        IWorkspaceHandle workspaceHandle = workspaceManager.
        		findWorkspaces(workspaceSearchCriteria, 1, monitor).get(0);
        workspaceConnection = workspaceManager.getWorkspaceConnection(workspaceHandle, monitor);
        
        return workspaceConnection;
	}
	
	public static IWorkspaceConnectionInteropAdapter getInteropAdaptor(IWorkspaceConnection workspaceConnection) {
		return (IWorkspaceConnectionInteropAdapter) workspaceConnection.
				getAdapter(IWorkspaceConnectionInteropAdapter.class);
	}
	
	public static IComponentHandle getComponent(ITeamRepository teamRepository, String componentName, 
			IProgressMonitor monitor) throws TeamRepositoryException {
		IComponentHandle componentHandle = null;
		
		IWorkspaceManager workspaceManager = getWorkspaceManager(teamRepository);
        //Get Component Handle
        IComponentSearchCriteria componentSearchCriteria = IComponentSearchCriteria.FACTORY.newInstance().
        		setExactName(componentName);
        componentHandle = workspaceManager.findComponents(componentSearchCriteria, 1, monitor).get(0);
		
		return componentHandle;
	}
	
	public static IConfiguration getConfiguration(IWorkspaceConnection workspaceConnection,
			IComponentHandle componentHandle) throws ItemNotFoundException, ComponentNotInWorkspaceException {
		 return workspaceConnection.configuration(componentHandle);
	}

	/**
	 * Tries to find a IFileItem node in a given IFolder. Returns the IFileItem
	 * found or null if none was found.
	 * 
	 * @param file
	 * @param parentFolder
	 * @return
	 * @throws TeamRepositoryException
	 */
	public static IFileItem getFile(String fileName, IFolderHandle parentFolder, IConfiguration configuration,
			IProgressMonitor monitor) throws TeamRepositoryException {
		IVersionable versionable = getVersionable(fileName, parentFolder, configuration, monitor);
		if(versionable != null) {
			if (versionable instanceof IFileItem) {
				return (IFileItem) versionable;
			}
		}
		return null;
	}
	/**
	 * Tries to create a IFileItem node in a given IFolder. Returns the
	 * IFileItem.
	 * 
	 * @param string
	 * @param zipEntry
	 * @param parentFolder
	 * 
	 * @return
	 * @throws TeamRepositoryException 
	 */
	public static IFileItem createFileItem(String name, IFolderHandle parentFolder, Date date) 
			throws TeamRepositoryException {
		IFileItem fileItem = (IFileItem) IFileItem.ITEM_TYPE.createItem();
		fileItem.setParent(parentFolder);
		fileItem.setName(name);
		fileItem.setContentType(IFileItem.CONTENT_TYPE_TEXT);
		fileItem.setFileTimestamp(date);
		return fileItem;
	}
	
	/**
	 * Copy the data from an input stream to an output stream. This is done to
	 * avoid the Jazz SCM closing the stream that contains the original data.
	 * 
	 * @param zipInStream
	 * @return
	 * @throws IOException
	 */
	public static ByteArrayOutputStream copyFileData(FileInputStream fileInputStream)
			throws IOException {
		ByteArrayOutputStream contents = new ByteArrayOutputStream();
		byte[] buf = new byte[2048];
		int read;
		while ((read = fileInputStream.read(buf)) != -1) {
			contents.write(buf, 0, read);
		}
		contents.flush();
		return contents;
	}
	
	/**
	 * Gets a versionable with a specific name from a parent folder.
	 * 
	 * @param name
	 * @param parentFolder
	 * @return
	 * @throws TeamRepositoryException
	 */
	public static IVersionable getVersionable(String name, IFolderHandle parentFolder, IConfiguration configuration,
			IProgressMonitor monitor)
			throws TeamRepositoryException {
		// get all the child entries
		@SuppressWarnings("unchecked")
		Map<String, IVersionableHandle> handles = configuration.childEntries(parentFolder, monitor);
		// try to find an entry with the name
		IVersionableHandle foundHandle = handles.get(name);
		if(foundHandle != null) {
			return configuration.fetchCompleteItem(foundHandle, monitor);
		}
		return null;
	}
	
}
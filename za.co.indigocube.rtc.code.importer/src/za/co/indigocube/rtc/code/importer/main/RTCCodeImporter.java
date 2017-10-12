/**
 * 
 */
package za.co.indigocube.rtc.code.importer.main;

import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;

import za.co.indigocube.rtc.code.importer.scm.ScmClient;
import za.co.indigocube.rtc.code.importer.scm.ScmUtils;
import za.co.indigocube.rtc.code.importer.scm.attribute.ScmAttributeUtils;
import za.co.indigocube.rtc.code.importer.scm.attribute.model.ScmAttributeDefinition;
import za.co.indigocube.rtc.code.importer.source.FolderReader;
import za.co.indigocube.rtc.code.importer.source.model.SourceFile;
import za.co.indigocube.rtc.code.importer.source.model.SourceFileVersion;
import za.co.indigocube.rtc.code.importer.workitem.WorkItemClient;

import com.ibm.team.filesystem.common.IFileItem;
import com.ibm.team.process.common.IProjectArea;
import com.ibm.team.repository.client.IContributorManager;
import com.ibm.team.repository.client.ITeamRepository;
import com.ibm.team.repository.client.TeamPlatform;
import com.ibm.team.repository.common.IContributor;
import com.ibm.team.repository.common.TeamRepositoryException;
import com.ibm.team.scm.client.IConfiguration;
import com.ibm.team.scm.client.IWorkspaceConnection;
import com.ibm.team.scm.common.IComponentHandle;
import com.ibm.team.scm.common.IVersionable;
import com.ibm.team.workitem.common.model.IWorkItem;


/**
 * @author Sudheer
 *
 */
public class RTCCodeImporter {
	
	private String teamRepositoryURI;
	private String userName;
	private String password;
	private String projectAreaName;
	private String sourceWorkspaceName;
	private String targetStreamName;
	private String componentName;
	private String sourceFolderName;
	private int workItemId;
	
	private int versionCount = 0;
	
	/**
	 * @param teamRepositoryURI
	 * @param userName
	 * @param password
	 * @param projectAreaName
	 * @param sourceWorkspaceName
	 * @param targetStreamName
	 * @param componentName
	 * @param sourceFolderName
	 */
	public RTCCodeImporter(String teamRepositoryURI, String userName,
			String password, String projectAreaName,
			String sourceWorkspaceName, String targetStreamName,
			String componentName, String sourceFolderName, int workItemId) {
		super();
		this.setTeamRepositoryURI(teamRepositoryURI);
		this.setUserName(userName);
		this.setPassword(password);
		this.setProjectAreaName(projectAreaName);
		this.setSourceWorkspaceName(sourceWorkspaceName);
		this.setTargetStreamName(targetStreamName);
		this.setComponentName(componentName);
		this.setSourceFolderName(sourceFolderName);
		this.setWorkItemId(workItemId);
	}

	/**
	 * @return the teamRepositoryURI
	 */
	public String getTeamRepositoryURI() {
		return teamRepositoryURI;
	}

	/**
	 * @param teamRepositoryURI the teamRepositoryURI to set
	 */
	public void setTeamRepositoryURI(String teamRepositoryURI) {
		this.teamRepositoryURI = teamRepositoryURI;
	}

	/**
	 * @return the userName
	 */
	public String getUserName() {
		return userName;
	}

	/**
	 * @param userName the userName to set
	 */
	public void setUserName(String userName) {
		this.userName = userName;
	}

	/**
	 * @return the password
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * @param password the password to set
	 */
	public void setPassword(String password) {
		this.password = password;
	}

	/**
	 * @return the projectAreaName
	 */
	public String getProjectAreaName() {
		return projectAreaName;
	}

	/**
	 * @param projectAreaName the projectAreaName to set
	 */
	public void setProjectAreaName(String projectAreaName) {
		this.projectAreaName = projectAreaName;
	}

	/**
	 * @return the sourceWorkspaceName
	 */
	public String getSourceWorkspaceName() {
		return sourceWorkspaceName;
	}

	/**
	 * @param sourceWorkspaceName the sourceWorkspaceName to set
	 */
	public void setSourceWorkspaceName(String sourceWorkspaceName) {
		this.sourceWorkspaceName = sourceWorkspaceName;
	}

	/**
	 * @return the targetStreamName
	 */
	public String getTargetStreamName() {
		return targetStreamName;
	}

	/**
	 * @param targetStreamName the targetStreamName to set
	 */
	public void setTargetStreamName(String targetStreamName) {
		this.targetStreamName = targetStreamName;
	}

	/**
	 * @return the componentName
	 */
	public String getComponentName() {
		return componentName;
	}

	/**
	 * @param componentName the componentName to set
	 */
	public void setComponentName(String componentName) {
		this.componentName = componentName;
	}

	/**
	 * @return the sourceFolderName
	 */
	public String getSourceFolderName() {
		return sourceFolderName;
	}

	/**
	 * @param sourceFolderName the sourceFolderName to set
	 */
	public void setSourceFolderName(String sourceFolderName) {
		this.sourceFolderName = sourceFolderName;
	}

	/**
	 * @return the workItemId
	 */
	public int getWorkItemId() {
		return workItemId;
	}

	/**
	 * @param workItemId the workItemId to set
	 */
	public void setWorkItemId(int workItemId) {
		this.workItemId = workItemId;
	}

	/**
	 * @return the versionCount
	 */
	public int getVersionCount() {
		return versionCount;
	}

	/**
	 * @param versionCount the versionCount to set
	 */
	public void setVersionCount(int versionCount) {
		this.versionCount = versionCount;
	}

	public ITeamRepository login(String repositoryURI, String userName, String password, 
			IProgressMonitor monitor) throws TeamRepositoryException {
	    ITeamRepository repository = TeamPlatform.getTeamRepositoryService().getTeamRepository(repositoryURI);
	    repository.registerLoginHandler(new ITeamRepository.ILoginHandler() {
	        public ILoginInfo challenge(ITeamRepository repository) {
	            return new ILoginInfo() {
	                public String getUserId() {
	                    return userName;
	                }
	                public String getPassword() {
	                    return password;                        
	                }
	            };
	        }
	    });
	    repository.login(monitor);
	    return repository;
	}

	public IFileItem importSourceFileToRTC(SourceFile sourceFile, ITeamRepository teamRepository,
			String sourceWorkspaceName, String targetStreamName, String componentName, IProgressMonitor monitor) 
					throws TeamRepositoryException, IOException, ParseException {
		
		IFileItem fileItem = null;
		
		ScmClient scmClient = new ScmClient();		
	    WorkItemClient wiClient = new WorkItemClient();
		      
        //Get Workspace Connection
        IWorkspaceConnection sourceWorkspaceConnection = ScmUtils.
        		getWorkspaceConnection(teamRepository, sourceWorkspaceName, monitor);
        
        //Get Target Stream Connection
        IWorkspaceConnection targetStreamConnection = ScmUtils.
        		getStreamConnection(teamRepository, targetStreamName, monitor);
		            
        //Get Component Handle
        IComponentHandle componentHandle = ScmUtils.getComponent(teamRepository, componentName, monitor);
        
        //Get Configuration
        IConfiguration config = ScmUtils.getConfiguration(sourceWorkspaceConnection, componentHandle);
	    
	    //Find Work Item
	    IWorkItem workItem = wiClient.findWorkItem(teamRepository, this.getWorkItemId(), monitor);
	    //System.out.println("Work Item Summary: " + workItem.getHTMLSummary().toString());
        
        //Get Source File Version History
        Map<Integer, SourceFileVersion> history = sourceFile.getHistory();
        
        for (int i = 0; i < history.size(); i++) {
    	SourceFileVersion sourceFileVersion = history.get(i);
	    	File file = new File(sourceFileVersion.getVersionFileName());
	    	String fileName = sourceFile.getName();
	    	String createdBy = sourceFileVersion.getCreatedBy();
	    	String creationDate = sourceFileVersion.getCreationDate();
	    	
	    	SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd-hhmm");
	    	
	    	int version = i;
	    	String comment = "Checking-in File: " + fileName + " version: " + version 
	    			+ " (Owner " + createdBy + ")";
	    	System.out.println(comment);
	    	Date date = dateFormat.parse(creationDate);
	    	System.out.println("Creation Date: " + date);
	    	
	    	//Commit File Version to Repository
	    	fileItem = scmClient.addFileToSourceControl(teamRepository, file, fileName, sourceWorkspaceConnection, 
	    			componentHandle, config, comment, date, workItem, monitor);
	    	
	    	//Deliver change to Target Stream
	    	System.out.println("Delivering change set to Stream");
	    	scmClient.deliverChangeSetsToStream(teamRepository, sourceWorkspaceConnection, targetStreamConnection, 
	    			componentHandle, monitor);
	    	
	    	//Get Versionable full state
	    	IVersionable versionable = config.fetchCompleteItem(fileItem, monitor);
	    	
	    	//Set custom attributes
	    	System.out.println("Setting custom attributes");
	    	Map<String, String> attributes = sourceFile.getMetadata();
	    	ScmAttributeUtils.setAttributes(versionable, attributes, ScmUtils.getScmService(teamRepository));
	    	
	    	ScmAttributeUtils.printAttributes(versionable, ScmUtils.getScmService(teamRepository));
	    	versionCount++;
        }
		
		return fileItem;		
	}
	
	public ArrayList<IFileItem> importSourceFilesToRTC(ArrayList<SourceFile> sourceFiles, 
			ITeamRepository teamRepository, String sourceWorkspaceName, String targetStreamName, 
			String componentName, IProgressMonitor monitor) throws TeamRepositoryException, IOException, ParseException {
		
		ArrayList<IFileItem> fileItems = new ArrayList<IFileItem>();
		
		for (SourceFile sourceFile : sourceFiles) {
			IFileItem fileItem = importSourceFileToRTC(sourceFile, teamRepository, sourceWorkspaceName, 
					targetStreamName, componentName, monitor);
			fileItems.add(fileItem);
		}
		
		return fileItems;
	}
	
	public ScmAttributeDefinition[] retrieveScmAttributeDefinitions(ITeamRepository teamRepository, 
			IProjectArea projectArea) throws TeamRepositoryException {
		
		ScmAttributeDefinition[] scmAttributeDefs = ScmAttributeUtils.
        		retrieveAttributes(teamRepository, projectArea);
        for (ScmAttributeDefinition scmAttributeDef : scmAttributeDefs) {
        	System.out.println("SCM Attribute: " + scmAttributeDef.getKey());
        }
        
        return scmAttributeDefs;
	}
	
	ArrayList<SourceFile> getSourceFileList() {
		FolderReader folderReader = new FolderReader();
		
		return folderReader.readFolderContents(this.getSourceFolderName());
	}
	
	void doImport(ArrayList<SourceFile> sourceFileList) {
	    TeamPlatform.startup();             
        try {     
            IProgressMonitor monitor = new NullProgressMonitor();
            System.out.println("Logging in to \"" + this.getTeamRepositoryURI() + "\" ...");
            ITeamRepository teamRepository = login(this.getTeamRepositoryURI(), this.getUserName(), 
            		this.getPassword(), monitor);
            System.out.println("Login success!");
            
            IProjectArea projectArea = ScmUtils.getProjectArea(teamRepository, this.getProjectAreaName());
            System.out.println("Project Area: " + projectArea.getName());

            //importSourceFilesToRTC(sourceFileList, teamRepository, 
            //		this.getSourceWorkspaceName(), this.getTargetStreamName(), this.getComponentName(), monitor);
            
            
    	    //Create Work Item
            String workItemTypeId = "task";
            String summary = "Created from JPJC";
            String creationDateString = "20170930-1430";
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd-hhmm");
            Date creationDate = dateFormat.parse(creationDateString);
            
            IContributorManager contributorManager = teamRepository.contributorManager();
            IContributor deb = contributorManager.fetchContributorByUserId("deb", monitor);

    	    WorkItemClient wiClient = new WorkItemClient();
    	    IWorkItem workItem = wiClient.createWorkItem(teamRepository, projectArea, workItemTypeId, summary, null, monitor);
            workItem.setCreationDate(new Timestamp(creationDate.getTime()));
            workItem.setCreator(deb);
            
        } catch (TeamRepositoryException e) {
            System.out.println("RTC Error: " + e.getMessage());
        //} catch (IOException e) {
		//	e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		} finally {
            TeamPlatform.shutdown();
        }
	}
	
    
	/**
	 * @param args
	 */
    public static void main(String[] args) {
    	
		//Repo Settings
	    final String REPOSITORY_ADDRESS = "https://localhost:7443/jazz";
	    final String USERNAME = "TestJazzAdmin1";
	    final String PASSWORD = "TestJazzAdmin1";
	    
	    //Project Area Settings
	    final String PROJECT_AREA = "Mainframe Code PoC";
	    //private static String PROJECT_AREA = "CARA Post Deliver Test";
	    
	    //SCM Settings
	    final String STREAM_NAME = "Mainframe Code Dev Stream";
	    final String COMPONENT_NAME = "COBOL";
	    final String WORKSPACE_NAME = "admin Mainframe Code Dev Stream Workspace";
	    
	    //Source Settings
	    final String SOURCE_FOLDER = "C:/RTC603Dev/MainframeCodeMigrationSample";
	    
	    final int WORKITEM_ID = 13;
	    
	    RTCCodeImporter rtcCodeImporter = new RTCCodeImporter(REPOSITORY_ADDRESS, USERNAME, PASSWORD, 
	    		PROJECT_AREA, WORKSPACE_NAME, STREAM_NAME, COMPONENT_NAME, SOURCE_FOLDER, WORKITEM_ID);
	    
	    //Get Source Files
	    ArrayList<SourceFile> sourceFileList = rtcCodeImporter.getSourceFileList();
	    
	    //Import Source Files
	    rtcCodeImporter.doImport(sourceFileList);
	    int numberOfSourceFiles = sourceFileList.size();
	    int numberOfVersions = rtcCodeImporter.getVersionCount();
	    
	    System.out.println("Import Complete: " + numberOfSourceFiles + " Files, " + numberOfVersions + " Versions.");
	    
    }
}

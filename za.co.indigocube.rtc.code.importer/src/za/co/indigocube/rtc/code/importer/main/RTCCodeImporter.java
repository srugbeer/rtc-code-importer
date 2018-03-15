/**
 * 
 */
package za.co.indigocube.rtc.code.importer.main;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;
import java.util.Properties;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
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
import za.co.indigocube.rtc.code.importer.workitem.WorkItemUtils;

import com.ibm.team.filesystem.common.IFileItem;
import com.ibm.team.process.common.IDevelopmentLine;
import com.ibm.team.process.common.IIterationHandle;
import com.ibm.team.process.common.IProjectArea;
import com.ibm.team.repository.client.ITeamRepository;
import com.ibm.team.repository.client.TeamPlatform;
import com.ibm.team.repository.common.IContributor;
import com.ibm.team.repository.common.ItemNotFoundException;
import com.ibm.team.repository.common.TeamRepositoryException;
import com.ibm.team.scm.client.IConfiguration;
import com.ibm.team.scm.client.IWorkspaceConnection;
import com.ibm.team.scm.common.IComponentHandle;
import com.ibm.team.scm.common.IVersionable;
import com.ibm.team.workitem.common.IWorkItemCommon;
import com.ibm.team.workitem.common.model.ICategory;
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
	
	private static final Logger LOGGER = Logger.getLogger(RTCCodeImporter.class);
	
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

	private IFileItem importSourceFileToRTC(SourceFile sourceFile, ITeamRepository teamRepository,
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
        
        //Get Work Item Common
        IWorkItemCommon wiCommon = WorkItemUtils.getWorkItemCommon(teamRepository);
        
        //Get Project Area
        IProjectArea projectArea = ScmUtils.getProjectArea(teamRepository, this.getProjectAreaName());
        
        IWorkItem workItem = null;
	    
	    //Find Work Item
        if (this.getWorkItemId() != -1) {
        	//Use Existing Work Item
        	workItem = wiClient.findWorkItem(teamRepository, this.getWorkItemId(), monitor);
        }
        else {
        	//Create new Work Item
        	
        	String workItemTypeId = "task";
        	String devLineName = "Main Development";
        	String summary = "Import " + sourceFile.getName();
        	ICategory rootCategory = wiCommon.findCategories(projectArea, ICategory.DEFAULT_PROFILE, monitor).get(0);
        	Timestamp currentTime = new Timestamp(System.currentTimeMillis());
        	IContributor loggedinUser = teamRepository.loggedInContributor();
        	
        	IDevelopmentLine devLine = WorkItemUtils.findDevelopmentLine(teamRepository, projectArea, devLineName, monitor);
    		IIterationHandle currentIteration = devLine.getCurrentIteration();
        	
        	workItem = wiClient.createWorkItem(teamRepository, projectArea, workItemTypeId, summary, rootCategory, 
        			currentTime, loggedinUser, loggedinUser, currentIteration, monitor);
        }
        
        //Get Source File Version History
        //Map<Integer, SourceFileVersion> history = sourceFile.getHistory();
        TreeSet<SourceFileVersion> versionHistory = sourceFile.getVersionHistory();
        
        //for (int i = 0; i < history.size(); i++) {
        int version = 0;
    	//LOGGER.info("================================================================");
        for (SourceFileVersion sourceFileVersion : versionHistory) {
        	//SourceFileVersion sourceFileVersion = history.get(i);
        	String fileName = sourceFile.getName();
        	String filePath = sourceFile.getPath();
        	String versionIndex = sourceFileVersion.getVersionFileName();
        	String versionFilePath = filePath + "/" + versionIndex + "-" + fileName;
        	//System.out.println("Version File Path: " + versionFilePath);
	    	File versionFile = new File(versionFilePath);
	    	LOGGER.info("File Version Path: " + versionFile.getAbsolutePath());
	    	
	    	if (!versionFile.exists()) {
	    		LOGGER.warn("Source Version File does not exist, but is listed in the audit file. Skipping this version.\n");
	    	}
	    	else {
		    	String createdBy = sourceFileVersion.getCreatedBy();
		    	Date creationDate = sourceFileVersion.getCreationDate();
		    	String project = sourceFileVersion.getProject();
		    	
		    	//SimpleDateFormat dateFormat = new SimpleDateFormat("yyMMddhhmmss");
		    	
		    	//int version = i;
		    	String comment = "Imported: " + fileName + " version: " + (version + 1)
		    			+ " Owner: " + createdBy + " Project: " + project;
		    	//System.out.println(comment);
		    	LOGGER.info(comment);
		    	//Date date = dateFormat.parse(creationDate);
		    	LOGGER.info("Creation Date: " + creationDate);
		    	//System.out.println("Creation Date: " + creationDate);
		    	
		    	IContributor creator = null;
	            try {
	            	creator = this.findContributor(teamRepository, createdBy.toLowerCase(), monitor);
	            }
	            catch (TeamRepositoryException e) {
	            	//User Not Found in Repo
	            	if (e instanceof ItemNotFoundException) {
	            		creator = this.findContributor(teamRepository, "abap331", monitor);
	            		LOGGER.warn(e.getMessage());
	            		LOGGER.info("Using default user.");
	            		//System.out.println(e.getMessage());
	            		//System.out.println("Using default Admin User.");
	            	}
	            }
	            
	/*            //Create Work Item for Project
	        	String workItemTypeId = "com.ibm.team.apt.workItemType.story";
	        	String devLineName = "Main Development";
	        	String summary = project;
	        	ICategory rootCategory = wiCommon.findCategories(projectArea, ICategory.DEFAULT_PROFILE, monitor).get(0);
	        	//Timestamp currentTime = new Timestamp(System.currentTimeMillis());
	        	Timestamp creationTime = new Timestamp(creationDate.getTime());
	        	IContributor loggedinUser = teamRepository.loggedInContributor();
	        	
	        	IDevelopmentLine devLine = WorkItemUtils.findDevelopmentLine(teamRepository, projectArea, devLineName, monitor);
	    		IIterationHandle currentIteration = devLine.getCurrentIteration();
	        	
	        	workItem = wiClient.createWorkItem(teamRepository, projectArea, workItemTypeId, summary, rootCategory, 
	        			creationTime, loggedinUser, loggedinUser, currentIteration, monitor); */
	            
		    	//Commit File Version to Repository
		    	fileItem = scmClient.addFileToSourceControl(teamRepository, versionFile, fileName, sourceWorkspaceConnection, 
		    			componentHandle, config, comment, creationDate, creator, workItem, monitor);
		    	
		    	//Deliver change to Target Stream
		    	LOGGER.info("Delivering change set to Stream");
		    	//System.out.println("Delivering change set to Stream");
		    	scmClient.deliverChangeSetsToStream(teamRepository, sourceWorkspaceConnection, targetStreamConnection, 
		    			componentHandle, monitor);
		    	
		    	//Get Versionable full state
		    	IVersionable versionable = config.fetchCompleteItem(fileItem, monitor);
		    	
		    	//Set custom attributes
		    	LOGGER.info("Setting custom attributes");
		    	//System.out.println("Setting custom attributes");
		    	Map<String, String> attributes = sourceFile.getMetadata();
		    	ScmAttributeUtils.setAttributes(versionable, attributes, ScmUtils.getScmService(teamRepository));
		    	
		    	ScmAttributeUtils.printAttributes(versionable, ScmUtils.getScmService(teamRepository), LOGGER);
		    	version++;
		    	versionCount++;
		    	LOGGER.info("");
	    	}
        }
		
		return fileItem;		
	}
	
	private ArrayList<IFileItem> importSourceFilesToRTC(ArrayList<SourceFile> sourceFiles, 
			ITeamRepository teamRepository, String sourceWorkspaceName, String targetStreamName, 
			String componentName, IProgressMonitor monitor) 
					throws TeamRepositoryException, IOException, ParseException {
		
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
	
	private ArrayList<SourceFile> getSourceFileList() {
		FolderReader folderReader = new FolderReader();
		
		return folderReader.readFolderContents(this.getSourceFolderName(), LOGGER);
	}
	
	private void doImport(ArrayList<SourceFile> sourceFileList) {
		
	    TeamPlatform.startup();             
        try {     
            IProgressMonitor monitor = new NullProgressMonitor();
            //System.out.println("Logging in to \"" + this.getTeamRepositoryURI() + "\" ...");
            LOGGER.info("Logging in to \"" + this.getTeamRepositoryURI() + "\" ...");
            ITeamRepository teamRepository = login(this.getTeamRepositoryURI(), this.getUserName(), 
            		this.getPassword(), monitor);
            //System.out.println("Login success!");
            LOGGER.info("Login success!");
            
            IProjectArea projectArea = ScmUtils.getProjectArea(teamRepository, this.getProjectAreaName());
            //System.out.println("Project Area: " + projectArea.getName());
            LOGGER.info("Project Area: " + projectArea.getName() + "\n");

            importSourceFilesToRTC(sourceFileList, teamRepository, 
            		this.getSourceWorkspaceName(), this.getTargetStreamName(), this.getComponentName(), monitor);            
        } catch (TeamRepositoryException e) {
            //System.out.println("RTC Error: " + e.getMessage());
            LOGGER.error("RTC Error: " + e.getMessage());
        } catch (IOException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		} finally {
            TeamPlatform.shutdown();
        }
	}
	
	private IContributor findContributor(ITeamRepository teamRepository, String contributorName, 
			IProgressMonitor monitor) throws TeamRepositoryException {
        return teamRepository.contributorManager().fetchContributorByUserId(contributorName, monitor);
        
	}
	
	public String formatExecutionTime(long executionTime) {
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
    
	/**
	 * @param args
	 */
    public static void main(String[] args) {
    	
    	//Logger logger = Logger.getLogger(RTCCodeImporter.class);
    	
    	Properties importProps = new Properties();
    	try {
			importProps.load(new FileInputStream("resources/import.properties"));
		} catch (FileNotFoundException e) {
			LOGGER.error("Unable to load properties file", e);
			//e.printStackTrace();
		} catch (IOException e) {
			LOGGER.error("Error reading properties file");
			//e.printStackTrace();
		}
    	
		//Repo Settings
    	final String REPOSITORY_ADDRESS = importProps.getProperty("rtc.repo.url", "https://localhost:9443/ccm");
    	final String USERNAME = importProps.getProperty("rtc.repo.username", "ADMIN");
    	final String PASSWORD = importProps.getProperty("rtc.repo.password", "ADMIN");
    	
	    //final String REPOSITORY_ADDRESS = "https://localhost:7443/jazz";
	    //final String USERNAME = "TestJazzAdmin1";
	    //final String PASSWORD = "TestJazzAdmin1";
	    
	    //Project Area Settings
	    final String PROJECT_AREA = importProps.getProperty("rtc.project.name", "JKE Banking");
    	
    	//final String PROJECT_AREA = "Mainframe Code PoC";
	    
	    //SCM Settings
	    final String STREAM_NAME = importProps.getProperty("rtc.stream.name", "JKE Banking Dev Stream");
	    final String COMPONENT_NAME = importProps.getProperty("rtc.component.name", "COBOL");
	    final String WORKSPACE_NAME = importProps.getProperty("rtc.workspace.name", "admin JKE Banking Dev Stream");
	    	    
	    //final String STREAM_NAME = "Mainframe Code Dev Stream";
	    //final String COMPONENT_NAME = "COBOL";
	    //final String WORKSPACE_NAME = "admin Mainframe Code Dev Stream Workspace";
	    
	    //Source Settings
	    final String SOURCE_FOLDER = importProps.getProperty("source.folder", "C:/Migration");
	    
	    //final String SOURCE_FOLDER = "C:/RTC603Dev/MainframeCodeMigrationSample";
	    
	    //RTC Work Item
	    final int WORKITEM_ID = Integer.parseInt(importProps.getProperty("rtc.workitem.id", "-1"));
	    
	    //final int WORKITEM_ID = -1;
	    
	    long start, end, total;
	    
	    //LOGGER.info("********************************************************************");
	    LOGGER.info("Started import process...\n");
	    long importStart = System.currentTimeMillis();
	    RTCCodeImporter rtcCodeImporter = new RTCCodeImporter(REPOSITORY_ADDRESS, USERNAME, PASSWORD, 
	    		PROJECT_AREA, WORKSPACE_NAME, STREAM_NAME, COMPONENT_NAME, SOURCE_FOLDER, WORKITEM_ID);
	    
	    start = System.currentTimeMillis();
	    LOGGER.info("Started reading source folder...");
	    //Get Source Files
	    ArrayList<SourceFile> sourceFileList = rtcCodeImporter.getSourceFileList();
	    end = System.currentTimeMillis();
	    
	    total = end - start;
	    String totalTimeReadingFiles = rtcCodeImporter.formatExecutionTime(total);
	    LOGGER.info("********************************************************************");
	    LOGGER.info("Completed reading source folder in " + totalTimeReadingFiles);
	    LOGGER.info("********************************************************************\n");
	    LOGGER.info("Start Importing file versions...");
	    start = System.currentTimeMillis();
	    //Import Source Files
	    rtcCodeImporter.doImport(sourceFileList);
	    end = System.currentTimeMillis();
	    
	    total = end - start;
	    String totalImportTime = rtcCodeImporter.formatExecutionTime(total);
	    LOGGER.info("********************************************************************");
	    LOGGER.info("Completed importing file versions in " + totalImportTime);
	    LOGGER.info("********************************************************************\n");
	    
	    int numberOfSourceFiles = sourceFileList.size();
	    int numberOfVersions = rtcCodeImporter.getVersionCount();
	    
	    long importEnd = System.currentTimeMillis();
	    String totalTime = rtcCodeImporter.formatExecutionTime(importEnd - importStart);
	    
	    LOGGER.info("********************************************************************"); 
	    LOGGER.info("Import Complete in " + totalTime + ": " 
	    		+ numberOfSourceFiles + " Files, " + numberOfVersions + " Versions.");
	    LOGGER.info("********************************************************************");
    }
}

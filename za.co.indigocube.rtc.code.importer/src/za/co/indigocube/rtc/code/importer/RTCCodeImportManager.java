/**
 * 
 */
package za.co.indigocube.rtc.code.importer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;

import za.co.indigocube.rtc.code.importer.scm.ScmClient;
import za.co.indigocube.rtc.code.importer.scm.ScmUtils;
import za.co.indigocube.rtc.code.importer.scm.attribute.ScmAttributeUtils;
import za.co.indigocube.rtc.code.importer.source.FolderReader;
import za.co.indigocube.rtc.code.importer.source.model.SourceFile;
import za.co.indigocube.rtc.code.importer.source.model.SourceFileVersion;
import za.co.indigocube.rtc.code.importer.source.model.SourceType;
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
public class RTCCodeImportManager {
	
	/* RTC Repository Settings */
	private String teamRepositoryURI;
	private String userName;
	private String password;
	private String projectAreaName;
	
	/* RTC SCM Settings */
	private String sourceWorkspaceName;
	private String targetStreamName;
	private String programComponentName;
	private String copybookComponentName;
	private String programzProjectName;
	private String copybookzProjectName;
	private String defaultChangesetOwner;
	private String sourceFolderName;
	
	/* RTC Enterprise Extensions Settings */
	private String cobolFolderName;
	private String copybookFolderName;
	private String asmFolderName;
	private String jclFolderName;
	private String prmFolderName;
	private String cobolLangDefUUID;
	private String ooCobolLangDefUUID;
	private String cobolDb2LangDefUUID;
	private String copybookLangDefUUID;
	
	/* Internal Counters */
	private int fileCount;
	private int versionCount;
	private int skippedFileCount;
	private int skippedVersionCount;
	
	/* Default Properties */
	private Properties defaultProperties;
	
	/* Internal Project Map */
	private HashMap<String, Integer> projectMap;
	  
    /* Logger */
    private static final Logger LOGGER = Logger.getLogger(RTCCodeImporter.class);
	
	public RTCCodeImportManager() {
		//Default Constructor
		projectMap = new HashMap<String, Integer>();
		String defaultPropertiesFile = RTCCodeImporterConstants.DEFAULT_PROPERTIES_FILE;
		defaultProperties = new Properties();
		try {
			defaultProperties.load(new FileInputStream(defaultPropertiesFile));
		}
		catch (Exception e) {
			LOGGER.error("Unable to load default properties '" + defaultPropertiesFile + "'");
		}
		getProperties(defaultProperties);
	}
	
	public RTCCodeImportManager(String propertiesFile) {
		this();
    	Properties importProps = new Properties(defaultProperties);
    	if (propertiesFile.equals("")) {
    		LOGGER.info("No properties file specified, using default properties '" + 
    				RTCCodeImporterConstants.DEFAULT_PROPERTIES_FILE + "'");
    	}
    	else {
	    	try {
				importProps.load(new FileInputStream(propertiesFile));
			} catch (FileNotFoundException e) {
				LOGGER.error("Unable to find properties file '" + propertiesFile + "'", e);
				//e.printStackTrace();
			} catch (IOException e) {
				LOGGER.error("Error reading properties file '" + propertiesFile + "'");
				//e.printStackTrace();
			}
    	}
    	getProperties(importProps);
	}
		
	private void getProperties(Properties properties) {
		this.teamRepositoryURI = properties.getProperty(RTCCodeImporterConstants.RTC_REPO_URL_PROP);
		this.userName = properties.getProperty(RTCCodeImporterConstants.RTC_REPO_USERNAME_PROP);
		this.password = properties.getProperty(RTCCodeImporterConstants.RTC_REPO_PASSWORD_PROP);
		this.projectAreaName = properties.getProperty(RTCCodeImporterConstants.RTC_PROJECT_NAME_PROP);
		this.sourceWorkspaceName = properties.getProperty(RTCCodeImporterConstants.RTC_WORKSPACE_NAME_PROP);
		this.targetStreamName = properties.getProperty(RTCCodeImporterConstants.RTC_STREAM_NAME_PROP);
		this.programComponentName = properties.getProperty(RTCCodeImporterConstants.RTC_PGM_COMPONENT_NAME);
		this.copybookComponentName = properties.getProperty(RTCCodeImporterConstants.RTC_CPY_COMPONENT_NAME);
		this.programzProjectName = properties.getProperty(RTCCodeImporterConstants.RTC_PGM_ZPROJECT_NAME_PROP);
		this.copybookzProjectName = properties.getProperty(RTCCodeImporterConstants.RTC_CPY_ZPROJECT_NAME_PROP);
		this.defaultChangesetOwner = properties.getProperty(RTCCodeImporterConstants.RTC_DEFAULT_CS_OWNER_PROP);
		this.sourceFolderName = properties.getProperty(RTCCodeImporterConstants.SOURCE_FOLDER_PROP);
		
		this.cobolFolderName = properties.getProperty(RTCCodeImporterConstants.RTCZ_ZFOLDER_COBOL_PROP, 
				RTCCodeImporterConstants.DEFAULT_COBOL_ZFOLDER);
		this.copybookFolderName = properties.getProperty(RTCCodeImporterConstants.RTCZ_ZFOLDER_COPYBOOK_PROP, 
				RTCCodeImporterConstants.DEFAULT_COPYBOOK_ZFOLDER);
		this.asmFolderName = properties.getProperty(RTCCodeImporterConstants.RTCZ_ZFOLDER_ASM_PROP, 
				RTCCodeImporterConstants.DEFAULT_ASM_ZFOLDER);
		this.jclFolderName = properties.getProperty(RTCCodeImporterConstants.RTCZ_ZFOLDER_JCL_PROP, 
				RTCCodeImporterConstants.DEFAULT_JCL_ZFOLDER);
		this.prmFolderName = properties.getProperty(RTCCodeImporterConstants.RTCZ_ZFOLDER_PRM_PROP, 
				RTCCodeImporterConstants.DEFAULT_PRM_ZFOLDER);
		
		this.cobolLangDefUUID = properties.getProperty(RTCCodeImporterConstants.RTCZ_LANGDEFS_COBOL_UUID_PROP);
		this.ooCobolLangDefUUID = properties.getProperty(RTCCodeImporterConstants.RTCZ_LANGDEFS_OOCOBOL_UUID_PROP);
		this.cobolDb2LangDefUUID = properties.getProperty(RTCCodeImporterConstants.RTCZ_LANGDEFS_COBOLDB2_UUID_PROP);
		this.copybookLangDefUUID = properties.getProperty(RTCCodeImporterConstants.RTCZ_LANGDEFS_COPYBOOK_UUID_PROP);
	}
	
	private ArrayList<SourceFile> getSourceFileList() {
		FolderReader folderReader = new FolderReader();
		
		return folderReader.readFolderContents(this.getSourceFolderName(), LOGGER);
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
			String sourceWorkspaceName, String targetStreamName, String componentName, String path, IProgressMonitor monitor) 
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
        
        //Get Source File Version History
        TreeSet<SourceFileVersion> versionHistory = sourceFile.getVersionHistory();
        
        //for (int i = 0; i < history.size(); i++) {
        int version = 0;
        for (SourceFileVersion sourceFileVersion : versionHistory) {
        	String fileName = sourceFile.getName();
        	String filePath = sourceFile.getPath();
        	String versionIndex = sourceFileVersion.getVersionFileName();
        	String versionFilePath = filePath + "/" + versionIndex + "-" + fileName;
	    	File versionFile = new File(versionFilePath);
	    	LOGGER.info("File Version Path: " + versionFile.getAbsolutePath());
	    	
	    	if (!versionFile.exists()) {
	    		LOGGER.warn("Source Version File does not exist, but is listed in the audit file. Skipping this version.\n");
	    		this.skippedVersionCount++;
	    	}
	    	else {
		    	String createdBy = sourceFileVersion.getCreatedBy();
		    	Date creationDate = sourceFileVersion.getCreationDate();
		    	String project = sourceFileVersion.getProject();
		    	
		    	String comment = "Imported: " + fileName + " version: " + (version + 1)
		    			+ " Owner: " + createdBy + " Project: " + project;
		    	//System.out.println(comment);
		    	LOGGER.info(comment);
		    	LOGGER.info("Creation Date: " + creationDate);
		    	
		    	IContributor creator = null;
	            try {
	            	creator = RTCCodeImportUtils.findContributor(teamRepository, createdBy.toLowerCase(), monitor);
	            }
	            catch (TeamRepositoryException e) {
	            	//User Not Found in Repo
	            	if (e instanceof ItemNotFoundException) {
	            		creator = RTCCodeImportUtils.findContributor(teamRepository, this.getDefaultChangesetOwner(), 
	            				monitor);
	            		LOGGER.warn(e.getMessage());
	            		LOGGER.info("Using default user: " + creator.getName() + " (" + creator.getUserId() + ")");
	            	}
	            }
	            
	            //Create Work Item for Project
	            
	            //First check if Project Work Item already exists
	            if (this.getProjectMap().containsKey(project)) {
	            	int workitemId = this.getProjectMap().get(project);
	            	workItem = wiClient.findWorkItem(teamRepository, workitemId, monitor);
	            }
	            else {
		        	String workItemTypeId = "task";
		        	String devLineName = "Main Development";
		        	String summary = project;
		        	ICategory rootCategory = wiCommon.findCategories(projectArea, ICategory.DEFAULT_PROFILE, monitor).get(0);

		        	Timestamp creationTime = new Timestamp(creationDate.getTime());
		        	
		        	IDevelopmentLine devLine = WorkItemUtils.findDevelopmentLine(teamRepository, projectArea, devLineName, monitor);
		    		IIterationHandle currentIteration = devLine.getCurrentIteration();
		        	
		        	workItem = wiClient.createWorkItem(teamRepository, projectArea, workItemTypeId, summary, rootCategory, 
		        			creationTime, creator, creator, currentIteration, monitor);
		        	//Add to project map
		        	this.getProjectMap().put(project, workItem.getId());
	            }
	            
	            try {
			    	Map<String, String> attributes = sourceFile.getMetadata();
			    	
			    	//Set Language Definition
			    	LOGGER.info("Setting RTC language definition");
			    	
			    	String languageDef = "";
			    	
			    	SourceType sourceType = sourceFile.getSourceType();
			    	
			    	if (sourceType.equals(SourceType.COPYBOOK)) {
			    		languageDef = "COPYBOOK";
			    	}
			    	else {
				    	String language = attributes.get("Language");
				    	String db2 = attributes.get("DB2");
				    	String ooCobol = attributes.get("OOCobol");
				    	String apfAuth = attributes.get("APFAuth");
				    	
				    	switch (language) {
				    		case "CBLE" : 
				    			languageDef = "COBOL";
				    			if (ooCobol.equals("Y"))
				    				languageDef = "OOCOBOL";
				    			break;
				    		case "ASM" :
				    			languageDef = "ASM";
				    			if (apfAuth.equals("Y"))
				    				languageDef = "AuthASM";
				    		default :
				    			break;
				    	}
		    			if (db2.equals("Y"))
		    				languageDef = languageDef.concat("&DB2");
			    	}
	    			LOGGER.info("Language Definition is: " + languageDef);
	    			String langDefUUID = "";
	    			
	    			switch (languageDef) {
	    				case "COBOL" : 
	    					langDefUUID = cobolLangDefUUID;
	    					break;
	    				case "OOCOBOL" : 
	    					langDefUUID = ooCobolLangDefUUID;
	    					break;
	    				case "COBOL&DB2" : 
	    					langDefUUID = cobolDb2LangDefUUID;
	    					break;
	    				case "COPYBOOK" : 
	    					langDefUUID = copybookLangDefUUID;
	    					break;
	    			}
	            	
					//Commit File Version to Repository
					LOGGER.info("Committing file version to source control");
					LOGGER.info("Workspace path: " + path);
					fileItem = scmClient.addFileToSourceControl(teamRepository, versionFile, fileName, 
							sourceWorkspaceConnection, path, componentHandle, config, comment, creationDate, 
							creator, workItem, RTCCodeImporterConstants.LANGUAGE_DEFINITION_USER_PROPERTY, 
							langDefUUID, monitor);    			
			    	
			    	//Deliver change to Target Stream
			    	LOGGER.info("Delivering change set to stream");
			    	//System.out.println("Delivering change set to Stream");
			    	scmClient.deliverChangeSetsToStream(teamRepository, sourceWorkspaceConnection, targetStreamConnection, 
			    			componentHandle, monitor);
			    	
			    	//Get Versionable full state
			    	IVersionable versionable = config.fetchCompleteItem(fileItem, monitor);
			    	
			    	//Set custom attributes
			    	LOGGER.info("Setting custom attributes");
			    	//System.out.println("Setting custom attributes");
			    	
			    	try {
			    		ScmAttributeUtils.setAttributes(versionable, attributes, ScmUtils.getScmService(teamRepository));
			    	
			    		ScmAttributeUtils.printAttributes(versionable, ScmUtils.getScmService(teamRepository), LOGGER);
			    	}
			    	catch (TeamRepositoryException e) {
			    		LOGGER.error("Custom attributes are not defined in the project area");
			    		LOGGER.warn("Custom attributes will not be set");
			    	}
			    	
			    	//Close work item
			    	version++;
			    	versionCount++;
			    	LOGGER.info("");
	            }
	            catch (TeamRepositoryException e) {
	            	LOGGER.error("An error occurred importing this version: " + e.getMessage());
	            	LOGGER.warn("Skipping this version");
	            	skippedVersionCount++;
	            }
	    	}
        }
		
		return fileItem;		
	}
	
	private ArrayList<IFileItem> importSourceFilesToRTC(ArrayList<SourceFile> sourceFiles, 
			ITeamRepository teamRepository, String sourceWorkspaceName, String targetStreamName, 
			String componentName, String path, IProgressMonitor monitor) 
					throws TeamRepositoryException, IOException, ParseException {
		
		ArrayList<IFileItem> fileItems = new ArrayList<IFileItem>();
		
		for (SourceFile sourceFile : sourceFiles) {
			String targetComponentName = componentName;
			String targetPath = path;
			
			SourceType sourceType = sourceFile.getSourceType();
			
			switch (sourceType) {
				case COBOL : 
					targetComponentName = programComponentName;
					targetPath = programzProjectName.concat("/zOSsrc/").concat(cobolFolderName);
					break;
				case COPYBOOK :
					targetComponentName = copybookComponentName;
					targetPath = copybookzProjectName.concat("/zOSsrc/").concat(copybookFolderName);
					break;
				case ASSEMBLER:
					break;
				case JCL:
					break;
				case LINK:
					break;
				case LOAD:
					break;
				case OTHER:
					break;
				case PRM:
					break;
				case REXX:
					break;
				default:
					break;
			}
			IFileItem fileItem = importSourceFileToRTC(sourceFile, teamRepository, sourceWorkspaceName, 
					targetStreamName, targetComponentName, targetPath, monitor);
			fileItems.add(fileItem);
		}
		
		return fileItems;
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
            		this.getSourceWorkspaceName(), this.getTargetStreamName(), 
            		this.getProgramComponentName(), this.getProgramzProjectName(),  monitor);            
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
	
	public void doImport() {
		long start, end, total;
	    
	    //LOGGER.info("********************************************************************");
	    LOGGER.info("Started import process...\n");
	    long importStart = System.currentTimeMillis();
	    
	    start = System.currentTimeMillis();
	    LOGGER.info("Started reading source folders...");
	    //Get Source Files
	    ArrayList<SourceFile> sourceFileList = getSourceFileList();
	    end = System.currentTimeMillis();
	    
	    total = end - start;
	    String totalTimeReadingFiles = RTCCodeImportUtils.formatExecutionTime(total);
	    LOGGER.info("********************************************************************");
	    LOGGER.info("Completed reading source folders in " + totalTimeReadingFiles);
	    LOGGER.info("********************************************************************\n");
	    LOGGER.info("Start Importing file versions...");
	    start = System.currentTimeMillis();
	    //Import Source Files
	    doImport(sourceFileList);
	    end = System.currentTimeMillis();
	    
	    total = end - start;
	    String totalImportTime = RTCCodeImportUtils.formatExecutionTime(total);
	    LOGGER.info("********************************************************************");
	    LOGGER.info("Completed importing file versions in " + totalImportTime);
	    LOGGER.info("********************************************************************\n");
	    
	    int numberOfSourceFiles = sourceFileList.size();
	    int numberOfVersions = getVersionCount();
	    int numberOfSkippedVersions = getSkippedVersionCount();
	    
	    long importEnd = System.currentTimeMillis();
	    String totalTime = RTCCodeImportUtils.formatExecutionTime(importEnd - importStart);
	    
	    LOGGER.info("********************************************************************"); 
	    LOGGER.info("Import Complete in " + totalTime + ": " 
	    		+ numberOfSourceFiles + " Files, " + numberOfVersions + " Versions, "
	    		+ numberOfSkippedVersions + " Versions Skipped.");
	    LOGGER.info("********************************************************************");
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
	 * @return the programComponentName
	 */
	public String getProgramComponentName() {
		return programComponentName;
	}

	/**
	 * @param programComponentName the programComponentName to set
	 */
	public void setProgramComponentName(String programComponentName) {
		this.programComponentName = programComponentName;
	}

	/**
	 * @return the copybookComponentName
	 */
	public String getCopybookComponentName() {
		return copybookComponentName;
	}

	/**
	 * @param copybookComponentName the copybookComponentName to set
	 */
	public void setCopybookComponentName(String copybookComponentName) {
		this.copybookComponentName = copybookComponentName;
	}

	/**
	 * @return the programzProjectName
	 */
	public String getProgramzProjectName() {
		return programzProjectName;
	}

	/**
	 * @param programzProjectName the programzProjectName to set
	 */
	public void setProgramzProjectName(String programzProjectName) {
		this.programzProjectName = programzProjectName;
	}

	/**
	 * @return the copybookzProjectName
	 */
	public String getCopybookzProjectName() {
		return copybookzProjectName;
	}

	/**
	 * @param copybookzProjectName the copybookzProjectName to set
	 */
	public void setCopybookzProjectName(String copybookzProjectName) {
		this.copybookzProjectName = copybookzProjectName;
	}

	/**
	 * @return the defaultChangesetOwner
	 */
	public String getDefaultChangesetOwner() {
		return defaultChangesetOwner;
	}

	/**
	 * @param defaultChangesetOwner the defaultChangesetOwner to set
	 */
	public void setDefaultChangesetOwner(String defaultChangesetOwner) {
		this.defaultChangesetOwner = defaultChangesetOwner;
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
	 * @return the cobolFolderName
	 */
	public String getCobolFolderName() {
		return cobolFolderName;
	}

	/**
	 * @param cobolFolderName the cobolFolderName to set
	 */
	public void setCobolFolderName(String cobolFolderName) {
		this.cobolFolderName = cobolFolderName;
	}

	/**
	 * @return the copybookFolderName
	 */
	public String getCopybookFolderName() {
		return copybookFolderName;
	}

	/**
	 * @param copybookFolderName the copybookFolderName to set
	 */
	public void setCopybookFolderName(String copybookFolderName) {
		this.copybookFolderName = copybookFolderName;
	}

	/**
	 * @return the asmFolderName
	 */
	public String getAsmFolderName() {
		return asmFolderName;
	}

	/**
	 * @param asmFolderName the asmFolderName to set
	 */
	public void setAsmFolderName(String asmFolderName) {
		this.asmFolderName = asmFolderName;
	}

	/**
	 * @return the jclFolderName
	 */
	public String getJclFolderName() {
		return jclFolderName;
	}

	/**
	 * @param jclFolderName the jclFolderName to set
	 */
	public void setJclFolderName(String jclFolderName) {
		this.jclFolderName = jclFolderName;
	}

	/**
	 * @return the prmFolderName
	 */
	public String getPrmFolderName() {
		return prmFolderName;
	}

	/**
	 * @param prmFolderName the prmFolderName to set
	 */
	public void setPrmFolderName(String prmFolderName) {
		this.prmFolderName = prmFolderName;
	}

	/**
	 * @return the cobolLangDefUUID
	 */
	public String getCobolLangDefUUID() {
		return cobolLangDefUUID;
	}

	/**
	 * @param cobolLangDefUUID the cobolLangDefUUID to set
	 */
	public void setCobolLangDefUUID(String cobolLangDefUUID) {
		this.cobolLangDefUUID = cobolLangDefUUID;
	}

	/**
	 * @return the ooCobolLangDefUUID
	 */
	public String getOoCobolLangDefUUID() {
		return ooCobolLangDefUUID;
	}

	/**
	 * @param ooCobolLangDefUUID the ooCobolLangDefUUID to set
	 */
	public void setOoCobolLangDefUUID(String ooCobolLangDefUUID) {
		this.ooCobolLangDefUUID = ooCobolLangDefUUID;
	}

	/**
	 * @return the cobolDb2LangDefUUID
	 */
	public String getCobolDb2LangDefUUID() {
		return cobolDb2LangDefUUID;
	}

	/**
	 * @param cobolDb2LangDefUUID the cobolDb2LangDefUUID to set
	 */
	public void setCobolDb2LangDefUUID(String cobolDb2LangDefUUID) {
		this.cobolDb2LangDefUUID = cobolDb2LangDefUUID;
	}

	/**
	 * @return the copybookLangDefUUID
	 */
	public String getCopybookLangDefUUID() {
		return copybookLangDefUUID;
	}

	/**
	 * @param copybookLangDefUUID the copybookLangDefUUID to set
	 */
	public void setCopybookLangDefUUID(String copybookLangDefUUID) {
		this.copybookLangDefUUID = copybookLangDefUUID;
	}

	/**
	 * @return the fileCount
	 */
	public int getFileCount() {
		return fileCount;
	}

	/**
	 * @param fileCount the fileCount to set
	 */
	public void setFileCount(int fileCount) {
		this.fileCount = fileCount;
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

	/**
	 * @return the skippedFileCount
	 */
	public int getSkippedFileCount() {
		return skippedFileCount;
	}

	/**
	 * @param skippedFileCount the skippedFileCount to set
	 */
	public void setSkippedFileCount(int skippedFileCount) {
		this.skippedFileCount = skippedFileCount;
	}

	/**
	 * @return the skippedVersionCount
	 */
	public int getSkippedVersionCount() {
		return skippedVersionCount;
	}

	/**
	 * @param skippedVersionCount the skippedVersionCount to set
	 */
	public void setSkippedVersionCount(int skippedVersionCount) {
		this.skippedVersionCount = skippedVersionCount;
	}

	/**
	 * @return the projectMap
	 */
	public HashMap<String, Integer> getProjectMap() {
		return projectMap;
	}

	/**
	 * @param projectMap the projectMap to set
	 */
	public void setProjectMap(HashMap<String, Integer> projectMap) {
		this.projectMap = projectMap;
	}

}

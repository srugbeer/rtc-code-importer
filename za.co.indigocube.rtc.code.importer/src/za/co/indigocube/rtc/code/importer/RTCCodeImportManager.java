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
import com.ibm.team.process.common.IIteration;
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
import com.ibm.team.workitem.common.model.IWorkItemType;
import com.ibm.team.workitem.common.model.WorkItemLinkTypes;

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
	
	/* RTC Work Item Settings */
	private String projectWorkItemTypeId;
	private String changesetWorkItemTypeId;
	private String projectTimeline;
	private String oldSWRCodeIteration;
	private String maintenanceIteration;
	private String teamIdCategory;
	
	/* RTC Enterprise Extensions Settings */
	private String cobolFolderName;
	private String copybookFolderName;
	private String asmFolderName;
	private String jclFolderName;
	private String prmFolderName;
	
	private String cobolLangDefUUID;
	private String ooCobolLangDefUUID;
	private String cobolIMSLangDefUUID;
	private String ooCobolIMSLangDefUUID;
	private String cobolDb2LangDefUUID;
	private String ooCobolDb2LangDefUUID;
	private String cobolIMSDb2LangDefUUID;
	private String ooCobolIMSDb2LangDefUUID;
	
	private String asmLangDefUUID;
	private String authAsmLangDefUUID;
	private String asmIMSLangDefUUID;
	private String authAsmIMSLangDefUUID;
	private String asmDb2LangDefUUID;
	private String authAsmDb2LangDefUUID;
	private String asmIMSDb2LangDefUUID;
	private String authAsmIMSDb2LangDefUUID;
	
	private String copybookLangDefUUID;
	private String jclLangDefUUID;
	private String prmLangDefUUID;
	
	/* Metadata Column Headings */
	private String memberTypeMetadataColumn;
	private String compileTypeMetadataColumn;
	private String compileLanguageMetadataColumn;
	private String imsMetadataColumn;
	private String db2MetadataColumn;
	private String ooCobolMetadataColumn;
	private String apfAuthMetadataColumn;
	private String groupIdMetadataColumn;
	private String teamIdMetadataColumn;
	
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
		
		this.projectWorkItemTypeId = properties.getProperty(RTCCodeImporterConstants.RTC_PROJECT_WORKITEM_TYPE_ID_PROP, 
				RTCCodeImporterConstants.DEFAULT_PROJECT_WORKITEM_TYPE_ID);		
		this.changesetWorkItemTypeId = properties.getProperty(RTCCodeImporterConstants.RTC_CHANGESET_WORKITEM_TYPE_ID_PROP, 
				RTCCodeImporterConstants.DEFAULT_CHANGESET_WORKITEM_TYPE_ID);
		
		this.projectTimeline = properties.getProperty(RTCCodeImporterConstants.RTC_PROJECT_TIMELINE_PROP, 
				RTCCodeImporterConstants.WORKITEM_MAIN_DEVELOPMENT_TIMELINE);
		
		this.oldSWRCodeIteration = properties.getProperty(RTCCodeImporterConstants.RTC_OLD_SWR_CODE_ITERATION);
		
		this.maintenanceIteration = properties.getProperty(RTCCodeImporterConstants.RTC_MAINTENANCE_ITERATION);
		
		this.teamIdCategory = properties.getProperty(RTCCodeImporterConstants.RTC_TEAM_ID_CATEGORY);
		
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
		this.cobolIMSLangDefUUID = properties.getProperty(RTCCodeImporterConstants.RTCZ_LANGDEFS_COBOLIMS_UUID_PROP);
		this.ooCobolIMSLangDefUUID = properties.getProperty(RTCCodeImporterConstants.RTCZ_LANGDEFS_OOCOBOLIMS_UUID_PROP);
		this.cobolDb2LangDefUUID = properties.getProperty(RTCCodeImporterConstants.RTCZ_LANGDEFS_COBOLDB2_UUID_PROP);
		this.ooCobolDb2LangDefUUID = properties.getProperty(RTCCodeImporterConstants.RTCZ_LANGDEFS_OOCOBOLDB2_UUID_PROP);
		this.cobolIMSDb2LangDefUUID = properties.getProperty(RTCCodeImporterConstants.RTCZ_LANGDEFS_COBOLIMSDB2_UUID_PROP);
		this.ooCobolIMSDb2LangDefUUID = properties.getProperty(RTCCodeImporterConstants.RTCZ_LANGDEFS_OOCOBOLIMSDB2_UUID_PROP);
		
		this.asmLangDefUUID = properties.getProperty(RTCCodeImporterConstants.RTCZ_LANGDEFS_ASM_UUID_PROP);
		this.authAsmLangDefUUID = properties.getProperty(RTCCodeImporterConstants.RTCZ_LANGDEFS_AuthASM_UUID_PROP);
		this.asmIMSLangDefUUID = properties.getProperty(RTCCodeImporterConstants.RTCZ_LANGDEFS_ASMIMS_UUID_PROP);
		this.authAsmIMSLangDefUUID = properties.getProperty(RTCCodeImporterConstants.RTCZ_LANGDEFS_AuthASMIMS_UUID_PROP);
		this.asmDb2LangDefUUID = properties.getProperty(RTCCodeImporterConstants.RTCZ_LANGDEFS_ASMDB2_UUID_PROP);
		this.authAsmDb2LangDefUUID = properties.getProperty(RTCCodeImporterConstants.RTCZ_LANGDEFS_AuthASMDB2_UUID_PROP);
		this.asmIMSDb2LangDefUUID = properties.getProperty(RTCCodeImporterConstants.RTCZ_LANGDEFS_ASMIMSDB2_UUID_PROP);
		this.authAsmIMSDb2LangDefUUID = properties.getProperty(RTCCodeImporterConstants.RTCZ_LANGDEFS_AuthASMIMSDB2_UUID_PROP);

		this.copybookLangDefUUID = properties.getProperty(RTCCodeImporterConstants.RTCZ_LANGDEFS_COPYBOOK_UUID_PROP);
		this.prmLangDefUUID = properties.getProperty(RTCCodeImporterConstants.RTCZ_LANGDEFS_PRM_UUID_PROP);
		this.jclLangDefUUID = properties.getProperty(RTCCodeImporterConstants.RTCZ_LANGDEFS_JCL_UUID_PROP);
		
		this.memberTypeMetadataColumn = properties.getProperty(RTCCodeImporterConstants.METADATA_HEADER_MEMBER_TYPE_PROP);
		this.compileTypeMetadataColumn = properties.getProperty(RTCCodeImporterConstants.METADATA_HEADER_COMPILE_TYPE_PROP);
		this.compileLanguageMetadataColumn = properties.getProperty(RTCCodeImporterConstants.METADATA_HEADER_COMPILE_LANGUAGE_PROP);
		this.ooCobolMetadataColumn = properties.getProperty(RTCCodeImporterConstants.METADATA_HEADER_OOCOBOL_PROP);
		this.apfAuthMetadataColumn = properties.getProperty(RTCCodeImporterConstants.METADATA_HEADER_APF_AUTH_PROP);
		this.imsMetadataColumn = properties.getProperty(RTCCodeImporterConstants.METADATA_HEADER_IMS_PROP);
		this.db2MetadataColumn = properties.getProperty(RTCCodeImporterConstants.METADATA_HEADER_DB2_PROP);
		this.groupIdMetadataColumn = properties.getProperty(RTCCodeImporterConstants.METADATA_HEADER_GROUP_ID_PROP);
		this.teamIdMetadataColumn = properties.getProperty(RTCCodeImporterConstants.METADATA_HEADER_TEAM_ID_PROP);
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
	
	private String getLanguageDefinition(SourceFile sourceFile) {
		
		String languageDef = "";
		
		SourceType sourceType = sourceFile.getSourceType();
		Map<String, String> metadata = sourceFile.getMetadata();
		String ims = metadata.get(imsMetadataColumn);
    	String db2 = metadata.get(db2MetadataColumn);
		
		switch (sourceType) {
			case COPYBOOK :
				languageDef = "COPYBOOK";
				break;
			case PRM :
				languageDef = "PRM";
				break;
			case JCL :
				languageDef = "JCL";
				break;
			case REXX :
				languageDef = "REXX";
				break;
			case COBOL :
				String ooCobol = metadata.get(ooCobolMetadataColumn);
				languageDef = ooCobol.equals("Y") ? "OOCOBOL" : "COBOL";
				break;
			case ASSEMBLER :
				String apfAuth = metadata.get(apfAuthMetadataColumn);
				languageDef = apfAuth.equals("Y") ? "AuthASM" : "ASM";
				break;
			default :
				break;
		}
		languageDef = languageDef.concat((ims != null && ims.equals("Y")) ? "&IMS" : "");
		languageDef = languageDef.concat((db2 != null && db2.equals("Y")) ? "&DB2" : "");
		/*SourceType sourceType = sourceFile.getSourceType();
		Map<String, String> metadata = sourceFile.getMetadata();
		
		if (sourceType.equals(SourceType.COPYBOOK)) {
    		languageDef = "COPYBOOK";
    	}
    	else {
	    	String language = metadata.get("Language");
	    	String ims = metadata.get("IMS");
	    	String db2 = metadata.get("DB2");
	    	String ooCobol = metadata.get("OOCobol");
	    	String apfAuth = metadata.get("APFAuth");
	    	
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
	    	if (ims != null && ims.equals("Y"))
	    		languageDef = languageDef.concat("&IMS");
			if (db2 != null && db2.equals("Y"))
				languageDef = languageDef.concat("&DB2");
    	}*/
		return languageDef;
	}
	
	private String getLanguageDefinitionUUID(String languageDef) {
		String langDefUUID = "";
		
		switch (languageDef) {
			case "COBOL" : 
				langDefUUID = cobolLangDefUUID;
				break;
			case "OOCOBOL" : 
				langDefUUID = ooCobolLangDefUUID;
				break;
			case "COBOL&IMS" :
				langDefUUID = cobolIMSLangDefUUID;
				break;
			case "OOCOBOL&IMS" :
				langDefUUID = ooCobolIMSLangDefUUID;
				break;
			case "COBOL&DB2" : 
				langDefUUID = cobolDb2LangDefUUID;
				break;
			case "OOCOBOL&DB2" :
				langDefUUID = ooCobolDb2LangDefUUID;
				break;
			case "COBOL&IMS&DB2" :
				langDefUUID = cobolIMSDb2LangDefUUID;
				break;
			case "OOCOBOL&IMS&DB2" :
				langDefUUID = ooCobolIMSDb2LangDefUUID;
				break;				
			case "ASM" : 
				langDefUUID = asmLangDefUUID;
				break;
			case "AuthASM" : 
				langDefUUID = authAsmLangDefUUID;
				break;
			case "ASM&IMS" :
				langDefUUID = asmIMSLangDefUUID;
				break;
			case "AuthASM&IMS" :
				langDefUUID = authAsmIMSLangDefUUID;
				break;
			case "ASM&DB2" : 
				langDefUUID = asmDb2LangDefUUID;
				break;
			case "AuthASM&DB2" :
				langDefUUID = authAsmDb2LangDefUUID;
				break;
			case "ASM&IMS&DB2" :
				langDefUUID = asmIMSDb2LangDefUUID;
				break;
			case "AuthASM&IMS&DB2" :
				langDefUUID = authAsmIMSDb2LangDefUUID;
				break;
			case "COPYBOOK" : 
				langDefUUID = copybookLangDefUUID;
				break;
			case "PRM" :
				langDefUUID = prmLangDefUUID;
				break;
			case "JCL" :
				langDefUUID = jclLangDefUUID;
				break;
		}
		return langDefUUID;
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
        
        //Project and Change Set Work Items
        IWorkItem projectWorkItem = null;
        IWorkItem changeSetWorkItem = null;
        
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
		    	
		    	IContributor projectCreator = null;
	            try {
	            	projectCreator = RTCCodeImportUtils.findContributor(teamRepository, createdBy, monitor);
	            }
	            catch (TeamRepositoryException e) {
	            	//User Not Found in Repo
	            	if (e instanceof ItemNotFoundException) {
	            		projectCreator = RTCCodeImportUtils.findContributor(teamRepository, this.getDefaultChangesetOwner(), 
	            				monitor);
	            		LOGGER.warn(e.getMessage());
	            		LOGGER.info("Using default user: " + projectCreator.getName() + " (" + projectCreator.getUserId() + ")");
	            	}
	            }
	            
	            //Create Work Item for Project
	            
	            //First check if Project Work Item already exists
	            if (this.getProjectMap().containsKey(project)) {
	            	int projectWorkitemId = this.getProjectMap().get(project);
	            	LOGGER.info("Using existing Project Work Item " + projectWorkitemId);
	            	projectWorkItem = wiClient.findWorkItem(teamRepository, projectWorkitemId, monitor);
	            }
	            else {
	            	LOGGER.info("Creating new Project Work Item");
		        	String projectWorkItemTypeId = this.getProjectWorkItemTypeId();
		        	IWorkItemType projectWorkItemType = wiCommon.findWorkItemType(projectArea, projectWorkItemTypeId, monitor);
		        	if (projectWorkItemType == null) {
		        		LOGGER.warn("Unable to find Project Work Item Type '" + projectWorkItemTypeId + "'");
		        		LOGGER.info("Using default Project Work Item Type '" + 
		        				RTCCodeImporterConstants.DEFAULT_PROJECT_WORKITEM_TYPE_ID + "'");		        		
		        		projectWorkItemType = wiCommon.findWorkItemType(projectArea, 
		        				RTCCodeImporterConstants.DEFAULT_PROJECT_WORKITEM_TYPE_ID, monitor);		        	
		        	}
		        	
		        	String projectSummary = project;
		        	String projectDescription = projectSummary;
		        	String projectComment = null;
		        	
		        	String projectTeamIdCategoryName = getTeamIdCategory();
		        	ICategory projectTeamIdCategory = null;
		        	if (projectTeamIdCategoryName.equals("")) {
		        		LOGGER.warn("Team Id not set");
		        	}
		        	else {
		        		LOGGER.info("Team Id is '" + projectTeamIdCategoryName + "'");
		        		LOGGER.info("Looking for the Category that corresponds to the Team Id...");
		        		projectTeamIdCategory = WorkItemUtils.
		        				findCategory(teamRepository, projectArea, projectTeamIdCategoryName, monitor);
		        		if (projectTeamIdCategory != null) {
		        			LOGGER.info("Found Category corresponding to Team Id '" + projectTeamIdCategoryName + "'");		        			
		        		}
		        		else {
		        			LOGGER.warn("Unable to find Category corresponding to Team Id '" + projectTeamIdCategoryName + "'");
		        		}
		        	}
		        	
		        	if (projectTeamIdCategory == null) {
		        		LOGGER.info("Using Root category");
		        		projectTeamIdCategory = wiCommon.findAllCategories(projectArea, ICategory.DEFAULT_PROFILE, monitor).get(0);
		        	}

		        	Timestamp projectCreationTime = new Timestamp(creationDate.getTime());
		        	
		        	String devLineName = getProjectTimeline();
		        	IDevelopmentLine devLine = WorkItemUtils.findDevelopmentLine(teamRepository, projectArea, devLineName, monitor);
		    		
		    		IIterationHandle projectIteration = null;   		
		    		LOGGER.info("Checking if version references a SWR Code...");
		    		String swrCode = sourceFileVersion.getSoftwareReleaseCode();
		    		
		    		if (!swrCode.equals("")) { //SWR Code is not empty
		    			LOGGER.info("Version references SWR Code '" + swrCode + "'");
		    			LOGGER.info("Looking for Iteration that corresponds to the SWR Code...");
		    			IIteration swrCodeIteration = WorkItemUtils.
		    					findIteration(teamRepository, devLine, swrCode, monitor);
		    			
		    			if (swrCodeIteration != null) { //Found corresponding iteration
		    				LOGGER.info("Found Iteration corresponding to SWR Code '" + swrCode + "'");
		    				projectIteration = swrCodeIteration;
		    			}
		    			else { //Did not find corresponding iteration
		    				LOGGER.warn("Iteration corresponding to the SWR Code '" 
		    						+ swrCode + "' not found. It is possibly an older SWR Code");
		    				LOGGER.info("Searching for Old SWR Code Iteration...");
		    				IIteration oldSWRCodeIteration = WorkItemUtils.
		    						findIteration(teamRepository, devLine, getOldSWRCodeIteration(), monitor);
		    				if (oldSWRCodeIteration != null) {
		    					projectIteration = oldSWRCodeIteration;
		    					projectComment = "Original SWR Code was: " + swrCode;
		    				}
		    			}
		    		}
		    		else { //SWR Code is empty, most likely version was created via Maintenance route
		    			LOGGER.info("Version does not reference a SWR Code");
		    			LOGGER.info("This is most likely because the version was created via the Maintenance Route");
		    			LOGGER.info("Searching for iteration that corresponds to Maintenance");
		    			String maintenanceIterationName = getMaintenanceIteration();
		    			IIteration maintenanceIteration = WorkItemUtils.
		    					findIteration(teamRepository, devLine, maintenanceIterationName, monitor);
		    			
		    			if (maintenanceIteration != null) { //Found maintenance iteration
		    				LOGGER.info("Found Maintenance Iteration");
		    				projectIteration = maintenanceIteration;
		    			}
		    			else { //Maintenance iteration not found
		    				LOGGER.warn("Maintenance Iteration not found.");
		    			}
		    		}
		        	
		    		if (projectIteration == null) { //Unable to find correct iteration. Defaulting to current iteration
		    			LOGGER.info("Defaulting to using current iteration");
		    			projectIteration = devLine.getCurrentIteration();
		    		}
		    		
		        	projectWorkItem = wiClient.createWorkItem(teamRepository, projectArea, projectWorkItemType, 
		        			projectSummary, projectDescription, null, projectTeamIdCategory, projectCreationTime, 
		        			projectCreator, projectCreator, projectIteration, projectComment, monitor);
		        	//Add to project map
		        	this.getProjectMap().put(project, projectWorkItem.getId());
		        	
		        	//Close Project Work Item (i.e. set state to 'In P')
		        	LOGGER.info("Closing Project Work Item");
		        	String projectStateId = RTCCodeImporterConstants.RTC_WORKFLOW_PROJECT_IN_P;
		        	wiClient.setWorkItemState(teamRepository, projectWorkItem, projectStateId, monitor);
	            }
	            
	            try {
			    	//Map<String, String> metadata = sourceFile.getMetadata();
			    				    	
			    	//Set Language Definition
			    	LOGGER.info("Setting RTC language definition");
			    	
			    	//Get Language Definition for Source File
			    	String languageDef = getLanguageDefinition(sourceFile);

	    			LOGGER.info("Language Definition is: " + languageDef);
	    			
	    			//Get Language Definition UUID
	    			String langDefUUID = getLanguageDefinitionUUID(languageDef);
	    			
	    			//Create Work Item for Change Set
	    			LOGGER.info("Creating CSI Work Item for change set");
		        	String csiWorkItemTypeId = this.getChangesetWorkItemTypeId();
		        	IWorkItemType csiWorkItemType = wiCommon.findWorkItemType(projectArea, csiWorkItemTypeId, monitor);
		        	if (csiWorkItemType == null) {
		        		LOGGER.warn("Unable to find Changeset Work Item Type '" + csiWorkItemTypeId + "'");
		        		LOGGER.info("Using default Changeset Work Item Type '" + 
		        				RTCCodeImporterConstants.DEFAULT_CHANGESET_WORKITEM_TYPE_ID + "'");		        		
		        		csiWorkItemType = wiCommon.findWorkItemType(projectArea, 
		        				RTCCodeImporterConstants.DEFAULT_CHANGESET_WORKITEM_TYPE_ID, monitor);		        	
		        	}
		        			        			        	
		        	String csiSummary = comment;
		        	String csiDescription = csiSummary;
		        	String csiTeamIdCategoryName = sourceFileVersion.getTeamId();
		        	ICategory csiTeamIdCategory = null;
		        	if (csiTeamIdCategoryName.equals("")) {
		        		LOGGER.warn("Team Id not set for version");
		        	}
		        	else {
		        		LOGGER.info("Team Id is '" + csiTeamIdCategoryName + "'");
		        		LOGGER.info("Looking for the Category that corresponds to the Team Id...");
		        		csiTeamIdCategory = WorkItemUtils.
		        				findCategory(teamRepository, projectArea, csiTeamIdCategoryName, monitor);
		        		if (csiTeamIdCategory != null) {
		        			LOGGER.info("Found Category corresponding to Team Id '" + csiTeamIdCategoryName + "'");		        			
		        		}
		        		else {
		        			LOGGER.warn("Unable to find Category corresponding to Team Id '" + csiTeamIdCategoryName + "'");
		        		}
		        	}
		        	
		        	if (csiTeamIdCategory == null) {
		        		LOGGER.info("Using Root category");
		        		csiTeamIdCategory = wiCommon.findAllCategories(projectArea, ICategory.DEFAULT_PROFILE, monitor).get(0);
		        	}
		        	
		        	Timestamp csiCreationTime = new Timestamp(creationDate.getTime());
		        	
		        	//String devLineName = RTCCodeImporterConstants.WORKITEM_MAIN_DEVELOPMENT_TIMELINE;
		        	//IDevelopmentLine devLine = WorkItemUtils.findDevelopmentLine(teamRepository, projectArea, devLineName, monitor);
		    		//IIterationHandle currentIteration = devLine.getCurrentIteration();
		        	
		        	changeSetWorkItem = wiClient.createWorkItem(teamRepository, projectArea, 
		        			csiWorkItemType, csiSummary, csiDescription, null, csiTeamIdCategory, 
		        			csiCreationTime, projectCreator, projectCreator, projectWorkItem.getTarget(), null, monitor);
		        	
		        	//Link Change Set Work Item to Project Work Item
		        	String linkType = WorkItemLinkTypes.PARENT_WORK_ITEM;
		        	wiClient.createWorkItemLink(teamRepository, changeSetWorkItem, projectWorkItem, linkType, monitor);
	            	
					//Commit File Version to Repository
					LOGGER.info("Committing file version to source control");
					LOGGER.info("Workspace path: " + path);
					fileItem = scmClient.addFileToSourceControl(teamRepository, versionFile, fileName, 
							sourceWorkspaceConnection, path, componentHandle, config, comment, creationDate, 
							projectCreator, changeSetWorkItem, RTCCodeImporterConstants.LANGUAGE_DEFINITION_USER_PROPERTY, 
							langDefUUID, monitor);    			
			    	
			    	//Deliver change to Target Stream
			    	LOGGER.info("Delivering change set to stream");
			    	//System.out.println("Delivering change set to Stream");
			    	scmClient.deliverChangeSetsToStream(teamRepository, sourceWorkspaceConnection, 
			    			targetStreamConnection, componentHandle, monitor);
			    	
			    	//Get Versionable full state
			    	IVersionable versionable = config.fetchCompleteItem(fileItem, monitor);
			    	
			    	//Set custom attributes
			    	LOGGER.info("Setting custom attributes");
			    	//System.out.println("Setting custom attributes");
			    	
			    	try {
			    		ScmAttributeUtils.setAttributes(versionable, sourceFile.getMetadata(), 
			    				ScmUtils.getScmService(teamRepository));
			    	
			    		ScmAttributeUtils.printAttributes(versionable, 
			    				ScmUtils.getScmService(teamRepository), LOGGER);
			    	}
			    	catch (TeamRepositoryException e) {
			    		LOGGER.error("Custom attributes are not defined in the project area");
			    		LOGGER.warn("Custom attributes will not be set");
			    	}
			    	
			    	//Close CSI Work Item (i.e. set state to 'In P')
			    	LOGGER.info("Closing CSI Work Item");
		        	String csiStateId = RTCCodeImporterConstants.RTC_WORKFLOW_CSI_IN_P;
			    	wiClient.setWorkItemState(teamRepository, changeSetWorkItem, csiStateId, monitor);
			    	
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
					targetComponentName = programComponentName;
					targetPath = programzProjectName.concat("/zOSsrc/").concat(asmFolderName);
					break;
				case JCL:
					targetComponentName = programComponentName;
					targetPath = programzProjectName.concat("/zOSsrc/").concat(jclFolderName);
					break;
				case LINK:
					break;
				case LOAD:
					break;
				case OTHER:
					break;
				case PRM:
					targetComponentName = programComponentName;
					targetPath = programzProjectName.concat("/zOSsrc/").concat(prmFolderName);
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
	 * @return the projectWorkItemTypeId
	 */
	public String getProjectWorkItemTypeId() {
		return projectWorkItemTypeId;
	}

	/**
	 * @param projectWorkItemTypeId the projectWorkItemTypeId to set
	 */
	public void setProjectWorkItemId(String projectWorkItemTypeId) {
		this.projectWorkItemTypeId = projectWorkItemTypeId;
	}

	/**
	 * @return the changesetWorkItemTypeId
	 */
	public String getChangesetWorkItemTypeId() {
		return changesetWorkItemTypeId;
	}

	/**
	 * @param changesetWorkItemTypeId the changesetWorkItemTypeId to set
	 */
	public void setChangesetWorkItemId(String changesetWorkItemTypeId) {
		this.changesetWorkItemTypeId = changesetWorkItemTypeId;
	}

	/**
	 * @return the projectTimeline
	 */
	public String getProjectTimeline() {
		return projectTimeline;
	}

	/**
	 * @param projectTimeline the projectTimeline to set
	 */
	public void setProjectTimeline(String projectTimeline) {
		this.projectTimeline = projectTimeline;
	}

	/**
	 * @return the oldSWRCodeIteration
	 */
	public String getOldSWRCodeIteration() {
		return oldSWRCodeIteration;
	}

	/**
	 * @param oldSWRCodeIteration the oldSWRCodeIteration to set
	 */
	public void setOldSWRCodeIteration(String oldSWRCodeIteration) {
		this.oldSWRCodeIteration = oldSWRCodeIteration;
	}

	/**
	 * @return the maintenanceIteration
	 */
	public String getMaintenanceIteration() {
		return maintenanceIteration;
	}

	/**
	 * @param maintenanceIteration the maintenanceIteration to set
	 */
	public void setMaintenanceIteration(String maintenanceIteration) {
		this.maintenanceIteration = maintenanceIteration;
	}

	/**
	 * @return the teamIdCategory
	 */
	public String getTeamIdCategory() {
		return teamIdCategory;
	}

	/**
	 * @param teamIdCategory the teamIdCategory to set
	 */
	public void setTeamIdCategory(String teamIdCategory) {
		this.teamIdCategory = teamIdCategory;
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
	 * @return the cobolIMSLangDefUUID
	 */
	public String getCobolIMSLangDefUUID() {
		return cobolIMSLangDefUUID;
	}

	/**
	 * @param cobolIMSLangDefUUID the cobolIMSLangDefUUID to set
	 */
	public void setCobolIMSLangDefUUID(String cobolIMSLangDefUUID) {
		this.cobolIMSLangDefUUID = cobolIMSLangDefUUID;
	}

	/**
	 * @return the ooCobolIMSLangDefUUID
	 */
	public String getOoCobolIMSLangDefUUID() {
		return ooCobolIMSLangDefUUID;
	}

	/**
	 * @param ooCobolIMSLangDefUUID the ooCobolIMSLangDefUUID to set
	 */
	public void setOoCobolIMSLangDefUUID(String ooCobolIMSLangDefUUID) {
		this.ooCobolIMSLangDefUUID = ooCobolIMSLangDefUUID;
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
	 * @return the ooCobolDb2LangDefUUID
	 */
	public String getOoCobolDb2LangDefUUID() {
		return ooCobolDb2LangDefUUID;
	}

	/**
	 * @param ooCobolDb2LangDefUUID the ooCobolDb2LangDefUUID to set
	 */
	public void setOoCobolDb2LangDefUUID(String ooCobolDb2LangDefUUID) {
		this.ooCobolDb2LangDefUUID = ooCobolDb2LangDefUUID;
	}

	/**
	 * @return the cobolIMSDb2LangDefUUID
	 */
	public String getCobolIMSDb2LangDefUUID() {
		return cobolIMSDb2LangDefUUID;
	}

	/**
	 * @param cobolIMSDb2LangDefUUID the cobolIMSDb2LangDefUUID to set
	 */
	public void setCobolIMSDb2LangDefUUID(String cobolIMSDb2LangDefUUID) {
		this.cobolIMSDb2LangDefUUID = cobolIMSDb2LangDefUUID;
	}

	/**
	 * @return the ooCobolIMSDb2LangDefUUID
	 */
	public String getOoCobolIMSDb2LangDefUUID() {
		return ooCobolIMSDb2LangDefUUID;
	}

	/**
	 * @param ooCobolIMSDb2LangDefUUID the ooCobolIMSDb2LangDefUUID to set
	 */
	public void setOoCobolIMSDb2LangDefUUID(String ooCobolIMSDb2LangDefUUID) {
		this.ooCobolIMSDb2LangDefUUID = ooCobolIMSDb2LangDefUUID;
	}

	/**
	 * @return the asmLangDefUUID
	 */
	public String getAsmLangDefUUID() {
		return asmLangDefUUID;
	}

	/**
	 * @param asmLangDefUUID the asmLangDefUUID to set
	 */
	public void setAsmLangDefUUID(String asmLangDefUUID) {
		this.asmLangDefUUID = asmLangDefUUID;
	}

	/**
	 * @return the authAsmLangDefUUID
	 */
	public String getAuthAsmLangDefUUID() {
		return authAsmLangDefUUID;
	}

	/**
	 * @param authAsmLangDefUUID the authAsmLangDefUUID to set
	 */
	public void setAuthAsmLangDefUUID(String authAsmLangDefUUID) {
		this.authAsmLangDefUUID = authAsmLangDefUUID;
	}

	/**
	 * @return the asmIMSLangDefUUID
	 */
	public String getAsmIMSLangDefUUID() {
		return asmIMSLangDefUUID;
	}

	/**
	 * @param asmIMSLangDefUUID the asmIMSLangDefUUID to set
	 */
	public void setAsmIMSLangDefUUID(String asmIMSLangDefUUID) {
		this.asmIMSLangDefUUID = asmIMSLangDefUUID;
	}

	/**
	 * @return the authAsmIMSLangDefUUID
	 */
	public String getAuthAsmIMSLangDefUUID() {
		return authAsmIMSLangDefUUID;
	}

	/**
	 * @param authAsmIMSLangDefUUID the authAsmIMSLangDefUUID to set
	 */
	public void setAuthAsmIMSLangDefUUID(String authAsmIMSLangDefUUID) {
		this.authAsmIMSLangDefUUID = authAsmIMSLangDefUUID;
	}

	/**
	 * @return the asmDb2LangDefUUID
	 */
	public String getAsmDb2LangDefUUID() {
		return asmDb2LangDefUUID;
	}

	/**
	 * @param asmDb2LangDefUUID the asmDb2LangDefUUID to set
	 */
	public void setAsmDb2LangDefUUID(String asmDb2LangDefUUID) {
		this.asmDb2LangDefUUID = asmDb2LangDefUUID;
	}

	/**
	 * @return the authAsmDb2LangDefUUID
	 */
	public String getAuthAsmDb2LangDefUUID() {
		return authAsmDb2LangDefUUID;
	}

	/**
	 * @param authAsmDb2LangDefUUID the authAsmDb2LangDefUUID to set
	 */
	public void setAuthAsmDb2LangDefUUID(String authAsmDb2LangDefUUID) {
		this.authAsmDb2LangDefUUID = authAsmDb2LangDefUUID;
	}

	/**
	 * @return the asmIMSDb2LangDefUUID
	 */
	public String getAsmIMSDb2LangDefUUID() {
		return asmIMSDb2LangDefUUID;
	}

	/**
	 * @param asmIMSDb2LangDefUUID the asmIMSDb2LangDefUUID to set
	 */
	public void setAsmIMSDb2LangDefUUID(String asmIMSDb2LangDefUUID) {
		this.asmIMSDb2LangDefUUID = asmIMSDb2LangDefUUID;
	}

	/**
	 * @return the authAsmIMSDb2LangDefUUID
	 */
	public String getAuthAsmIMSDb2LangDefUUID() {
		return authAsmIMSDb2LangDefUUID;
	}

	/**
	 * @param authAsmIMSDb2LangDefUUID the authAsmIMSDb2LangDefUUID to set
	 */
	public void setAuthAsmIMSDb2LangDefUUID(String authAsmIMSDb2LangDefUUID) {
		this.authAsmIMSDb2LangDefUUID = authAsmIMSDb2LangDefUUID;
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
	 * @return the jclLangDefUUID
	 */
	public String getJclLangDefUUID() {
		return jclLangDefUUID;
	}

	/**
	 * @param jclLangDefUUID the jclLangDefUUID to set
	 */
	public void setJclLangDefUUID(String jclLangDefUUID) {
		this.jclLangDefUUID = jclLangDefUUID;
	}

	/**
	 * @return the prmLangDefUUID
	 */
	public String getPrmLangDefUUID() {
		return prmLangDefUUID;
	}

	/**
	 * @param prmLangDefUUID the prmLangDefUUID to set
	 */
	public void setPrmLangDefUUID(String prmLangDefUUID) {
		this.prmLangDefUUID = prmLangDefUUID;
	}

	/**
	 * @return the memberTypeMetadataColumn
	 */
	public String getMemberTypeMetadataColumn() {
		return memberTypeMetadataColumn;
	}

	/**
	 * @param memberTypeMetadataColumn the memberTypeMetadataColumn to set
	 */
	public void setMemberTypeMetadataColumn(String memberTypeMetadataColumn) {
		this.memberTypeMetadataColumn = memberTypeMetadataColumn;
	}

	/**
	 * @return the compileTypeMetadataColumn
	 */
	public String getCompileTypeMetadataColumn() {
		return compileTypeMetadataColumn;
	}

	/**
	 * @param compileTypeMetadataColumn the compileTypeMetadataColumn to set
	 */
	public void setCompileTypeMetadataColumn(String compileTypeMetadataColumn) {
		this.compileTypeMetadataColumn = compileTypeMetadataColumn;
	}

	/**
	 * @return the compileLanguageMetadataColumn
	 */
	public String getCompileLanguageMetadataColumn() {
		return compileLanguageMetadataColumn;
	}

	/**
	 * @param compileLanguageMetadataColumn the compileLanguageMetadataColumn to set
	 */
	public void setCompileLanguageMetadataColumn(
			String compileLanguageMetadataColumn) {
		this.compileLanguageMetadataColumn = compileLanguageMetadataColumn;
	}

	/**
	 * @return the imsMetadataColumn
	 */
	public String getImsMetadataColumn() {
		return imsMetadataColumn;
	}

	/**
	 * @param imsMetadataColumn the imsMetadataColumn to set
	 */
	public void setImsMetadataColumn(String imsMetadataColumn) {
		this.imsMetadataColumn = imsMetadataColumn;
	}

	/**
	 * @return the db2MetadataColumn
	 */
	public String getDb2MetadataColumn() {
		return db2MetadataColumn;
	}

	/**
	 * @param db2MetadataColumn the db2MetadataColumn to set
	 */
	public void setDb2MetadataColumn(String db2MetadataColumn) {
		this.db2MetadataColumn = db2MetadataColumn;
	}

	/**
	 * @return the ooCobolMetadataColumn
	 */
	public String getOoCobolMetadataColumn() {
		return ooCobolMetadataColumn;
	}

	/**
	 * @param ooCobolMetadataColumn the ooCobolMetadataColumn to set
	 */
	public void setOoCobolMetadataColumn(String ooCobolMetadataColumn) {
		this.ooCobolMetadataColumn = ooCobolMetadataColumn;
	}

	/**
	 * @return the apfAuthMetadataColumn
	 */
	public String getApfAuthMetadataColumn() {
		return apfAuthMetadataColumn;
	}

	/**
	 * @param apfAuthMetadataColumn the apfAuthMetadataColumn to set
	 */
	public void setApfAuthMetadataColumn(String apfAuthMetadataColumn) {
		this.apfAuthMetadataColumn = apfAuthMetadataColumn;
	}

	/**
	 * @return the groupIdMetadataColumn
	 */
	public String getGroupIdMetadataColumn() {
		return groupIdMetadataColumn;
	}

	/**
	 * @param groupIdMetadataColumn the groupIdMetadataColumn to set
	 */
	public void setGroupIdMetadataColumn(String groupIdMetadataColumn) {
		this.groupIdMetadataColumn = groupIdMetadataColumn;
	}

	/**
	 * @return the teamIdMetadataColumn
	 */
	public String getTeamIdMetadataColumn() {
		return teamIdMetadataColumn;
	}

	/**
	 * @param teamIdMetadataColumn the teamIdMetadataColumn to set
	 */
	public void setTeamIdMetadataColumn(String teamIdMetadataColumn) {
		this.teamIdMetadataColumn = teamIdMetadataColumn;
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
	 * @return the defaultProperties
	 */
	public Properties getDefaultProperties() {
		return defaultProperties;
	}

	/**
	 * @param defaultProperties the defaultProperties to set
	 */
	public void setDefaultProperties(Properties defaultProperties) {
		this.defaultProperties = defaultProperties;
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

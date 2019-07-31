/**
 * 
 */
package za.co.indigocube.rtc.code.importer;

/**
 * @author Sudheer
 *
 */
public class RTCCodeImporterConstants {
	
	/* Property Name Constants */
	
	//RTC Repository Properties
	public static final String RTC_REPO_URL_PROP = "rtc.repo.url";
	public static final String RTC_REPO_USERNAME_PROP = "rtc.repo.username";
	public static final String RTC_REPO_PASSWORD_PROP = "rtc.repo.password";
	public static final String RTC_PROJECT_NAME_PROP = "rtc.project.name";
	
	//RTC SCM Properties
	public static final String RTC_STREAM_NAME_PROP = "rtc.stream.name";
	public static final String RTC_WORKSPACE_NAME_PROP = "rtc.workspace.name";
    public static final String RTC_PGM_COMPONENT_NAME = "rtc.pgm.component.name";
    public static final String RTC_CPY_COMPONENT_NAME = "rtc.cpy.component.name";
    public static final String RTC_PGM_ZPROJECT_NAME_PROP = "rtc.pgm.zproject.name";
    public static final String RTC_CPY_ZPROJECT_NAME_PROP = "rtc.cpy.zproject.name";
    public static final String RTC_DEFAULT_CS_OWNER_PROP = "rtc.default.changeset.owner";
    
    //RTC Work Item Properties
    public static final String RTC_PROJECT_WORKITEM_TYPE_ID_PROP = "rtc.project.workitem.type.id";
    public static final String RTC_CHANGESET_WORKITEM_TYPE_ID_PROP = "rtc.changeset.workitem.type.id";
	public static final String RTC_PROJECT_TIMELINE_PROP = "rtc.project.timeline";
    public static final String RTC_OLD_SWR_CODE_ITERATION = "rtc.old.swr.code.iteration";
    public static final String RTC_MAINTENANCE_ITERATION = "rtc.maintenance.iteration";
    public static final String RTC_TEAM_ID_CATEGORY = "rtc.team.id.category";
    
    //RTC Work Item Workflow Properties
    public static final String RTC_WORKFLOW_PROJECT_IN_P = "za.co.absa.workitem.projectWorkflow.state.s10";
    public static final String RTC_WORKFLOW_CSI_IN_P = "za.co.absa.workitem.changeSetItemWorkflow.state.s8";
    
    //RTC Enterprise Extensions Language Definition Properties
    public static final String RTCZ_LANGDEFS_COBOL_UUID_PROP = "rtcz.langdefs.COBOL.uuid";
    public static final String RTCZ_LANGDEFS_OOCOBOL_UUID_PROP = "rtcz.langdefs.OOCOBOL.uuid";
    public static final String RTCZ_LANGDEFS_COBOLIMS_UUID_PROP = "rtcz.langdefs.COBOL&IMS.uuid";
    public static final String RTCZ_LANGDEFS_OOCOBOLIMS_UUID_PROP = "rtcz.langdefs.OOCOBOL&IMS.uuid";    
    public static final String RTCZ_LANGDEFS_COBOLDB2_UUID_PROP = "rtcz.langdefs.COBOL&DB2.uuid";
    public static final String RTCZ_LANGDEFS_OOCOBOLDB2_UUID_PROP = "rtcz.langdefs.OOCOBOL&DB2.uuid";
    public static final String RTCZ_LANGDEFS_COBOLIMSDB2_UUID_PROP = "rtcz.langdefs.COBOL&IMS&DB2.uuid";
    public static final String RTCZ_LANGDEFS_OOCOBOLIMSDB2_UUID_PROP = "rtcz.langdefs.OOCOBOL&IMS&DB2.uuid";
    public static final String RTCZ_LANGDEFS_ASM_UUID_PROP = "rtcz.langdefs.ASM.uuid";
    public static final String RTCZ_LANGDEFS_AuthASM_UUID_PROP = "rtcz.langdefs.AuthASM.uuid";
    public static final String RTCZ_LANGDEFS_ASMIMS_UUID_PROP = "rtcz.langdefs.ASM&IMS.uuid";
    public static final String RTCZ_LANGDEFS_AuthASMIMS_UUID_PROP = "rtcz.langdefs.AuthASM&IMS.uuid";
    public static final String RTCZ_LANGDEFS_ASMDB2_UUID_PROP = "rtcz.langdefs.ASM&DB2.uuid";
    public static final String RTCZ_LANGDEFS_AuthASMDB2_UUID_PROP = "rtcz.langdefs.AuthASM&DB2.uuid";
    public static final String RTCZ_LANGDEFS_ASMIMSDB2_UUID_PROP = "rtcz.langdefs.ASM&IMS&DB2.uuid";
    public static final String RTCZ_LANGDEFS_AuthASMIMSDB2_UUID_PROP = "rtcz.langdefs.AuthASM&IMS&DB2.uuid";
    public static final String RTCZ_LANGDEFS_COPYBOOK_UUID_PROP = "rtcz.langdefs.COPYBOOK.uuid";
    public static final String RTCZ_LANGDEFS_PRM_UUID_PROP = "rtcz.langdefs.PRM.uuid";
    public static final String RTCZ_LANGDEFS_JCL_UUID_PROP = "rtcz.langdefs.JCL.uuid";
    
    //RTC Enterprise Extensions zFolder Properties
    public static final String RTCZ_ZFOLDER_COBOL_PROP = "rtcz.zfolder.cobol";
    public static final String RTCZ_ZFOLDER_COPYBOOK_PROP = "rtcz.zfolder.copybook";
    public static final String RTCZ_ZFOLDER_ASM_PROP = "rtcz.zfolder.asm";
    public static final String RTCZ_ZFOLDER_JCL_PROP = "rtcz.zfolder.jcl";
    public static final String RTCZ_ZFOLDER_PRM_PROP = "rtcz.zfolder.prm";
       
    /* Metadata Column Heading Properties */
    public static final String METADATA_HEADER_MEMBER_TYPE_PROP = "metadata.header.membertype";
    public static final String METADATA_HEADER_COMPILE_TYPE_PROP = "metadata.header.compiletype";
    public static final String METADATA_HEADER_COMPILE_LANGUAGE_PROP = "metadata.header.compilelanguage";
    public static final String METADATA_HEADER_IMS_PROP = "metadata.header.ims";
    public static final String METADATA_HEADER_DB2_PROP = "metadata.header.db2";
    public static final String METADATA_HEADER_OOCOBOL_PROP = "metadata.header.oocobol";
    public static final String METADATA_HEADER_APF_AUTH_PROP = "metadata.header.apfauth";
    public static final String METADATA_HEADER_GROUP_ID_PROP = "metadata.header.groupid";
    public static final String METADATA_HEADER_TEAM_ID_PROP = "metadata.header.teamid";
    
    //Source Folder Property
    public static final String SOURCE_FOLDER_PROP = "source.folder";
    
    /* RTC Work Item Type Defaults */
    public static final String DEFAULT_PROJECT_WORKITEM_TYPE_ID = "com.ibm.team.apt.workItemType.story";
    public static final String DEFAULT_CHANGESET_WORKITEM_TYPE_ID = "task";
    public static final String WORKITEM_MAIN_DEVELOPMENT_TIMELINE = "Main Development";
    
    /* zFolder Constants */
    public static final String DEFAULT_COBOL_ZFOLDER = "COBOL";
    public static final String DEFAULT_COPYBOOK_ZFOLDER = "COPYBOOK";
    public static final String DEFAULT_ASM_ZFOLDER = "ASM";
    public static final String DEFAULT_JCL_ZFOLDER = "JCL";
    public static final String DEFAULT_PRM_ZFOLDER = "PRM";
    
    /* Language Definition User Property */
    public static final String LANGUAGE_DEFINITION_USER_PROPERTY = "team.enterprise.language.definition";
    
    /* Default Properties File */
    public static final String DEFAULT_PROPERTIES_FILE = "resources/import.properties";
}

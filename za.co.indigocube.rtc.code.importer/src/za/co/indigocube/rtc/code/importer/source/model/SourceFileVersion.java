/**
 * 
 */
package za.co.indigocube.rtc.code.importer.source.model;

import java.util.Date;

/**
 * @author Sudheer
 *
 */
public class SourceFileVersion implements Comparable<SourceFileVersion> {
	//private int fVersion;
	private String fVersionFileName;
	private Date fCreationDate;
	private String fCreatedBy;
	private String fProject;
	private String fSoftwareReleaseCode;
	private String fTeamId;
	
	/**
	 * @param createdBy
	 * @param creationDate
	 */
	public SourceFileVersion(String createdBy, Date creationDate) {
		this("", createdBy, creationDate, "", "", "");
	}
	
	/**
	 * @param versionFileName
	 * @param createdDate
	 * @param creationBy
	 */
	public SourceFileVersion(String versionFileName, String createdBy, Date creationDate) {
		this(versionFileName, createdBy, creationDate, "", "", "");
	}
	
	/**
	 * @param versionFileName
	 * @param createdDate
	 * @param creationBy
	 * @param project
	 */
	public SourceFileVersion(String versionFileName, String createdBy, Date creationDate, String project) {
		this(versionFileName, createdBy, creationDate, project, "", "");
	}
	
	/**
	 * @param versionFileName
	 * @param createdDate
	 * @param creationBy
	 * @param project
	 * @param swrCode
	 */
	public SourceFileVersion(String versionFileName, String createdBy, Date creationDate, 
			String project, String swrCode) {
		this(versionFileName, createdBy, creationDate, project, swrCode, "");
	}
	
	/**
	 * @param versionFileName
	 * @param createdDate
	 * @param creationBy
	 * @param project
	 * @param swrCode
	 * @param teamId
	 */
	public SourceFileVersion(String versionFileName, String createdBy, Date creationDate, 
			String project, String swrCode, String teamId) {
		setVersionFileName(versionFileName);
		setCreationDate(creationDate);		
		setCreatedBy(createdBy);
		setProject(project);
		setSoftwareReleaseCode(swrCode);
		setTeamId(teamId);
	}
	
	/**
	 * @return the versionFileName
	 */
	public String getVersionFileName() {
		return fVersionFileName;
	}

	/**
	 * @param versionFileName the versionFileName to set
	 */
	public void setVersionFileName(String versionFileName) {
		this.fVersionFileName = versionFileName;
	}

	/**
	 * @return the createdBy
	 */
	public String getCreatedBy() {
		return fCreatedBy;
	}
	/**
	 * @param createdBy the createdBy to set
	 */
	public void setCreatedBy(String createdBy) {
		this.fCreatedBy = createdBy;
	}
	/**
	 * @return the creationDate
	 */
	public Date getCreationDate() {
		return fCreationDate;
	}
	/**
	 * @param creationDate the creationDate to set
	 */
	public void setCreationDate(Date creationDate) {
		this.fCreationDate = creationDate;
	}
	
	/**
	 * @return the Project
	 */
	public String getProject() {
		return fProject;
	}

	/**
	 * @param project the Project to set
	 */
	public void setProject(String project) {
		this.fProject = project;
	}

	/**
	 * @return the softwareReleaseCode
	 */
	public String getSoftwareReleaseCode() {
		return fSoftwareReleaseCode;
	}

	/**
	 * @param softwareReleaseCode the softwareReleaseCode to set
	 */
	public void setSoftwareReleaseCode(String softwareReleaseCode) {
		this.fSoftwareReleaseCode = softwareReleaseCode;
	}

	/**
	 * @return the teamId
	 */
	public String getTeamId() {
		return fTeamId;
	}

	/**
	 * @param teamId the teamId to set
	 */
	public void setTeamId(String teamId) {
		this.fTeamId = teamId;
	}

	@Override
	public String toString() {
		return "Version File: " + getVersionFileName() + 
				"; Creation Date: " + getCreationDate() + 
				"; Created By: " + getCreatedBy() + 
				"; Project: " + getProject() +
				"; SWR Code: " + getSoftwareReleaseCode() + 
				"; Team Id: " + getTeamId();
	}

	@Override
	public int compareTo(SourceFileVersion o) {
		return this.fCreationDate.compareTo(o.fCreationDate);
	}

}

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
	
	/**
	 * @param createdBy
	 * @param creationDate
	 */
	public SourceFileVersion(String createdBy, Date creationDate) {
		//this.setVersion(version);
		new SourceFileVersion("", createdBy, creationDate);
	}
	
	/**
	 * @param versionFileName
	 * @param createdDate
	 * @param creationBy
	 */
	public SourceFileVersion(String versionFileName, String createdBy, Date creationDate) {
		//this.setVersion(version);
		new SourceFileVersion(versionFileName, createdBy, creationDate, "");
	}
	
	/**
	 * @param versionFileName
	 * @param createdDate
	 * @param creationBy
	 * @param project
	 */
	public SourceFileVersion(String versionFileName, String createdBy, Date creationDate, String project) {
		//this.setVersion(version);
		this.setVersionFileName(versionFileName);
		this.setCreationDate(creationDate);		
		this.setCreatedBy(createdBy);
		this.setProject(project);
	}
	
	/**
	 * @return the version
	 */
	/*public int getVersion() {
		return fVersion;
	}*/
	/**
	 * @param version the version to set
	 */
	/*public void setVersion(int version) {
		this.fVersion = version;
	}*/
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

	@Override
	public String toString() {
		return "Version File: " + this.getVersionFileName() + 
				"; Creation Date: " + this.getCreationDate() + 
				"; Created By: " + this.getCreatedBy() + 
				"; Project: " + this.getProject();
	}

	@Override
	public int compareTo(SourceFileVersion o) {
		return this.fCreationDate.compareTo(o.fCreationDate);
	}

}

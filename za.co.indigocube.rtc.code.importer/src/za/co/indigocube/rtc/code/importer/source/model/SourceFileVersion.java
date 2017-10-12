/**
 * 
 */
package za.co.indigocube.rtc.code.importer.source.model;

/**
 * @author Sudheer
 *
 */
public class SourceFileVersion {
	//private int fVersion;
	private String fVersionFileName;
	private String fCreatedBy;
	private String fCreationDate;
	/**
	 * @param createdBy
	 * @param creationDate
	 */
	public SourceFileVersion(String createdBy, String creationDate) {
		//this.setVersion(version);
		this.setCreatedBy(createdBy);
		this.setCreationDate(creationDate);
		this.setVersionFileName("");
	}
	
	/**
	 * @param versionFileName
	 * @param createdBy
	 * @param creationDate
	 */
	public SourceFileVersion(String versionFileName, String createdBy, String creationDate) {
		//this.setVersion(version);
		this.setVersionFileName(versionFileName);
		this.setCreatedBy(createdBy);
		this.setCreationDate(creationDate);		
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
	public String getCreationDate() {
		return fCreationDate;
	}
	/**
	 * @param creationDate the creationDate to set
	 */
	public void setCreationDate(String creationDate) {
		this.fCreationDate = creationDate;
	}
	
	@Override
	public String toString() {
		return "Version File: " + this.getVersionFileName() + ", Creation Date: " + 
					this.getCreationDate() + ", Created By: " + this.getCreatedBy();
	}

}

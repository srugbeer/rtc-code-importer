/**
 * 
 */
package za.co.indigocube.rtc.code.importer.source.model;

import java.util.Map;
import java.util.TreeSet;

/**
 * @author Sudheer
 *
 */
public class SourceFile {
	private String fName;
	private String fPath;
	private SourceType fSourceType;
	private Map<String, String> fMetadata;
	//private Map<Integer, SourceFileVersion> fHistory;
	private TreeSet<SourceFileVersion> fVersionHistory;
	private int fNumberOfVersions;
	
	/**
	 * @param name
	 * @param metadata
	 * @param versionHistory
	 */
	public SourceFile(String name, String path, Map<String, String> metadata,
			TreeSet<SourceFileVersion> versionHistory) {
		super();
		setName(name);
		setPath(path);
		setMetadata(metadata);
		setVersionHistory(versionHistory);
		setNumberOfVersions(versionHistory.size());
		String fileExtension = name.substring(name.lastIndexOf(".") + 1).toLowerCase();
		switch (fileExtension) {
			case "cob" :
			case "cbl" : setSourceType(SourceType.COBOL);
				break;
			case "cpy" : setSourceType(SourceType.COPYBOOK);
				break;
			case "asm" : setSourceType(SourceType.ASSEMBLER);
				break;
			case "jcl" : setSourceType(SourceType.JCL);
				break;
			case "prm" : setSourceType(SourceType.PRM);
				break;
			case "lnk" :
			case "link": setSourceType(SourceType.LINK);
				break;
			case "load": setSourceType(SourceType.LOAD);
				break;
			case "rex" :
			case "rexx": setSourceType(SourceType.REXX);
				break;
			default : setSourceType(SourceType.OTHER);
		}
	}
	/**
	 * @param name
	 * @param metadata
	 * @param history
	 */
/*	public SourceFile(String name, String path, Map<String, String> metadata,
			Map<Integer, SourceFileVersion> history) {
		super();
		this.setName(name);
		this.setPath(path);
		this.setMetadata(metadata);
		this.setHistory(history);
		this.setNumberOfVersions(history.size());
	}*/

	/**
	 * @return the name
	 */
	public String getName() {
		return fName;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.fName = name;
	}

	/**
	 * @return the Path
	 */
	public String getPath() {
		return fPath;
	}
	/**
	 * @param path the Path to set
	 */
	public void setPath(String path) {
		this.fPath = path;
	}
	/**
	 * @return the sourceType
	 */
	public SourceType getSourceType() {
		return fSourceType;
	}
	/**
	 * @param sourceType the sourceType to set
	 */
	public void setSourceType(SourceType sourceType) {
		this.fSourceType = sourceType;
	}
	/**
	 * @return the metadata
	 */
	public Map<String, String> getMetadata() {
		return fMetadata;
	}

	/**
	 * @param metadata the metadata to set
	 */
	public void setMetadata(Map<String, String> metadata) {
		this.fMetadata = metadata;
	}
	
	/**
	 * @return the versionHistory
	 */
	public TreeSet<SourceFileVersion> getVersionHistory() {
		return fVersionHistory;
	}

	/**
	 * @param versionHistory the versionHistory to set
	 */
	public void setVersionHistory(TreeSet<SourceFileVersion> versionHistory) {
		this.fVersionHistory = versionHistory;
	}

	/**
	 * @return the history
	 */
//	public Map<Integer, SourceFileVersion> getHistory() {
//		return fHistory;
//	}

	/**
	 * @param history the history to set
	 */
//	public void setHistory(Map<Integer, SourceFileVersion> history) {
//		this.fHistory = history;
//	}
//	
	/**
	 * @return the numberOfVersions
	 */
	public int getNumberOfVersions() {
		return fNumberOfVersions;
	}

	/**
	 * @param numberOfVersions the numberOfVersions to set
	 */
	public void setNumberOfVersions(int numberOfVersions) {
		this.fNumberOfVersions = numberOfVersions;
	}

	@Override
	public String toString() {
		String string = "Filename: " + this.getName() + 
				"\nMetadata: " + this.getMetadata() + 
				"\nNumber of Versions: " + this.getNumberOfVersions() +
				"\nVersion History:\n\t" + getVersionHistory();
		return string;
	}

}

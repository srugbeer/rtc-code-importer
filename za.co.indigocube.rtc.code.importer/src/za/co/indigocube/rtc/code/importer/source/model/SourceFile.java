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
	private Map<String, String> fMetadata;
	private Map<Integer, SourceFileVersion> fHistory;
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
		this.setName(name);
		this.setPath(path);
		this.setMetadata(metadata);
		this.setVersionHistory(versionHistory);
		this.setNumberOfVersions(versionHistory.size());
	}
	/**
	 * @param name
	 * @param metadata
	 * @param history
	 */
	public SourceFile(String name, String path, Map<String, String> metadata,
			Map<Integer, SourceFileVersion> history) {
		super();
		this.setName(name);
		this.setPath(path);
		this.setMetadata(metadata);
		this.setHistory(history);
		this.setNumberOfVersions(history.size());
	}

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
	public Map<Integer, SourceFileVersion> getHistory() {
		return fHistory;
	}

	/**
	 * @param history the history to set
	 */
	public void setHistory(Map<Integer, SourceFileVersion> history) {
		this.fHistory = history;
	}
	
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

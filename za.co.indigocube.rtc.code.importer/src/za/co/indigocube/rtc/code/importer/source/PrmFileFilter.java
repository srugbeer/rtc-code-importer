/**
 * 
 */
package za.co.indigocube.rtc.code.importer.source;

import java.io.File;
import java.io.FileFilter;

/**
 * @author Sudheer
 *
 */
public class PrmFileFilter implements FileFilter {

	/* (non-Javadoc)
	 * @see java.io.FileFilter#accept(java.io.File)
	 */
	@Override
	public boolean accept(File file) {
		if (file.isFile() && file.getName().toLowerCase().endsWith(".prm"))
			return true;
		else 
			return false;
	}
}
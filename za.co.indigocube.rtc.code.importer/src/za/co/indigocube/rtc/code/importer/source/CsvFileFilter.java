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
public class CsvFileFilter implements FileFilter {

	/* (non-Javadoc)
	 * @see java.io.FileFilter#accept(java.io.File)
	 */
	@Override
	public boolean accept(File pathname) {
		if (pathname.isFile() && pathname.getName().endsWith("csv"))
			return true;
		else
			return false;
	}

}

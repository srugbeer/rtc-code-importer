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
public class MainframeSourceFileFilter implements FileFilter {

	/* (non-Javadoc)
	 * @see java.io.FileFilter#accept(java.io.File)
	 */
	@Override
	public boolean accept(File file) {
		
		return ((new AssemblerFileFilter()).accept(file)) ||
				(new CobolFileFilter().accept(file)) ||
				(new CopybookFileFilter().accept(file)) ||
				(new JclFileFilter().accept(file)) ||
				(new PrmFileFilter().accept(file));
		
//		if (pathname.isFile() && 
//				(pathname.getName().endsWith(".cbl") ||
//				 pathname.getName().endsWith(".cpy") ||
//				 pathname.getName().endsWith(".jcl") ||
//				 pathname.getName().endsWith(".asm")))
//			return true;
//		else 
//			return false;
	}

}

/**
 * 
 */
package za.co.indigocube.rtc.code.importer;


/**
 * @author Sudheer
 *
 */
public class RTCCodeImporter {
	  
	/**
	 * @param args
	 */
    public static void main(String[] args) {
    	String propertiesFile = "";
    	if (args.length > 0) {
    		propertiesFile = args[0];
    	}
    	
    	RTCCodeImportManager rtcCodeImportManager = new RTCCodeImportManager(propertiesFile);
    	rtcCodeImportManager.doImport();
    }
}

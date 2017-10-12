/**
 * 
 */
package za.co.indigocube.rtc.code.importer.source;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import za.co.indigocube.rtc.code.importer.source.model.SourceFileVersion;

/**
 * @author Sudheer
 *
 */
public class CsvFileReader {
	
	public static Map<String, String> readMetadataCsvFile(File metadataFile) {
		
		String[] metadataNames = null, metadataValues = null;
		Map<String, String> metadataMap = new HashMap<String, String>();
		
		BufferedReader br = null;
		String line = "";
		String seperator = ",";
		try {
            br = new BufferedReader(new FileReader(metadataFile));
            
            //Read header line
            line = br.readLine();
            if (line != null) {
            	metadataNames = line.split(seperator);
            }
            while ((line = br.readLine()) != null) {
                // use comma as separator
                metadataValues = line.split(seperator);
                for (int i = 0; i < metadataNames.length; i++) {
                	metadataMap.put(metadataNames[i], metadataValues[i]);
                }
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
		return metadataMap;
	}
	
	public static Map<Integer, SourceFileVersion> readAuditCsvFile(File auditFile) {
		
		Map<Integer, SourceFileVersion> versionMap = new HashMap<Integer, SourceFileVersion>();
		
		String[] headers = null, values = null;
		BufferedReader br = null;
		String line = "";
		String seperator = ",";  // use comma as separator
		try {
            br = new BufferedReader(new FileReader(auditFile));
            
            //Read header line
            line = br.readLine();
            if (line != null) {
            	headers = line.split(seperator);
            }
            while ((line = br.readLine()) != null) {
            	int version = -1;
            	String creationDate = "";
            	String createdBy = "";
            	
                values = line.split(seperator);                
                for (int i = 0; i < headers.length; i++) {                	
                	//versionMap.put(headers[i], values[i]);
                	switch (headers[i]) {
                		case "Version" : version = Integer.valueOf(values[i]);
                		case "CreationDate" : creationDate = values[i];
                		case "CreatedBy" : createdBy = values[i];
                	}                	
                }
                SourceFileVersion sourceFileVersion = new SourceFileVersion(createdBy, creationDate);
                versionMap.put(version, sourceFileVersion);
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
		return versionMap;
	}
}

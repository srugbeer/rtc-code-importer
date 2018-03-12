/**
 * 
 */
package za.co.indigocube.rtc.code.importer.source;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;

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
	
	public static TreeSet<SourceFileVersion> readAuditCsvFile(File auditFile) {
		
		//Map<Integer, SourceFileVersion> versionMap = new HashMap<Integer, SourceFileVersion>();
		TreeSet<SourceFileVersion> versionHistory = new TreeSet<SourceFileVersion>();
		
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
            	//int version = -1;
            	String versionIndex = "";
            	String creationDate = "";
            	String createdBy = "";
            	
                values = line.split(seperator);                
                for (int i = 0; i < headers.length; i++) {                	
                	//versionMap.put(headers[i], values[i]);
                	switch (headers[i]) {
                		case "Version" : versionIndex = values[i];
                		case "CreationDate" : creationDate = values[i];
                		case "CreatedBy" : createdBy = values[i];
                	}                	
                }
        		SimpleDateFormat dateFormat = new SimpleDateFormat("yyMMddhhmmss");
        		Date date = dateFormat.parse(creationDate);
        		//String versionFileName = version + "-" + auditFile.getName().substring(0, auditFile.getName().indexOf("-"));
        		
        		System.out.println("Version Index: " + versionIndex);
        		
                SourceFileVersion sourceFileVersion = new SourceFileVersion(versionIndex, createdBy, date);
                versionHistory.add(sourceFileVersion);
                //versionMap.put(version, sourceFileVersion);
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
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
		//return versionMap;
		return versionHistory;
	}
}

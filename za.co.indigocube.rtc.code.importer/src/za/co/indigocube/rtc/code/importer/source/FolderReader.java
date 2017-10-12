/**
 * 
 */
package za.co.indigocube.rtc.code.importer.source;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import za.co.indigocube.rtc.code.importer.source.model.SourceFile;
import za.co.indigocube.rtc.code.importer.source.model.SourceFileVersion;

/**
 * @author Sudheer
 *
 */
public class FolderReader {
	
	public FolderReader() {
		//Default Constructor
		super();
	}
	
	public ArrayList<SourceFile> readFolderContents(String parentFolderName) {
		ArrayList<SourceFile> sourceFiles = new ArrayList<SourceFile>();
		
		File parentFolder = new File(parentFolderName);
		
		if (parentFolder.isDirectory()) {

			File[] sourceDirs = parentFolder.listFiles();
			for (File sourceDir : sourceDirs) {
				if (sourceDir.isDirectory()) {
					String dirName = sourceDir.getName();					
					String metadataFileName = dirName + "-metadata.csv";
					String auditFileName = dirName + "-audit.csv";
					Map<String, String> metadata = new HashMap<String, String>();
					Map<Integer, SourceFileVersion> history = new HashMap<Integer, SourceFileVersion>();
					
					System.out.println("Processing Folder: " + dirName);
					File[] mainframeSourceFiles = sourceDir.listFiles(new MainframeSourceFileFilter());
					//for (File sourceFile : sourceFiles) {
					//	System.out.println("Source File: " + sourceFile.getName());
					//}
					File[] csvFiles = sourceDir.listFiles(new CsvFileFilter());
					for (File csvFile : csvFiles) {
						//System.out.println("CSV File: " + csvFile.getName());
						if (csvFile.getName().equals(metadataFileName)) {
							System.out.println("Reading metadata file...");
							metadata = CsvFileReader.readMetadataCsvFile(csvFile);
							//System.out.println(metadata);
						}
						if (csvFile.getName().equals(auditFileName)) {
							System.out.println("Reading audit file...");
							history = CsvFileReader.readAuditCsvFile(csvFile);
							//System.out.println(history);
						}
					}
					
					//Update version file names
					for (int i = 0; i < mainframeSourceFiles.length; i++) {			
						String versionFilePath = mainframeSourceFiles[i].getAbsolutePath();
						String fileName = mainframeSourceFiles[i].getName();
						int versionNumber = Integer.valueOf(fileName.substring(0, fileName.indexOf("-")));
						//System.out.println("Version Number extracted from filename: " + versionNumber);
						history.get(versionNumber).setVersionFileName(versionFilePath);
					}
					String filename = mainframeSourceFiles[0].getName().
							substring(mainframeSourceFiles[0].getName().indexOf("-") + 1);
					SourceFile sourceFile = new SourceFile(filename, metadata, history);
					System.out.println("Source File: " + sourceFile.getName()); 
					System.out.println("Number of Versions: " + sourceFile.getNumberOfVersions());
					sourceFiles.add(sourceFile);
				}
			}
		}
		return sourceFiles;
	}
}

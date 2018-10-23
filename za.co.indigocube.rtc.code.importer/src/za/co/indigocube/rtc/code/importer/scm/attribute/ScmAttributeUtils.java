/**
 * 
 */
package za.co.indigocube.rtc.code.importer.scm.attribute;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import za.co.indigocube.rtc.code.importer.scm.attribute.model.ScmAttributeDefinition;

import com.ibm.team.process.client.IProcessClientService;
import com.ibm.team.process.common.IProcessConfigurationData;
import com.ibm.team.process.common.IProcessConfigurationElement;
import com.ibm.team.process.common.IProjectArea;
import com.ibm.team.repository.client.ITeamRepository;
import com.ibm.team.repository.common.TeamRepositoryException;
import com.ibm.team.scm.common.IScmService;
import com.ibm.team.scm.common.IVersionableHandle;
import com.ibm.team.scm.common.dto.ICustomAttributeList;

/**
 * @author Sudheer
 *
 */
public class ScmAttributeUtils {
	
	public static void printAttributes(IVersionableHandle versionable, IScmService scmService) 
			throws TeamRepositoryException {
		printAttributes(versionable, scmService, Logger.getRootLogger());
	}
	
	public static void printAttributes(IVersionableHandle versionable, IScmService scmService, 
			Logger logger) throws TeamRepositoryException {
		IVersionableHandle[] versionables = {versionable};
		printAttributes(versionables, scmService, logger);
	}
	
	/**
	 * Print the attributes for an array of versionable handles
	 * 
	 * @param vhandles
	 * @param scmService
	 * @param logger
	 * @throws TeamRepositoryException
	 */
	public static void printAttributes(IVersionableHandle[] vhandles, IScmService scmService, Logger logger)
			throws TeamRepositoryException {
		logger.info("Custom Attributes:");
		ICustomAttributeList[] versionableAttributesList = scmService
				.fetchCustomAttributesForVersionable(vhandles, null);

		for (int i = 0; i < versionableAttributesList.length; i++) {
			ICustomAttributeList attributeList = versionableAttributesList[i];
			Map<String, Object> attributeMap = attributeList
					.getCustomAttributes();
			printAttributes(attributeMap, logger);
		}
	}
	
	/**
	 * @param message
	 * @param attributeMap
	 * @param Logger
	 */
	private static void printAttributes(Map<String, Object> attributeMap, Logger logger) {
		Set<String> keys = attributeMap.keySet();
		if(keys.isEmpty()){
			logger.warn("No attributes found");
		}
		for (String key : keys) {
			Object value = attributeMap.get(key);
			printAttributeValue(key, value, logger);			
		}	
	}

	/**
	 * @param message
	 * @param key
	 * @param value
	 * @param logger
	 */
	private static void printAttributeValue(String key, Object value, Logger logger) {
		if (value != null) {
			logger.info("\t" + key + " = " + value);
		} else {
			logger.info("\t" + key + " = " + " ");
		}
	}
	
	public static void setAttributes(IVersionableHandle versionable, Map<String, String> attributes,
			IScmService scmService) throws TeamRepositoryException {
		for (String attributeName : attributes.keySet()) {
			String attributeValue = attributes.get(attributeName);
			setAttribute(versionable, attributeName, attributeValue, scmService);
		}
	}
	
	public static void setAttribute(IVersionableHandle versionable, String attributeName, String attributeValue,
			IScmService scmService) throws TeamRepositoryException {
		IVersionableHandle[] versionables = {versionable};
		setAttribute(versionables, attributeName, attributeValue, scmService);
	}
	
	
	/**
	 * Set a specific attribute of versionable handles
	 * 
	 * @param vhandles
	 * @param attributeName
	 * @param attributeValue
	 * @param scmService
	 * @throws TeamRepositoryException
	 */
	public static void setAttribute(IVersionableHandle[] vhandles,
			String attributeName, String attributeValue, IScmService scmService)
			throws TeamRepositoryException {
		ICustomAttributeList[] versionableAttributesList = scmService
				.fetchCustomAttributesForVersionable(vhandles, null);
		
		for (int i = 0; i < versionableAttributesList.length; i++) {
			ICustomAttributeList attributeList = versionableAttributesList[i];
			Map<String, Object> attributeMap = attributeList
					.getCustomAttributes();
			attributeMap.put(attributeName, attributeValue);
			attributeList.setCustomAttributes(attributeMap);
			scmService.saveCustomAttributesForVersionable(vhandles[i],
					attributeList, null);
		}
	}
	
	/**
	 * Remove a specific attribute of versionable handles
	 * 
	 * @param vhandles
	 * @param attributeName
	 * @param attributeValue
	 * @param scmService
	 * @throws TeamRepositoryException
	 */
	public static void removeAttribute(IVersionableHandle[] vhandles,
			String attributeName, String attributeValue, IScmService scmService )
			throws TeamRepositoryException {
	
		ICustomAttributeList[] versionableAttributesList = scmService
			.fetchCustomAttributesForVersionable(vhandles, null);

		for (int i = 0; i < versionableAttributesList.length; i++) {
			ICustomAttributeList attributeList = versionableAttributesList[i];
			attributeList.setCustomAttributes(new HashMap<String, Object>());
			scmService.saveCustomAttributesForVersionable(vhandles[i],
					attributeList, null);
		}
	}
	
	public static ScmAttributeDefinition[] retrieveAttributes(ITeamRepository teamRepository, 
			IProjectArea projectArea) throws TeamRepositoryException {
		
		IProcessClientService processClientService =
			    (IProcessClientService) teamRepository.getClientLibrary(IProcessClientService.class);
		
		IProcessConfigurationData extendedAttribConfigData =
		    processClientService.getClientProcess(projectArea, null).getProjectConfigurationData(
		        "com.ibm.team.scm.service.extendedAttributeDefinition", null);
		
		if (extendedAttribConfigData != null) {
			IProcessConfigurationElement fileConfigElement = extendedAttribConfigData
					.getElements()[0];
			IProcessConfigurationElement[] fileConfigElementChildren = fileConfigElement
					.getChildren();
			int size = fileConfigElementChildren.length;
			ScmAttributeDefinition[] attributeDefinitions = new ScmAttributeDefinition[size];
			for (int i = 0; i < size; i++) {
				String key = fileConfigElementChildren[i]
						.getAttribute("extendedAttributeDefinitionKey");
				String defaultVal = fileConfigElementChildren[i]
						.getAttribute("extendedAttributeDefinitionDefault");
				String type = fileConfigElementChildren[i]
						.getAttribute("extendedAttributeDefinitionType");
				String mod = fileConfigElementChildren[i]
						.getAttribute("extendedAttributeDefinitionMod");
				String inherit = fileConfigElementChildren[i]
						.getAttribute("extendedAttributeDefinitionInherit");
				attributeDefinitions[i] = new ScmAttributeDefinition(key, type,
						defaultVal, mod, inherit);
			}
			return attributeDefinitions;
		}
		else return new ScmAttributeDefinition[0];
	}

}

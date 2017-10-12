/**
 * 
 */
package za.co.indigocube.rtc.code.importer.scm.attribute.model;

/**
 * @author Sudheer
 *
 */
public class ScmAttributeDefinition {
	String fKey;
	String fType;
	String fDefault;
	String fModifiable;
	String fInherit;
	
	public ScmAttributeDefinition() {
		//Default Constructor
	}
	
	public ScmAttributeDefinition(String key, String type, String defaultValue, String modifiable, String inherit) {
		this.setKey(key);
		this.setType(type);
		this.setDefault(defaultValue);
		this.setModifiable(modifiable);
		this.setInherit(inherit);
	}
	
		/**
	 * @return the key
	 */
	public String getKey() {
		return this.fKey;
	}
	/**
	 * @param key the key to set
	 */
	public void setKey(String key) {
		this.fKey = key;
	}
	/**
	 * @return the type
	 */
	public String getType() {
		return fType;
	}
	/**
	 * @param type the type to set
	 */
	public void setType(String type) {
		this.fType = type;
	}
	/**
	 * @return the default
	 */
	public String getDefault() {
		return fDefault;
	}
	/**
	 * @param default the default to set
	 */
	public void setDefault(String def) {
		this.fDefault = def;
	}
	/**
	 * @return the modifiable
	 */
	public String getModifiable() {
		return fModifiable;
	}
	/**
	 * @param modifiable the modifiable to set
	 */
	public void setModifiable(String modifiable) {
		this.fModifiable = modifiable;
	}
	/**
	 * @return the inherit
	 */
	public String getInherit() {
		return fInherit;
	}
	/**
	 * @param inherit the inherit to set
	 */
	public void setInherit(String inherit) {
		this.fInherit = inherit;
	}
}

package cz.metacentrum.perun.core.impl.modules.attributes;


import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.ConsistencyErrorException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.blImpl.AttributesManagerBlImpl;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.impl.Utils;
import cz.metacentrum.perun.core.implApi.modules.attributes.UserFacilityVirtualAttributesModuleAbstract;
import cz.metacentrum.perun.core.implApi.modules.attributes.UserFacilityVirtualAttributesModuleImplApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import static cz.metacentrum.perun.core.impl.PerunLocksUtils.lockAttribute;

/**
 * Checks and fills at specified facility users UID.
 *
 * @date 22.4.2011 10:43:48
 * @author Lukáš Pravda   <luky.pravda@gmail.com>
 */
public class urn_perun_user_facility_attribute_def_virt_UID extends UserFacilityVirtualAttributesModuleAbstract implements UserFacilityVirtualAttributesModuleImplApi {
	private final static Logger log = LoggerFactory.getLogger(AttributesManagerBlImpl.class);

	/**
	 * Checks the new UID of the user at the specified facility. The new UID must
	 * not be lower than the min UID or greater than the max UID. Also no collision between
	 * existing user and the new user is allowed.
	 */
	@Override
	public void checkAttributeSemantics(PerunSessionImpl sess, User user, Facility facility, Attribute attribute) throws WrongReferenceAttributeValueException, WrongAttributeAssignmentException {
		try {
			Attribute uidNamespaceAttribute = sess.getPerunBl().getAttributesManagerBl().getAttribute(sess, facility, AttributesManager.NS_FACILITY_ATTR_DEF + ":uid-namespace");

			Attribute uidAttribute;
			if (uidNamespaceAttribute.getValue() != null) {
				// Get the u:uid-namespace[uidNamespaceAttribute]
				uidAttribute = sess.getPerunBl().getAttributesManagerBl().getAttribute(sess, user, AttributesManager.NS_USER_ATTR_DEF + ":uid-namespace:" + uidNamespaceAttribute.getValue());
				uidAttribute.setValue(attribute.getValue());
				sess.getPerunBl().getAttributesManagerBl().checkAttributeSemantics(sess, user, uidAttribute);
			} else {
				throw new WrongReferenceAttributeValueException(attribute, null, user, facility, "Required facility attribute uid-namespace value is null.");
			}
		} catch (AttributeNotExistsException e) {
			throw new ConsistencyErrorException(e);
		}
	}

	/**
	 * Fills the new UID for the user at the specified facility. First empty slot
	 * in range (minUID, maxUID) is returned.
	 */
	@Override
	public Attribute fillAttribute(PerunSessionImpl sess, User user, Facility facility, AttributeDefinition attribute) throws WrongAttributeAssignmentException {
		try {
			Attribute uidNamespaceAttribute = sess.getPerunBl().getAttributesManagerBl().getAttribute(sess, facility, AttributesManager.NS_FACILITY_ATTR_DEF + ":uid-namespace");

			Attribute attr = new Attribute(attribute);
			if (uidNamespaceAttribute.getValue() != null) {
				// Get the u:uid-namespace[uidNamespaceAttribute]
				Attribute uidAttribute = sess.getPerunBl().getAttributesManagerBl().getAttribute(sess, user, AttributesManager.NS_USER_ATTR_DEF + ":uid-namespace:" + uidNamespaceAttribute.getValue());
				uidAttribute = sess.getPerunBl().getAttributesManagerBl().fillAttribute(sess, user, uidAttribute);
				attr.setValue(uidAttribute.getValue());
			} else {
				attr.setValue(null);
			}
			return attr;
		} catch (AttributeNotExistsException e) {
			throw new ConsistencyErrorException(e);
		}
	}

	/**
	 * Gets the value of the attribute f:uid-namespace and then finds the value of the attribute u:uid-namespace:[uid-namespace]
	 */
	@Override
	public Attribute getAttributeValue(PerunSessionImpl sess, User user, Facility facility, AttributeDefinition attributeDefinition) {
		Attribute attr = new Attribute(attributeDefinition);

		Attribute uidAttribute;

		try {
			// Get the f:uid-namespace attribute
			Attribute uidNamespaceAttribute = sess.getPerunBl().getAttributesManagerBl().getAttribute(sess, facility, AttributesManager.NS_FACILITY_ATTR_DEF + ":uid-namespace");

			if (uidNamespaceAttribute.getValue() != null) {
				// Get the u:uid-namespace[uidNamespaceAttribute]
				uidAttribute = sess.getPerunBl().getAttributesManagerBl().getAttribute(sess, user, AttributesManager.NS_USER_ATTR_DEF + ":uid-namespace:" + uidNamespaceAttribute.getValue());
				Utils.copyAttributeToVirtualAttributeWithValue(uidAttribute, attr);
			} else {
				attr.setValue(null);
			}
		} catch (AttributeNotExistsException | WrongAttributeAssignmentException e) {
			throw new ConsistencyErrorException(e);
		}

		return attr;
	}

	@Override
	public boolean setAttributeValue(PerunSessionImpl sess, User user, Facility facility, Attribute attribute) throws WrongReferenceAttributeValueException {
		AttributeDefinition userUidAttributeDefinition;
		Attribute userUidAttribute;
		try {
			// Get the f:uid-namespace attribute
			Attribute uidNamespaceAttribute = sess.getPerunBl().getAttributesManagerBl().getAttribute(sess, facility, AttributesManager.NS_FACILITY_ATTR_DEF + ":uid-namespace");

			if (uidNamespaceAttribute.getValue() == null) {
				throw new WrongReferenceAttributeValueException(attribute, uidNamespaceAttribute);
			}
			userUidAttributeDefinition = sess.getPerunBl().getAttributesManagerBl().getAttributeDefinition(sess, AttributesManager.NS_USER_ATTR_DEF + ":uid-namespace:" + uidNamespaceAttribute.getValue());
			userUidAttribute = new Attribute(userUidAttributeDefinition);
			lockAttribute(userUidAttributeDefinition);
			log.debug("VIRT UID ATTR SET:" + sess.getPerunBl().getAttributesManagerBl().setAttributeWithoutCheck(sess, user, userUidAttribute));
			userUidAttribute = sess.getPerunBl().getAttributesManagerBl().fillAttribute(sess, user, userUidAttribute);
			log.debug(" VIRT UID VALUE :" + userUidAttribute);
		} catch (AttributeNotExistsException | WrongAttributeValueException e) {
			throw new InternalErrorException(e);
		} catch (WrongAttributeAssignmentException e) {
			throw new ConsistencyErrorException(e);
		}

		try {
			return sess.getPerunBl().getAttributesManagerBl().setAttributeWithoutCheck(sess, user, userUidAttribute);
		} catch(WrongAttributeValueException e) {
			throw new InternalErrorException(e);
		} catch (WrongAttributeAssignmentException e) {
			throw new ConsistencyErrorException(e);
		}
	}

	@Override
	public List<String> getDependencies() {
		List<String> dependencies = new ArrayList<>();
		dependencies.add(AttributesManager.NS_FACILITY_ATTR_DEF + ":uid-namespace");
		dependencies.add(AttributesManager.NS_USER_ATTR_DEF + ":uid-namespace" + ":*");
		return dependencies;
	}

	@Override
	public List<String> getStrongDependencies() {
		List<String> StrongDependencies = new ArrayList<>();
		StrongDependencies.add(AttributesManager.NS_FACILITY_ATTR_DEF + ":uid-namespace");
		StrongDependencies.add(AttributesManager.NS_USER_ATTR_DEF + ":uid-namespace" + ":*");
		return StrongDependencies;
	}

	@Override
	public AttributeDefinition getAttributeDefinition() {
		AttributeDefinition attr = new AttributeDefinition();
		attr.setNamespace(AttributesManager.NS_USER_FACILITY_ATTR_VIRT);
		attr.setFriendlyName("UID");
		attr.setDisplayName("UID");
		attr.setType(String.class.getName());
		attr.setDescription("UID if is set.");
		return attr;
	}
}

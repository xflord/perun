package cz.metacentrum.perun.core.api;

import cz.metacentrum.perun.core.api.exceptions.ConsentHubNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.FacilityNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.ConsentNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.PrivilegeException;
import cz.metacentrum.perun.core.api.exceptions.UserNotExistsException;

import java.util.List;

/**
 * Consents entry logic.
 *
 * @author Radoslav Čerhák <r.cerhak@gmail.com>
 */
public interface ConsentsManager {

	/**
	 * Get all consents for chosen ConsentHub with the specified status
	 *
	 * @param sess
	 * @param id id of the ConsentHub
	 * @param status status of the consent
	 * @return consents for chosen ConsentHub with the specified status
	 * @throws PrivilegeException
	 */
	List<Consent> getConsentsForConsentHub(PerunSession sess, int id, ConsentStatus status) throws PrivilegeException;

	/**
	 * Get all consents for chosen ConsentHub
	 *
	 * @param sess
	 * @param id id of the ConsentHub
	 * @return consents for chosen ConsentHub
	 * @throws PrivilegeException
	 */
	List<Consent> getConsentsForConsentHub(PerunSession sess, int id) throws PrivilegeException;

	/**
	 * Get all consents for chosen User with the specified status
	 *
	 * @param sess
	 * @param id id of the User
	 * @param status status of the consent
	 * @return consents for chosen User with the specified status
	 * @throws PrivilegeException
	 * @throws UserNotExistsException
	 */
	List<Consent> getConsentsForUser(PerunSession sess, int id, ConsentStatus status) throws PrivilegeException, UserNotExistsException;

	/**
	 * Get all consents for chosen User
	 *
	 * @param sess
	 * @param id id of the user
	 * @return consents for chosen User
	 * @throws PrivilegeException
	 * @throws UserNotExistsException
	 */
	List<Consent> getConsentsForUser(PerunSession sess, int id) throws PrivilegeException, UserNotExistsException;

	/**
	 * Get consent object with specified id
	 *
	 * @param sess
	 * @param id id of desired consent object
	 * @return consent object with specified id
	 * @throws ConsentNotExistsException thrown if consent with the id doesn't exist
	 * @throws PrivilegeException
	 */
	Consent getConsentById(PerunSession sess, int id) throws ConsentNotExistsException, PrivilegeException;

	/**
	 * Get list of all Consent Hubs
	 *
	 * @param sess perun session
	 * @return list of Consent Hubs
	 * @throws PrivilegeException
	 * @throws InternalErrorException
	 */
	List<ConsentHub> getAllConsentHubs(PerunSession sess) throws PrivilegeException;

	/**
	 * Finds existing Consent Hub by id.
	 *
	 * @param sess perun session
	 * @param id id of the Consent Hub you are looking for
	 * @return found Consent Hub
	 * @throws ConsentHubNotExistsException
	 * @throws InternalErrorException
	 */
	ConsentHub getConsentHubById(PerunSession sess, int id) throws ConsentHubNotExistsException, PrivilegeException;

	/**
	 * Finds existing Consent Hub by name.
	 *
	 * @param sess perun session
	 * @param name name of the Consent Hub you are looking for
	 * @return found Consent Hub
	 * @throws ConsentHubNotExistsException
	 * @throws InternalErrorException
	 */
	ConsentHub getConsentHubByName(PerunSession sess, String name) throws ConsentHubNotExistsException, PrivilegeException;

	/**
	 * Finds existing Consent Hub by facility.
	 *
	 * @param sess perun session
	 * @param facilityId id of facility for which consent hub is searched
	 * @return found Consent Hub
	 * @throws ConsentHubNotExistsException
	 * @throws InternalErrorException
	 * @throws PrivilegeException
	 * @throws FacilityNotExistsException
	 */
	ConsentHub getConsentHubByFacility(PerunSession sess, int facilityId) throws ConsentHubNotExistsException, PrivilegeException, FacilityNotExistsException;

}

package cz.metacentrum.perun.core.bl;

import cz.metacentrum.perun.core.api.Consent;
import cz.metacentrum.perun.core.api.ConsentHub;
import cz.metacentrum.perun.core.api.ConsentStatus;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.exceptions.ConsentNotExistsException;

import java.util.List;

/**
 * Consents BL logic.
 *
 * @author Radoslav Čerhák <r.cerhak@gmail.com>
 */
public interface ConsentsManagerBl {


	/**
	 * Gel all consents
	 *
	 * @param sess
	 * @return all existing consents in the database
	 */
	List<Consent> getAllConsents(PerunSession sess);

	/**
	 * Get all consents for chosen ConsentHub with the specified status
	 *
	 * @param sess
	 * @param id id of the ConsentHub
	 * @param status status of the consent
	 * @return consents for chosen ConsentHub with the specified status
	 */
	List<Consent> getConsentsForConsentHub(PerunSession sess, int id, ConsentStatus status);

	/**
	 * Get all consents for chosen ConsentHub
	 *
	 * @param sess
	 * @param id id of the ConsentHub
	 * @return consents for chosen ConsentHub
	 */
	List<Consent> getConsentsForConsentHub(PerunSession sess, int id);

	/**
	 * Get all consents for chosen User with the specified status
	 *
	 * @param sess
	 * @param id id of the User
	 * @param status status of the consent
	 * @return consents for chosen User with the specified status
	 */
	List<Consent> getConsentsForUser(PerunSession sess, int id, ConsentStatus status);

	/**
	 * Get all consents for chosen User
	 *
	 * @param sess
	 * @param id id of the user
	 * @return consents for chosen User
	 */
	List<Consent> getConsentsForUser(PerunSession sess, int id);

	/**
	 * Get consent object with specified id
	 *
	 * @param sess
	 * @param id id of desired consent object
	 * @return consent object with specified id
	 * @throws ConsentNotExistsException thrown if consent with the id doesn't exist
	 */
	Consent getConsentById(PerunSession sess, int id) throws ConsentNotExistsException;

	void checkConsentExists(PerunSession sess, Consent consent) throws ConsentNotExistsException;

	/**
	 * Get list of all Consent Hubs
	 *
	 * @param sess perun session
	 * @return list of Consent Hubs
	 * @throws InternalErrorException
	 */
	List<ConsentHub> getAllConsentHubs(PerunSession sess);

	/**
	 * Finds existing Consent Hub by id.
	 *
	 * @param sess perun session
	 * @param id id of the Consent Hub you are looking for
	 * @return found Consent Hub
	 * @throws ConsentHubNotExistsException
	 * @throws InternalErrorException
	 */
	ConsentHub getConsentHubById(PerunSession sess, int id) throws ConsentHubNotExistsException;

	/**
	 * Finds existing Consent Hub by name.
	 *
	 * @param sess perun session
	 * @param name name of the Consent Hub you are looking for
	 * @return found Consent Hub
	 * @throws ConsentHubNotExistsException
	 * @throws InternalErrorException
	 */
	ConsentHub getConsentHubByName(PerunSession sess, String name) throws ConsentHubNotExistsException;

	/**
	 * Finds existing Consent Hub by facility id.
	 *
	 * @param sess perun session
	 * @param facilityId facility for which consent hub is searched
	 * @return found Consent Hub
	 * @throws ConsentHubNotExistsException
	 */
	ConsentHub getConsentHubByFacility(PerunSession sess, int facilityId) throws ConsentHubNotExistsException;

	/**
	 * Creates new consent hub.
	 * @param perunSession session
	 * @param consentHub consent hub
	 * @return new consent hub
	 * @throws ConsentHubExistsException if consent hub with similar name exists
	 */
	ConsentHub createConsentHub(PerunSession perunSession, ConsentHub consentHub) throws ConsentHubExistsException;

	/**
	 * Deletes consent hub.
	 * @param perunSession session
	 * @param consentHub consent hub
	 * @throws ConsentHubAlreadyRemovedException if no such consent hub stored in db
	 */
	void deleteConsentHub(PerunSession perunSession, ConsentHub consentHub) throws ConsentHubAlreadyRemovedException;

	/**
	 * Returns true, if consent hub exists, false otherwise.
	 * @param sess session
	 * @param consentHub consent hub
	 * @return whether consent hub exists
	 */
	boolean consentHubExists(PerunSession sess, ConsentHub consentHub);

	/**
	 * Throws exception if consent hub does not exist.
	 * @param sess session
	 * @param consentHub consent hub
	 * @throws ConsentHubNotExistsException if consent hub does not exist
	 */
	void checkConsentHubExists(PerunSession sess, ConsentHub consentHub) throws ConsentHubNotExistsException;
}

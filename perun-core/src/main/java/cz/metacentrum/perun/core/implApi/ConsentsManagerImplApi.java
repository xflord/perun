package cz.metacentrum.perun.core.implApi;

import cz.metacentrum.perun.core.api.Consent;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.exceptions.ConsentExistsException;
import cz.metacentrum.perun.core.api.ConsentStatus;
import cz.metacentrum.perun.core.api.exceptions.ConsentNotExistsException;
import cz.metacentrum.perun.core.api.ConsentHub;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.exceptions.ConsentHubNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.ConsentHubAlreadyRemovedException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;

import java.util.List;

/**
 * Consents database logic.
 *
 * @author Radoslav Čerhák <r.cerhak@gmail.com>
 */
public interface ConsentsManagerImplApi {

	/**
	 * Save consent to database.
	 *
	 * @param perunSession PerunSession
	 * @param consent Consent
	 * @return created consent
	 * @throws cz.metacentrum.perun.core.api.exceptions.IllegalArgumentException if consent is null
	 */
	Consent createConsent(PerunSession perunSession, Consent consent) throws ConsentExistsException;

	/**
	 * Delete consent from the database.
	 *
	 * @param perunSession PerunSession
	 * @param consent Consent
	 * @throws cz.metacentrum.perun.core.api.exceptions.IllegalArgumentException if consent is null
	 */
	void deleteConsent(PerunSession perunSession, Consent consent) throws ConsentNotExistsException;
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

	/**
	 * Check if consent exists in underlying data source.
	 *
	 * @param sess
	 * @param consent
	 * @throws ConsentNotExistsException
	 */
	void checkConsentExists(PerunSession sess, Consent consent) throws ConsentNotExistsException;

	/**
	 * Check if consent exists in underlying data source.
	 *
	 * @param sess
	 * @param consent
	 * @return true if consent exists in data source, false otherwise
	 */
	boolean consentExists(PerunSession sess, Consent consent);

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
	 * Get list of all facilities associated to the given Consent Hub
	 *
	 * @param consentHub Consent Hub
	 * @return list of facilities
	 */
	List<Facility> getFacilitiesForConsentHub(ConsentHub consentHub);

	/**
	 * Finds existing Consent Hub by facility.
	 *
	 * @param sess perun session
	 * @param facilityId facility for which consent hub is searched
	 * @return found Consent Hub
	 * @throws ConsentHubNotExistsException
	 * @throws InternalErrorException
	 */
	ConsentHub getConsentHubByFacility(PerunSession sess, int facilityId) throws ConsentHubNotExistsException;

	/**
	 * Creates new consent hub.
	 * @param perunSession session
	 * @param consentHub consent hub
	 * @return new consent hub
	 */
	ConsentHub createConsentHub(PerunSession perunSession, ConsentHub consentHub);

	/**
	 * Deletes the consent hub.
	 * @param perunSession session
	 * @param consentHub consent hub
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

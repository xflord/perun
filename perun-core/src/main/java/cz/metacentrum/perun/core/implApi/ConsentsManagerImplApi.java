package cz.metacentrum.perun.core.implApi;

import cz.metacentrum.perun.core.api.Consent;
import cz.metacentrum.perun.core.api.ConsentStatus;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.exceptions.ConsentExistsException;
import cz.metacentrum.perun.core.api.exceptions.ConsentNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.ConsentHubNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.PrivilegeException;
import cz.metacentrum.perun.core.api.exceptions.UserNotExistsException;

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
	 * @throws IllegalArgumentException if consent is null
	 */
	Consent createConsent(PerunSession perunSession, Consent consent) throws ConsentExistsException, UserNotExistsException, PrivilegeException, ConsentNotExistsException, ConsentHubNotExistsException;

	/**
	 * Delete consent from the database.
	 *
	 * @param perunSession PerunSession
	 * @param consent Consent
	 * @throws ConsentNotExistsException if consent is null
	 */
	void deleteConsent(PerunSession perunSession, Consent consent) throws ConsentNotExistsException;

	/**
	 * Gel all consents
	 *
	 * @param sess PerunSession
	 * @return all existing consents in the database
	 */
	List<Consent> getAllConsents(PerunSession sess);

	/**
	 * Get all consents for chosen ConsentHub with the specified status
	 *
	 * @param sess PerunSession
	 * @param id id of the ConsentHub
	 * @param status status of the consent
	 * @return consents for chosen ConsentHub with the specified status
	 */
	List<Consent> getConsentsForConsentHub(PerunSession sess, int id, ConsentStatus status);

	/**
	 * Get all consents for chosen ConsentHub
	 *
	 * @param sess PerunSession
	 * @param id id of the ConsentHub
	 * @return consents for chosen ConsentHub
	 */
	List<Consent> getConsentsForConsentHub(PerunSession sess, int id);

	/**
	 * Get all consents for chosen User with the specified status
	 *
	 * @param sess PerunSession
	 * @param id id of the User
	 * @param status status of the consent
	 * @return consents for chosen User with the specified status
	 */
	List<Consent> getConsentsForUser(PerunSession sess, int id, ConsentStatus status);

	/**
	 * Get all consents for chosen User
	 *
	 * @param sess PerunSession
	 * @param id id of the user
	 * @return consents for chosen User
	 */
	List<Consent> getConsentsForUser(PerunSession sess, int id);

	/**
	 * Get consent object with specified id
	 *
	 * @param sess PerunSession
	 * @param id id of desired consent object
	 * @return consent object with specified id
	 * @throws ConsentNotExistsException thrown if consent with the id doesn't exist
	 */
	Consent getConsentById(PerunSession sess, int id) throws ConsentNotExistsException;

	/**
	 * Check if consent exists in underlying data source.
	 *
	 * @param sess PerunSession
	 * @param consent Consent
	 * @throws ConsentNotExistsException if consent is null
	 */
	void checkConsentExists(PerunSession sess, Consent consent) throws ConsentNotExistsException;

	/**
	 * Check if consent exists in underlying data source.
	 *
	 * @param sess PerunSession
	 * @param consent Consent to check
	 * @return true if consent exists in data source, false otherwise
	 */
	boolean consentExists(PerunSession sess, Consent consent);

	/**
	 * Get list of consents for user and consent hub
	 *
	 * @param sess PerunSession
	 * @param userId id of the user
	 * @param consentHubId id of the consent hub
	 *
	 * @return list of consents for the user and consent hub
	 *
	 * @throws PrivilegeException if user doesn't have rights to get consent
	 * @throws UserNotExistsException if user with the id doesn't exist
	 * @throws ConsentHubNotExistsException if consent hub with the id doesn't exist
	 */
	List<Consent> getConsentsForUserAndConsentHub(PerunSession sess, int userId, int consentHubId) throws PrivilegeException, UserNotExistsException, ConsentHubNotExistsException;

	List<Consent> getConsentsForUserAndConsentHub(PerunSession sess, int userId, int consentHubId, ConsentStatus status) throws UserNotExistsException, PrivilegeException, ConsentHubNotExistsException;
}

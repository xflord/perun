package cz.metacentrum.perun.core.implApi;

import cz.metacentrum.perun.core.api.Consent;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.exceptions.ConsentExistsException;
import cz.metacentrum.perun.core.api.exceptions.ConsentNotExistsException;

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
}

package cz.metacentrum.perun.core.bl;

import cz.metacentrum.perun.core.api.Consent;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.exceptions.ConsentExistsException;
import cz.metacentrum.perun.core.api.exceptions.ConsentNotExistsException;

/**
 * Consents BL logic.
 *
 * @author Radoslav Čerhák <r.cerhak@gmail.com>
 */
public interface ConsentsManagerBl {

	/**
	 * Creates a new consent with status 'UNSIGNED'
	 *
	 * @param perunSession perun session
	 * @param consent Consent to create
	 *
	 * @return created consent
	 *
	 * @throws ConsentExistsException if consent already exists
	 */
	Consent createConsent(PerunSession perunSession, Consent consent) throws ConsentExistsException;

	/**
	 * Deletes consent
	 *
	 * @param sess perun session
	 * @param consent consent to delete
	 *
	 * @throws ConsentNotExistsException if consent doesn't exist
	 */
	void deleteConsent(PerunSession sess, Consent consent) throws ConsentNotExistsException;
}

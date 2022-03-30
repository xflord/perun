package cz.metacentrum.perun.core.blImpl;

import cz.metacentrum.perun.audit.events.ConsentManager.ConsentCreated;
import cz.metacentrum.perun.core.api.Consent;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.exceptions.ConsentExistsException;
import cz.metacentrum.perun.core.api.exceptions.ConsentNotExistsException;
import cz.metacentrum.perun.core.bl.ConsentsManagerBl;
import cz.metacentrum.perun.core.bl.PerunBl;
import cz.metacentrum.perun.core.impl.Utils;
import cz.metacentrum.perun.core.implApi.ConsentsManagerImplApi;

/**
 * Consents BL logic.
 *
 * @author Radoslav Čerhák <r.cerhak@gmail.com>
 */
public class ConsentsManagerBlImpl implements ConsentsManagerBl {

	private final ConsentsManagerImplApi consentsManagerImpl;
	private PerunBl perunBl;

	public ConsentsManagerBlImpl(ConsentsManagerImplApi consentsManagerImpl) {
		this.consentsManagerImpl = consentsManagerImpl;
	}

	public ConsentsManagerImplApi getConsentsManagerImpl() {
		return this.consentsManagerImpl;
	}

	public PerunBl getPerunBl() {
		return this.perunBl;
	}

	public void setPerunBl(PerunBl perunBl) {
		this.perunBl = perunBl;
	}

	public Consent createConsent(PerunSession sess, Consent consent) throws ConsentExistsException {
		Utils.notNull(consent, "consent");

		consent = getConsentsManagerImpl().createConsent(sess, consent);
		getPerunBl().getAuditer().log(sess, new ConsentCreated(consent));

		return consent;
	}

	public void deleteConsent(PerunSession sess, Consent consent) throws ConsentNotExistsException {
		Utils.notNull(consent, "consent");
		Utils.notNull(consent.getId(), "consent.id");

		getConsentsManagerImpl().deleteConsent(sess, consent);
	}
}

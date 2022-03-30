package cz.metacentrum.perun.core.blImpl;

import cz.metacentrum.perun.audit.events.ConsentManager.ConsentCreated;
import cz.metacentrum.perun.core.api.exceptions.ConsentExistsException;
import cz.metacentrum.perun.core.api.Consent;
import cz.metacentrum.perun.core.api.ConsentStatus;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.exceptions.ConsentNotExistsException;
import cz.metacentrum.perun.core.api.ConsentHub;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.exceptions.ConsentHubExistsException;
import cz.metacentrum.perun.core.api.exceptions.ConsentHubNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.ConsentHubAlreadyRemovedException;
import cz.metacentrum.perun.core.bl.ConsentsManagerBl;
import cz.metacentrum.perun.core.bl.PerunBl;
import cz.metacentrum.perun.core.impl.Utils;
import cz.metacentrum.perun.core.implApi.ConsentsManagerImplApi;

import java.util.List;

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


	@Override
	public List<Consent> getAllConsents(PerunSession sess) {
		return consentsManagerImpl.getAllConsents(sess);
	}

	@Override
	public List<ConsentHub> getAllConsentHubs(PerunSession sess) {
		return getConsentsManagerImpl().getAllConsentHubs(sess);
	}

	@Override
	public ConsentHub getConsentHubById(PerunSession sess, int id) throws ConsentHubNotExistsException {
		return getConsentsManagerImpl().getConsentHubById(sess, id);
	}

	@Override
	public ConsentHub getConsentHubByName(PerunSession sess, String name) throws ConsentHubNotExistsException {
		return getConsentsManagerImpl().getConsentHubByName(sess, name);
	}

	@Override
	public ConsentHub getConsentHubByFacility(PerunSession sess, int facilityId) throws ConsentHubNotExistsException {
		return getConsentsManagerImpl().getConsentHubByFacility(sess, facilityId);
	}

	@Override
	public ConsentHub createConsentHub(PerunSession sess, ConsentHub consentHub) throws ConsentHubExistsException{
		if (consentHubExists(sess, consentHub)) {
			throw new ConsentHubExistsException(consentHub);
		}

		return getConsentsManagerImpl().createConsentHub(sess, consentHub);
	}

	@Override
	public void deleteConsentHub(PerunSession sess, ConsentHub consentHub) throws ConsentHubAlreadyRemovedException {
		getConsentsManagerImpl().deleteConsentHub(sess, consentHub);
	}

	@Override
	public boolean consentHubExists(PerunSession sess, ConsentHub consentHub) {
		return getConsentsManagerImpl().consentHubExists(sess, consentHub);
	}

	@Override
	public void checkConsentHubExists(PerunSession sess, ConsentHub consentHub) throws ConsentHubNotExistsException {
		getConsentsManagerImpl().checkConsentHubExists(sess, consentHub);
	}

	@Override
	public List<Consent> getConsentsForConsentHub(PerunSession sess, int id, ConsentStatus status) {
		return consentsManagerImpl.getConsentsForConsentHub(sess, id, status);
	}

	@Override
	public List<Consent> getConsentsForConsentHub(PerunSession sess, int id) {
		return consentsManagerImpl.getConsentsForConsentHub(sess, id);
	}

	@Override
	public List<Consent> getConsentsForUser(PerunSession sess, int id, ConsentStatus status) {
		return consentsManagerImpl.getConsentsForUser(sess, id, status);
	}

	@Override
	public List<Consent> getConsentsForUser(PerunSession sess, int id) {
		return consentsManagerImpl.getConsentsForUser(sess, id);
	}

	@Override
	public Consent getConsentById(PerunSession sess, int id) throws ConsentNotExistsException {
		return consentsManagerImpl.getConsentById(sess, id);
	}
}

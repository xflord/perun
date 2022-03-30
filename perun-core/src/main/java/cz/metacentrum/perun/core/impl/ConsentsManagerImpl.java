package cz.metacentrum.perun.core.impl;

import cz.metacentrum.perun.core.api.BeansUtils;
import cz.metacentrum.perun.core.api.Consent;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.ConsentNotExistsException;
import cz.metacentrum.perun.core.implApi.ConsentsManagerImplApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcPerunTemplate;

import javax.sql.DataSource;

/**
 * Consents database logic.
 *
 * @author Radoslav Čerhák <r.cerhak@gmail.com>
 */
public class ConsentsManagerImpl implements ConsentsManagerImplApi {

	final static Logger log = LoggerFactory.getLogger(ConsentsManagerImpl.class);

	private static JdbcPerunTemplate jdbc;

	public ConsentsManagerImpl(DataSource perunPool) {
		jdbc = new JdbcPerunTemplate(perunPool);
		jdbc.setQueryTimeout(BeansUtils.getCoreConfig().getQueryTimeout());
	}

	@Override
	public Consent createConsent(PerunSession perunSession, Consent consent) {
		try {
			int newId = Utils.getNewId(jdbc, "consents_id_seq");

			jdbc.update("insert into consents(id, user_id, consent_hub_id, consent_status, created_at, created_by, modified_at, modified_by, created_by_uid, modified_by_uid) " +
					"values (?,?,?,?,"+Compatibility.getSysdate()+",?,"+Compatibility.getSysdate()+",?,?,?)", newId, consent.getUserId(),
				consent.getConsentHub().getId(), "UNSIGNED", perunSession.getPerunPrincipal().getActor(), perunSession.getPerunPrincipal().getActor(),
				perunSession.getPerunPrincipal().getUserId(), perunSession.getPerunPrincipal().getUserId());

			log.info("Consent {} created.", consent);
			consent.setId(newId);

			return consent;
		} catch (Exception e){
			throw new InternalErrorException(e);
		}
	}

	@Override
	public void deleteConsent(PerunSession perunSession, Consent consent) {
		try {
			int numberOfRows = jdbc.update("delete from consents where id=?", consent.getId());
			if (numberOfRows != 1) throw new ConsentNotExistsException(consent);
			log.info("Consent {} deleted.", consent);
		} catch (Exception e){
			throw new InternalErrorException(e);
		}
	}
}

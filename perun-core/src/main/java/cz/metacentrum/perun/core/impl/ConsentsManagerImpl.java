package cz.metacentrum.perun.core.impl;

import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.BeansUtils;
import cz.metacentrum.perun.core.api.Consent;
import cz.metacentrum.perun.core.api.ConsentHub;
import cz.metacentrum.perun.core.api.ConsentStatus;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.exceptions.ConsentExistsException;
import cz.metacentrum.perun.core.api.exceptions.ConsentHubNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.ConsistencyErrorException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.ConsentNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.PrivilegeException;
import cz.metacentrum.perun.core.api.exceptions.UserNotExistsException;
import cz.metacentrum.perun.core.implApi.ConsentsManagerImplApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcPerunTemplate;
import org.springframework.jdbc.core.RowMapper;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;

/**
 * Consents database logic.
 *
 * @author Radoslav Čerhák <r.cerhak@gmail.com>
 */
public class ConsentsManagerImpl implements ConsentsManagerImplApi {

	private final static String consentMappingSelectQuery = " consents.id as consents_id, consents.user_id as consents_user_id, consents.consent_hub_id as consents_consent_hub_id, " +
		"consents.status as consents_status, consents.created_at as consents_created_at, consents.created_by as consents_created_by, consents.modified_by as consents_modified_by, consents.modified_at as consents_modified_at, " +
		"consents.modified_by_uid as consents_modified_by_uid, consents.created_by_uid as consents_created_by_uid ";

	private final static String consentHubMappingSelectQuery = "";

	private final static RowMapper<ConsentHub> CONSENT_HUB_MAPPER = (rs, rowNum) -> {
		return new ConsentHub();
	};

	// Consent mapper
	private final static RowMapper<Consent> CONSENT_MAPPER = (rs, rowNum) -> {
		Consent c = new Consent();
		c.setId(rs.getInt("consents_id"));
		c.setUserId(rs.getInt("consents_user_id"));
		c.setConsentHub(CONSENT_HUB_MAPPER.mapRow(rs, rowNum));
		c.setStatus(ConsentStatus.valueOf(rs.getString("consents_status")));
		c.setCreatedAt(rs.getString("consents_created_at"));
		c.setCreatedBy(rs.getString("consents_created_by"));
		c.setModifiedAt(rs.getString("consents_modified_at"));
		c.setModifiedBy(rs.getString("consents_modified_by"));
		if(rs.getInt("consents_modified_by_uid") == 0) c.setModifiedByUid(null);
		else c.setModifiedByUid(rs.getInt("consents_modified_by_uid"));
		if(rs.getInt("consents_created_by_uid") == 0) c.setCreatedByUid(null);
		else c.setCreatedByUid(rs.getInt("consents_created_by_uid"));
		return c;
	};

	final static Logger log = LoggerFactory.getLogger(ConsentsManagerImpl.class);

	private static JdbcPerunTemplate jdbc;

	public ConsentsManagerImpl(DataSource perunPool) {
		jdbc = new JdbcPerunTemplate(perunPool);
		jdbc.setQueryTimeout(BeansUtils.getCoreConfig().getQueryTimeout());
	}

	@Override
	public Consent createConsent(PerunSession perunSession, Consent consent) throws UserNotExistsException, PrivilegeException, ConsentNotExistsException, ConsentHubNotExistsException, ConsentExistsException {
		// Check if consent already exists
		if(consentExists(perunSession, consent)){
			throw new ConsentExistsException("Consent already exists.");
		}

		// Remove UNSIGNED consents from consent hub
		for (Consent c: getConsentsForUserAndConsentHub(perunSession, consent.getUserId(), consent.getConsentHub().getId(), ConsentStatus.UNSIGNED)) {
			deleteConsent(perunSession, c);
		}

		try {
			int newId = Utils.getNewId(jdbc, "consents_id_seq");

			jdbc.update("insert into consents(id, user_id, consent_hub_id, consent_status, created_at, created_by, modified_at, modified_by, created_by_uid, modified_by_uid) " +
					"values (?,?,?,?,"+Compatibility.getSysdate()+",?,"+Compatibility.getSysdate()+",?,?,?)", newId, consent.getUserId(),
				consent.getConsentHub().getId(), ConsentStatus.UNSIGNED, perunSession.getPerunPrincipal().getActor(), perunSession.getPerunPrincipal().getActor(),
				perunSession.getPerunPrincipal().getUserId(), perunSession.getPerunPrincipal().getUserId());
			log.info("Consent {} created.", consent);
			consent.setId(newId);

			consent.setAttributes(getAttrDefsForConsent(perunSession, consent.getId()));

			return consent;
		} catch (Exception e){
			throw new InternalErrorException(e);
		}
	}

	@Override
	public void deleteConsent(PerunSession perunSession, Consent consent) throws ConsentNotExistsException {
		// Check if consent exists
		checkConsentExists(perunSession, consent);

		try {
			int numberOfRows = jdbc.update("delete from consents where id=?", consent.getId());
			if (numberOfRows != 1) throw new ConsentNotExistsException(consent);
			log.info("Consent {} deleted.", consent);
			consent.setAttributes(null);
			removeAttrsForConsent(perunSession, consent.getId());
		} catch (Exception e){
			throw new InternalErrorException(e);
		}
	}

	@Override
	public List<Consent> getAllConsents(PerunSession sess) {
		try {
			List<Consent> consents = jdbc.query("select " + consentMappingSelectQuery + ", " + consentHubMappingSelectQuery +
				" from consents left join consent_hubs on consents.consent_hub_id=consent_hubs.id ", CONSENT_MAPPER);
			for (Consent consent : consents) {
				if (consent != null) {
					consent.setAttributes(getAttrDefsForConsent(sess, consent.getId()));
				}
			}
			return consents;
		} catch (EmptyResultDataAccessException ex) {
			return new ArrayList<>();
		} catch (RuntimeException err) {
			throw new InternalErrorException(err);
		}
	}

	@Override
	public List<Consent> getConsentsForConsentHub(PerunSession sess, int id, ConsentStatus status) {
		try {
			List<Consent> consents = jdbc.query("select " + consentMappingSelectQuery + ", " + consentHubMappingSelectQuery +
				" from consents left join consent_hubs on consents.consent_hub_id=consent_hubs.id " + " where consents.consent_hub_id=? and consents.status=? ", CONSENT_MAPPER, id, status);
			for (Consent consent : consents) {
				if (consent != null) {
					consent.setAttributes(getAttrDefsForConsent(sess, consent.getId()));
				}
			}
			return consents;
		} catch (EmptyResultDataAccessException ex) {
			return new ArrayList<>();
		} catch (RuntimeException err) {
			throw new InternalErrorException(err);
		}
	}

	@Override
	public List<Consent> getConsentsForConsentHub(PerunSession sess, int id) {
		try {
			List<Consent> consents = jdbc.query("select " + consentMappingSelectQuery + ", " + consentHubMappingSelectQuery +
				" from consents left join consent_hubs on consents.consent_hub_id=consent_hubs.id " + " where consents.consent_hub_id=? ", CONSENT_MAPPER, id);
			for (Consent consent : consents) {
				if (consent != null) {
					consent.setAttributes(getAttrDefsForConsent(sess, consent.getId()));
				}
			}
			return consents;
		} catch (EmptyResultDataAccessException ex) {
			return new ArrayList<>();
		} catch (RuntimeException err) {
			throw new InternalErrorException(err);
		}
	}

	@Override
	public List<Consent> getConsentsForUser(PerunSession sess, int id, ConsentStatus status) {
		try {
			List<Consent> consents = jdbc.query("select " + consentMappingSelectQuery + ", " + consentHubMappingSelectQuery +
				" from consents left join consent_hubs on consents.consent_hub_id=consent_hubs.id " + " where consents.user_id=? and consents.status=? ", CONSENT_MAPPER, id, status);
			for (Consent consent : consents) {
				if (consent != null) {
					consent.setAttributes(getAttrDefsForConsent(sess, consent.getId()));
				}
			}
			return consents;
		} catch (EmptyResultDataAccessException ex) {
			return new ArrayList<>();
		} catch (RuntimeException err) {
			throw new InternalErrorException(err);
		}
	}

	@Override
	public List<Consent> getConsentsForUser(PerunSession sess, int id) {
		try {
			List<Consent> consents = jdbc.query("select " + consentMappingSelectQuery + ", " + consentHubMappingSelectQuery +
				" from consents left join consent_hubs on consents.consent_hub_id=consent_hubs.id " + " where consents.user_id=? ", CONSENT_MAPPER, id);
			for (Consent consent : consents) {
				if (consent != null) {
					consent.setAttributes(getAttrDefsForConsent(sess, consent.getId()));
				}
			}
			return consents;
		} catch (EmptyResultDataAccessException ex) {
			return new ArrayList<>();
		} catch (RuntimeException err) {
			throw new InternalErrorException(err);
		}
	}

	@Override
	public Consent getConsentById(PerunSession sess, int id) throws ConsentNotExistsException {
		try {
			Consent consent = jdbc.queryForObject("select " + consentMappingSelectQuery + ", " + consentHubMappingSelectQuery +
				" from consents left join consent_hubs on consents.consent_hub_id=consent_hubs.id " + " where consents.id=? ", CONSENT_MAPPER, id);
			if (consent != null) {
				consent.setAttributes(getAttrDefsForConsent(sess, id));
			}
			return consent;
		} catch (EmptyResultDataAccessException ex) {
			throw new ConsentNotExistsException(ex);
		} catch (RuntimeException err) {
			throw new InternalErrorException(err);
		}
	}

	@Override
	public void checkConsentExists(PerunSession sess,  Consent consent) throws ConsentNotExistsException {
		if (!consentExists(sess, consent)) throw new ConsentNotExistsException("Consent: " + consent);
	}

	@Override
	public boolean consentExists(PerunSession sess, Consent consent) {
		Utils.notNull(consent, "consent");
		try {
			int numberOfExistences = jdbc.queryForInt("select count(1) from consents where id=?", consent.getId());
			if (numberOfExistences == 1) {
				return true;
			} else if (numberOfExistences > 1) {
				throw new ConsistencyErrorException("Consent " + consent + " exists more than once.");
			}
			return false;
		} catch (EmptyResultDataAccessException ex) {
			return false;
		} catch (RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}
	@Override
	public List<Consent> getConsentsForUserAndConsentHub(PerunSession sess, int userId, int consentHubId) throws PrivilegeException, UserNotExistsException, ConsentHubNotExistsException {
		User user = sess.getPerun().getUsersManager().getUserById(sess, userId);
		ConsentHub consentHub = sess.getPerun().getConsentsManager().getConsentHubById(sess, consentHubId);
		if (user == null) throw new UserNotExistsException("User: " + userId);
		if (consentHub == null) throw new ConsentHubNotExistsException("ConsentHub: " + consentHubId);

		try {
			List<Consent> consents = jdbc.query("select " + consentMappingSelectQuery + " from consents where user_id=? and consent_hub_id=?", CONSENT_MAPPER, userId, consentHubId);
			if(consents.isEmpty()){
				throw new ConsentNotExistsException("User " + userId + " has no consents for consent hub " + consentHubId);
			}

			for (Consent consent : consents) {
				if (consent != null) {
					consent.setAttributes(getAttrDefsForConsent(sess, consent.getId()));
				}
			}
			return consents;
		} catch (EmptyResultDataAccessException ex) {
			return new ArrayList<>();
		} catch (Exception e) {
			throw new InternalErrorException(e);
		}
	}

	@Override
	public List<Consent> getConsentsForUserAndConsentHub(PerunSession sess, int userId, int consentHubId, ConsentStatus status) throws UserNotExistsException, PrivilegeException, ConsentHubNotExistsException {
		List<Consent> consents = getConsentsForUserAndConsentHub(sess, userId, consentHubId);
		return consents.stream().filter(c -> c.getStatus().equals(status)).toList();
	}

	private List<AttributeDefinition> getAttrDefsForConsent(PerunSession sess, int consentId) {
		try {
			return jdbc.query("select " + AttributesManagerImpl.attributeDefinitionMappingSelectQuery + " from attr_names join consent_attr_defs on attr_names.id = consent_attr_defs.attr_id" +
				" where consent_attr_defs.consent_id=?", AttributesManagerImpl.ATTRIBUTE_DEFINITION_MAPPER, consentId);
		} catch (EmptyResultDataAccessException ex) {
			return new ArrayList<>();
		} catch (RuntimeException err) {
			throw new InternalErrorException(err);
		}
	}

	private void removeAttrsForConsent(PerunSession sess, int consentId) {
		try {
			jdbc.update("delete from consent_attr_defs where consent_id=?", consentId);
		} catch (RuntimeException err) {
			throw new InternalErrorException(err);
		}
	}

}

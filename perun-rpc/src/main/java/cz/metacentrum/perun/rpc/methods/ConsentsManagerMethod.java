package cz.metacentrum.perun.rpc.methods;

import cz.metacentrum.perun.core.api.ConsentHub;
import cz.metacentrum.perun.core.api.Consent;
import cz.metacentrum.perun.core.api.ConsentStatus;
import cz.metacentrum.perun.core.api.exceptions.PerunException;
import cz.metacentrum.perun.rpc.ApiCaller;
import cz.metacentrum.perun.rpc.ManagerMethod;
import cz.metacentrum.perun.rpc.deserializer.Deserializer;

import java.util.List;

public enum ConsentsManagerMethod implements ManagerMethod {

	/*#
	 * Returns all existing Consents
	 *
	 * @return List<Consent> list of Consents
	 */
	getAllConsents {
		@Override
		public List<Consent> call (ApiCaller ac, Deserializer parms) throws PerunException {
			return ac.getConsentsManager().getAllConsents(ac.getSession());
		}
	},

	/*#
	 * Returns Consent with the corresponding id
	 *
	 * @param id int Consent <code>id</code>
	 * @throw ConsentNotExistsException when Consent specified by <code>id</code> doesn't exist
	 * @return List<Consent> list of Consents
	 */
	getConsentById {
		@Override
		public Consent call (ApiCaller ac, Deserializer parms) throws PerunException {
			return ac.getConsentsManager().getConsentById(ac.getSession(), parms.readInt("id"));
		}
	},

	/*#
	 * Returns all consents for a User
	 *
	 * @param user int User <code>id</code>
	 * @throw UserNotExistsException when User specified by <code>id</code> doesn't exist
	 * @return List<Consent> Consents of the User
	 */
	/*#
	 * Returns all consents with a specific status for a User.
	 *
	 * @param user int User <code>id</code>
	 * @param status String UNSIGNED | GRANTED | REVOKED
	 * @throw UserNotExistsException when User specified by <code>id</code> doesn't exist
	 * @return List<Consent> Consents of the User
	 */
	getConsentsForUser {
		@Override
		public List<Consent> call (ApiCaller ac, Deserializer parms) throws PerunException {
			if (parms.contains("status")) {
				return ac.getConsentsManager().getConsentsForUser(ac.getSession(), parms.readInt("user"), ConsentStatus.valueOf(parms.readString("status")));
			} else {
				return ac.getConsentsManager().getConsentsForUser(ac.getSession(), parms.readInt("user"));
			}
		}
	},

	/*#
	 * Returns all consents for a ConsentHub
	 *
	 * @param id int ConsentHub <code>id</code>
	 * @throw ConsentHubNotExistsException when Consent Hub specified by <code>id</code> doesn't exist
	 * @return List<Consent> Consents of the ConsentHub
	 */
	/*#
	 * Returns all consents with a specific status for a ConsentHub.
	 *
	 * @param id int ConsentHub <code>id</code>
	 * @param status String UNSIGNED | GRANTED | REVOKED
	 * @throw ConsentHubNotExistsException when Consent Hub specified by <code>id</code> doesn't exist
	 * @return List<Consent> Consents of the ConsentHub
	 */
	getConsentsForConsentHub {
		@Override
		public List<Consent> call (ApiCaller ac, Deserializer parms) throws PerunException {
			if (parms.contains("status")) {
				return ac.getConsentsManager().getConsentsForConsentHub(ac.getSession(), parms.readInt("id"), ConsentStatus.valueOf(parms.readString("status")));
			} else {
				return ac.getConsentsManager().getConsentsForConsentHub(ac.getSession(), parms.readInt("id"));
			}
		}
	},

	/*#
	 * Return list of all Consent Hubs.
	 *
	 * @return List<ConsentHub> list of Consent Hubs
	 */
	getAllConsentHubs {
		@Override
		public List<ConsentHub> call(ApiCaller ac, Deserializer params) throws PerunException {
			return ac.getConsentsManager().getAllConsentHubs(ac.getSession());
		}
	},

	/*#
	 * Returns a Consent Hub by its <code>id</code>.
	 *
	 * @param id int Consent Hub <code>id</code>
	 * @throw ConsentHubNotExistsException When Consent Hub specified by <code>id</code> doesn't exist.
	 * @return ConsentHub Found Consent Hub
	 */
	getConsentHubById {
		@Override
		public ConsentHub call(ApiCaller ac, Deserializer params) throws PerunException {
			return ac.getConsentsManager().getConsentHubById(ac.getSession(), params.readInt("id"));
		}
	},

	/*#
	 * Returns a Consent Hub by its name.
	 *
	 * @param name String Consent Hub name
	 * @throw ConsentHubNotExistsException When Consent Hub specified by name doesn't exist.
	 * @return ConsentHub Found Consent Hub
	 */
	getConsentHubByName {
		@Override
		public ConsentHub call(ApiCaller ac, Deserializer params) throws PerunException {
			return ac.getConsentsManager().getConsentHubByName(ac.getSession(), params.readString("name"));
		}
	},

	/*#
	 * Returns a Consent Hub by facility id.
	 *
	 * @param facilityId facility id
	 * @throw ConsentHubNotExistsException When Consent Hub for facility with given id doesn't exist.
	 * @throw FacilityNotExistsException if facility with given id does not exist
	 * @return ConsentHub Found Consent Hub
	 */
	getConsentHubByFacility {
		@Override
		public ConsentHub call(ApiCaller ac, Deserializer params) throws PerunException {
			return ac.getConsentsManager().getConsentHubByFacility(ac.getSession(), params.readInt("facility"));
		}
	};
}

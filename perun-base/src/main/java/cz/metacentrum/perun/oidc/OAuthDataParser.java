package cz.metacentrum.perun.oidc;

import com.fasterxml.jackson.databind.JsonNode;
import cz.metacentrum.perun.core.api.BeansUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Map;

public class OAuthDataParser {

	/**
	 * Parsing extsource login (sub) from user info.
	 * We are looking for the first nonempty property value that can be login
	 * @param userInfo
	 * @return extsource login
	 */
	private String getExtSourceLogin(Map<String, Object> data) {
		List<String> ExtSourceLoginOptions = BeansUtils.getCoreConfig().getUserInfoEndpointExtSourceLogin();
		String login = "";
		for(String property: ExtSourceLoginOptions) {
			String loginNode = data.get(property).toString();
			login = loginNode.split(" ")[0];
			if(StringUtils.isNotEmpty(login)) {
				return login;
			}
		}
		if(StringUtils.isEmpty(login)) {
			log.info("user sub from user info endpoint was empty or null: {}", login);
		}
		return login;
	}

	/**
	 * Parsing extsource name from userinfo
	 * @param userInfo
	 * @return extsource name
	 */
	private String getExtSourceName(Map<String, Object> data) {
		String pathToExtSourceName = BeansUtils.getCoreConfig().getUserInfoEndpointExtSourceName();
		String extSourceName = data.get(pathToExtSourceName).toString();
		if(StringUtils.isEmpty(extSourceName)) {
			log.info("issuer from user info endpoint was empty or null: {}", extSourceName);
		}
		return extSourceName;
	}

	/**
	 * Filling additional information with user's name, user's email and user-friendly format of extsource name
	 * @param userInfo
	 * @param additionalInformation
	 */
	private void fillAdditionalInformationWithDataFromUserInfo(Map<String, Object> data, Map<String, String> additionalInformation) {
		//retrieving name
		String name = data.get("name").toString();
		if(StringUtils.isEmpty(name)) {
			String firstName = data.get("given_name").toString();
			String familyName = data.get("family_name").toString();
			if(StringUtils.isNotEmpty(firstName) && StringUtils.isNotEmpty(familyName)) {
				name =  firstName + " " + familyName;
			}
		}
		if(StringUtils.isNotEmpty(name)) {
			additionalInformation.put("displayName", name);
		}

		//retrieving email
		String email = data.get("email").toString();
		if(StringUtils.isNotEmpty(email)) {
			additionalInformation.put("mail", email);
		}

		//retrieve friendly IdP name from the nested property
		List<String> pathToIdpName = BeansUtils.getCoreConfig().getUserInfoEndpointExtSourceFriendlyName();
		String idpName = "";
		for(String path: pathToIdpName) {
			String idpNameNode = data.get(path).toString();
			idpName = idpNameNode.split(" ")[0];
		}
		if(StringUtils.isNotEmpty(idpName)) {
			additionalInformation.put("sourceIdPName", idpName);
		}
	}

	/**
	 * Returns mfa timestamp if acr value is equal to MFA acr value
	 * @param userInfo parsed response from userInfo endpoint
	 */
	private String getMfaTimestamp(Map<String, Object> data) {
		String acrProperty = BeansUtils.getCoreConfig().getUserInfoEndpointAcrPropertyName();
		String acr = data.get(acrProperty).toString();
		if (StringUtils.isNotEmpty(acr) && acr.equals(BeansUtils.getCoreConfig().getUserInfoEndpointMfaAcrValue())) {
			String introspectionTimestamp = data.get("auth_time").toString();
			if (StringUtils.isNotEmpty(introspectionTimestamp)) {
				return introspectionTimestamp;
			}
			// if introspection timestamp property not filled, user endpoint was used
			String mfaTimestampProperty = BeansUtils.getCoreConfig().getUserInfoEndpointMfaAuthTimestampPropertyName();
			String mfaTimestamp = data.get(mfaTimestampProperty).toString();
			if (StringUtils.isNotEmpty(mfaTimestamp)) {
				return mfaTimestamp;
			}
		}

		return null;
	}
}

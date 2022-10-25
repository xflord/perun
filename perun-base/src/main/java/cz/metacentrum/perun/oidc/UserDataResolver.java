package cz.metacentrum.perun.oidc;

import com.fasterxml.jackson.databind.JsonNode;
import cz.metacentrum.perun.core.api.BeansUtils;
import cz.metacentrum.perun.core.api.exceptions.ExpiredTokenException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static cz.metacentrum.perun.core.api.PerunPrincipal.MFA_TIMESTAMP;

public class UserDataResolver {
	public final static String WELL_KNOWN_CONFIGURATION_PATH = "/.well-known/openid-configuration";

	private String SOURCE_IDP_NAME = "sourceIdPName";
	private String
	private static final Logger log = LoggerFactory.getLogger(UserDataResolver.class);
	private final UserInfoEndpointCall userInfoEndpointCall = new UserInfoEndpointCall();
	private final IntrospectionEndpointCall introspectionEndpointCall = new IntrospectionEndpointCall();

//	public EndpointResponse getEndpointData(String accessToken, String issuer, Map<String, String> additionalInformation) throws ExpiredTokenException {
//
//		JsonNode endpointResponse = callEndpoint(accessToken, issuer);
//
//		fillAdditionalInformationWithUserData(endpointResponse, additionalInformation);
//
//		String extSourceName = getExtSourceName(endpointResponse);
//			if (StringUtils.isEmpty(extSourceName)) {
//		log.info("issuer from user info endpoint was empty or null: {}", extSourceName);
//	}
//		String extSourceLogin = getExtSourceLogin(endpointResponse);
//			if (StringUtils.isEmpty(login)) {
//		log.info("user sub from user info endpoint was empty or null: {}", login);
//	}
//		return new EndpointResponse(extSourceName, extSourceLogin);
//	}

	public JsonNode getUserInfoEndpointData;
	public JsonNode getIntrospectionEndpointData;
	public JsonNode getWellKnownConfigurationEndpointData;

	public EndpointResponse getUserInfoFromOidc;

	/**
	 * Filling additional information with user's name, user's email and user-friendly format of extsource name
	 * @param endpointResponse
	 * @param additionalInformation
	 */
	private void fillAdditionalInformationWithUserData(JsonNode endpointResponse, Map<String, String> additionalInformation) {
		String name = JsonNodeParser.getName(endpointResponse);
		if(StringUtils.isNotEmpty(name)) {
			additionalInformation.put("displayName", name);
		}

		String email = JsonNodeParser.getSimpleField(endpointResponse, "email");
		if(StringUtils.isNotEmpty(email)) {
			additionalInformation.put(MAIL, email);
		}

		// retrieve friendly IdP name from the nested property
		String idpName = JsonNodeParser.getIdpName(endpointResponse);
		if (StringUtils.isNotEmpty(idpName)) {
			additionalInformation.put(SOURCE_IDP_NAME, idpName);
		}

		// update MFA timestamp
		String mfaTimestamp = JsonNodeParser.getMfaTimestamp(endpointResponse);
		if (mfaTimestamp != null && !mfaTimestamp.isEmpty()) {
			Instant mfaReadableTimestamp = Instant.ofEpochSecond(Long.parseLong(mfaTimestamp));
			additionalInformation.put(MFA_TIMESTAMP, mfaReadableTimestamp.toString());
		}
	}


}

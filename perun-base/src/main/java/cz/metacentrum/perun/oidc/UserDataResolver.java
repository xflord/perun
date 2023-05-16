package cz.metacentrum.perun.oidc;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import cz.metacentrum.perun.core.api.BeansUtils;
import cz.metacentrum.perun.core.api.exceptions.ExpiredTokenException;
import io.jsonwebtoken.impl.crypto.DefaultJwtSignatureValidator;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.spec.SecretKeySpec;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;

import static cz.metacentrum.perun.core.api.PerunPrincipal.MFA_TIMESTAMP;

public class UserDataResolver {

	private final String SOURCE_IDP_NAME = "sourceIdPName";
	private final String MAIL = "mail";
	private final String DISPLAY_NAME = "displayName";
	private final String EMAIL = "email";
	private static final Logger log = LoggerFactory.getLogger(UserDataResolver.class);
	private final EndpointCaller endpointCaller = new EndpointCaller();

	/**
	 * Parse and verify ID token as described in https://www.baeldung.com/java-jwt-token-decode.
	 * If parsing fails, returns null. Otherwise, returns JsonNode of user data to be parsed.
	 * @param token id token
	 * @return user data from parsed id token
	 */
	public static JsonNode parseIdToken(String token, String secretKey){
		ObjectMapper mapper = new ObjectMapper();
		String[] tokenParts = token.split("\\.");

		Base64.Decoder decoder = Base64.getUrlDecoder();
		String header = new String(decoder.decode(tokenParts[0]));
		String payload = new String(decoder.decode(tokenParts[1]));

		JsonNode headerData;
		try {
			headerData = mapper.readTree(header);
		} catch (JsonProcessingException e) {
			log.error("Could not parse ID token " + token);
			return null;
		}

		if (headerData.get("alg").isNull()) {
			log.info("ID token " + token + " is missing header part.");
			return null;
		}
		String algorithm = headerData.get("alg").textValue();

		io.jsonwebtoken.SignatureAlgorithm sa = io.jsonwebtoken.SignatureAlgorithm.valueOf(algorithm);
		SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getBytes(), algorithm);

		DefaultJwtSignatureValidator validator = new DefaultJwtSignatureValidator(sa, secretKeySpec);
		if (!validator.isValid(tokenParts[0] + "." + tokenParts[1], tokenParts[2])) {
			log.info("ID token " + token + " has invalid signature.");
			return null;
		}

		JsonNode payloadData;
		try {
			payloadData = mapper.readTree(payload);
		} catch (JsonProcessingException e) {
			log.error("Could not parse ID token " + token);
			return null;
		}

		return payloadData;
	}

	public EndpointResponse fetchUserData(String accessToken, String issuer, Map<String, String> additionalInformation) throws ExpiredTokenException {
		boolean useUserInfo = BeansUtils.getCoreConfig().getRequestUserInfoEndpoint();
		boolean useIntrospection = BeansUtils.getCoreConfig().getRequestIntrospectionEndpoint();

		EndpointResponse response = new EndpointResponse(null, null);
		if (!useUserInfo && !useIntrospection) {
			log.info("Fetching data from userInfo or introspection endpoint is not allowed.");
			return response;
		}

		JsonNode configurationResponse = endpointCaller.callConfigurationEndpoint(issuer);

		if (useIntrospection) {
			String url = JsonNodeParser.getSimpleField(configurationResponse, EndpointCaller.INTROSPECTION_ENDPOINT);
			if (url != null && !url.isBlank()) {
				JsonNode responseData = endpointCaller.callIntrospectionEndpoint(accessToken, url);
				response = processResponseData(responseData, additionalInformation);
				log.info("Retrieved info from introspection endpoint " + response);
			} else {
				log.error("Introspection endpoint URL not retrieved from well-known configuration from issuer " + issuer);
			}
		}

		if (useUserInfo) {
			String url = JsonNodeParser.getSimpleField(configurationResponse, EndpointCaller.USERINFO_ENDPOINT);
			if (url != null && !url.isBlank()) {
				JsonNode responseData = endpointCaller.callUserInfoEndpoint(accessToken, url);
				EndpointResponse userInfoResponse = processResponseData(responseData, additionalInformation);
				response = response.getIssuer() == null ? userInfoResponse : response;
				log.info("Retrieved info from user info endpoint " + response);
			} else {
				log.error("UserInfo endpoint URL not retrieved from well-known configuration from issuer " + issuer);
			}
		}

		return response;
	}

	public EndpointResponse fetchIntrospectionData(String accessToken, String issuer, Map<String, String> additionalInformation) throws ExpiredTokenException {
		boolean useIntrospection = BeansUtils.getCoreConfig().getRequestIntrospectionEndpoint();
		EndpointResponse response = new EndpointResponse(null, null);

		if (useIntrospection) {
			JsonNode configurationResponse = endpointCaller.callConfigurationEndpoint(issuer);
			String url = JsonNodeParser.getSimpleField(configurationResponse, EndpointCaller.INTROSPECTION_ENDPOINT);
			if (url != null && !url.isBlank()) {
				JsonNode responseData = endpointCaller.callIntrospectionEndpoint(accessToken, url);
				response = processResponseData(responseData, additionalInformation);
			} else {
				log.error("Introspection endpoint URL not retrieved from well-known configuration from issuer " + issuer);
			}
		}

		return response;
	}

	/**
	 * Filling additional information with user's name, user's email and user-friendly format of extsource name
	 * @param endpointResponse
	 * @param additionalInformation
	 */
	private void fillAdditionalInformationWithUserData(JsonNode endpointResponse, Map<String, String> additionalInformation) {
		String name = JsonNodeParser.getName(endpointResponse);
		if(StringUtils.isNotEmpty(name)) {
			additionalInformation.put(DISPLAY_NAME, name);
		}

		String email = JsonNodeParser.getSimpleField(endpointResponse, EMAIL);
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

	private EndpointResponse processResponseData(JsonNode endpointResponse, Map<String, String> additionalInformation) {
		fillAdditionalInformationWithUserData(endpointResponse, additionalInformation);

		String extSourceName = JsonNodeParser.getExtSourceName(endpointResponse);
		if (StringUtils.isEmpty(extSourceName)) {
			log.info("Issuer from endpoint was empty or null: {}", extSourceName);
		}
		String extSourceLogin = JsonNodeParser.getExtSourceLogin(endpointResponse);
		if (StringUtils.isEmpty(extSourceLogin)) {
			log.info("User sub from endpoint was empty or null: {}", extSourceLogin);
		}
		return new EndpointResponse(extSourceName, extSourceLogin);
	}

}

package cz.metacentrum.perun.oidc;

import com.fasterxml.jackson.databind.JsonNode;
import cz.metacentrum.perun.core.api.exceptions.ExpiredTokenException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import org.apache.commons.lang3.StringUtils;

/**
 * Class for executing call to User info endpoint.
 *
 * @author Lucie Kureckova <luckureckova@gmail.com>
 */
public class UserInfoEndpointCall extends EndpointCall {

	public static String USERINFO_ENDPOINT = "userinfo_endpoint";
	private final RestTemplate restTemplate = new RestTemplate();

	public JsonNode callEndpoint(String accessToken, String endpointUrl) throws ExpiredTokenException {

		HttpHeaders headers = new HttpHeaders();
		headers.setBearerAuth(accessToken);
		HttpEntity<Void> entity = new HttpEntity<>(headers);
		ResponseEntity<JsonNode> endpointResponse;

		try {
			endpointResponse = restTemplate.exchange(endpointUrl, HttpMethod.GET, entity, JsonNode.class);
		} catch (HttpClientErrorException ex) {
			if (ex.getStatusCode() == HttpStatus.FORBIDDEN) {
				log.error("Token {} is no longer valid when calling: {}", accessToken, endpointUrl);
				throw new ExpiredTokenException("Your token is no longer valid. Please retry from the start.");
			} else {
				log.error("Failed to get userInfoResponse for access token: {}. The response code was: {}", accessToken, ex.getStatusCode());
				throw new InternalErrorException("Failed to get userInfoResponse for access token: " + accessToken + ". The response code was: " + ex.getStatusCode());
			}
		}

		JsonNode userInfo = endpointResponse.getBody();
		if (userInfo == null) {
			log.error("User info from userInfo endpoint was null.");
			throw new InternalErrorException("Could not retrieve user data from userInfo endpoint.");
		}
		if (StringUtils.isNotEmpty(userInfo.path("error").asText())) {
			log.error("Call to user info endpoint failed, the error is: {}", userInfo);
			throw new InternalErrorException("Call to user info endpoint failed, the error is" + userInfo);
		}
		log.debug("User info retrieved - userInfo endpoint: {}", userInfo);
		return userInfo;
	}

	public String getEndpointUrl(String issuer) {
		JsonNode config = restTemplate.getForObject(issuer + WELL_KNOWN_CONFIGURATION_PATH, JsonNode.class);
		if (config == null || config.path(USERINFO_ENDPOINT) == null) {
			log.error("UserInfo endpoint not retrieved from " + issuer + WELL_KNOWN_CONFIGURATION_PATH);
			throw new InternalErrorException("Could not retrieve userInfo endpoint url from issuer " + issuer);
		}
		return config.path(USERINFO_ENDPOINT).asText();
	}
}

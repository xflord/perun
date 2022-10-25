package cz.metacentrum.perun.oidc;

import com.fasterxml.jackson.databind.JsonNode;
import cz.metacentrum.perun.core.api.BeansUtils;
import cz.metacentrum.perun.core.api.exceptions.ExpiredTokenException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;

/**
 * Class for executing call to Introspection endpoint.
 */
public class IntrospectionEndpointCall extends EndpointCall {

	public static String INTROSPECTION_ENDPOINT = "introspection_endpoint";
	private final RestTemplate restTemplate = new RestTemplate();
	private final static String TOKEN = "token";

	public JsonNode callEndpoint(String accessToken, String endpointUrl) throws ExpiredTokenException {
		HttpHeaders headers = prepareHeaders();

		HttpEntity<Void> entity = new HttpEntity<>(headers);
		ResponseEntity<JsonNode> endpointResponse;
		HashMap<String, String> params = new HashMap<>();
		params.put(TOKEN, accessToken);

		try {
			endpointResponse = restTemplate.exchange(endpointUrl, HttpMethod.GET, entity, JsonNode.class, params);
		} catch (HttpClientErrorException ex) {
			if (ex.getStatusCode() == HttpStatus.FORBIDDEN) {
				log.error("Token {} is no longer valid when calling: {}", accessToken, endpointUrl);
				throw new ExpiredTokenException("Your token is no longer valid. Please retry from the start.");
			} else {
				log.error("Failed to get introspectionResponse for access token: {}. The response code was: {}", accessToken, ex.getStatusCode());
				throw new InternalErrorException("Failed to get introspectionResponse for access token: " + accessToken + ". The response code was: " + ex.getStatusCode());
			}
		}

		JsonNode userInfo = endpointResponse.getBody();
		if (userInfo == null) {
			log.error("User info from introspection endpoint was null.");
			throw new InternalErrorException("Could not retrieve user data from introspection endpoint.");
		}
		if (StringUtils.isNotEmpty(userInfo.path("error").asText())) {
			log.error("Call to user introspection endpoint failed, the error is: {}", userInfo);
			throw new InternalErrorException("Call to introspection endpoint failed, the error is" + userInfo);
		}
		log.debug("User info retrieved - introspection endpoint: {}", userInfo);
		return userInfo;
	}

	private String getEndpointUrl(String issuer) {
		JsonNode config = restTemplate.getForObject(issuer + WELL_KNOWN_CONFIGURATION_PATH, JsonNode.class);
		if (config == null || config.path(INTROSPECTION_ENDPOINT) == null) {
			log.error("Introspection endpoint not retrieved from " + issuer + WELL_KNOWN_CONFIGURATION_PATH);
			throw new InternalErrorException("Could not retrieve introspection endpoint url from issuer " + issuer);
		}
		return config.path(INTROSPECTION_ENDPOINT).asText();
	}

	private HttpHeaders prepareHeaders() {
		HttpHeaders headers = new HttpHeaders();
		String clientId = BeansUtils.getCoreConfig().getOidcClientId();
		String clientSecret = BeansUtils.getCoreConfig().getOidcClientSecret();
		headers.setBasicAuth(clientId, clientSecret);
		return headers;
	}
}

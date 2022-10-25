package cz.metacentrum.perun.oidc;

import com.fasterxml.jackson.databind.JsonNode;
import cz.metacentrum.perun.core.api.exceptions.ExpiredTokenException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class EndpointCall {
	public final static String WELL_KNOWN_CONFIGURATION_PATH = "/.well-known/openid-configuration";

	protected static final Logger log = LoggerFactory.getLogger(EndpointCall.class);

	abstract public JsonNode callEndpoint(String accessToken, String endpointUrl) throws ExpiredTokenException;
}

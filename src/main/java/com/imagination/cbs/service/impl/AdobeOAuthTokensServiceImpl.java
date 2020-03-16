package com.imagination.cbs.service.impl;

import static com.imagination.cbs.util.AdobeConstant.ACCESS_TOKEN;
import static com.imagination.cbs.util.AdobeConstant.ADOBE;
import static com.imagination.cbs.util.AdobeConstant.ADOBE_ACCESS_TOKEN;
import static com.imagination.cbs.util.AdobeConstant.ADOBE_ACCESS_TOKEN_EXP_TIME;
import static com.imagination.cbs.util.AdobeConstant.ADOBE_AUTH_CODE;
import static com.imagination.cbs.util.AdobeConstant.ADOBE_CLIENT_ID;
import static com.imagination.cbs.util.AdobeConstant.ADOBE_CLIENT_SECRET;
import static com.imagination.cbs.util.AdobeConstant.ADOBE_GRANT_TYPE;
import static com.imagination.cbs.util.AdobeConstant.ADOBE_REDIRECT_URL;
import static com.imagination.cbs.util.AdobeConstant.ADOBE_REFRESH_TOKEN;
import static com.imagination.cbs.util.AdobeConstant.BEARER;
import static com.imagination.cbs.util.AdobeConstant.CLIENT_ID;
import static com.imagination.cbs.util.AdobeConstant.CLIENT_SECRET;
import static com.imagination.cbs.util.AdobeConstant.CODE;
import static com.imagination.cbs.util.AdobeConstant.EXPIRES_IN;
import static com.imagination.cbs.util.AdobeConstant.GRANT_TYPE;
import static com.imagination.cbs.util.AdobeConstant.OAUTH_ACCESS_TOKEN_ENDPOINT;
import static com.imagination.cbs.util.AdobeConstant.OAUTH_BASE_URL;
import static com.imagination.cbs.util.AdobeConstant.OAUTH_REFRESH_TOKEN_ENDPOINT;
import static com.imagination.cbs.util.AdobeConstant.REDIRECT_URI;
import static com.imagination.cbs.util.AdobeConstant.REFRESH_TOKEN;
import static com.imagination.cbs.util.AdobeConstant.TOKEN_TYPE;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.imagination.cbs.domain.Config;
import com.imagination.cbs.model.AdobeOAuth;
import com.imagination.cbs.repository.ConfigRepository;
import com.imagination.cbs.service.AdobeOAuthTokensService;
import com.imagination.cbs.service.OAuthService;
import com.imagination.cbs.util.AdobeUtils;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class AdobeOAuthTokensServiceImpl implements AdobeOAuthTokensService {

	@Autowired
	private Environment env;

	@Autowired
	private ConfigRepository configRepository;

	@Autowired
	private RestTemplate restTemplate;

	@Autowired
	private OAuthService oAuthService;

	public String getOauthAccessToken() {
		String oauthAccessToken = "";
		String oauthRefreshToken = "";

		Map<String, Config> keys = getAdobeKeyDetails(ADOBE);
		keys.forEach((k, v) -> log.info("DB KeyName= {} KeyValue= {}", k, v.getKeyValue()));

		if (!isMapKeyValueEmptyOrNull(keys, ADOBE_ACCESS_TOKEN)) {

			if (AdobeUtils.isExpired(keys.get(ADOBE_ACCESS_TOKEN_EXP_TIME).getKeyValue())) {

				oauthAccessToken = keys.get(ADOBE_ACCESS_TOKEN).getKeyValue();
				return BEARER + oauthAccessToken;

			} else {
				oauthRefreshToken = keys.get(ADOBE_REFRESH_TOKEN).getKeyValue();
				oauthAccessToken = getNewAccessToken(oauthRefreshToken);
				return BEARER + oauthAccessToken;
			}

		} else {

			if (!isMapKeyValueEmptyOrNull(keys, ADOBE_REFRESH_TOKEN))
				oauthRefreshToken = keys.get(ADOBE_REFRESH_TOKEN).getKeyValue();

			oauthAccessToken = getNewAccessToken(oauthRefreshToken);
		}

		log.debug("BEARER TOKEN:::: {}", BEARER + oauthAccessToken);

		return BEARER + oauthAccessToken;
	}

	public AdobeOAuth getOauthAccessTokenFromRefreshToken(String oAuthRefreshToken) {
		log.debug("oAuthToken::Refresh:: {}", oAuthRefreshToken);
		JsonNode response = null;
		AdobeOAuth oAuth = null;

		String uri = OAUTH_BASE_URL + OAUTH_REFRESH_TOKEN_ENDPOINT;
		HttpHeaders headers = new HttpHeaders();

		try {

			headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE);

			MultiValueMap<String, String> requestBody = getRequestBody(oAuthRefreshToken);
			HttpEntity<?> httpEntity = new HttpEntity<>(requestBody, headers);

			response = restTemplate.exchange(uri, HttpMethod.POST, httpEntity, JsonNode.class).getBody();
			oAuth = convertJsonToObj(response);
			log.info("Get access token using aefresh token::: {}", oAuth);

			oAuth.setRefreshToken(oAuthRefreshToken);

		} catch (Exception e) {
			log.info("Exception refresh token:::{}", e);
		}
		return oAuth;
	}

	public String getBaseURIForRestAPI(String accessToken) {
		String servicesBaseUrl = "";
		Map<String, Config> keyValue = getAdobeKeyDetails("ADOBE_SERVICES_BASE_URL");
		log.info("servicesBaseUrl= {}", keyValue);

		if (!isNullOrEmptyMap(keyValue)) {

			servicesBaseUrl = keyValue.get("ADOBE_SERVICES_BASE_URL").getKeyValue();
			return servicesBaseUrl;

		} else {

			ResponseEntity<JsonNode> res = null;

			HttpHeaders head = new HttpHeaders();
			head.add(HttpHeaders.AUTHORIZATION, accessToken);

			HttpEntity<String> httpEntity = new HttpEntity<>(head);

			res = restTemplate.exchange(servicesBaseUrl, HttpMethod.GET, httpEntity, JsonNode.class);

			servicesBaseUrl = res.getBody().path("apiAccessPoint").asText() + "api/rest/v6";
			log.info("ApiAccessPoint BaseUris:::{}", servicesBaseUrl);

			return servicesBaseUrl;
		}
	}

	public AdobeOAuth convertJsonToObj(JsonNode res) {
		AdobeOAuth oAuth = null;

		try {
			oAuth = new AdobeOAuth();
			oAuth.setAccessToken(res.path(ACCESS_TOKEN).asText());
			oAuth.setRefreshToken(res.path(REFRESH_TOKEN).asText());
			oAuth.setTokenType(res.path(TOKEN_TYPE).asText());
			oAuth.setExpiresIn(res.path(EXPIRES_IN).asInt());
			log.info("JsonNode to AdobeoAuth:: {}", oAuth);

		} catch (Exception e) {
			log.info("Exception inside convertJsonToObj():: {}" + e);
		}

		return oAuth;
	}

	public String getNewAccessToken(String oauthRefreshToken) {

		ResponseEntity<JsonNode> results = null;
		AdobeOAuth oAuths = null;

		String url = OAUTH_BASE_URL + OAUTH_ACCESS_TOKEN_ENDPOINT;
		MultiValueMap<String, String> headers = new HttpHeaders();

		headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE);
		HttpEntity<?> httpEntity = new HttpEntity<>(getRequestBody(), headers);

		try {

			if (oauthRefreshToken.isEmpty()) {

				results = restTemplate.exchange(url, HttpMethod.POST, httpEntity, JsonNode.class);
				log.info("AdobeOAuth:::results: {}", results);
				oAuths = convertJsonToObj(results.getBody());
				log.info("AdobeOAuth:::statusCode: {} result: :{}", results.getStatusCode(), results);

			} else {
				oAuths = getOauthAccessTokenFromRefreshToken(oauthRefreshToken);
				log.info("Get Access Token Used by Refresh Token :::{}", oAuths);
			}

		} catch (Exception e) {
			log.info("Exception insdie the:::{}", e);
		}

		saveOrUpdateAdobeKeys(oAuths);
		return oAuths.getAccessToken();

	}

	private void saveOrUpdateAdobeKeys(AdobeOAuth oAuth) {

		boolean result = oAuthService.saveOrUpdateAccessToken(oAuth);
		log.info("Inside saveOrUpdateAdobeKeys() :::: {}" + result);

	}

	public MultiValueMap<String, String> getRequestBody(String refreshToken) {

		Map<String, Config> adobeKeys = getAdobeKeyDetails(ADOBE);

		MultiValueMap<String, String> body = new LinkedMultiValueMap<>();

		body.add(REFRESH_TOKEN, refreshToken);
		body.add(CLIENT_ID, adobeKeys.get(ADOBE_CLIENT_ID).getKeyValue());
		body.add(CLIENT_SECRET, adobeKeys.get(ADOBE_CLIENT_SECRET).getKeyValue());
		body.add(GRANT_TYPE, REFRESH_TOKEN);

		log.info("Refresh Token Body ::: {} ", body);
		return body;
	}

	public MultiValueMap<String, String> getRequestBody() {

		Map<String, Config> adobeKeys = getAdobeKeyDetails(ADOBE);

		MultiValueMap<String, String> body = new LinkedMultiValueMap<>();

		body.add(CODE, adobeKeys.get(ADOBE_AUTH_CODE).getKeyValue());
		body.add(CLIENT_ID, adobeKeys.get(ADOBE_CLIENT_ID).getKeyValue());
		body.add(CLIENT_SECRET, adobeKeys.get(ADOBE_CLIENT_SECRET).getKeyValue());
		body.add(REDIRECT_URI, adobeKeys.get(ADOBE_REDIRECT_URL).getKeyValue());
		body.add(GRANT_TYPE, adobeKeys.get(ADOBE_GRANT_TYPE).getKeyValue());

		log.info("Access Token Body:::{}", body);

		return body;
	}

	public Map<String, Config> getAdobeKeyDetails(String keyName) {

		log.info("keyName:::{}", keyName);

		Map<String, Config> map = null;
		List<Config> keysList = null;

		keysList = configRepository.findBykeyNameStartingWith(keyName);

		if (keysList == null || keysList.isEmpty())
			return null;

		map = keysList.stream().filter(key -> key.getKeyValue() != null || !key.getKeyValue().isEmpty())
				.collect(Collectors.toMap(Config::getKeyName, config -> config));

		if (isNullOrEmptyMap(map))
			return null;

		return map;
	}

	public static boolean isNullOrEmptyMap(Map<?, ?> map) {

		return ((map == null || map.isEmpty()));
	}

	public static boolean isMapKeyValueEmptyOrNull(Map<?, ?> map, String key) {

		if ((map == null || map.isEmpty()))
			return true;

		if (key == null || key.isEmpty())
			return true;

		Config config = (Config) map.get(key);
		log.info("Config object config= {} key= {}" + config);

		return (config == null || config.getKeyValue().isEmpty());

	}

}
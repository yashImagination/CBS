package com.imagination.cbs.service.impl;

import static com.imagination.cbs.util.AdobeConstant.ACCESS_POINT;
import static com.imagination.cbs.util.AdobeConstant.ADOBE_ACCESS_TOKEN;
import static com.imagination.cbs.util.AdobeConstant.ADOBE_ACCESS_TOKEN_EXP_TIME;
import static com.imagination.cbs.util.AdobeConstant.ADOBE_API_BASE_URI;
import static com.imagination.cbs.util.AdobeConstant.ADOBE_AUTH_CODE;
import static com.imagination.cbs.util.AdobeConstant.ADOBE_OAUTH_BASE_URL;
import static com.imagination.cbs.util.AdobeConstant.ADOBE_REFRESH_TOKEN;
import static com.imagination.cbs.util.AdobeConstant.ADOBE_TOKEN_TYPE;
import static com.imagination.cbs.util.AdobeConstant.AGREEMENTS_COMBINEDDOCUMENT;
import static com.imagination.cbs.util.AdobeConstant.AGREEMENTS_ENDPOINT;
import static com.imagination.cbs.util.AdobeConstant.EMAIL;
import static com.imagination.cbs.util.AdobeConstant.FILEINFOS;
import static com.imagination.cbs.util.AdobeConstant.ID;
import static com.imagination.cbs.util.AdobeConstant.MEMBERINFOS;
import static com.imagination.cbs.util.AdobeConstant.NAME;
import static com.imagination.cbs.util.AdobeConstant.OAUTH_CODE_ACCESS_ENDPOINT;
import static com.imagination.cbs.util.AdobeConstant.ORDER;
import static com.imagination.cbs.util.AdobeConstant.PARTICIPANTSETSINFO;
import static com.imagination.cbs.util.AdobeConstant.ROLE;
import static com.imagination.cbs.util.AdobeConstant.SIGNATURETYPE;
import static com.imagination.cbs.util.AdobeConstant.SIGNATURE_ESIGN;
import static com.imagination.cbs.util.AdobeConstant.STATE;
import static com.imagination.cbs.util.AdobeConstant.STATE_IN_PROCESS;
import static com.imagination.cbs.util.AdobeConstant.TRANSIENT_DOCUMENTS_ENDPOINT;
import static com.imagination.cbs.util.AdobeConstant.TRANSIENT_DOCUMENT_ID;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.StringJoiner;

import javax.transaction.Transactional;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.imagination.cbs.domain.BookingRevision;
import com.imagination.cbs.domain.Config;
import com.imagination.cbs.dto.AdobeOAuthDto;
import com.imagination.cbs.exception.CBSApplicationException;
import com.imagination.cbs.exception.ResourceNotFoundException;
import com.imagination.cbs.repository.ConfigRepository;
import com.imagination.cbs.service.AdobeSignService;
import com.imagination.cbs.util.AdobeConstant;
import com.imagination.cbs.util.AdobeTokenUtility;
import com.imagination.cbs.util.CBSDateUtils;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class AdobeSignServiceImpl implements AdobeSignService {

	@Autowired
	private ConfigRepository configRepository;

	@Autowired
	private RestTemplate restTemplate;

	@Autowired
	private Environment env;

	@Autowired
	private AdobeTokenUtility adobeTokenUtility;

	public InputStream downloadAgreement(String agreementId) {

		ResponseEntity<byte[]> result = null;
		InputStream inputStream = null;

		try {

			String accessToken = adobeTokenUtility.getOauthAccessToken();

			HttpHeaders headers = new HttpHeaders();
			headers.add(HttpHeaders.AUTHORIZATION, accessToken);

			HttpEntity<String> httpEntity = new HttpEntity<>(headers);

			String url = adobeTokenUtility.getBaseURIForRestAPI(accessToken) + AGREEMENTS_COMBINEDDOCUMENT;

			UriComponents uriComponents = UriComponentsBuilder.fromUriString(url).build();
			uriComponents = uriComponents.expand(Collections.singletonMap("agreementId", agreementId));

			log.info("uri::: {}" + uriComponents.toUriString());

			result = restTemplate.exchange(uriComponents.toUriString(), HttpMethod.GET, httpEntity, byte[].class);

			inputStream = new ByteArrayInputStream(result.getBody());

		} catch (RuntimeException exception) {

			throw new ResourceNotFoundException(exception.getMessage());
		}

		return inputStream;

	}

	@Transactional
	@Override
	public void saveOrUpdateAdobeKeys(AdobeOAuthDto oAuth) {

		Map<String, Config> result = new HashMap<>();

		try {
			Config c1 = configRepository.findByKeyName(ADOBE_ACCESS_TOKEN).map(c -> {
				c.setKeyValue(oAuth.getAccessToken());
				c.setChangedDate(new Timestamp(System.currentTimeMillis()));
				return configRepository.save(c);
			}).orElseGet(() -> {
				Config con = new Config();
				con.setKeyName(ADOBE_ACCESS_TOKEN);
				con.setKeyValue(oAuth.getAccessToken());
				con.setChangedDate(new Timestamp(System.currentTimeMillis()));
				con.setKeyDescription("Adobe access token");
				return configRepository.save(con);
			});
			result.put(ADOBE_ACCESS_TOKEN, c1);

			Config c2 = configRepository.findByKeyName(ADOBE_REFRESH_TOKEN).map(c -> {
				c.setKeyValue(oAuth.getRefreshToken());
				c.setChangedDate(new Timestamp(System.currentTimeMillis()));
				return configRepository.save(c);
			}).orElseGet(() -> {
				Config con = new Config();
				con.setKeyName(ADOBE_REFRESH_TOKEN);
				con.setKeyValue(oAuth.getRefreshToken());
				con.setChangedDate(new Timestamp(System.currentTimeMillis()));
				con.setKeyDescription("Adobe refresh token");
				return configRepository.save(con);
			});
			result.put(ADOBE_REFRESH_TOKEN, c2);

			Config c3 = configRepository.findByKeyName(ADOBE_TOKEN_TYPE).map(c -> {
				c.setKeyValue(oAuth.getTokenType());
				c.setChangedDate(new Timestamp(System.currentTimeMillis()));
				return configRepository.save(c);
			}).orElseGet(() -> {
				Config con = new Config();
				con.setKeyName(ADOBE_TOKEN_TYPE);
				con.setKeyValue(oAuth.getTokenType());
				con.setChangedDate(new Timestamp(System.currentTimeMillis()));
				con.setKeyDescription("Adobe access token type");
				return configRepository.save(con);
			});
			result.put("ADOBE_TOKEN_TYPE", c3);

			Config c4 = configRepository.findByKeyName(ADOBE_ACCESS_TOKEN_EXP_TIME).map(c -> {
				c.setKeyValue(CBSDateUtils.getCurrentDateTime(oAuth.getExpiresIn()));
				c.setChangedDate(new Timestamp(System.currentTimeMillis()));
				return configRepository.save(c);
			}).orElseGet(() -> {
				Config con = new Config();
				con.setKeyName(ADOBE_ACCESS_TOKEN_EXP_TIME);
				con.setKeyValue(CBSDateUtils.getCurrentDateTime(oAuth.getExpiresIn()));
				con.setChangedDate(new Timestamp(System.currentTimeMillis()));
				con.setKeyDescription("Adobe access token expired in next one hours");
				return configRepository.save(con);
			});
			result.put(ADOBE_ACCESS_TOKEN_EXP_TIME, c4);
			log.info("result:::{}", new ObjectMapper().writeValueAsString(result));

		} catch (Exception exception) {

			throw new ResourceNotFoundException(exception.getLocalizedMessage());

		}
	}

	@Override
	public String uploadAndCreateAgreement(InputStream inputStream, String fileName) {

		try {

			ResponseEntity<JsonNode> result = null;
			LinkedMultiValueMap<String, Object> body = null;

			String accessToken = adobeTokenUtility.getOauthAccessToken();
			String transientDocUrl = adobeTokenUtility.getBaseURIForRestAPI(accessToken) + TRANSIENT_DOCUMENTS_ENDPOINT;

			log.info("transientDocUrl={} fileName={}", transientDocUrl, fileName);

			body = new LinkedMultiValueMap<>();
			body.add(AdobeConstant.HttpHeaderField.FILE.toString(), new InputStreamResource(inputStream));
			body.add(AdobeConstant.HttpHeaderField.FILE_NAME.toString(), fileName);
			body.add(AdobeConstant.HttpHeaderField.MIME_TYPE.toString(), MediaType.APPLICATION_PDF_VALUE);

			HttpHeaders headers = new HttpHeaders();
			headers.add(HttpHeaders.CONTENT_TYPE, MediaType.MULTIPART_FORM_DATA_VALUE);
			headers.add(AdobeConstant.HttpHeaderField.AUTHORIZATION.toString(), accessToken);

			HttpEntity<?> entity = new HttpEntity<>(body, headers);

			result = restTemplate.exchange(transientDocUrl, HttpMethod.POST, entity, JsonNode.class);

			String transientDocumentId = result.getBody().path(TRANSIENT_DOCUMENT_ID).asText();
			log.info("transientDocumentId:: {}", transientDocumentId);

			return transientDocumentId;

		} catch (Exception e) {
			log.info("Exception inside Upload Agreement");
		}
		return null;
	}

	@Override
	public String sendAgreement(String transientDocId, BookingRevision bookingRevision) {

		try {

			ResponseEntity<JsonNode> response = null;
			HttpHeaders header = null;
			String accessToken = adobeTokenUtility.getOauthAccessToken();
			String agreementsApiUrl = adobeTokenUtility.getBaseURIForRestAPI(accessToken) + AGREEMENTS_ENDPOINT;
			String requestBody = createAgrrementRequestBody(transientDocId, bookingRevision);

			header = new HttpHeaders();
			header.setContentType(MediaType.APPLICATION_JSON);
			header.add(HttpHeaders.AUTHORIZATION, accessToken);

			HttpEntity<?> httpentity = new HttpEntity<>(requestBody, header);
			response = restTemplate.exchange(agreementsApiUrl, HttpMethod.POST, httpentity, JsonNode.class);

			log.info("Agreements Id:: {}", response.getBody());

			return response.getBody().path(ID).asText();

		} catch (Exception exception) {

			throw new CBSApplicationException(exception.getLocalizedMessage());
		}
	}

	@Override
	public void saveOrUpdateAuthCode(String authcode, String apiAccessPoint, String webAccessPoint) {

		Optional<Config> config = configRepository.findByKeyName(ADOBE_AUTH_CODE);
		if (config.isPresent()) {
			Config con = config.get();
			con.setKeyValue(authcode);
			con.setChangedDate(new Timestamp(System.currentTimeMillis()));
			configRepository.save(con);
		} else {
			Config con = new Config();
			con.setKeyName(ADOBE_AUTH_CODE);
			con.setKeyValue(authcode);
			con.setChangedDate(new Timestamp(System.currentTimeMillis()));
			con.setChangedDate(new Timestamp(System.currentTimeMillis()));
			configRepository.save(con);
		}

		Optional<Config> configApiAccessPoint = configRepository.findByKeyName(ADOBE_API_BASE_URI);
		if (configApiAccessPoint.isPresent()) {
			Config con = configApiAccessPoint.get();
			con.setKeyValue(apiAccessPoint + ACCESS_POINT);
			con.setChangedDate(new Timestamp(System.currentTimeMillis()));
			configRepository.save(con);
		} else {
			Config con = new Config();
			con.setKeyName(ADOBE_API_BASE_URI);
			con.setKeyValue(apiAccessPoint + ACCESS_POINT);
			con.setChangedDate(new Timestamp(System.currentTimeMillis()));
			con.setKeyDescription("Adobe API access base uri");
			configRepository.save(con);
		}

		Optional<Config> configAdobeOauthBaseUrl = configRepository.findByKeyName(ADOBE_OAUTH_BASE_URL);
		if (configAdobeOauthBaseUrl.isPresent()) {
			Config con = configAdobeOauthBaseUrl.get();
			con.setKeyValue(apiAccessPoint + OAUTH_CODE_ACCESS_ENDPOINT);
			con.setChangedDate(new Timestamp(System.currentTimeMillis()));
			configRepository.save(con);
		} else {
			Config con = new Config();
			con.setKeyName(ADOBE_OAUTH_BASE_URL);
			con.setKeyValue(apiAccessPoint + OAUTH_CODE_ACCESS_ENDPOINT);
			con.setChangedDate(new Timestamp(System.currentTimeMillis()));
			con.setKeyDescription("Adobe OAuth code base uri");
			configRepository.save(con);
		}
	}

	@SuppressWarnings("unchecked")
	private String createAgrrementRequestBody(String transientDocId, BookingRevision bookingRevision)
			throws JsonProcessingException {

		ObjectMapper mapper = new ObjectMapper();
		StringJoiner nameJoiner = new StringJoiner("-");

		nameJoiner.add(String.valueOf(bookingRevision.getBooking().getBookingId()));
		nameJoiner.add(bookingRevision.getJobNumber());
		nameJoiner.add(bookingRevision.getJobname());

		JSONObject agreement = new JSONObject();
		agreement.put(NAME, nameJoiner.toString());
		agreement.put(SIGNATURETYPE, SIGNATURE_ESIGN);
		agreement.put(STATE, STATE_IN_PROCESS);

		JSONArray fileInfosArray = new JSONArray();
		JSONObject fileInfos = new JSONObject();
		fileInfos.put(TRANSIENT_DOCUMENT_ID, transientDocId);
		fileInfosArray.add(fileInfos);

		JSONArray participantSetsInfoArray = new JSONArray();
		JSONObject participant = new JSONObject();
		participant.put(ORDER, 1);
		participant.put(ROLE, "SIGNER");

		JSONArray memberInfosArray = new JSONArray();
		JSONObject memberInfos = new JSONObject();

		if (Arrays.stream(env.getActiveProfiles()).anyMatch(envi -> (envi.equalsIgnoreCase("dev")
				|| envi.equalsIgnoreCase("local") || envi.equalsIgnoreCase("qual")))) {

			memberInfos.put(EMAIL, "ramesh.suryaneni@imagination.com");

		} else {

			memberInfos.put(EMAIL, bookingRevision.getContractor().getEmail());
		}

		memberInfosArray.add(memberInfos);

		participant.put(MEMBERINFOS, memberInfosArray);
		participantSetsInfoArray.add(participant);

		agreement.put(FILEINFOS, fileInfosArray);
		agreement.put(PARTICIPANTSETSINFO, participantSetsInfoArray);

		return mapper.writeValueAsString(agreement);

	}

}
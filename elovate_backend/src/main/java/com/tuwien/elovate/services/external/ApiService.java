package com.tuwien.elovate.services.external;

/*-
 * #%L
 * ELOvate
 * %%
 * Copyright (C) 2024 ELOvate GmbH.
 * %%
 * Copyright (C) 2024 ELOvate GmbH. - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * #L%
 */

import com.google.gson.Gson;
import com.tuwien.elovate.exceptions.archetype.Message;
import com.tuwien.elovate.exceptions.impl.BadRequestException;
import com.tuwien.elovate.exceptions.impl.ServiceUnavailableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;


@Service
public class ApiService {

    private static final Logger log = LoggerFactory.getLogger(ApiService.class);

    /**
     * Sends a post request with the given request body to the given url.
     *
     * @param url         Url that post request is sent to
     * @param requestBody Body that will be sent with the request
     * @return the JSON body as String. Can be mapped with mapJsonToEntity method
     */
    public String sendPostRequest(final String url, final Object requestBody) {
        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        final HttpEntity<Object> requestEntity = new HttpEntity<>(requestBody, headers);

        final RestTemplate restTemplate = new RestTemplate();
        try {
            log.info("Sending Post Request to {}", url);
            final ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);
            return response.getBody();
        } catch (HttpClientErrorException e) {
            log.error("Post request to {} has failed. Client error", url);
            log.error(e.getMessage());
            throw new BadRequestException(Message.SERVER_ERROR);
        } catch (HttpServerErrorException e) {
            log.error("Post request to {} has failed. Service is unavailable or timed out", url);
            throw new ServiceUnavailableException(Message.SERVER_ERROR);
        }
    }

    /**
     * Maps a json String to the corresponding class. Throws an Exception if mapping is not possible
     *
     * @param jsonData     json Data as a String
     * @param targetEntity Class of the entity that the data should be mapped
     * @param <T>          Class which is parsed by targetEntity.
     * @return the Entity of the Data
     */
    public <T> T mapJsonToEntity(final String jsonData, Class<T> targetEntity) {
        final Gson gson = new Gson();
        return gson.fromJson(jsonData, targetEntity);
    }
}

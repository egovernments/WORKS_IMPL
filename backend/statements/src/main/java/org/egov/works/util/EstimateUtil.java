package org.egov.works.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.models.RequestInfoWrapper;
import org.egov.common.contract.request.RequestInfo;
import org.egov.tracer.model.ServiceCallException;
import org.egov.works.config.StatementConfiguration;
import org.egov.works.services.common.models.estimate.EstimateResponse;
import org.egov.works.web.models.Statement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.HashSet;
import java.util.Set;

@Component
@Slf4j
public class EstimateUtil {

private final StatementConfiguration statementConfiguration;
private final RestTemplate restTemplate;
    @Autowired
    private ObjectMapper mapper;


@Autowired
    public EstimateUtil(StatementConfiguration statementConfiguration,RestTemplate restTemplate) {
        this.statementConfiguration = statementConfiguration;
        this.restTemplate=restTemplate;
    }


    public Boolean isValidEstimate( String estimateId,String tenantId, String statementType,RequestInfo requestInfo){

        EstimateResponse estimateResponse=getEstimate(estimateId, tenantId,statementType,requestInfo);
        return (estimateResponse!=null &&!estimateResponse.getEstimates().isEmpty());
    }
    private StringBuilder getSearchURLWithParams(String tenantId,String statementType, Set<String> estimateIds) {
        StringBuilder url = new StringBuilder(statementConfiguration.getEstimateHost());
        url.append(statementConfiguration.getEstimateSearchEndpoint());
        url.append("?tenantId=");
        url.append(tenantId);
        url.append("&ids=");
        url.append(String.join(", ", estimateIds));
        if(statementType.equals(Statement.StatementTypeEnum.UTILIZATION.toString())){
            url.append("&status=ACTIVE");
        }

        return url;
    }

    public EstimateResponse getEstimate(String estimateId, String tenantId, String statementType, RequestInfo requestInfo) {
        log.info("EstimateUtil::getEstimate");
        Set<String> estimateIdSet= new HashSet<>();
        estimateIdSet.add(estimateId);
        StringBuilder url = getSearchURLWithParams(tenantId,statementType, estimateIdSet);
        RequestInfoWrapper requestInfoWrapper = RequestInfoWrapper.builder().requestInfo(requestInfo).build();
        
        // Retry logic to handle race condition when estimate is not yet persisted
        int maxRetries = 3;
        int retryDelayMs = 1000; // Start with 1 second
        EstimateResponse fetchEstimates = null;

        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            fetchEstimates = fetchResult(url, requestInfoWrapper);

            if (fetchEstimates != null && fetchEstimates.getEstimates() != null && !fetchEstimates.getEstimates().isEmpty()) {
                log.info("Successfully fetched estimate on attempt {}", attempt);
                return fetchEstimates;
            }

            if (attempt < maxRetries) {
                log.warn("Estimate not found on attempt {}. Retrying after {} ms. This may happen if estimate is not yet persisted to database.",
                         attempt, retryDelayMs);
                try {
                    Thread.sleep(retryDelayMs);
                    retryDelayMs *= 2; // Exponential backoff
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    log.error("Retry interrupted", e);
                    break;
                }
            }
        }

        log.error("Failed to fetch estimate after {} attempts for ID: {}, tenant: {}", maxRetries, estimateId, tenantId);
        return fetchEstimates;
    }
    public EstimateResponse fetchResult(StringBuilder uri, Object request) {
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        EstimateResponse response = null;
        try {
            log.info("Calling estimate service with URL: {}", uri.toString());
            response = restTemplate.postForObject(uri.toString(), request, EstimateResponse.class);
            log.info("Estimate service response received: {}", response != null ? "Not null" : "NULL");
            if (response != null && response.getEstimates() != null) {
                log.info("Number of estimates in response: {}", response.getEstimates().size());
            }
        } catch (HttpClientErrorException e) {
            log.error("External Service threw an Exception: ", e);
            throw new ServiceCallException(e.getResponseBodyAsString());
        } catch (Exception e) {
            log.error("Exception while fetching from searcher: ", e);
            throw new ServiceCallException(e.getMessage());
        }

        return response;
    }
}


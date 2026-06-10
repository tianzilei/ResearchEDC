package org.researchedc.service.pmanage;

import java.util.concurrent.TimeUnit;


import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import org.researchedc.dao.core.CoreResources;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

public class RandomizationRegistrar {

    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());
    public static final String AVAILABLE = "available";
    public static final String UNAVAILABLE = "unavailable";
    public static final String INVALID = "invalid";
    public static final String UNKNOWN = "unknown";
    public static final int RANDOMIZATION_READ_TIMEOUT = 5000;
    private static final String CACHE_KEY = "randomizeObject";
    private final Cache<String, SeRandomizationDTO> cache;

    public RandomizationRegistrar() {
        this.cache = Caffeine.newBuilder()
            .maximumSize(200)
            .expireAfterWrite(10000, TimeUnit.SECONDS)
            .build();
    }

    // Rest Call to OCUI to get Randomization
    public SeRandomizationDTO getRandomizationDTOObject(String studyOid) {
        String ocUrl = CoreResources.getField("sysURL.base") + "rest2/openrosa/" + studyOid;
        String randomizationUrl = CoreResources.getField("moduleManager") + "/app/rest/oc/se_randomizations?studyoid=" + studyOid + "&instanceurl=" + ocUrl;
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();

        requestFactory.setReadTimeout(RANDOMIZATION_READ_TIMEOUT);
        RestTemplate rest = new RestTemplate(requestFactory);

        try {
            SeRandomizationDTO response = rest.getForObject(randomizationUrl, SeRandomizationDTO.class);
            if (response.getStudyOid() != null) {
                return response;
            } else {
                return null;
            }

        } catch (Exception e) {
            logger.error(e.getMessage());
            logger.error(ExceptionUtils.getStackTrace(e));
            System.out.println(e.getMessage());
            System.out.println(ExceptionUtils.getStackTrace(e));

        }
        return null;
    }

    public SeRandomizationDTO getCachedRandomizationDTOObject(String studyOid, Boolean resetCache) throws Exception {
        SeRandomizationDTO seRandomizationDTO = null;
        String ocUrl = CoreResources.getField("sysURL.base");
        String mapKey = ocUrl + studyOid;
        if (!resetCache) {
            seRandomizationDTO = cache.getIfPresent(mapKey);
        }

        if (seRandomizationDTO == null) {
            seRandomizationDTO = getRandomizationDTOObject(studyOid);
        }
        if (seRandomizationDTO != null) {
            cache.put(mapKey, seRandomizationDTO);
        }
        return seRandomizationDTO;
    }


}

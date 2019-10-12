package com.vektorraum.voicecontrol.twilio.security;

import com.twilio.security.RequestValidator;
import com.vektorraum.voicecontrol.twilio.config.TwilioConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@WebFilter(urlPatterns = "/twilio/*")
@Slf4j
public class TwilioValidationFilter implements Filter {
    private TwilioConfig twilioConfig;
    private RequestValidator requestValidator;

    @Autowired
    public TwilioValidationFilter(TwilioConfig twilioConfig) {
        this.twilioConfig = twilioConfig;
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        requestValidator = new RequestValidator(twilioConfig.getAuthToken());
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        boolean isValidRequest = false;
        if (request instanceof HttpServletRequest) {
            HttpServletRequest httpRequest = (HttpServletRequest) request;

            // Concatenates the request URL with the query string
            String pathAndQueryUrl = getRequestUrlAndQueryString(httpRequest);
            // Extracts only the POST parameters and converts the parameters Map type
            Map<String, String> postParams = extractPostParams(httpRequest);
            String signatureHeader = httpRequest.getHeader("X-Twilio-Signature");

            isValidRequest = requestValidator.validate(
                    pathAndQueryUrl,
                    postParams,
                    signatureHeader);
        }

        if(isValidRequest) {
            log.debug("Twilio validation filter successfully validated the request");
            chain.doFilter(request, response);
        } else {
            log.warn("Invalid request to twilio interface from IP={}", request.getRemoteAddr());
            ((HttpServletResponse)response).sendError(HttpServletResponse.SC_FORBIDDEN);
        }

    }

    private Map<String, String> extractPostParams(HttpServletRequest request) {
        String queryString = request.getQueryString();
        Map<String, String[]> requestParams = request.getParameterMap();
        List<String> queryStringKeys = getQueryStringKeys(queryString);

        return requestParams.entrySet().stream()
                .filter(e -> !queryStringKeys.contains(e.getKey()))
                .collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue()[0]));
    }

    private List<String> getQueryStringKeys(String queryString) {
        if(queryString == null || queryString.length() == 0) {
            return Collections.emptyList();
        } else {
            return Arrays.stream(queryString.split("&"))
                    .map(pair -> pair.split("=")[0])
                    .collect(Collectors.toList());
        }
    }

    private String getRequestUrlAndQueryString(HttpServletRequest request) {
        String requestUrl;
        if (twilioConfig.getServerBaseUrl() == null) {
            requestUrl = request.getRequestURL().toString();
        } else {
            requestUrl = twilioConfig.getServerBaseUrl() + request.getServletPath();
        }

        String queryString = request.getQueryString();
        if(queryString != null && queryString != "") {
            return requestUrl + "?" + queryString;
        }

        return requestUrl;
    }
}

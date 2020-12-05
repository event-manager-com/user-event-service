package gregad.eventmanager.usereventservice.security;

import gregad.eventmanager.usereventservice.api.ApiConstants;
import gregad.eventmanager.usereventservice.api.ExternalApiConstants;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;


/**
 * @author Greg Adler
 */
public class JwtTokenValidatorFilter extends GenericFilterBean {
    private String securityServiceUrl;
    private int timeout;
    private RestTemplate restTemplate;

    public JwtTokenValidatorFilter(String securityServiceUrl, int timeout, RestTemplate restTemplate) {
        this.securityServiceUrl = securityServiceUrl;
        this.timeout = timeout;
        this.restTemplate = restTemplate;
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
        String token =httpServletRequest.getHeader(ApiConstants.HEADER);
        HttpHeaders headers = new HttpHeaders();
        headers.set(ApiConstants.HEADER,token);
        restTemplate.postForEntity(securityServiceUrl + ExternalApiConstants.VALIDATE, new HttpEntity<>(headers), Boolean.class);
        doFilter(servletRequest,servletResponse,filterChain);
    }
}

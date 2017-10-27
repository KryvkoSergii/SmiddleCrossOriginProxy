package ua.com.smiddle.SmiddleCrossOriginProxy.web;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.client.urlconnection.HTTPSProperties;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import javax.net.ssl.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.Enumeration;
import java.util.logging.Logger;

/**
 * @author ksa on 10/26/17.
 * @project SmiddleCrossOriginProxy
 */
@RestController
@PropertySource("classpath:application.properties")
public class Controller {
    @Value("${host.name}")
    private volatile String HOSTNAME;
    @Value("${self.name}")
    private volatile String SELFNAME;
    @Value("${host.port}")
    private volatile int PORT;
    @Value("${self.port}")
    private volatile int SELFPORT;

    private Logger logger = Logger.getGlobal();


    private WebResource service = null;
    private Client client = null;

    @PostConstruct
    private void init() {
        // Install the all-trusting trust manager
        SSLContext context = null;
        // Create a trust manager that does not validate certificate chains
        TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
            public X509Certificate[] getAcceptedIssuers() {
                return null;
            }

            public void checkClientTrusted(X509Certificate[] certs, String authType) {
            }

            public void checkServerTrusted(X509Certificate[] certs, String authType) {
            }
        }};

        try {
            context = SSLContext.getInstance("SSL");
            context.init(null, trustAllCerts, null);
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            e.printStackTrace();
        }

        // turn the hostname verification off.
        HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
            public boolean verify(String string, SSLSession ssls) {
                return true;
            }
        });

        ClientConfig config = new DefaultClientConfig();
        config.getProperties().put(HTTPSProperties.PROPERTY_HTTPS_PROPERTIES, new HTTPSProperties(null, context));
        client = Client.create(config);
        service = client.resource("https://" + HOSTNAME + ":" + PORT);
        logger.info("initialized");
    }

    @RequestMapping(method = RequestMethod.GET)
    @CrossOrigin
    public void get(HttpServletRequest request, HttpServletResponse response) throws Exception {
        logger.fine("get " + service.getURI() + request.getServletPath());
        ClientResponse cr = addHeaders(service, request).get(ClientResponse.class);
        setResponse(response, cr);
    }

    @RequestMapping(method = RequestMethod.POST)
    @CrossOrigin
    public void post(@RequestBody String body, HttpServletRequest request, HttpServletResponse response) throws Exception {
        logger.fine("post " + service.getURI() + request.getServletPath());
        ClientResponse cr = addHeaders(service, request).post(ClientResponse.class, body);
        setResponse(response, cr);
    }

    @RequestMapping(method = RequestMethod.PUT)
    @CrossOrigin
    public void put(@RequestBody String body, HttpServletRequest request, HttpServletResponse response) throws Exception {
        logger.fine("put " + service.getURI() + request.getServletPath());
        ClientResponse cr = addHeaders(service, request).put(ClientResponse.class, body);
        setResponse(response, cr);
    }

    @RequestMapping(method = RequestMethod.DELETE)
    @CrossOrigin
    public void delete(@RequestBody String body, HttpServletRequest request, HttpServletResponse response) throws Exception {
        logger.fine("delete " + service.getURI() + request.getServletPath());
        ClientResponse cr = addHeaders(service, request).delete(ClientResponse.class, body);
        setResponse(response, cr);
    }

    private WebResource.Builder addHeaders(WebResource resource, HttpServletRequest request) {
        resource = resource.path(request.getServletPath());
        String s;
        Enumeration<String> en = request.getHeaderNames();
        while (en.hasMoreElements()) {
            s = en.nextElement();
            if (s.equalsIgnoreCase("Authorization"))
                return resource.header("Authorization", request.getHeader(s));
        }
        throw new IllegalStateException("no auth found");
    }

    private void setResponse(HttpServletResponse response, ClientResponse cr) throws IOException {
        response.setStatus(cr.getStatus());
        IOUtils.copy(cr.getEntityInputStream(), response.getOutputStream());
        cr.getHeaders()
                .forEach((e, k) -> response.addHeader(e, k.contains(HOSTNAME) ? k.get(0).replace(HOSTNAME, SELFNAME) : k.get(0)));
    }

}

package com.mikkezavala.sat.util;

import com.mikkezavala.sat.domain.sat.SoapEndpoint;
import com.mikkezavala.sat.domain.sat.auth.AuthResponse;
import com.mikkezavala.sat.domain.sat.auth.Timestamp;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.ZonedDateTime;
import java.util.Iterator;
import java.util.Objects;
import java.util.UUID;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.Node;
import javax.xml.soap.SOAPConnection;
import javax.xml.soap.SOAPConnectionFactory;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

public class SoapUtil {

    public final static String ENV_PREFIX = "s";

    private static final String SEPARATOR = File.separator;

    private static final String TOKEN_FORMAT = "WRAP access_token=\"%s\"";
    private static final Logger LOGGER = LoggerFactory.getLogger(SoapUtil.class);

    public static <T> T callWebService(SOAPMessage message, Class<T> clazz, SoapEndpoint endpoint,
        String authHeader)
        throws Exception {

        MimeHeaders headers = new MimeHeaders();
        String uuid = UUID.randomUUID().toString();

        String filePrefix = getFilePrefix(clazz);
        File soapRequest = saveFile(message, uuid, filePrefix + "-" + "request");

        String url;
        String action;

        if (authHeader != null) {
            headers.addHeader("Authorization", String.format(TOKEN_FORMAT, authHeader));
        }

        url = endpoint.getEndpoint();
        action = endpoint.getAction();

        headers.addHeader("SOAPAction", action);
        FileInputStream fos = new FileInputStream(soapRequest);
        SOAPMessage msg = MessageFactory.newInstance().createMessage(headers, fos);

        SOAPConnectionFactory soapConnectionFactory = SOAPConnectionFactory.newInstance();
        SOAPConnection soapConnection = soapConnectionFactory.createConnection();

        LOGGER.info("Calling Action: {} in url: {} for: {}", action, url, endpoint.name());

        SOAPMessage resp = soapConnection.call(msg, url);
        saveFile(resp, uuid, filePrefix + "-" + "response");

        JAXBContext context = JAXBContext.newInstance(clazz);
        Document doc = resp.getSOAPBody().extractContentAsDocument();
        JAXBElement<T> body = context.createUnmarshaller().unmarshal(doc, clazz);

        T returnValue = body.getValue();
        if (endpoint.equals(SoapEndpoint.AUTENTICA) && returnValue instanceof AuthResponse) {
            ((AuthResponse) returnValue).setTimestamp(createTimestamp(resp.getSOAPHeader()));
        }

        return returnValue;
    }

    public static SOAPElement getFirstNode(SOAPElement body) {
        for (Iterator<?> iterator = body.getChildElements(); iterator.hasNext(); ) {
            Object child = iterator.next();
            if (child instanceof SOAPElement) {
                return (SOAPElement) child;
            }
        }
        return null;
    }

    private static File saveFile(SOAPMessage data, String id, String fileName)
        throws IOException {
        Path dir = Paths.get(".", SEPARATOR, "soap", SEPARATOR, id);

        File file = dir.resolve(fileName + ".xml").toFile();
        FileUtils.forceMkdir(dir.toFile());

        try (FileOutputStream fos = new FileOutputStream(file)) {
            data.writeTo(fos);

        } catch (SOAPException | IOException e) {
            LOGGER.error("Whoops", e);
        }

        return file;
    }

    private static <T> String getFilePrefix(Class<T> clazz) {
        String[] parts = clazz.getName().split("\\.");
        String group = parts.length >= 3 ? parts[parts.length - 3] : "unknown";

        return (group + "-" + parts[parts.length - 2]).toLowerCase();
    }

    private static Timestamp createTimestamp(SOAPElement header) {
        String created = null;
        String expires = null;

        SOAPElement authHeader = getFirstNode(
            Objects.requireNonNull(
                getFirstNode(header)
            )
        );

        if (Objects.nonNull(authHeader)) {
            Iterator<Node> elements = authHeader.getChildElements();
            while (elements.hasNext()) {
                Node el = elements.next();
                if (el instanceof SOAPElement) {
                    SOAPElement soapElement = (SOAPElement) el;
                    String name = soapElement.getElementName().getLocalName();
                    if (name.equals("Created")) {
                        created = el.getTextContent();
                    } else if (name.equals("Expires")) {
                        expires = el.getTextContent();
                    }
                }
            }
        }

        if (Objects.nonNull(created) && Objects.nonNull(expires)) {
            Timestamp ts = new Timestamp();
            ts.setCreated(ZonedDateTime.parse(created));
            ts.setExpires(ZonedDateTime.parse(expires));

            return ts;
        }

        return null;
    }

}

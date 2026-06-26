package org.researchedc.module.openrosa.service;

import java.io.StringReader;
import java.io.StringWriter;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.researchedc.module.openrosa.dto.FormListResponse;
import org.researchedc.module.openrosa.dto.ManifestResponse;
import org.researchedc.module.openrosa.dto.SubmissionResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

@Service
public class OpenRosaXmlService {

    private static final Logger log = LoggerFactory.getLogger(OpenRosaXmlService.class);

    private static final DocumentBuilderFactory DBF = DocumentBuilderFactory.newInstance();
    private static final TransformerFactory TF = TransformerFactory.newInstance();

    static {
        DBF.setNamespaceAware(true);
        DBF.setIgnoringComments(true);
    }

    public Map<String, String> parseSubmissionXml(String xml) {
        Map<String, String> values = new LinkedHashMap<>();
        try {
            Document doc = DBF.newDocumentBuilder()
                    .parse(new InputSource(new StringReader(xml)));
            collectLeafElements(doc.getDocumentElement(), "", values);
        } catch (Exception e) {
            log.warn("Failed to parse submission XML: {}", e.getMessage());
        }
        return values;
    }

    private void collectLeafElements(Element element, String pathPrefix, Map<String, String> values) {
        NodeList children = element.getChildNodes();
        boolean hasElementChild = false;
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (child.getNodeType() == Node.ELEMENT_NODE) {
                hasElementChild = true;
                String name = child.getLocalName();
                if (name == null) name = child.getNodeName();
                String newPath = pathPrefix.isEmpty() ? name : pathPrefix + "/" + name;
                collectLeafElements((Element) child, newPath, values);
            }
        }
        if (!hasElementChild) {
            String text = element.getTextContent();
            if (text != null && !text.trim().isEmpty()) {
                values.put(pathPrefix, text.trim());
            }
        }
    }

    public String buildFormListXml(FormListResponse response) {
        try {
            Document doc = DBF.newDocumentBuilder().newDocument();
            Element root = doc.createElementNS("http://openrosa.org/xforms/xformsList", "xforms");
            doc.appendChild(root);

            for (FormListResponse.XFormEntry entry : response.getXforms()) {
                Element xform = doc.createElementNS(null, "xform");
                root.appendChild(xform);

                appendElement(doc, xform, "formID", entry.getFormID());
                appendElement(doc, xform, "name", entry.getName());
                if (entry.getMajorMinorVersion() != null) {
                    appendElement(doc, xform, "majorMinorVersion", entry.getMajorMinorVersion());
                }
                if (entry.getVersion() != null) {
                    appendElement(doc, xform, "version", entry.getVersion());
                }
                if (entry.getHash() != null) {
                    appendElement(doc, xform, "hash", entry.getHash());
                }
                appendElement(doc, xform, "downloadUrl", entry.getDownloadUrl());
                if (entry.getManifestUrl() != null) {
                    appendElement(doc, xform, "manifestUrl", entry.getManifestUrl());
                }
            }

            return serialize(doc);
        } catch (Exception e) {
            log.error("Failed to build formList XML", e);
            return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                    + "<xforms xmlns=\"http://openrosa.org/xforms/xformsList\"/>";
        }
    }

    public String buildManifestXml(ManifestResponse response) {
        try {
            Document doc = DBF.newDocumentBuilder().newDocument();
            Element root = doc.createElementNS("http://openrosa.org/xforms/xformsManifest", "manifest");
            doc.appendChild(root);

            for (ManifestResponse.MediaFileEntry mf : response.getMediaFiles()) {
                Element mediaFile = doc.createElementNS(null, "mediaFile");
                root.appendChild(mediaFile);

                appendElement(doc, mediaFile, "filename", mf.getFilename());
                if (mf.getHash() != null) {
                    appendElement(doc, mediaFile, "hash", mf.getHash());
                }
                appendElement(doc, mediaFile, "downloadUrl", mf.getDownloadUrl());
            }

            return serialize(doc);
        } catch (Exception e) {
            log.error("Failed to build manifest XML", e);
            return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                    + "<manifest xmlns=\"http://openrosa.org/xforms/xformsManifest\"/>";
        }
    }

    public String buildSubmissionResponseXml(SubmissionResponse response) {
        return response.toXml();
    }

    public static String computeMd5Hash(String content) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(content.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            return "md5:" + Base64.getEncoder().encodeToString(digest);
        } catch (NoSuchAlgorithmException e) {
            return "md5:" + Integer.toHexString(content.hashCode());
        }
    }

    public static String extractOidFromInstance(String xml) {
        Map<String, String> items = new OpenRosaXmlService().parseSubmissionXml(xml);
        for (Map.Entry<String, String> e : items.entrySet()) {
            if (e.getKey().endsWith("/meta/instanceID")) {
                String instanceId = e.getValue();
                int colonIdx = instanceId.lastIndexOf(':');
                if (colonIdx > 0) {
                    return instanceId.substring(0, colonIdx);
                }
                return instanceId;
            }
        }
        return null;
    }

    private void appendElement(Document doc, Element parent, String name, String value) {
        if (value == null) return;
        Element el = doc.createElementNS(null, name);
        el.setTextContent(value);
        parent.appendChild(el);
    }

    private String serialize(Document doc) throws Exception {
        var transformer = TF.newTransformer();
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
        StringWriter sw = new StringWriter();
        transformer.transform(new DOMSource(doc), new StreamResult(sw));
        return sw.toString();
    }
}

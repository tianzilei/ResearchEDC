package org.researchedc.module.openrosa.service;

import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Optional;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.researchedc.module.crf.entity.CrfEntity;
import org.researchedc.module.crf.entity.CrfVersionEntity;
import org.researchedc.module.crf.repository.CrfRepository;
import org.researchedc.module.crf.repository.CrfVersionRepository;
import org.researchedc.module.openrosa.internal.adapter.OpenRosaCrfAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

@Service
public class OpenRosaFormService {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final OpenRosaCrfAdapter adapter;
    private final CrfVersionRepository crfVersionRepository;
    private final CrfRepository crfRepository;

    public OpenRosaFormService(OpenRosaCrfAdapter adapter,
                               CrfVersionRepository crfVersionRepository,
                               CrfRepository crfRepository) {
        this.adapter = adapter;
        this.crfVersionRepository = crfVersionRepository;
        this.crfRepository = crfRepository;
    }

    public String buildFormListXml(String studyOid) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.newDocument();

            Element xforms = doc.createElementNS("http://openrosa.org/xforms/xformsList", "xforms");
            doc.appendChild(xforms);

            List<CrfVersionEntity> versions = adapter.findActiveCrfVersions();
            for (CrfVersionEntity cv : versions) {
                Optional<CrfEntity> crfOpt = crfRepository.findById(cv.getCrfId());
                if (crfOpt.isEmpty()) continue;

                CrfEntity crf = crfOpt.get();
                Element xform = doc.createElement("xform");

                Element formId = doc.createElement("formID");
                formId.setTextContent(cv.getOcOid());
                xform.appendChild(formId);

                Element name = doc.createElement("name");
                name.setTextContent(crf.getName());
                xform.appendChild(name);

                Element version = doc.createElement("version");
                version.setTextContent(cv.getName());
                xform.appendChild(version);

                Element hash = doc.createElement("hash");
                hash.setTextContent("md5:" + md5(cv.getXform() != null ? cv.getXform() : cv.getOcOid()));
                xform.appendChild(hash);

                String baseUrl = ""; // use relative paths
                Element downloadUrl = doc.createElement("downloadUrl");
                downloadUrl.setTextContent(baseUrl + "/api/v1/openrosa/" + studyOid + "/xform/" + cv.getOcOid());
                xform.appendChild(downloadUrl);

                Element manifestUrl = doc.createElement("manifestUrl");
                manifestUrl.setTextContent(baseUrl + "/api/v1/openrosa/" + studyOid + "/xformsManifest/" + cv.getOcOid());
                xform.appendChild(manifestUrl);

                xforms.appendChild(xform);
            }

            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            StringWriter writer = new StringWriter();
            transformer.transform(new DOMSource(doc), new StreamResult(writer));
            return writer.toString().replaceFirst("<\\?xml[^?]*\\?>\\s*", "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        } catch (Exception e) {
            logger.error("Failed to build formList XML", e);
            return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<xforms xmlns=\"http://openrosa.org/xforms/xformsList\"/>";
        }
    }

    public String buildXFormXml(String formId) {
        Optional<CrfVersionEntity> cvOpt = adapter.findCrfVersionByOcOid(formId);
        if (cvOpt.isEmpty()) return null;

        CrfVersionEntity cv = cvOpt.get();
        if (cv.getXform() != null && !cv.getXform().isBlank()) {
            return cv.getXform();
        }

        // Generate minimal XForm from CRF metadata
        return generateBasicXForm(cv);
    }

    public String buildManifestXml(String formId) {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
               "<manifest xmlns=\"http://openrosa.org/xforms/xformsManifest\"/>";
    }

    private String generateBasicXForm(CrfVersionEntity cv) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.newDocument();

            Element html = doc.createElementNS("http://www.w3.org/1999/xhtml", "html");
            html.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:h", "http://www.w3.org/1999/xhtml");
            html.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:jr", "http://openrosa.org/javarosa");
            html.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:xsd", "http://www.w3.org/2001/XMLSchema");
            html.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns", "http://www.w3.org/2002/xforms");
            doc.appendChild(html);

            Element head = doc.createElementNS("http://www.w3.org/1999/xhtml", "head");
            html.appendChild(head);

            Element title = doc.createElementNS("http://www.w3.org/1999/xhtml", "title");
            title.setTextContent(cv.getOcOid());
            head.appendChild(title);

            Element model = doc.createElementNS("http://www.w3.org/2002/xforms", "model");
            head.appendChild(model);

            Element instance = doc.createElementNS("http://www.w3.org/2002/xforms", "instance");
            model.appendChild(instance);

            Element root = doc.createElement(cv.getOcOid());
            root.setAttribute("id", cv.getOcOid());
            instance.appendChild(root);

            Element meta = doc.createElement("meta");
            Element instanceId = doc.createElement("instanceID");
            meta.appendChild(instanceId);
            root.appendChild(meta);

            // Add bind for instanceID
            Element bind = doc.createElement("bind");
            bind.setAttribute("nodeset", "/" + cv.getOcOid() + "/meta/instanceID");
            bind.setAttribute("readonly", "true()");
            bind.setAttribute("type", "string");
            bind.setAttribute("calculate", "concat('uuid:', uuid())");
            model.appendChild(bind);

            Element body = doc.createElementNS("http://www.w3.org/1999/xhtml", "body");
            html.appendChild(body);

            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            StringWriter writer = new StringWriter();
            transformer.transform(new DOMSource(doc), new StreamResult(writer));
            return writer.toString();
        } catch (Exception e) {
            logger.error("Failed to generate XForm for {}", cv.getOcOid(), e);
            return null;
        }
    }

    private String md5(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            return "";
        }
    }
}

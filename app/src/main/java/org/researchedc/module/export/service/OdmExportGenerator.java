package org.researchedc.module.export.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.researchedc.module.export.enums.OdmContractVersion;
import org.researchedc.module.export.internal.OdmFormDataSnapshot;
import org.researchedc.module.export.internal.OdmItemDataSnapshot;
import org.researchedc.module.export.internal.OdmItemGroupDataSnapshot;
import org.researchedc.module.export.internal.OdmStudyEventDataSnapshot;
import org.researchedc.module.export.internal.OdmStudySnapshot;
import org.researchedc.module.export.internal.OdmSubjectDataSnapshot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.StringWriter;

@Component
public class OdmExportGenerator {

    private static final Logger log = LoggerFactory.getLogger(OdmExportGenerator.class);
    private static final String ODM_NS = "http://www.cdisc.org/ns/odm/v1.3";
    private static final String OC_NS = "http://www.openclinica.org/ns/odm_ext_v130/v3.1";
    private static final String XSI_NS = "http://www.w3.org/2001/XMLSchema-instance";
    private static final DateTimeFormatter ODM_DT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    private final OdmSchemaResourceResolver schemaResolver;

    public OdmExportGenerator(OdmSchemaResourceResolver schemaResolver) {
        this.schemaResolver = schemaResolver;
    }

    public String generate(OdmStudySnapshot studySnapshot, List<OdmSubjectDataSnapshot> subjects,
                           OdmContractVersion contractVersion, String fileOid) {
        try {
            Document doc = DocumentBuilderFactory.newInstance()
                    .newDocumentBuilder()
                    .newDocument();

            Element root = doc.createElementNS(ODM_NS, "ODM");
            root.setAttribute("FileType", "Snapshot");
            root.setAttribute("FileOID", fileOid);
            root.setAttribute("CreationDateTime", LocalDateTime.now().format(ODM_DT));
            root.setAttribute("ODMVersion", "1.3.2");
            root.setAttributeNS(XSI_NS, "xsi:schemaLocation", resolveSchemaLocation(contractVersion));
            root.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:oc", OC_NS);
            root.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:xsi", XSI_NS);
            doc.appendChild(root);

            Element studyElement = buildStudyElement(doc, studySnapshot, contractVersion);
            root.appendChild(studyElement);

            Element clinicalData = doc.createElementNS(ODM_NS, "ClinicalData");
            clinicalData.setAttribute("StudyOID", studySnapshot.oid());
            clinicalData.setAttribute("MetaDataVersionOID", studySnapshot.metaDataVersionOid());
            root.appendChild(clinicalData);

            for (OdmSubjectDataSnapshot subject : subjects) {
                clinicalData.appendChild(buildSubjectDataElement(doc, subject));
            }

            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(javax.xml.transform.OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
            StringWriter writer = new StringWriter();
            transformer.transform(new DOMSource(doc), new StreamResult(writer));

            String xml = writer.toString();
            log.debug("Generated ODM XML: {} bytes, {} subjects", xml.length(), subjects.size());
            return xml;

        } catch (ParserConfigurationException | TransformerException e) {
            throw new RuntimeException("ODM XML generation failed: " + e.getMessage(), e);
        }
    }

    private Element buildStudyElement(Document doc, OdmStudySnapshot studySnapshot, OdmContractVersion contractVersion) {
        Element studyEl = doc.createElementNS(ODM_NS, "Study");
        studyEl.setAttribute("OID", studySnapshot.oid());

        Element studyName = doc.createElementNS(ODM_NS, "StudyName");
        studyName.setTextContent(studySnapshot.name());
        studyEl.appendChild(studyName);

        Element studyDescription = doc.createElementNS(ODM_NS, "StudyDescription");
        studyDescription.setTextContent(studySnapshot.description() != null ? studySnapshot.description() : "");
        studyEl.appendChild(studyDescription);

        Element protocol = doc.createElementNS(ODM_NS, "Protocol");
        Element protocolName = doc.createElementNS(ODM_NS, "ProtocolName");
        protocolName.setTextContent(studySnapshot.protocolName() != null ? studySnapshot.protocolName() : studySnapshot.name());
        protocol.appendChild(protocolName);
        studyEl.appendChild(protocol);

        Element metaVersion = doc.createElementNS(ODM_NS, "MetaDataVersion");
        metaVersion.setAttribute("OID", studySnapshot.metaDataVersionOid());
        metaVersion.setAttribute("Name", studySnapshot.metaDataVersionName() != null ? studySnapshot.metaDataVersionName() : "");
        studyEl.appendChild(metaVersion);

        if (contractVersion == OdmContractVersion.OC2_0_COMPAT) {
            Element facility = doc.createElementNS(OC_NS, "StudyDetails");
            Element facilityName = doc.createElementNS(OC_NS, "FacilityName");
            facilityName.setTextContent(studySnapshot.facilityName() != null ? studySnapshot.facilityName() : "");
            facility.appendChild(facilityName);
            Element facilityContact = doc.createElementNS(OC_NS, "FacilityContactEmail");
            facilityContact.setTextContent(studySnapshot.facilityContactEmail() != null ? studySnapshot.facilityContactEmail() : "");
            facility.appendChild(facilityContact);
            metaVersion.appendChild(facility);
        }

        return studyEl;
    }

    private Element buildSubjectDataElement(Document doc, OdmSubjectDataSnapshot subject) {
        Element subjectData = doc.createElementNS(ODM_NS, "SubjectData");
        subjectData.setAttribute("SubjectKey", subject.subjectKey());
        subjectData.setAttribute("UniqueID", subject.uniqueId() != null ? subject.uniqueId() : "");

        for (OdmStudyEventDataSnapshot eventData : subject.studyEvents()) {
            Element studyEventData = doc.createElementNS(ODM_NS, "StudyEventData");
            studyEventData.setAttribute("StudyEventOID", eventData.studyEventOid());
            if (eventData.eventRepeatKey() != null) {
                studyEventData.setAttribute("EventRepeatKey", eventData.eventRepeatKey());
            }
            subjectData.appendChild(studyEventData);

            for (OdmFormDataSnapshot formData : eventData.forms()) {
                Element form = doc.createElementNS(ODM_NS, "FormData");
                form.setAttribute("FormOID", formData.formOid());
                if (formData.formRepeatKey() != null) {
                    form.setAttribute("FormRepeatKey", formData.formRepeatKey());
                }
                studyEventData.appendChild(form);

                for (OdmItemGroupDataSnapshot itemGroupData : formData.itemGroups()) {
                    Element itemGroup = doc.createElementNS(ODM_NS, "ItemGroupData");
                    itemGroup.setAttribute("ItemGroupOID", itemGroupData.itemGroupOid());
                    if (itemGroupData.itemGroupRepeatKey() != null) {
                        itemGroup.setAttribute("ItemGroupRepeatKey", itemGroupData.itemGroupRepeatKey());
                    }
                    form.appendChild(itemGroup);

                    for (OdmItemDataSnapshot itemData : itemGroupData.items()) {
                        Element item = doc.createElementNS(ODM_NS, "ItemData");
                        item.setAttribute("ItemOID", itemData.itemOid());
                        item.setAttribute("Value", itemData.value() != null ? itemData.value() : "");
                        if (itemData.isMonitored()) {
                            item.setAttributeNS(OC_NS, "oc:Monitored", "Yes");
                        }
                        itemGroup.appendChild(item);
                    }
                }
            }
        }

        return subjectData;
    }

    private String resolveSchemaLocation(OdmContractVersion version) {
        var paths = schemaResolver.resolve(version);
        return ODM_NS + " " + paths.mainXsd() + " " + OC_NS + " " + paths.toOdmXsd();
    }
}

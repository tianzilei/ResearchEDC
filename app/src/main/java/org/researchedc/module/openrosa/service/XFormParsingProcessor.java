package org.researchedc.module.openrosa.service;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.researchedc.module.crf.entity.ItemEntity;
import org.researchedc.module.crf.repository.ItemRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class XFormParsingProcessor implements Processor {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final ItemRepository itemRepository;

    public XFormParsingProcessor(ItemRepository itemRepository) {
        this.itemRepository = itemRepository;
    }

    @Override
    public void process(SubmissionContext ctx) throws Exception {
        String xml = ctx.getRequestBody();
        if (xml == null || xml.isBlank()) {
            ctx.addError("Empty submission body");
            return;
        }

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(false);
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new InputSource(new StringReader(xml)));

            doc.getDocumentElement().normalize();

            String formId = doc.getDocumentElement().getAttribute("id");
            if (formId != null && !formId.isEmpty()) {
                ctx.getSubjectContextMap().putIfAbsent("crfVersionOid", formId);
            }

            List<ItemValue> items = extractItems(doc.getDocumentElement());
            ctx.setItems(items);
            logger.debug("Parsed {} items from submission XML", items.size());
        } catch (Exception e) {
            logger.error("Failed to parse submission XML", e);
            ctx.addError("XML parsing failed: " + e.getMessage());
        }
    }

    private List<ItemValue> extractItems(Element root) {
        List<ItemValue> items = new ArrayList<>();
        NodeList childNodes = root.getChildNodes();

        for (int i = 0; i < childNodes.getLength(); i++) {
            Node node = childNodes.item(i);
            extractNodeItems(node, items, 0);
        }
        return items;
    }

    private void extractNodeItems(Node node, List<ItemValue> items, int depth) {
        if (node == null) return;

        if (node.getNodeType() == Node.ELEMENT_NODE) {
            String nodeName = node.getNodeName();

            if (!"meta".equals(nodeName) && !"instanceID".equals(nodeName)) {
                boolean hasElementChildren = hasElementChild(node);
                String text = extractTextContent(node);

                if (!hasElementChildren && text != null && !text.trim().isEmpty()) {
                    String itemOid = nodeName;
                    Integer itemDataTypeId = resolveItemDataType(itemOid);
                    items.add(new ItemValue(itemOid, text.trim(), itemDataTypeId, null));
                }

                if (hasElementChildren && depth < 10) {
                    NodeList children = node.getChildNodes();
                    for (int j = 0; j < children.getLength(); j++) {
                        extractNodeItems(children.item(j), items, depth + 1);
                    }
                }
            }
        }
    }

    private boolean hasElementChild(Node node) {
        NodeList children = node.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            if (children.item(i).getNodeType() == Node.ELEMENT_NODE) {
                return true;
            }
        }
        return false;
    }

    private String extractTextContent(Node node) {
        NodeList children = node.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            if (children.item(i).getNodeType() == Node.TEXT_NODE) {
                return children.item(i).getNodeValue();
            }
        }
        return null;
    }

    private Integer resolveItemDataType(String itemOid) {
        List<ItemEntity> items = itemRepository.findByOcOid(itemOid);
        if (!items.isEmpty()) {
            return items.get(0).getItemDataTypeId();
        }
        return null;
    }
}

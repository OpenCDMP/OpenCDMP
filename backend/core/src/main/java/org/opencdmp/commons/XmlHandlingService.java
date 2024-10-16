package org.opencdmp.commons;

import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.management.InvalidApplicationException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
public class XmlHandlingService {

    public String generateXml(Document doc) throws TransformerException {
        TransformerFactory tFact = TransformerFactory.newInstance();
        Transformer trans = tFact.newTransformer();
        StringWriter writer = new StringWriter();
        StreamResult result = new StreamResult(writer);
        DOMSource source = new DOMSource(doc);
        trans.setOutputProperty(OutputKeys.INDENT, "yes");
        trans.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
        trans.transform(source, result);
        return writer.toString();
    }
    
    public String toXml(Object item) throws JAXBException {
       
        
        JAXBContext context = JAXBContext.newInstance(item.getClass());
        Marshaller marshaller = context.createMarshaller();
        StringWriter out = new StringWriter();
        marshaller.marshal(item, out);
        return out.toString();
    }

    public String toXmlSafe(Object item) {
        if (item == null) return null;
        try {
            return this.toXml(item);
        } catch (Exception ex) {
            return null;
        }
    }

    public <T> T fromXml(Class<T> type, String xmlString) throws JAXBException, InstantiationException, IllegalAccessException, ParserConfigurationException, IOException, SAXException {
        JAXBContext jaxbContext = JAXBContext.newInstance(type);
        Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();

        return (T) jaxbUnmarshaller.unmarshal(new StringReader(xmlString));
    }

    public <T> T fromXmlSafe(Class<T> type, String xmlString) {
        if (xmlString == null) return null;
        try {
            return this.fromXml(type, xmlString);
        } catch (Exception ex) {
            return null;
        }
    }

//    public <T extends XmlSerializable<T>> T xmlSerializableFromXml(Class<T> type, String xmlString) throws JAXBException, InstantiationException, IllegalAccessException, ParserConfigurationException, IOException, SAXException {
//        T object = type.newInstance();
//        return (T) object.fromXml(this.getDocument(xmlString).getDocumentElement());
//    }
//    
//    public <T extends XmlSerializable<T>> T xmlSerializableFromXmlSafe(Class<T> type, String xmlString) {
//        if (xmlString == null) return null;
//        try {
//            return this.xmlSerializableFromXml(type, xmlString);
//        } catch (Exception ex) {
//            return null;
//        }
//    }

    public Document getDocument(String xml) throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
        InputSource inputStream = new InputSource(new StringReader(xml));
        return docBuilder.parse(inputStream);
    }

    public Document getDocument() throws ParserConfigurationException {
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
        return docBuilder.newDocument();
    }
}

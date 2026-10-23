package org.opencdmp.migration;

import gr.cite.tools.logging.LoggerService;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;
import org.opencdmp.commons.XmlHandlingService;
import org.opencdmp.service.tenant.TenantProperties;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Base64;
import java.util.UUID;

// Only the <authUrlBody> elements are touched, so the rest of each definition is written back as it was.
@Component
public class V2_0_0_011__Encrypt_external_fetcher_auth_request_body extends BaseJavaMigration {

    private static final LoggerService logger = new LoggerService(LoggerFactory.getLogger(V2_0_0_011__Encrypt_external_fetcher_auth_request_body.class));

    private final XmlHandlingService xmlHandlingService;
    private final TenantProperties tenantProperties;

    public V2_0_0_011__Encrypt_external_fetcher_auth_request_body(XmlHandlingService xmlHandlingService, TenantProperties tenantProperties) {
        this.xmlHandlingService = xmlHandlingService;
        this.tenantProperties = tenantProperties;
    }

    @Override
    public void migrate(Context context) throws Exception {
        Connection connection = context.getConnection();
        this.encrypt(connection, "ReferenceType", "?");
        this.encrypt(connection, "PrefillingSource", "?::xml");
    }

    private void encrypt(Connection connection, String table, String definitionParameter) throws Exception {
        int total = 0, updated = 0;
        try (Statement select = connection.createStatement();
             ResultSet rows = select.executeQuery("SELECT id, definition FROM public.\"" + table + "\"");
             PreparedStatement update = connection.prepareStatement("UPDATE public.\"" + table + "\" SET definition = " + definitionParameter + " WHERE id = ?")) {
            while (rows.next()) {
                total++;
                UUID id = rows.getObject("id", UUID.class);
                String xml = rows.getString("definition");
                if (xml == null || xml.isBlank()) continue;

                Document document;
                try {
                    document = this.xmlHandlingService.getDocument(xml);
                } catch (Exception e) {
                    throw new FlywayException("The definition of " + table + " " + id + " could not be parsed. Fix it, then restart.", e);
                }

                boolean changed = false;
                NodeList bodies = document.getElementsByTagName("authUrlBody");
                for (int i = 0; i < bodies.getLength(); i++) {
                    Node body = bodies.item(i);
                    String value = body.getTextContent();
                    if (value == null || value.isEmpty() || this.isEncrypted(value)) continue;
                    body.setTextContent(this.encryptAES(value));
                    changed = true;
                }
                if (!changed) continue;

                update.setString(1, this.toXml(document));
                update.setObject(2, id);
                update.addBatch();
                updated++;
            }
            update.executeBatch();
        }
        logger.info("{} sign-in bodies: inspected {} definitions, encrypted {}", table, total, updated);
    }

    // a value that already decrypts was saved by a version that encrypts it
    private boolean isEncrypted(String value) {
        try {
            this.cipher(Cipher.DECRYPT_MODE).doFinal(Base64.getDecoder().decode(value));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // EncryptionService's algorithm, inlined: the service pulls in AuthorizationService, which needs Keycloak before Flyway can run
    private String encryptAES(String value) throws Exception {
        return Base64.getEncoder().encodeToString(this.cipher(Cipher.ENCRYPT_MODE).doFinal(value.getBytes()));
    }

    private Cipher cipher(int mode) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(mode, new SecretKeySpec(this.tenantProperties.getConfigEncryptionAesKey().getBytes(), "AES"), new IvParameterSpec(this.tenantProperties.getConfigEncryptionAesIv().getBytes()));
        return cipher;
    }

    // XmlHandlingService.generateXml indents, which would add whitespace to every definition
    private String toXml(Document document) throws Exception {
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        StringWriter writer = new StringWriter();
        transformer.transform(new DOMSource(document), new StreamResult(writer));
        return writer.toString();
    }
}

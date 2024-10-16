package org.opencdmp.service.storage;

import org.opencdmp.commons.enums.StorageType;
import org.opencdmp.commons.enums.SupportiveMaterialFieldType;
import org.opencdmp.model.StorageFile;
import org.opencdmp.model.persist.StorageFilePersist;
import gr.cite.tools.fieldset.FieldSet;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;

import javax.management.InvalidApplicationException;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.time.Instant;
import java.util.UUID;

public interface StorageFileService extends ApplicationListener<ApplicationReadyEvent> {
	StorageFile persistBytes(StorageFilePersist model, byte[] payload, FieldSet fields) throws IOException;
	StorageFile persistString(StorageFilePersist model, String payload, FieldSet fields, Charset charset) throws IOException;
	StorageFile moveToStorage(UUID fileId, StorageType storageType, boolean replaceDestination, FieldSet fields);
	StorageFile copyToStorage(UUID fileId, StorageType storageType, boolean replaceDestination, FieldSet fields);

	boolean exists(UUID fileId);

	boolean fileRefExists(String fileRef, StorageType storageType);

	void updatePurgeAt(UUID fileId, Instant purgeAt) throws InvalidApplicationException;
	boolean purgeSafe(UUID fileId);
	String readAsTextSafe(UUID fileId, Charset charset);
	byte[] readAsBytesSafe(UUID fileId);
	String readByFileRefAsTextSafe(String fileRef, StorageType storageType, Charset charset);
	byte[] readByFileRefAsBytesSafe(String fileRef, StorageType storageType);

	byte[] getSemanticsFile();

	byte[] getSupportiveMaterial(SupportiveMaterialFieldType type, String language);

	byte[] getCookiePolicy(String language);

	void setCookiePolicy(String language, byte[] payload);

	byte[] getUserGuide(String language);

	void setUserGuide(String language, byte[] payload);

	byte[] getAbout(String language);

	void setAbout(String language, byte[] payload);

	byte[] getTermsOfService(String language);

	void setTermsOfService(String language, byte[] payload);

	byte[] getGlossary(String language);

	void setGlossary(String language, byte[] payload);

	byte[] getLanguage(String language);

	File getLanguageFileName(String language);

	void setLanguage(String language, byte[] payload);

	byte[] getFaq(String language);

	void setFaq(String language, byte[] payload);
}

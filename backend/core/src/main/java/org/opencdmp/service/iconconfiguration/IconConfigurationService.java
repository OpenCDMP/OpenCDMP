package org.opencdmp.service.iconconfiguration;

import org.opencdmp.commons.types.iconconfiguration.IconConfigurationEntity;
import org.opencdmp.commons.types.iconconfiguration.importexport.IconConfigurationImportExport;
import org.opencdmp.model.iconconfiguration.IconConfiguration;
import org.opencdmp.model.persist.iconconfiguration.IconConfigurationPersist;

import javax.management.InvalidApplicationException;
import java.io.IOException;
import java.util.UUID;

public interface IconConfigurationService {

    IconConfigurationEntity buildIconConfigurationEntity(IconConfigurationPersist persist, UUID oldStorageFileId) throws InvalidApplicationException;

    void reassignIconConfiguration(IconConfiguration model);

    void reassignNewStorageFilesIfEqualsWithOld(IconConfigurationPersist persist);

    IconConfigurationPersist xmlIconConfigurationToPersist(IconConfigurationImportExport importXml) throws IOException;

    IconConfigurationImportExport iconConfigurationXmlToExport(IconConfigurationEntity entity) ;
}

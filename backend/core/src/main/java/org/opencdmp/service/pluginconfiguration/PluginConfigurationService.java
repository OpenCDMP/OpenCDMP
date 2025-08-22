package org.opencdmp.service.pluginconfiguration;

import org.opencdmp.commons.types.pluginconfiguration.PluginConfigurationEntity;
import org.opencdmp.commons.types.pluginconfiguration.PluginConfigurationUserEntity;
import org.opencdmp.commons.types.pluginconfiguration.importexport.PluginConfigurationImportExport;
import org.opencdmp.model.persist.pluginconfiguration.PluginConfigurationPersist;
import org.opencdmp.model.persist.pluginconfiguration.PluginConfigurationUserPersist;
import org.opencdmp.model.pluginconfiguration.PluginConfiguration;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.management.InvalidApplicationException;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

public interface PluginConfigurationService {

    PluginConfigurationEntity buildPluginConfigurationEntity(PluginConfigurationPersist persist, List<PluginConfigurationEntity> oldPlugins) throws InvalidApplicationException;

    void reassignPluginConfiguration(PluginConfiguration model);

    void reassignNewStorageFilesIfEqualsWithOld(List<PluginConfigurationPersist> persists, List<PluginConfigurationEntity> entities);

    PluginConfigurationPersist xmlPluginConfigurationToPersist(PluginConfigurationImportExport importXml) throws IOException;

    PluginConfigurationImportExport pluginConfigurationXmlToExport(PluginConfigurationEntity entity) ;

    PluginConfigurationUserEntity buildPluginConfigurationUserEntity(PluginConfigurationUserPersist persist, List<PluginConfigurationUserEntity> oldPlugins) throws InvalidApplicationException, InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException;

}

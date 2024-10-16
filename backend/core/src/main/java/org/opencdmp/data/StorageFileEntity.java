package org.opencdmp.data;


import jakarta.persistence.*;
import org.opencdmp.commons.enums.StorageType;
import org.opencdmp.data.converters.enums.StorageTypeConverter;
import org.opencdmp.data.tenant.TenantScopedBaseEntity;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "\"StorageFile\"")
public class StorageFileEntity extends TenantScopedBaseEntity {

    @Id
    @Column(name = "id", columnDefinition = "uuid", updatable = false, nullable = false)
    private UUID id;
    public final static String _id = "id";

    @Column(name = "file_ref", length = _fileRefLen, nullable = false)
    private String fileRef;
    public final static String _fileRef = "fileRef";
    public final static int _fileRefLen = 100;

    @Column(name = "name", length = _nameLen, nullable = false)
    private String name;
    public final static String _name = "name";
    public final static int _nameLen = 250;

    @Column(name = "extension", length = _extensionLen, nullable = false)
    private String extension;
    public final static String _extension = "extension";
    public final static int _extensionLen = 10;

    @Column(name = "mime_type", length = _mimeTypeLen, nullable = false)
    private String mimeType;
    public final static String _mimeType = "mimeType";
    public final static int _mimeTypeLen = 200;

    @Column(name = "storage_type", nullable = false)
    @Convert(converter = StorageTypeConverter.class)
    private StorageType storageType;
    public final static String _storageType = "storageType";

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
    public final static String _createdAt = "createdAt";

    @Column(name = "purge_at", nullable = true)
    private Instant purgeAt;
    public final static String _purgeAt = "purgeAt";

    @Column(name = "purged_at", nullable = true)
    private Instant purgedAt;
    public final static String _purgedAt = "purgedAt";

    @Column(name = "owner", nullable = true)
    private UUID ownerId;
    public final static String _ownerId = "ownerId";

    public UUID getId() {
        return this.id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getFileRef() {
        return this.fileRef;
    }

    public void setFileRef(String fileRef) {
        this.fileRef = fileRef;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getExtension() {
        return this.extension;
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }

    public String getMimeType() {
        return this.mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public StorageType getStorageType() {
        return this.storageType;
    }

    public void setStorageType(StorageType storageType) {
        this.storageType = storageType;
    }


    public Instant getCreatedAt() {
        return this.createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getPurgeAt() {
        return this.purgeAt;
    }

    public void setPurgeAt(Instant purgeAt) {
        this.purgeAt = purgeAt;
    }

    public Instant getPurgedAt() {
        return this.purgedAt;
    }

    public void setPurgedAt(Instant purgedAt) {
        this.purgedAt = purgedAt;
    }

    public UUID getOwnerId() {
        return this.ownerId;
    }

    public void setOwnerId(UUID ownerId) {
        this.ownerId = ownerId;
    }
}

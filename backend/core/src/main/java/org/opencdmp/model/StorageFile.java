package org.opencdmp.model;

import org.opencdmp.commons.enums.StorageType;
import org.opencdmp.model.user.User;

import java.time.Instant;
import java.util.UUID;

public class StorageFile {

    private UUID id;
    public final static String _id = "id";

    private String fileRef;
    public final static String _fileRef = "fileRef";

    private String name;
    public final static String _name = "name";

    private String fullName;
    public final static String _fullName = "fullName";

    private String extension;
    public final static String _extension = "extension";

    private String mimeType;
    public final static String _mimeType = "mimeType";

    private StorageType storageType;
    public final static String _storageType = "storageType";

    private Instant createdAt;
    public final static String _createdAt = "createdAt";

    private Instant purgeAt;
    public final static String _purgeAt = "purgeAt";

    private Instant purgedAt;
    public final static String _purgedAt = "purgedAt";

    private User owner;
    public final static String _owner = "owner";

    private Boolean belongsToCurrentTenant;
    public static final String _belongsToCurrentTenant = "belongsToCurrentTenant";

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getFileRef() {
        return fileRef;
    }

    public void setFileRef(String fileRef) {
        this.fileRef = fileRef;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getExtension() {
        return extension;
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public StorageType getStorageType() {
        return storageType;
    }

    public void setStorageType(StorageType storageType) {
        this.storageType = storageType;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getPurgeAt() {
        return purgeAt;
    }

    public void setPurgeAt(Instant purgeAt) {
        this.purgeAt = purgeAt;
    }

    public Instant getPurgedAt() {
        return purgedAt;
    }

    public void setPurgedAt(Instant purgedAt) {
        this.purgedAt = purgedAt;
    }

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    public Boolean getBelongsToCurrentTenant() {
        return belongsToCurrentTenant;
    }

    public void setBelongsToCurrentTenant(Boolean belongsToCurrentTenant) {
        this.belongsToCurrentTenant = belongsToCurrentTenant;
    }
}

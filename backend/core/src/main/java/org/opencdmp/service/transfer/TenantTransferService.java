package org.opencdmp.service.transfer;

import javax.management.InvalidApplicationException;
import java.io.IOException;

public interface TenantTransferService {

    byte[] exportBundle(TransferExportRequest request) throws IOException, InvalidApplicationException;

    TransferImportReport analyzeImport(byte[] bundle) throws IOException, InvalidApplicationException;

    TransferImportReport importBundle(byte[] bundle) throws IOException, InvalidApplicationException;

    TransferImportReport analyzeCopy(TenantCopyRequest request) throws InvalidApplicationException;

    TransferImportReport copyTenant(TenantCopyRequest request) throws InvalidApplicationException;
}

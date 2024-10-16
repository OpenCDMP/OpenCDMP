package org.opencdmp.service.responseutils;

import org.springframework.http.ResponseEntity;

public interface ResponseUtilsService {
    ResponseEntity<byte[]> buildResponseFileFromText(String text, String fileName);
}

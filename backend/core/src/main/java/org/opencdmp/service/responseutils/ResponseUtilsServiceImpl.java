package org.opencdmp.service.responseutils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

@Service
public class ResponseUtilsServiceImpl implements ResponseUtilsService {

    @Autowired
    public ResponseUtilsServiceImpl() {
    }

    public ResponseEntity<byte[]> buildResponseFileFromText(String text, String fileName)  {
        byte[] content = text.getBytes(StandardCharsets.UTF_8);
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.setContentLength(content.length);
        responseHeaders.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        fileName = fileName.replace(" ", "_").replace(",", "_");
        responseHeaders.set("Content-Disposition", "attachment;filename=" + fileName);
        responseHeaders.set("Access-Control-Expose-Headers", "Content-Disposition");
        responseHeaders.get("Access-Control-Expose-Headers").add("Content-Type");
        return new ResponseEntity<>(content, responseHeaders, HttpStatus.OK);
    }
}

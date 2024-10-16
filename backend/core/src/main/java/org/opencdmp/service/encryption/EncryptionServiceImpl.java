package org.opencdmp.service.encryption;

import org.opencdmp.convention.ConventionService;
import gr.cite.commons.web.authz.service.AuthorizationService;
import gr.cite.tools.logging.LoggerService;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

@Service
public class EncryptionServiceImpl implements EncryptionService {

    private static final LoggerService logger = new LoggerService(LoggerFactory.getLogger(EncryptionServiceImpl.class));
    private final AuthorizationService authorizationService;

    private final ConventionService conventionService;
    @Autowired
    public EncryptionServiceImpl(
            AuthorizationService authorizationService,
            ConventionService conventionService) {
        this.authorizationService = authorizationService;
        this.conventionService = conventionService;
    }

    @Override
    public String encryptAES(String input, String keyString, String ivString) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException,
            BadPaddingException, IllegalBlockSizeException {

        SecretKey key = new SecretKeySpec(keyString.getBytes(),"AES");
        IvParameterSpec ivParameterSpec = new IvParameterSpec(ivString.getBytes());

        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, key, ivParameterSpec );
        byte[] cipherText = cipher.doFinal(input.getBytes());
        return Base64.getEncoder()
                .encodeToString(cipherText);
    }

    @Override
    public String decryptAES(String cipherText, String keyString, String ivString) throws NoSuchPaddingException, NoSuchAlgorithmException,
            InvalidAlgorithmParameterException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {

        SecretKey key = new SecretKeySpec(keyString.getBytes(),"AES");
        IvParameterSpec iv =  new IvParameterSpec(ivString.getBytes());

        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, key, iv);
        byte[] plainText = cipher.doFinal(Base64.getDecoder()
                .decode(cipherText));
        return new String(plainText);
    }

}


package com.example.rsa;

import java.security.*;

public class RSAKeyGenerater {
    private KeyPair keyPair;

    public RSAKeyGenerater() {
        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
            keyGen.initialize(2048);
            keyPair = keyGen.generateKeyPair();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public PublicKey getPublicKey() {
        return keyPair.getPublic();
    }

    public PrivateKey getPrivateKey() {
        return keyPair.getPrivate();
    }
    public KeyPair getKeyPair() {
        return keyPair;
    }
}


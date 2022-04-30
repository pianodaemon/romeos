package com.immortalcrab.cfdi.utils;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;

import org.apache.commons.codec.binary.Base64;

public class Certificado {
    public static void main(String[] args) {
        try {
            var cert = readFromFile("/home/userd/dev/uploads/CSD_Ecatepec_MOBO8001149UA_20180623_002721s.cer");
            System.out.println(cert);
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public static String readFromFile(String path) throws IOException {
        var file = new File(path);
        byte[] bytes = Files.readAllBytes(file.toPath());

        try {
            return new String(Base64.encodeBase64(bytes), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            return "";
        }
    }
}

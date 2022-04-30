package com.immortalcrab.cfdi.utils;

import java.io.*;
import javax.xml.transform.*;
import javax.xml.transform.stream.*;

public class CadenaOriginal {

    public static void main(String[] args) {
        try {
            String cfdiXml = readXml("/home/userd/dev/lola/DOS/cfdi/service/signer-py/5b52aef2-c0a7-4267-9f79-85aaeaddb651.xml");
            String cadenaOriginal = build(cfdiXml, "/home/userd/dev/lola/DOS/cfdi/service/signer-py/cadenaoriginal_3_3.xslt");
            System.out.println(cadenaOriginal);

            if (cadenaOriginal.equals("||...||")) {
                System.out.println("----------OK-------------");
            } else {
                System.out.println("ERROR!!!!!!!");
            }
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public static String readXml(String path) throws IOException {
        StringBuilder sb = new StringBuilder();

        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            String sCurrentLine;
            while ((sCurrentLine = br.readLine()) != null) {
                sb.append(sCurrentLine);
            }
        }
        return sb.toString();
    }

    public static String build(String cfdiXml, String xsltPath) throws Exception {
        
        InputStream is = new ByteArrayInputStream(cfdiXml.getBytes("UTF-8"));
        
        Source xmlSource = new StreamSource(is);
        Source xsltSource = new StreamSource(new File(xsltPath));
        
        StringWriter cadenaSalida = new StringWriter();
        
        Result bufferResultado = new StreamResult(cadenaSalida);
        
        TransformerFactory factoriaTrans = TransformerFactory.newInstance();
        Transformer transformador = factoriaTrans.newTransformer(xsltSource);
        
        transformador.transform(xmlSource, bufferResultado);
        
        return cadenaSalida.toString();
    }
}

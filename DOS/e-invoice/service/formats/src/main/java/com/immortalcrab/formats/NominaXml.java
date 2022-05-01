package com.immortalcrab.formats;

import com.immortalcrab.opaque.engine.CfdiRequest;
import com.immortalcrab.opaque.engine.Storage;
import mx.gob.sat.sitio_internet.cfd.catalogos.CMoneda;
import mx.gob.sat.sitio_internet.cfd.catalogos.CPais;
import mx.gob.sat.sitio_internet.cfd.catalogos.CTipoDeComprobante;
import mx.gob.sat.cfd._4.ObjectFactory;
import mx.gob.sat.cfd._4.Comprobante;
import java.nio.charset.StandardCharsets;
import java.io.StringWriter;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.JAXBException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.datatype.DatatypeConfigurationException;
import java.io.IOException;
import com.immortalcrab.opaque.error.FormatError;
import com.immortalcrab.opaque.error.StorageError;
import java.io.ByteArrayInputStream;
import java.util.HashMap;

public class NominaXml {

    private final CfdiRequest cfdiReq;
    private final Storage st;

    private NominaXml(CfdiRequest cfdiReq, Storage st) {

        this.cfdiReq = cfdiReq;
        this.st = st;
    }

    public static String render(CfdiRequest cfdiReq, Storage st) throws FormatError, StorageError {

        NominaXml ic = new NominaXml(cfdiReq, st);
        StringWriter cfdi = ic.shape();
        var results = ic.timbrarCfdi(cfdi);
        ic.save((StringWriter) results.get("cfdiTimbrado"));

        return (String) results.get("uuid");
    }

    private void save(StringWriter sw) throws FormatError, StorageError {

        StringBuffer buf = sw.getBuffer();
        byte[] in = buf.toString().getBytes(StandardCharsets.UTF_8);
        /*var ds = this.cfdiReq.getDs();

        {
            final String fileName = (String) ds.get("SERIE") + (String) ds.get("FOLIO") + ".xml";

            this.st.upload("text/xml", in.length, fileName, new ByteArrayInputStream(in));
        }*/
    }

    private StringWriter shape() throws FormatError {

        StringWriter sw = new StringWriter();

        try {
            ObjectFactory cfdiFactory = new ObjectFactory();
            Comprobante cfdi = cfdiFactory.createComprobante();
            cfdi.setVersion("4.0");
            cfdi.setTipoDeComprobante(CTipoDeComprobante.E);

            cfdi.setMoneda(CMoneda.MXN);

            // Emisor
            Comprobante.Emisor emisor = cfdiFactory.createComprobanteEmisor();
            emisor.setRfc("PCM130624FR7");
            emisor.setNombre("PUCHA COATINGS MEXICO S DE RL DE CV");
            emisor.setRegimenFiscal("PERSONA MORAL REGIMEN GENERAL DE LEY");
            cfdi.setEmisor(emisor);

            // Receptor
            Comprobante.Receptor receptor = cfdiFactory.createComprobanteReceptor();
            receptor.setRfc("SKAG700702KZ5");
            receptor.setNombre("FRANCO ESCAMILLA");
            cfdi.setReceptor(receptor);

            // Conceptos
            Comprobante.Conceptos conceptos = cfdiFactory.createComprobanteConceptos();

            cfdi.setConceptos(conceptos);

            String contextPath = "mx.gob.sat.cfd._4";
            String schemaLocation = "http://www.sat.gob.mx/cfd/4 http://www.sat.gob.mx/sitio_internet/cfd/4/cfdv40.xsd";

            // Hacer el marshalling del cfdi object
            JAXBContext context = JAXBContext.newInstance(contextPath);
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty("jaxb.schemaLocation", schemaLocation);
            marshaller.marshal(cfdi, sw);

            // Armar la cadena original del comprobante + complemento de carta porte
            String cfdiXml = sw.toString();

            sw = new StringWriter();
            marshaller.marshal(cfdi, sw);
            System.out.println(sw.toString());

        } catch (Exception ex) {
            ex.printStackTrace();
            var stackTraceElem = ex.getStackTrace()[0];
            String err = ex.toString() + " at " + stackTraceElem.toString();
            throw new FormatError("Error al generar el xml (posible omisión de un dato requerido en el input). " + err, ex);
        }

        return sw;
    }
    
    private HashMap<String, Object> timbrarCfdi(StringWriter cfdiSw) throws FormatError {
        
        return null;
        
    }
}

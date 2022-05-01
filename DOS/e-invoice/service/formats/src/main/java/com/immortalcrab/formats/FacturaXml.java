package com.immortalcrab.formats;

import mx.gob.sat.sitio_internet.cfd.catalogos.CMoneda;
import mx.gob.sat.sitio_internet.cfd.catalogos.CPais;
import mx.gob.sat.sitio_internet.cfd.catalogos.CTipoDeComprobante;
import mx.gob.sat.cfd._4.ObjectFactory;
import mx.gob.sat.cfd._4.Comprobante;

import java.io.StringWriter;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.JAXBException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.datatype.DatatypeConfigurationException;
import java.io.IOException;
import com.immortalcrab.opaque.error.FormatError;

public class FacturaXml {

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
            var conceptos = cfdiFactory.createComprobanteConceptos();

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
}

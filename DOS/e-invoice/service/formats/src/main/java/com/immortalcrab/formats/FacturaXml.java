package com.immortalcrab.formats;

import com.immortalcrab.fantastic4.CMoneda;
import com.immortalcrab.fantastic4.CPais;
import com.immortalcrab.fantastic4.CTipoDeComprobante;
import com.immortalcrab.fantastic4.Comprobante;
import com.immortalcrab.fantastic4.ObjectFactory;
import com.immortalcrab.opaque.error.FormatError;
import java.io.StringWriter;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.JAXBException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.datatype.DatatypeConfigurationException;
import java.io.IOException;

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

        } catch (Exception ex) {
            ex.printStackTrace();
            var stackTraceElem = ex.getStackTrace()[0];
            String err = ex.toString() + " at " + stackTraceElem.toString();
            throw new FormatError("Error al generar el xml (posible omisión de un dato requerido en el input). " + err, ex);
        }

        return sw;
    }
}

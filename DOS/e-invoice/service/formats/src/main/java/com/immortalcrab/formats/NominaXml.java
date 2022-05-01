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
import java.math.BigDecimal;
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

            {
                Comprobante.Conceptos.Concepto concepto = cfdiFactory.createComprobanteConceptosConcepto();
                //concepto.setClaveProdServ(c.get("DCVESERV"));
                concepto.setCantidad(new BigDecimal("1.00"));
                //concepto.setClaveUnidad(c.get("DCUME"));
                concepto.setUnidad("SERVICIO");
                concepto.setDescripcion("PAGO DE LA QUINCENA DEL 16/10/2016 AL 31/10/2016");
                concepto.setValorUnitario(new BigDecimal("7185.8200"));
                concepto.setImporte(new BigDecimal("7185.8200"));
                
                Comprobante.Conceptos.Concepto.Impuestos conceptoImpuestos = cfdiFactory.createComprobanteConceptosConceptoImpuestos();

                // Retenciones
                {
                    Comprobante.Conceptos.Concepto.Impuestos.Retenciones retenciones = cfdiFactory.createComprobanteConceptosConceptoImpuestosRetenciones();

                    {
                        Comprobante.Conceptos.Concepto.Impuestos.Retenciones.Retencion retencion = cfdiFactory.createComprobanteConceptosConceptoImpuestosRetencionesRetencion();

                        //retencion.setBase(new BigDecimal(c.get("DBASE")));
                        retencion.setImpuesto("ISR");
                        //retencion.setTipoFactor(CTipoFactor.fromValue(c.get("DIRTF")));
                        //retencion.setTasaOCuota(new BigDecimal(c.get("DIRTC")));
                        retencion.setImporte(new BigDecimal("987.62"));

                        retenciones.getRetencions().add(retencion);
                    }

                    conceptoImpuestos.setRetenciones(retenciones);
                }

                concepto.setImpuestos(conceptoImpuestos);

                conceptos.getConceptos().add(concepto);
            }

            cfdi.setConceptos(conceptos);

            // Impuestos
            Comprobante.Impuestos impuestos = cfdiFactory.createComprobanteImpuestos();

            {
                impuestos.setTotalImpuestosRetenidos(new BigDecimal("987.62"));

                var impuestosRetenciones = cfdiFactory.createComprobanteImpuestosRetenciones();
                var impuestosRetencionList = impuestosRetenciones.getRetencions();
                var impuestosRetencion = cfdiFactory.createComprobanteImpuestosRetencionesRetencion();
                impuestosRetencion.setImpuesto("000");
                impuestosRetencion.setImporte(new BigDecimal("987.62"));
                impuestosRetencionList.add(impuestosRetencion);
                impuestos.setRetenciones(impuestosRetenciones);
            }

            cfdi.setImpuestos(impuestos);

            String contextPath = "mx.gob.sat.cfd._4";
            String schemaLocation = "http://www.sat.gob.mx/cfd/4 http://www.sat.gob.mx/sitio_internet/cfd/4/cfdv40.xsd";

            // Hacer el marshalling del cfdi object
            JAXBContext context = JAXBContext.newInstance(contextPath);
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty("jaxb.schemaLocation", schemaLocation);
            marshaller.setProperty("jaxb.formatted.output", true);
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

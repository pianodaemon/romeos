package com.immortalcrab.flow.pipeline;

import com.immortalcrab.opaque.engine.CfdiRequest;
import com.immortalcrab.opaque.engine.StepDecode;
import com.immortalcrab.opaque.engine.StepPdf;
import com.immortalcrab.opaque.engine.StepXml;
import com.immortalcrab.opaque.engine.Storage;
import com.immortalcrab.opaque.error.CfdiRequestError;
import com.immortalcrab.opaque.error.DecodeError;
import com.immortalcrab.opaque.error.FormatError;
import com.immortalcrab.opaque.error.PipelineError;
import com.immortalcrab.opaque.error.StorageError;
import org.javatuples.Triplet;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Pipeline {

    private static Pipeline ic = null;

    private Storage st = null;
    private Logger LOGGER = null;

    private final Map<String, Triplet<StepDecode, StepXml, StepPdf>> scenarios = new HashMap<>();

    public Storage getStorage() {
        return st;
    }

    public Logger getLOGGER() {
        return LOGGER;
    }

    public static synchronized Pipeline getInstance() throws StorageError {

        if (ic == null) {

            //SthreeStorage sti = SthreeStorage.configure();

            ic = new Pipeline();

            ic.LOGGER = LoggerFactory.getLogger(ic.getClass());
            //ic.st = sti;

            //ic.scenarios.put("fac", new Triplet<>(FacturaRequest::render, FacturaXml::render, FacturaPdf::render));
        }

        return ic;
    }

    public synchronized Triplet<StepDecode, StepXml, StepPdf> incept(final String kind) throws PipelineError {

        Triplet<StepDecode, StepXml, StepPdf> pf = ic.scenarios.get(kind);

        if (pf != null) {
            return pf;
        }

        throw new PipelineError("cfdi " + kind + " is unsupported");
    }

    public static String issue(final String kind, InputStreamReader reader)
            throws DecodeError, CfdiRequestError, PipelineError, StorageError, FormatError {

        Triplet<StepDecode, StepXml, StepPdf> stages = Pipeline.getInstance().incept(kind);

        /* First stage of the pipeline
           It stands for decoding what has been read
           from the data origin (in this case the infamous as400) */
        StepDecode sdec = stages.getValue0();
        CfdiRequest cfdiReq = sdec.render(reader);

        //Pipeline.getInstance().getLOGGER().info(cfdiReq.getDs().toString());

        /* Second stage of the pipeline
           It stands for hand craft a valid xml at sat */
        StepXml sxml = stages.getValue1();
        String uuid = sxml.render(cfdiReq, Pipeline.getInstance().getStorage());

        /* Third stage of the pipeline
           It stands for hand craft a arbitrary
           representation of a cfdi in pdf format  */
        StepPdf spdf = stages.getValue2();
        spdf.render(cfdiReq, Pipeline.getInstance().getStorage());

        return uuid;
    }
}

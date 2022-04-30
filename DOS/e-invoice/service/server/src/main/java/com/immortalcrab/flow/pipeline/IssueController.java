package com.immortalcrab.flow.pipeline;

import com.immortalcrab.opaque.error.CfdiRequestError;
import com.immortalcrab.opaque.error.DecodeError;
import com.immortalcrab.opaque.error.FormatError;
import com.immortalcrab.opaque.error.PipelineError;
import com.immortalcrab.opaque.error.StorageError;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class IssueController {

    @RequestMapping(
            path = "/{kind}",
            method = RequestMethod.POST,
            consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    ResponseEntity<Map<String, Object>> issue(
            @PathVariable("kind") String kind,
            @RequestPart MultipartFile tokensDoc) throws IOException {

        Map<String, Object> rhm = new HashMap<>() {
            {
                put("code", 0);
                put("desc", "");
            }
        };

        InputStream is = tokensDoc.getInputStream();

        try {
            String uuid = Pipeline.issue(kind, new InputStreamReader(is));
            rhm.put("desc", uuid);

        } catch (FormatError | DecodeError | CfdiRequestError | PipelineError | StorageError ex) {

            LOGGER.error(ex.getMessage());

            rhm.put("code", ex.getErrorCode());
            rhm.put("desc", ex.getMessage());

            return new ResponseEntity<>(rhm, HttpStatus.BAD_REQUEST);
        }

        return new ResponseEntity<>(rhm, HttpStatus.CREATED);
    }

    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());
}

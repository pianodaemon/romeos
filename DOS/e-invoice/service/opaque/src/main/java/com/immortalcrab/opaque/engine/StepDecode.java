package com.immortalcrab.opaque.engine;

import com.immortalcrab.opaque.error.CfdiRequestError;
import com.immortalcrab.opaque.error.DecodeError;
import java.io.InputStreamReader;

public interface StepDecode {

    public CfdiRequest render(InputStreamReader inReader) throws CfdiRequestError, DecodeError;
}

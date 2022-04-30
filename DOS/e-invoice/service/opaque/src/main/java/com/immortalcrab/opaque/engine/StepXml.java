package com.immortalcrab.opaque.engine;

import com.immortalcrab.opaque.error.FormatError;
import com.immortalcrab.opaque.error.StorageError;

public interface StepXml {

    public String render(CfdiRequest cfdiReq, Storage st) throws FormatError, StorageError;
}
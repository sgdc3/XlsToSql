package com.github.sgdc3.xlstosql;

import java.io.IOException;

@SuppressWarnings("serial")
public class XlsParseException extends IOException {

    public XlsParseException(String message) {
        super(message);
    }
}

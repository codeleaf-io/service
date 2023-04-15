package io.codeleaf.service.engines.jaxrs;

import org.apache.camel.Exchange;
import org.apache.camel.http.common.DefaultHttpBinding;
import org.apache.camel.http.common.HttpMessage;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public final class HttpResponseAlreadyWrittenBinding extends DefaultHttpBinding {

    public HttpResponseAlreadyWrittenBinding() {
        setMapHttpMessageBody(false);
    }

    public Object parseBody(HttpMessage httpMessage) {
        return "";
    }

    protected void readBody(HttpServletRequest request, HttpMessage message) {
    }

    public void writeResponse(Exchange exchange, HttpServletResponse response) {
    }

}

package com.company.Control_unit.ClientsType;

import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.Utils;
import org.eclipse.californium.core.coap.CoAP;
import org.eclipse.californium.core.coap.Request;

import org.eclipse.californium.elements.exception.ConnectorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;


public class GETClient {
    private final static Logger logger = LoggerFactory.getLogger(POSTClient.class);
    public String ResponseString = null;

    public String getResponseString() {
        return ResponseString;
    }

    public GETClient(String URLendpoint) {


        CoapClient coapClient = new CoapClient(URLendpoint);


        Request request = new Request(CoAP.Code.GET);


        request.setConfirmable(true);

        logger.info("Request Pretty Print:\n{}", Utils.prettyPrint(request));


        CoapResponse coapResp = null;

        try {

            coapResp = coapClient.advanced(request);

            logger.info("Response Pretty Print: \n{}", Utils.prettyPrint(coapResp));

            String text = coapResp.getResponseText();
            this.ResponseString = text;
            logger.info("Payload: {}", text);
            logger.info("Message ID: " + coapResp.advanced().getMID());
            logger.info("Token: " + coapResp.advanced().getTokenString());

            logger.info(Arrays.toString(coapResp.getPayload()));

        } catch (ConnectorException | IOException e) {
            e.printStackTrace();
        }
    }

    public boolean isOn(String content) {
        return content.equals("true");

    }
}

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


public class POSTClient {
    private final static Logger logger = LoggerFactory.getLogger(POSTClient.class);

    public POSTClient(String URLendpoint) {

        CoapClient coapClient = new CoapClient(URLendpoint);

        Request request = new Request(CoAP.Code.POST);

        request.setConfirmable(false);

        logger.info("Request Pretty Print to "+URLendpoint+":\n{}", Utils.prettyPrint(request));


        CoapResponse coapResp = null;

        try {

            coapResp = coapClient.advanced(request);

            //Pretty print for the received response
            logger.info("Response Pretty Print: \n{}", Utils.prettyPrint(coapResp));

            //The "CoapResponse" message contains the response.

            String text = coapResp.getResponseText();
            logger.info("Payload: {}", text);
            logger.info("Message ID: " + coapResp.advanced().getMID());
            logger.info("Token: " + coapResp.advanced().getTokenString());
            logger.info("FROM: "+URLendpoint);

        } catch (ConnectorException | IOException e) {
            e.printStackTrace();
        }
    }
}

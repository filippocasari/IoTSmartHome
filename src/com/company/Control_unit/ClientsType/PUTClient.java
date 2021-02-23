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

public class PUTClient {

    private final static Logger logger = LoggerFactory.getLogger(PUTClient.class);


    public PUTClient(String URL, String payload) {
        CoapClient coapClient = new CoapClient(URL);
        coapClient.setURI(URL);

        Request request = new Request(CoAP.Code.PUT);

        request.setPayload(payload);

        request.setConfirmable(true);

        logger.info("Request Pretty Print:" + URL + "\n{}", Utils.prettyPrint(request));

        //Synchronously send the POST request (blocking call)
        CoapResponse coapResp = null;

        try {

            coapResp = coapClient.advanced(request);


            String text = coapResp.getResponseText();
            logger.info("Payload: {}", text);
            logger.info("Message ID: " + coapResp.advanced().getMID());
            logger.info("Token: " + coapResp.advanced().getTokenString());
            logger.info("FROM: " + URL);

        } catch (ConnectorException | IOException e) {
            e.printStackTrace();
        }
    }
}
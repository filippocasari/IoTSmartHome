package com.company.Control_unit;



import org.eclipse.californium.core.*;
import org.eclipse.californium.core.coap.CoAP;
import org.eclipse.californium.core.coap.Request;
import org.eclipse.californium.elements.exception.ConnectorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class LIGHTSConsumptionTask extends Thread {
    public Double Consuption = 0.0;
    public static String URLenergy;
    public static String URLswitch;
    private final static Logger logger = LoggerFactory.getLogger(LIGHTSConsumptionTask.class);

    public LIGHTSConsumptionTask(String URLenergy, String URLswitch) {
        super("LIGHTS TASK CONSUPTION");
        this.URLenergy = URLenergy;
        this.URLswitch = URLswitch;

    }

    @Override
    public void start() {
        createGetRequestObserving();
    }

    private void createGetRequestObserving() {
        CoapClient client = new CoapClient(URLenergy);

        logger.info("OBSERVING LIGHTS... {}", URLenergy);

        Request request = Request.newGet().setURI(URLenergy).setObserve();
        request.setConfirmable(true);


        CoapObserveRelation relation = client.observe(request, new CoapHandler() {

            public void onLoad(CoapResponse response) {
                String content = response.getResponseText();
                Consuption += Double.parseDouble(content);
                System.out.println("Total Consumption : " + Consuption);
                System.out.println("NOTIFICATION Body: " + content);
                if (ControlUnit.checkConsumption(Consuption)) {

                    new Thread(() -> createPostRequest());

                    System.out.println("consumo energetico fuori range: metodo POST per spegnere le lampadine... ");

                }


            }

            public void onError() {
                logger.error("OBSERVING LIGHTS FAILED");
            }
        });

        // Observes the coap resource for 30 seconds then the observing relation is deleted
        try {
            Thread.sleep(60 * 2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        logger.info("CANCELLATION.....");
        relation.proactiveCancel();
    }

    //POST CON BODY VUOTO PER SPEGNERE LE LUCI
    private void createPostRequest() {

        CoapClient coapClient = new CoapClient(URLswitch);

        Request request = new Request(CoAP.Code.POST);

        request.setConfirmable(true);

        System.out.println("Post to turn the swich off");

        CoapResponse coapResp = null;

        try {

            coapResp = coapClient.advanced(request);

            System.out.println("Post Request sent!!");
            String text = coapResp.getResponseText();
            System.out.println(text+"\n");




        } catch (ConnectorException | IOException e) {
            e.printStackTrace();
        }
    }

}


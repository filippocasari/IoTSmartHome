package com.company.Control_unit;


import org.eclipse.californium.core.*;
import org.eclipse.californium.core.coap.CoAP;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.eclipse.californium.core.coap.OptionSet;
import org.eclipse.californium.core.coap.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FRIDGEConsumptionTask implements Runnable {
    public Double Consuption = 0.0;
    public static String URLenergy;


    private final static Logger logger = LoggerFactory.getLogger(FRIDGEConsumptionTask.class);

    public FRIDGEConsumptionTask(String URL) {

        URLenergy = URL;


    }

    private void createGetRequestObserving() {
        CoapClient client = new CoapClient(URLenergy);

        System.out.println("OBSERVING FRIDGE system...  @ " + URLenergy);

        Request request = new Request(CoAP.Code.GET);
        request.setOptions(new OptionSet().setAccept(MediaTypeRegistry.APPLICATION_SENML_JSON));
        request.setConfirmable(true);
        request.setObserve();


        CoapObserveRelation relation = client.observe(request, new CoapHandler() {

            public void onLoad(CoapResponse response) {
                logger.info("Response Pretty Print: \n{}", Utils.prettyPrint(response));

                String text = response.getResponseText();
                logger.info("Payload: {}", text);
                logger.info("Message ID: " + response.advanced().getMID());
                logger.info("Token: " + response.advanced().getTokenString());

                String[] ValuesSring = text.split(",");
                String value = ValuesSring[3].split(":")[1];
                double InstantConsumption = Double.parseDouble(value);

                Consuption += InstantConsumption;

                System.out.println("\n\nTotal Consumption Fridge : " + Consuption + " W");
                System.out.println("Instant Consumption Fridge: " + value + " W\n\n");

            }


            public void onError() {
                logger.error("OBSERVING Fridge FAILED");
            }
        });
        try {
            Thread.sleep(60 * 3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Observes the coap resource for 30 seconds then the observing relation is deleted
        try {
            Thread.sleep(60 * 3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        logger.info("CANCELLATION.....");
        relation.proactiveCancel();
    }


    @Override
    public void run() {
        createGetRequestObserving();
    }
}


package com.company.Control_unit;


import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapObserveRelation;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.coap.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.eclipse.californium.core.CoapHandler;

public class FRIDGEConsumptionTask implements Runnable {
    public Double Consuption = 0.0;
    public static String URLenergy;

    private final static Logger logger = LoggerFactory.getLogger(FRIDGEConsumptionTask.class);

    public FRIDGEConsumptionTask(String URLenergy) {

        this.URLenergy = URLenergy;


    }

    private void createGetRequestObserving() {
        CoapClient client = new CoapClient(URLenergy);

        logger.info("OBSERVING FRIDGE system...  @ " + URLenergy);

        Request request = Request.newGet().setURI(URLenergy).setObserve();
        request.setConfirmable(true);


        CoapObserveRelation relation = client.observe(request, new CoapHandler() {

            public void onLoad(CoapResponse response) {
                String content = response.getResponseText();
                double InstantConsumption = Double.parseDouble(content);

                Consuption += InstantConsumption;
                if (InstantConsumption > 2.0) {
                    ControlUnit.Notificationconsumption("FRIDGE system");
                }

                System.out.println("Total Consumption Fridge : " + Consuption+" W");
                System.out.println("Instant Consumption Fridge: " + content+" W");

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


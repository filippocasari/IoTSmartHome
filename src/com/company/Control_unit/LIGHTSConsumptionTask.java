package com.company.Control_unit;


import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapObserveRelation;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.Utils;
import org.eclipse.californium.core.coap.CoAP.Code;
import org.eclipse.californium.core.coap.Request;
import org.eclipse.californium.elements.exception.ConnectorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.eclipse.californium.core.CoapHandler;


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


                    new Thread(() -> new POSTClient(URLswitch)).start();

                    //System.out.println("consumo energetico fuori range: metodo POST per spegnere le lampadine... ");
                    Notificationconsumption();

                }


            }

            public void onError() {
                logger.error("OBSERVING LIGHTS FAILED");
            }
        });

        // Observes the coap resource for 30 seconds then the observing relation is deleted
        try {
            Thread.sleep(60 * 3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        logger.info("CANCELLATION.....");
        relation.proactiveCancel();
    }


    public void Notificationconsumption() {
        logger.info("Too hight Consumption from Lights: switch must be set off");
    }

}


package com.company.Control_unit.ThreadsClientControlUnit;


//import com.company.Control_unit.ClientsType.GETClient;

import com.company.Control_unit.ClientsType.POSTClient;
import com.company.Control_unit.ThreadsClientControlUnit.ControlUnit;
import org.eclipse.californium.core.*;
import org.eclipse.californium.core.coap.CoAP;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.eclipse.californium.core.coap.OptionSet;
import org.eclipse.californium.core.coap.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LIGHTSConsumptionTask implements Runnable {
    public Double Consuption = 0.0;
    public static String URLenergy;
    public static String URLswitch;
    int count = 0;
    private final static Logger logger = LoggerFactory.getLogger(LIGHTSConsumptionTask.class);

    public LIGHTSConsumptionTask(String URLenergy, String URLswitch) {

        this.URLenergy = URLenergy;
        this.URLswitch = URLswitch;

    }


    private void createGetRequestObserving() {
        CoapClient client = new CoapClient(URLenergy);
        System.out.println("OBSERVING LIGHTS... @ " + URLenergy);
        //logger.info("OBSERVING LIGHTS... {}", URLenergy);

        Request request = new Request(CoAP.Code.GET);
        request.setOptions(new OptionSet().setAccept(MediaTypeRegistry.APPLICATION_SENML_JSON));
        request.setObserve();
        request.setConfirmable(true);


        CoapObserveRelation relation = client.observe(request, new CoapHandler() {

            public void onLoad(CoapResponse response) {
                logger.info("Response Pretty Print: \n{}", Utils.prettyPrint(response));

                //The "CoapResponse" message contains the response.
                String text = response.getResponseText();
                logger.info("Payload: {}", text);
                logger.info("Message ID: " + response.advanced().getMID());
                logger.info("Token: " + response.advanced().getTokenString());

                String[] ValuesSring = text.split(",");
                String value = ValuesSring[3].split(":")[1];
                double InstantConsumption = Double.parseDouble(value);
                try {
                    count = ControlUnit.turnOnSwitchCondition(InstantConsumption, URLswitch, count, URLenergy); //turn on the switch if lights are off for too much time
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Consuption += InstantConsumption;

                System.out.println("\n\nTotal Consumption Lights : " + Consuption + " W");
                System.out.println("Instant Consumption Lights : " + InstantConsumption + " W\n\n");
                Runnable runnable = () -> {
                    //GETClient getClient = new GETClient(URLswitch);

                    //if (getClient.isOn(getClient.getResponseString())) {
                    new Thread(() -> ControlUnit.Notificationconsumption("LIGHTS")).start();

                    new Thread(() -> new POSTClient(URLswitch)).start();

                    /*} else {
                        System.err.println("Switch just off");
                        //logger.info("Switch just off");
                    }*/

                };

                if (ControlUnit.checkConsumption(InstantConsumption, "lights")) {
                    Thread t = new Thread(runnable);
                    t.start();

                }


            }


            public void onError() {
                System.err.println("OBSERVING LIGHTS FAILED");
                //logger.error("OBSERVING LIGHTS FAILED");
            }
        });


    }


    @Override
    public void run() {
        createGetRequestObserving();
    }
}


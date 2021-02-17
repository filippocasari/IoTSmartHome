package com.company.Control_unit;


import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapObserveRelation;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.coap.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.eclipse.californium.core.CoapHandler;

public class LIGHTSConsumptionTask implements Runnable {
    public Double Consuption = 0.0;
    public static String URLenergy;
    public static String URLswitch;
    //private final static Logger logger = LoggerFactory.getLogger(LIGHTSConsumptionTask.class);

    public LIGHTSConsumptionTask(String URLenergy, String URLswitch) {

        this.URLenergy = URLenergy;
        this.URLswitch = URLswitch;

    }




    private void createGetRequestObserving() {
        CoapClient client = new CoapClient(URLenergy);
        System.out.println("OBSERVING LIGHTS... @ "+URLenergy);
        //logger.info("OBSERVING LIGHTS... {}", URLenergy);

        Request request = Request.newGet().setURI(URLenergy).setObserve();
        request.setConfirmable(true);


        CoapObserveRelation relation = client.observe(request, new CoapHandler() {

            public void onLoad(CoapResponse response) {
                String content = response.getResponseText();
                double InstantConsumption = Double.parseDouble(content);

                Consuption += InstantConsumption;

                System.out.println("Total Consumption Lights : " + Consuption);
                System.out.println("Instant Consumption Lights : " + content);
                Runnable runnable = () -> {
                    GETClient getClient = new GETClient(URLswitch);

                    if (getClient.isOn(getClient.getResponseString())){
                        Notificationconsumption();
                        new Thread(() -> new POSTClient(URLswitch)).start();

                    } else {
                        System.err.println("Switch just off");
                        //logger.info("Switch just off");
                    }

                };

                if(ControlUnit.checkConsumption(Consuption, InstantConsumption) ){
                    Thread t = new Thread(runnable);
                    t.start();

                }



            }


            public void onError() {
                System.err.println("OBSERVING LIGHTS FAILED");
                //logger.error("OBSERVING LIGHTS FAILED");
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
        System.err.println("CANCELLATION...");
        //logger.info("CANCELLATION.....");
        relation.proactiveCancel();
    }


    public void Notificationconsumption() {
        System.err.println("Too hight Consumption from Lights: switch must be set off");
        //logger.info("Too hight Consumption from Lights: switch must be set off");
    }


    @Override
    public void run() {
        createGetRequestObserving();
    }
}


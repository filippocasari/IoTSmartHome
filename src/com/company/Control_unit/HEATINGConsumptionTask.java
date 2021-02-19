package com.company.Control_unit;


import com.company.Control_unit.ClientsType.GETClient;
import com.company.Control_unit.ClientsType.POSTClient;
import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapObserveRelation;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.coap.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.eclipse.californium.core.CoapHandler;

public class HEATINGConsumptionTask implements Runnable {
    public Double Consuption = 0.0;
    public static String URLenergy;
    public static String URLswitch;
   // public int count=0;
    private final static Logger logger = LoggerFactory.getLogger(HEATINGConsumptionTask.class);

    public HEATINGConsumptionTask(String URLenergy, String URLswitch) {

        this.URLenergy = URLenergy;
        this.URLswitch = URLswitch;

    }


    private void createGetRequestObserving() {
        CoapClient client = new CoapClient(URLenergy);

        //logger.info("OBSERVING HEATING system... {}", URLenergy);
        System.out.println("OBSERVING HEATING system @ "+URLenergy);
        Request request = Request.newGet().setURI(URLenergy).setObserve();
        request.setConfirmable(true);


        CoapObserveRelation relation = client.observe(request, new CoapHandler() {

            public void onLoad(CoapResponse response) {
                String content = response.getResponseText();
                double InstantConsumption = Double.parseDouble(content);

                Consuption += InstantConsumption;

                System.out.println("Total Consumption heating system : " + Consuption);
                System.out.println("Instant Consumption heating system: " + content);
                Runnable runnable = () -> {
                    GETClient getClient = new GETClient(URLswitch);

                    if (getClient.isOn(getClient.getResponseString())) {
                        ControlUnit.Notificationconsumption("HEATING system");
                        System.err.println("POST REQUEST TO HEATING SWITCH");
                        new Thread(() -> new POSTClient(URLswitch)).start();

                    } else {
                        logger.info("Switch just off");
                    }

                };

                if (ControlUnit.checkConsumption(InstantConsumption, "heatingsystem")) {
                    Thread t = new Thread(runnable);
                    t.start();

                }


            }


            public void onError() {
                logger.error("OBSERVING Heating system FAILED");
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


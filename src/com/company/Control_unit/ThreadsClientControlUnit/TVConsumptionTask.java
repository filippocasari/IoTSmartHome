package com.company.Control_unit.ThreadsClientControlUnit;


import com.company.Control_unit.ClientsType.POSTClient;
import com.company.Control_unit.ThreadsClientControlUnit.ControlUnit;
import com.company.Control_unit.Utils.SenMLPack;
import com.company.Control_unit.Utils.SenMLRecord;
import com.google.gson.Gson;
import org.eclipse.californium.core.*;
import org.eclipse.californium.core.coap.CoAP;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.eclipse.californium.core.coap.OptionSet;
import org.eclipse.californium.core.coap.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TVConsumptionTask implements Runnable {
    public Double Consuption = 0.0;
    public static String URLenergy;
    public static String URLswitch;
    public int count = 0;
    private final static Logger logger = LoggerFactory.getLogger(TVConsumptionTask.class);

    public TVConsumptionTask(String URLenergy, String URLswitch) {

        this.URLenergy = URLenergy;
        this.URLswitch = URLswitch;

    }


    private void createGetRequestObserving() {
        CoapClient client = new CoapClient(URLenergy);

        //logger.info("OBSERVING TV... {}", URLenergy);
        System.out.println("OBSERVING TV... @ " + URLenergy);
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

                Gson gson = new Gson();
                SenMLPack senMLPack = gson.fromJson(text, SenMLPack.class);
                SenMLRecord senMLRecord = senMLPack.get(0);

                double InstantConsumption = Double.parseDouble(senMLRecord.getV().toString());
                if (InstantConsumption < 0.0) {
                    InstantConsumption = 0.0;
                }

                Consuption += InstantConsumption;

                System.out.println("\n\nTotal Consumption tv : " + Consuption + senMLRecord.getU());
                System.out.println("Instant Consumption tv: " + InstantConsumption +" "+senMLRecord.getU()+ " \n\n");
                try {
                    count = ControlUnit.turnOnSwitchCondition(InstantConsumption, URLswitch, count, URLenergy);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Runnable runnable = () -> {
                    //GETClient getClient = new GETClient(URLswitch);

                    //if (getClient.isOn(getClient.getResponseString())){
                    ControlUnit.Notificationconsumption("TV system");

                    new Thread(() -> new POSTClient(URLswitch)).start();

                    /*} else {
                        System.err.println("Switch of Tv just off");
                        //logger.info("Switch just off");
                    }*/

                };

                if (ControlUnit.checkConsumption(InstantConsumption, "tv")) {
                    Thread t = new Thread(runnable);
                    t.start();

                }


            }


            public void onError() {
                System.err.println("OBSERVING TV FAILED");
                //logger.error("OBSERVING TV FAILED");
            }
        });
        try {
            Thread.sleep(60 * 3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


    }


    @Override
    public void run() {
        createGetRequestObserving();
    }
}


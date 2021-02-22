package com.company.Control_unit.ThreadsClientControlUnit;


//import com.company.Control_unit.ClientsType.GETClient;

import com.company.Control_unit.ClientsType.GETClient;
import com.company.Control_unit.ClientsType.POSTClient;
import com.company.Control_unit.ClientsType.PUTClient;
import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapObserveRelation;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.coap.CoAP;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.eclipse.californium.core.coap.OptionSet;
import org.eclipse.californium.core.coap.Request;
import org.eclipse.californium.core.CoapHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class THERMOSTATMonitoringTask implements Runnable {
    //public Double Consuption = 0.0;
    public static String URLenergy;
    public static String URLswitch;
    public static String URLtemperature;

    private final static Logger logger = LoggerFactory.getLogger(LIGHTSConsumptionTask.class);

    public THERMOSTATMonitoringTask(String URLenergy, String URLswitch, String URLtemperature) {

        this.URLenergy = URLenergy;
        this.URLswitch = URLswitch;
        this.URLtemperature = URLtemperature;

    }


    private void createGetRequestObserving() {
        CoapClient client = new CoapClient(URLtemperature);
        System.out.println("OBSERVING THERMOSTAT... @ " + URLtemperature);

        Request request = new Request(CoAP.Code.GET);

        request.setObserve();
        request.setConfirmable(true);


        CoapObserveRelation relation = client.observe(request, new CoapHandler() {

            public void onLoad(CoapResponse response) {
                String text = response.getResponseText();
                logger.info("Payload: {}", text);
                logger.info("Message ID: " + response.advanced().getMID());
                logger.info("Token: " + response.advanced().getTokenString());
                if(!text.equals("null")){
                    printTemperature(Double.parseDouble(text));
                }




            }

            public void onError() {
                System.err.println("OBSERVING THERMOSTAT FAILED");

            }
        });

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

    private void printTemperature(double temperaturecaught) {
        System.out.println("\n\nHome's Temperature: " + temperaturecaught + " Cel\n\n");
    }

    private void checkTemperatureRange(double temperaturecaught) {
        if (temperaturecaught > 25.0) {
            System.err.println("\n\nTemperature out of range, TOO HIGH TEMPERATURE!!! ==> TURN SWITCH OFF\n\n");
            new Thread(() -> new POSTClient(URLswitch));
        } else if (temperaturecaught < 21.5) {
            System.err.println("\n\nTemperature out of range, TOO LOW TEMPERATURE!!!==> TURN SWITCH ON\n\n");
            new Thread(() -> new POSTClient(URLswitch));
        }
    }


    @Override
    public void run() {
        createGetRequestObserving();
    }
}


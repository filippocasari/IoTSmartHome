package com.company.Control_unit;


//import com.company.Control_unit.ClientsType.GETClient;
import com.company.Control_unit.ClientsType.POSTClient;
import com.company.Control_unit.ClientsType.PUTClient;
import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapObserveRelation;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.coap.Request;
import org.eclipse.californium.core.CoapHandler;

public class HEATINGConsumptionTask implements Runnable {
    public Double Consuption = 0.0;
    public static String URLenergy;
    public static String URLswitch;
    int count = 0;
    //private final static Logger logger = LoggerFactory.getLogger(LIGHTSConsumptionTask.class);

    public HEATINGConsumptionTask(String URLenergy, String URLswitch) {

        this.URLenergy = URLenergy;
        this.URLswitch = URLswitch;

    }


    private void createGetRequestObserving() {
        CoapClient client = new CoapClient(URLenergy);
        System.out.println("OBSERVING HEATING SYSTEM... @ " + URLenergy);
        //logger.info("OBSERVING LIGHTS... {}", URLenergy);

        Request request = Request.newGet().setURI(URLenergy).setObserve();
        request.setConfirmable(true);


        CoapObserveRelation relation = client.observe(request, new CoapHandler() {

            public void onLoad(CoapResponse response) {
                String content = response.getResponseText();
                double InstantConsumption = Double.parseDouble(content);
                try {
                    count = ControlUnit.turnOnSwitchCondition(InstantConsumption, URLswitch, count, URLenergy); //turn on the switch if lights are off for too much time
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Consuption += InstantConsumption;

                System.out.println("Total Consumption HEATING SYSTEM : " + Consuption + " W");
                System.out.println("Instant Consumption HEATING SYSTEM : " + content + " W");
                Runnable runnable = () -> {
                    //GETClient getClient = new GETClient(URLswitch);

                    //if (getClient.isOn(getClient.getResponseString())) {
                    new Thread(()->ControlUnit.Notificationconsumption("HEATING SYSTEM")).start();

                    new Thread(() -> new POSTClient(URLswitch)).start();

                    /*} else {
                        System.err.println("Switch just off");
                        //logger.info("Switch just off");
                    }*/

                };

                if (ControlUnit.checkConsumption(InstantConsumption, "heating system")) {
                    Thread t = new Thread(runnable);
                    t.start();

                }


            }


            public void onError() {
                System.err.println("OBSERVING HEATING SYSTEM FAILED");
                //logger.error("OBSERVING LIGHTS FAILED");
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


    @Override
    public void run() {
        createGetRequestObserving();
    }
}


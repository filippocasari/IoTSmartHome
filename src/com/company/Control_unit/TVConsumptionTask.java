package com.company.Control_unit;


import com.company.Control_unit.ClientsType.GETClient;
import com.company.Control_unit.ClientsType.POSTClient;
import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapObserveRelation;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.coap.Request;
import org.eclipse.californium.core.CoapHandler;

public class TVConsumptionTask implements Runnable {
    public Double Consuption = 0.0;
    public static String URLenergy;
    public static String URLswitch;
    //private final static Logger logger = LoggerFactory.getLogger(TVConsumptionTask.class);

    public TVConsumptionTask(String URLenergy, String URLswitch) {

        this.URLenergy = URLenergy;
        this.URLswitch = URLswitch;

    }


    private void createGetRequestObserving() {
        CoapClient client = new CoapClient(URLenergy);

        //logger.info("OBSERVING TV... {}", URLenergy);
        System.out.println("OBSERVING TV... @ "+URLenergy);
        Request request = Request.newGet().setURI(URLenergy).setObserve();
        request.setConfirmable(true);


        CoapObserveRelation relation = client.observe(request, new CoapHandler() {

            public void onLoad(CoapResponse response) {
                String content = response.getResponseText();
                double InstantConsumption = Double.parseDouble(content);

                Consuption += InstantConsumption;

                System.out.println("Total Consumption : " + Consuption);
                System.out.println("Instant Consumption: " + content);
                Runnable runnable = () -> {
                    GETClient getClient = new GETClient(URLswitch);

                    if (getClient.isOn(getClient.getResponseString())){
                        ControlUnit.Notificationconsumption("TV system");
                        System.err.println("POST REQUEST TO TV SWITCH...");
                        new Thread(() -> new POSTClient(URLswitch)).start();

                    } else {
                        System.err.println("Switch of Tv just off");
                        //logger.info("Switch just off");
                    }

                };

                if(ControlUnit.checkConsumption(Consuption, InstantConsumption) ){
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

        // Observes the coap resource for 30 seconds then the observing relation is deleted
        try {
            Thread.sleep(60 * 3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("CANCELLATION.....");
        //logger.info("CANCELLATION.....");
        relation.proactiveCancel();
    }



    @Override
    public void run() {
        createGetRequestObserving();
    }
}


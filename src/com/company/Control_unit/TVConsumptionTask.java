package com.company.Control_unit;


import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapHandler;
import org.eclipse.californium.core.CoapObserveRelation;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.coap.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TVConsumptionTask extends Thread{
    public int Consuption = 0;
    public static String URLenergy;
    public static String URLswitch;
    private final static Logger logger = LoggerFactory.getLogger(TVConsumptionTask.class);

    public TVConsumptionTask(String URLenergy) {
        super("TV TASK CONSUPTION");
        this.URLenergy = URLenergy;
        this.URLswitch = URLswitch;

    }

    @Override
    public void start() {
        createGetRequestObserving();
    }

    private void createGetRequestObserving() {
        CoapClient client = new CoapClient(URLenergy);

        //logger.info("OBSERVING ... {}", URL);

        Request request = Request.newGet().setURI(URLenergy).setObserve();
        request.setConfirmable(true);


        CoapObserveRelation relation = client.observe(request, new CoapHandler() {

            public void onLoad(CoapResponse response) {
                String content = response.getResponseText();

                logger.info("NOTIFICATION Body: " + content);
            }

            public void onError() {
                logger.error("OBSERVING TV FAILED");
            }
        });

        // Observes the coap resource for 30 seconds then the observing relation is deleted
        try {
            Thread.sleep(60 * 2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        logger.info("CANCELLATION.....");
        relation.proactiveCancel();
    }


}

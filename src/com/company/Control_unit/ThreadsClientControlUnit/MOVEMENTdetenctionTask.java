package com.company.Control_unit.ThreadsClientControlUnit;


import com.company.Control_unit.Utils.SenMLPack;
import com.company.Control_unit.Utils.SenMLRecord;
import com.google.gson.Gson;
import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapObserveRelation;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.coap.CoAP;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.eclipse.californium.core.coap.OptionSet;
import org.eclipse.californium.core.coap.Request;
import org.eclipse.californium.core.CoapHandler;

public class MOVEMENTdetenctionTask implements Runnable {

    public static String URLmovement;
    public boolean vb;

    //private final static Logger logger = LoggerFactory.getLogger(LIGHTSConsumptionTask.class);

    public MOVEMENTdetenctionTask(String URLmovement) {

        this.URLmovement = URLmovement;

    }

    private void createGetRequestObserving() {
        CoapClient client = new CoapClient(URLmovement);
        System.out.println("OBSERVING MOVEMENT sensor... @ " + URLmovement);

        Request request = new Request(CoAP.Code.GET);
        request.setOptions(new OptionSet().setAccept(MediaTypeRegistry.APPLICATION_SENML_JSON));
        request.setObserve();
        request.setConfirmable(true);


        CoapObserveRelation relation = client.observe(request, new CoapHandler() {

            public void onLoad(CoapResponse response) {

                Gson gson = new Gson();
                String text = response.getResponseText();
                SenMLPack senMLPack = gson.fromJson(text, SenMLPack.class);
                SenMLRecord senMLRecord = senMLPack.get(0);
                vb=senMLRecord.getVb();
                System.err.println("MOVEMENT DETENCTION: " + vb);
                if (!vb) {
                    try {

                        ControlUnit.settingEcomodeON();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                } else {
                    try {
                        ControlUnit.disablingEcomode();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }

            }

            public void onError() {
                System.err.println("OBSERVING MOVEMENT FAILED");
            }
        });

    }


    @Override
    public void run() {
        createGetRequestObserving();
    }
}



package com.company.Control_unit;


import com.company.Control_unit.ClientsType.POSTClient;
import com.company.Control_unit.ClientsType.PUTClient;
import com.company.Control_unit.UtilsTime.SimTime;
import org.eclipse.californium.core.*;
import org.eclipse.californium.core.coap.CoAP;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.eclipse.californium.core.coap.OptionSet;
import org.eclipse.californium.core.coap.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author Filippo Casari
 * @version final
 * @ControlUnit
 * @collaboration with: Luca Mantovani, Davide Casnici
 */


class ControlUnit2v {
    public boolean lightSwitch = false;
    public boolean tvSwitch = true;
    public boolean washerSwitch = false;


    public final static Logger logger = LoggerFactory.getLogger(ControlUnit2v.class);

    private static final Double MAX_VALUE_WASHER = 97.0;
    private static final Double MAX_VALUE_LIGHTS = 2.0;
    private static final Double MAX_VALUE_TV = 57.0;

    private Double Consumption = 0.0;

    private static final String COAP_ENDPOINT_ENERGY_THERMOSTAT = "coap://127.0.0.1:5683/thermostat/energy";
    public static final String COAP_ENDPOINT_SWITCH_THERMOSTAT = "coap://127.0.0.1:5683/thermostat/switch";
    public static final String COAP_ENDPOINT_TEMPERATURE_THERMOSTAT = "coap://127.0.0.1:5683/thermostat/temperature";
    private static final String COAP_ENDPOINT_ENERGY_LIGHTS = "coap://127.0.0.1:5683/lights/energy";
    public static final String COAP_ENDPOINT_SWITCH_LIGHTS = "coap://127.0.0.1:5683/lights/switch";
    private static final String COAP_ENDPOINT_SWITCH_TV = "coap://127.0.0.1:5683/TV/switch";
    public static final String COAP_ENDPOINT_ENERGY_TV = "coap://127.0.0.1:5683/TV/energy";
    private static final String COAP_ENDPOINT_ENERGY_WASHER = "coap://127.0.0.1:5683/washer/energy";
    private static final String COAP_ENDPOINT_SWITCH_WASHER = "coap://127.0.0.1:5683/washer/switch";
    //private static final String COAP_ENDPOINT_ENERGY_HEATING = "coap://127.0.0.1:5683/heating-system/energy";
    //private static final String COAP_ENDPOINT_SWITCH_FRIDGE = "coap://127.0.0.1:5683/fridge/switch";
    //private static final String COAP_ENDPOINT_SWITCH_HEATING = "coap://127.0.0.1:5683/heating-system/switch";
    private static final String COAP_ENDPOINT_ENERGY_FRIDGE = "coap://127.0.0.1:5683/fridge/energy";
    private static final String COAP_ENDPOINT_MOVEMENT_SENSOR = "coap://127.0.0.1:5683/detector/movement";

    public boolean EcoMode = false;
    private String Datedetails = null;


    public ControlUnit2v() {
        CoapClient client = new CoapClient();

        createNewCoapClientObserving(COAP_ENDPOINT_ENERGY_LIGHTS, COAP_ENDPOINT_SWITCH_LIGHTS,
                "lights", client);
        createNewCoapClientObserving(COAP_ENDPOINT_ENERGY_TV, COAP_ENDPOINT_SWITCH_TV,
                "tv", client);
        createNewCoapClientObserving(COAP_ENDPOINT_ENERGY_WASHER, COAP_ENDPOINT_SWITCH_WASHER,
                "washer", client);
        createNewCoapClientObserving(COAP_ENDPOINT_MOVEMENT_SENSOR,
                "movement sensor", client);
        createNewCoapClientObserving(COAP_ENDPOINT_TEMPERATURE_THERMOSTAT, COAP_ENDPOINT_SWITCH_THERMOSTAT,
                "temperature", client);
        createNewCoapClientObserving(COAP_ENDPOINT_ENERGY_FRIDGE, "fridge", client);


    }


    public boolean isEcoMode() {
        return EcoMode;
    }


    private void printTotalConsumptionfromAll(String day) {
        System.out.println("Daily consumption for the day : " + day + " is : " + Consumption);
        TotalCostEuros();
        Consumption = 0.0;
    }

    private static String createStringDate(SimTime simTime) {
        return simTime.getDay() + ", " + simTime.getHour() + " : " + simTime.getMinute();
    }


    public static void main(String[] args) throws InterruptedException {

        SimTime simTime = new SimTime();

        System.out.println("Starting Time...\nDay: " + simTime.getDay().toString());
        System.out.println("Hour: " + simTime.getHour());
        System.out.println("Minute: " + simTime.getMinute());
        System.out.println("Second: " + simTime.getSecond());

        ControlUnit2v controlUnit2v = new ControlUnit2v();
        simTime.setSpeed(1000); //or 1000 speed, if we want to check total daily consumption
        simTime.start();


        String day = simTime.getDay().toString();

        while (true) {
            //control if day is different
            String Datedetails = createStringDate(simTime); // create a string of timestamp
            System.out.println(Datedetails + " ...checking Consumptions and Ecomode...");
            if (!day.equals(simTime.getDay().toString())) {

                controlUnit2v.printTotalConsumptionfromAll(day);
            }

            try {
                if (!controlUnit2v.isEcoMode()) { //if ecomode is off
                    //check if it's time to turn ecomode on
                    if (checkEcoMode(simTime)) { // if Ecomode is true, put request to turn all switches off
                        System.err.println("HOUR > " + simTime.getHour());
                        settingEcomodeON();
                    } else {
                        System.err.println("Ecomode just set");
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            day = simTime.getDay().toString(); //day of the week
            try {
                Thread.sleep(4000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }


        }


    }

    public static boolean checkConsumption(Double InstantConsumption, String fromWho) {
        double max_value = 0.0;
        switch (fromWho) {
            case ("washer"):
                max_value = MAX_VALUE_WASHER;
                break;
            case ("tv"):
                max_value = MAX_VALUE_TV;
                break;

            case ("lights"):
                max_value = MAX_VALUE_LIGHTS;
                break;
        }
        return InstantConsumption > max_value;

    }

    public static boolean checkEcoMode(SimTime simTime) throws InterruptedException {

        return ((simTime.getHour() > 0 && simTime.getHour() < 5));

    }

    public static void Notificationconsumption(String fromWho) {
        System.err.println("\nToo hight Consumption from " + fromWho + ": switch must be set off");
        System.err.println("\nPOST REQUEST TO " + fromWho + "-- SWITCH");
    }


    public static void settingEcomodeON() throws InterruptedException {


        System.err.println("ECOMODE IS TRUE: PUT REQUESTS FOR EACH DEVICE");

        new Thread(() -> new PUTClient(COAP_ENDPOINT_SWITCH_LIGHTS, String.valueOf(false))).start();

        new Thread(() -> new PUTClient(COAP_ENDPOINT_SWITCH_TV, String.valueOf(false))).start();
        new Thread(() -> new PUTClient(COAP_ENDPOINT_SWITCH_WASHER, String.valueOf(false))).start();


    }

    public static void disablingEcomode() throws InterruptedException {
        System.err.println("ECOMODE IS FALSE : PUT REQUESTS FOR EACH DEVICE");
        new Thread(() -> new PUTClient(COAP_ENDPOINT_SWITCH_LIGHTS, String.valueOf(true))).start();

        new Thread(() -> new PUTClient(COAP_ENDPOINT_SWITCH_TV, String.valueOf(true))).start();
        new Thread(() -> new PUTClient(COAP_ENDPOINT_SWITCH_WASHER, String.valueOf(true))).start();

    }

    private void TotalCostEuros() {
        System.out.println("Cost of the day is: " + (Consumption * 0.06256) / 1000 + " euros");

    }

    private void createNewCoapClientObserving(String URLenergy, String URLswitch, String Who, CoapClient client) {

        System.out.println("OBSERVING " + Who + "... @ " + URLenergy);
        //logger.info("OBSERVING LIGHTS... {}", URLenergy);

        Request request = new Request(CoAP.Code.GET);
        client.setURI(URLenergy);
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
                logger.info("FROM : " + URLenergy);

                String[] ValuesSring = text.split(",");
                String value = ValuesSring[3].split(":")[1];

                if (URLenergy.equals(COAP_ENDPOINT_TEMPERATURE_THERMOSTAT) && ((ValuesSring[3]).split(":"))[0].equals("v")) {

                    double temperaturecaught = Double.parseDouble(value);
                    System.out.println("TEMPERATURE?S HOME IS : " + temperaturecaught);
                    if (temperaturecaught > 25.0) {
                        System.err.println("\n\nTemperature out of range, TOO HIGH TEMPERATURE!!! ==> TURN SWITCH OFF\n\n");
                        new Thread(() -> new POSTClient(URLswitch));
                    } else if (temperaturecaught < 21.5) {
                        System.err.println("\n\nTemperature out of range, TOO LOW TEMPERATURE!!!==> TURN SWITCH ON\n\n");
                        new Thread(() -> new POSTClient(URLswitch));
                    }
                } else if (!URLenergy.equals(COAP_ENDPOINT_TEMPERATURE_THERMOSTAT)) {
                    double InstantConsumption = Double.parseDouble(value);
                    Consumption += InstantConsumption;

                    System.out.println("\n\nTotal Consumption: " + Consumption + " W");
                    System.out.println("Instant Consumption " + Who + " : " + InstantConsumption + " W\n\n");


                    Runnable runnable = () -> {


                        new Thread(() -> ControlUnit2v.Notificationconsumption(Who)).start();

                        new Thread(() -> new POSTClient(URLswitch)).start();


                    };
                    if (ControlUnit2v.checkConsumption(InstantConsumption, Who) && !isEcoMode()) {
                        Thread t = new Thread(runnable);
                        t.start();

                    }
                }


            }

            public void onError() {
                System.err.println("OBSERVING" + Who + " FAILED");
                //logger.error("OBSERVING LIGHTS FAILED");
            }
        });
    }

    private void createNewCoapClientObserving(String URL, String Who, CoapClient client) {

        System.out.println("OBSERVING " + Who + "... @ " + URL);
        //logger.info("OBSERVING LIGHTS... {}", URLenergy);

        Request request = new Request(CoAP.Code.GET);
        client.setURI(URL);
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
                logger.info("FROM " + URL);

                String[] ValuesSring = text.split(",");
                String value = ValuesSring[2].split(":")[1];
                if(URL.equals(COAP_ENDPOINT_ENERGY_FRIDGE) && ((ValuesSring[3]).split(":"))[0].equals("v")){
                    Consumption+=Double.parseDouble(value);
                    System.out.println("\n\nTotal Consumption: " + Consumption + " W");
                    System.out.println("Instant Consumption " + Who + " : " + Consumption + " W\n\n");
                }

                if (value.equals("false")) {
                    System.err.println("VALUE OF MOVEMENT SENSOR IS: " + value);
                    try {
                        if (!isEcoMode()) {
                            ControlUnit2v.settingEcomodeON();
                            EcoMode = true;
                        }

                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                } else if (value.equals("true")) {
                    System.err.println("VALUE OF MOVEMENT SENSOR IS: " + value);
                    try {
                        if (isEcoMode()) {
                            ControlUnit2v.disablingEcomode();
                            EcoMode = false;
                        }

                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }


            }

            public void onError() {
                System.err.println("OBSERVING" + Who + " FAILED");
            }
        });
    }

}
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


/**
 * set UPDATE_PERIOD and TASK_DELAY_TIME to 10 seconds for TEMPERATURE SENSOR AND  20 secs for ENERGY SENSOR
 * set Thread.sleep to 10 seconds for MOVEMENT (Switch Actuator)
 * to have better responses
 */

class ControlUnit2v {


    public final static Logger logger = LoggerFactory.getLogger(ControlUnit2v.class);

    //if value > MAX_VALUES ==> turn specific switch off
    private static final Double MAX_VALUE_WASHER = 97.0;
    private static final Double MAX_VALUE_LIGHTS = 2.3;
    private static final Double MAX_VALUE_TV = 57.0;

    private Double Consumption = 0.0;

    //private static final String COAP_ENDPOINT_ENERGY_HEATING = "coap://127.0.0.1:5683/heating-system/energy";
    //private static final String COAP_ENDPOINT_SWITCH_FRIDGE = "coap://127.0.0.1:5683/fridge/switch";
    //private static final String COAP_ENDPOINT_SWITCH_HEATING = "coap://127.0.0.1:5683/heating-system/switch";
    //private static final String COAP_ENDPOINT_ENERGY_THERMOSTAT = "coap://192.168.0.132:5683/thermostat/energy";
    public static final String LOCALHOST = "127.0.0.1";
    public static final String IP_AND_PORT = "coap://" + LOCALHOST + ":5683"; //192.168.0.132
    public static final String COAP_ENDPOINT_SWITCH_THERMOSTAT = IP_AND_PORT + "/switch";
    public static final String COAP_ENDPOINT_TEMPERATURE_THERMOSTAT = IP_AND_PORT + "/thermostat/temperature";
    private static final String COAP_ENDPOINT_ENERGY_LIGHTS = IP_AND_PORT + "/lights/energy";
    public static final String COAP_ENDPOINT_SWITCH_LIGHTS = IP_AND_PORT + "/lights/switch";
    private static final String COAP_ENDPOINT_SWITCH_TV = IP_AND_PORT + "/TV/switch";
    public static final String COAP_ENDPOINT_ENERGY_TV = IP_AND_PORT + "/TV/energy";
    private static final String COAP_ENDPOINT_ENERGY_WASHER = IP_AND_PORT + "/washer/energy";
    private static final String COAP_ENDPOINT_SWITCH_WASHER = IP_AND_PORT + "/washer/switch";
    private static final String COAP_ENDPOINT_ENERGY_FRIDGE = IP_AND_PORT + "/fridge/energy";
    private static final String COAP_ENDPOINT_MOVEMENT_SENSOR = IP_AND_PORT + "/detector/movement";

    public boolean EcoMode = false; // Eco-mode is false at the beginning


    public ControlUnit2v() {
        //start new Client coap for every obs request/response
        CoapClient client = new CoapClient();

        //invoking function that starts the communication for every obj
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


    public static void main(String[] args) throws InterruptedException {
        //Simulationg Real Time
        SimTime simTime = new SimTime();
        simTime.setSpeed(1); // from 1 to 10000 (speed)
        simTime.start(); //start thread

        System.out.println("Starting Time...\n: ");
        createStringDate(simTime);

        //now create The Control Unit
        ControlUnit2v controlUnit2v = new ControlUnit2v();


        String day = simTime.getDay().toString(); //create a variable day to store the current day
        String Datedetails;

        while (true) {
            //checking if day is different
            Datedetails = createStringDate(simTime); // create a string of timestamp
            System.out.println(Datedetails + " ...checking Consumptions and Eco-mode...");
            if (!day.equals(simTime.getDay().toString())) {

                controlUnit2v.printTotalConsumptionfromAll(day);

            }

            try {
                if (!controlUnit2v.isEcoMode()) { //if eco-mode is off
                    //check if it's time to turn eco-mode on
                    if (checkEcoMode(simTime)) { // if Eco-mode is true, put request to turn all switches off
                        System.err.println("HOUR > " + simTime.getHour());
                        settingEcomodeON();
                        controlUnit2v.EcoMode = true;
                    } else {
                        System.err.println("Not time to set Ecomode ON");
                    }
                } else {
                    System.err.println("Ecomode just set");
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            day = simTime.getDay().toString(); //update the day of the week
            try {
                Thread.sleep(2000); //delay loop
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }

    }

    public boolean isEcoMode() {
        return EcoMode;
    }


    private void printTotalConsumptionfromAll(String day) {
        System.out.println("Daily consumption for the day : " + day + " is : " + Consumption+" Watt");
        TotalCostEuros();
        Consumption = 0.0;
    }

    private static String createStringDate(SimTime simTime) {
        return simTime.getDay() + ", " + simTime.getHour() + " : " + simTime.getMinute();
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

    public static boolean checkEcoMode(SimTime simTime) {

        return ((simTime.getHour() > 0 && simTime.getHour() < 5));

    }

    public static void Notificationconsumption(String fromWho) {
        System.err.println("\nToo hight Consumption from " + fromWho + ": switch must be set off");
        System.err.println("\nPOST REQUEST TO " + fromWho + "-- SWITCH");
    }


    public static void settingEcomodeON() throws InterruptedException {


        System.err.println("ECOMODE IS TRUE: PUT REQUESTS FOR EACH DEVICE");

        new Thread(() -> new PUTClient(COAP_ENDPOINT_SWITCH_LIGHTS, "false")).start();
        new Thread(() -> new PUTClient(COAP_ENDPOINT_SWITCH_TV, "false")).start();
        new Thread(() -> new PUTClient(COAP_ENDPOINT_SWITCH_WASHER, "false")).start();


    }

    public static void disablingEcomode() throws InterruptedException {

        System.err.println("ECOMODE IS FALSE : PUT REQUESTS FOR EACH DEVICE");

        new Thread(() -> new PUTClient(COAP_ENDPOINT_SWITCH_LIGHTS, "true")).start();
        new Thread(() -> new PUTClient(COAP_ENDPOINT_SWITCH_TV, "true")).start();
        new Thread(() -> new PUTClient(COAP_ENDPOINT_SWITCH_WASHER, "true")).start();

    }

    private void TotalCostEuros() {
        System.out.println("Cost of the day is: " + (Consumption * 0.06256)/1000 + " euros");

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

                String text = response.getResponseText();

                logger.info("Payload: {}", text);
                logger.info("Message ID: " + response.advanced().getMID());
                logger.info("Token: " + response.advanced().getTokenString());
                logger.info("FROM : " + URLenergy);

                String[] ValuesSring = text.split(",");
                String value = ValuesSring[3].split(":")[1];

                if (URLenergy.equals(COAP_ENDPOINT_TEMPERATURE_THERMOSTAT) && ValuesSring.length>4) {

                    double temperaturecaught = Double.parseDouble(value);
                    System.out.println("\nTEMPERATURE?S HOME IS : " + temperaturecaught+"\n");

                } else if (!URLenergy.equals(COAP_ENDPOINT_TEMPERATURE_THERMOSTAT)) {
                    double InstantConsumption = Double.parseDouble(value);
                    if (InstantConsumption < 0.0) {
                        InstantConsumption = 0.0;
                    }
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
                if (URL.equals(COAP_ENDPOINT_ENERGY_FRIDGE) && ((ValuesSring[3]).split(":"))[0].equals("v")) {
                    Consumption += Double.parseDouble(value);
                    System.out.println("\n\nTotal Consumption: " + Consumption + " W");
                    System.out.println("Instant Consumption " + Who + " : " + Consumption + " W\n\n");
                } else if (URL.equals(COAP_ENDPOINT_MOVEMENT_SENSOR)) {
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


            }

            public void onError() {
                System.err.println("OBSERVING" + Who + " FAILED");
            }
        });

    }

}
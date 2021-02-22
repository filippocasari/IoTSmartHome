package com.company.Control_unit.ThreadsClientControlUnit;


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


public class ControlUnit {

    public final static Logger logger = LoggerFactory.getLogger(ControlUnit.class);

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


    public ControlUnit()  {


        /*TVConsumptionTask tvConsuptionTask = new TVConsumptionTask(COAP_ENDPOINT_ENERGY_TV, COAP_ENDPOINT_SWITCH_TV);
        FRIDGEConsumptionTask fridgeConsumptionTask = new FRIDGEConsumptionTask(COAP_ENDPOINT_ENERGY_FRIDGE);
        LIGHTSConsumptionTask lightsConsumptionTask = new LIGHTSConsumptionTask(COAP_ENDPOINT_ENERGY_LIGHTS, COAP_ENDPOINT_SWITCH_LIGHTS);

        WASHERConsumptionTask washerConsumptionTask = new WASHERConsumptionTask(COAP_ENDPOINT_ENERGY_WASHER, COAP_ENDPOINT_SWITCH_WASHER);
        MOVEMENTdetenctionTask movemenTdetenctionTask = new MOVEMENTdetenctionTask(COAP_ENDPOINT_MOVEMENT_SENSOR);
        THERMOSTATMonitoringTask thermostatMonitoringTask = new THERMOSTATMonitoringTask(COAP_ENDPOINT_ENERGY_THERMOSTAT,
                COAP_ENDPOINT_SWITCH_THERMOSTAT,
                COAP_ENDPOINT_TEMPERATURE_THERMOSTAT);*/


        //print TimeStamp


        /*
        //create new Task for Energy Consumption
        Thread t1 = new Thread(fridgeConsumptionTask);
        t1.setName("THREAD FRIDGE");

        Thread t2 = new Thread(lightsConsumptionTask);
        t1.setName("THREAD LIGHTS");

        Thread t3 = new Thread(tvConsuptionTask);

        t1.setName("THREAD TV");
        Thread t4 = new Thread(washerConsumptionTask);

        t1.setName("THREAD WASHER");
        Thread t5 = new Thread(movemenTdetenctionTask);
        t5.setPriority(Thread.MAX_PRIORITY);
        t1.setName("THREAD MOVEMENT");

        Thread t6 = new Thread(thermostatMonitoringTask);
        t1.setName("THREAD THERMOSTAT");

        //start periodic task to check ecomode
        Thread periodicTask = new Thread(PeriodicTask);
        periodicTask.setName("THREAD PERIODICTASK");
        periodicTask.setPriority(Thread.MAX_PRIORITY);


        //start thread for observable resource energy
        t1.setPriority(Thread.MAX_PRIORITY);
        t1.start();
        t2.setPriority(Thread.MAX_PRIORITY);
        t2.start();
        t3.setPriority(Thread.MAX_PRIORITY);
        t3.start();
        t4.setPriority(Thread.MAX_PRIORITY);
        t4.start();
        t5.setPriority(Thread.MAX_PRIORITY);
        t5.start();
        t6.setPriority(Thread.MAX_PRIORITY);
        t6.start();


        t1.join();
        t2.join();
        t3.join();
        t4.join();
        t5.join();
        t6.join();*/
        //start periodic task to check ecomode

        createNewCoapClientObserving(COAP_ENDPOINT_ENERGY_LIGHTS, COAP_ENDPOINT_SWITCH_LIGHTS, "lights");
        createNewCoapClientObserving(COAP_ENDPOINT_ENERGY_TV, COAP_ENDPOINT_SWITCH_TV, "tv");
        createNewCoapClientObserving(COAP_ENDPOINT_ENERGY_WASHER, COAP_ENDPOINT_SWITCH_WASHER, "washer");
        createNewCoapClientObserving(COAP_ENDPOINT_MOVEMENT_SENSOR, "movement sensor");
        createNewCoapClientObserving(COAP_ENDPOINT_TEMPERATURE_THERMOSTAT, COAP_ENDPOINT_SWITCH_THERMOSTAT, "temperature");


    }


    public boolean isEcoMode() {
        return EcoMode;
    }

    private void printTotalConsumptionfromAll(String day, LIGHTSConsumptionTask lights, FRIDGEConsumptionTask fridge, TVConsumptionTask tv, WASHERConsumptionTask washer) {
        System.out.println("Daily consumption for the day : " + day + " is : ");
        System.out.println("\nfor fridge: " + fridge.Consuption + " W");
        System.out.println("\nfor tv: " + tv.Consuption + " W");
        System.out.println("\nfor lights: " + lights.Consuption + " W");
        System.out.println("\nfor washer: " + washer.Consuption + " W");
        TotalCostEuros(lights.Consuption + tv.Consuption + fridge.Consuption + washer.Consuption);
        lights.Consuption = 0.0;
        fridge.Consuption = 0.0;
        tv.Consuption = 0.0;
        washer.Consuption = 0.0;

    }
    private void printTotalConsumptionfromAll(String day){
        System.out.println("Daily consumption for the day : " + day + " is : "+Consumption);
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

        ControlUnit controlUnit=new ControlUnit();
        simTime.setSpeed(1); //or 1000 speed, if we want to check total daily consumption
        simTime.start();



        String day = simTime.getDay().toString();

        while (true) {
            //control if day is different
            String Datedetails = createStringDate(simTime); // create a string of timestamp
            System.out.println(Datedetails);
            if (!day.equals(simTime.getDay().toString())) {

                //printTotalConsumptionfromAll(day, lightsConsumptionTask, fridgeConsumptionTask, tvConsuptionTask, washerConsumptionTask); //print total consumption
                controlUnit.printTotalConsumptionfromAll(day);
            }

            try {
                if (!controlUnit.isEcoMode()) { //if ecomode is off
                     //check if it's time to turn ecomode on
                    if (checkEcoMode(simTime)) { // if Ecomode is true, put request to turn all switches off
                        System.err.println("HOUR > "+simTime.getHour());
                        settingEcomodeON();
                    }
                    else{
                        System.err.println("Ecomode just set");
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            day = simTime.getDay().toString(); //day of the week
            try {
                Thread.sleep(5000);
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

    public static int turnOnSwitchCondition(double instantConsumption, String URLforPost, int count, String URLenergy) throws InterruptedException {
        if (instantConsumption == 0.0) {
            count++;
        }
        if (count == 5) { //if energy consumption of device is for 5 times 0.0 W ==> turn its switch on
            Thread t = new Thread(() -> System.err.println("\n \n5 value '0.0 W' caught from " +
                    URLenergy + " , now POST request to turn the switch on **SIMULATION"));
            t.start();
            t.join(300);
            count = 0;

            new Thread(() -> new POSTClient(URLforPost)).start();
        }
        return count;
    }

    public static void settingEcomodeON() throws InterruptedException {


        System.err.println("ECOMODE IS TRUE: PUT REQUESTS FOR EACH DEVICE");

        Thread t1 = new Thread(() -> new PUTClient(COAP_ENDPOINT_SWITCH_LIGHTS, String.valueOf(false)));

        Thread t2 = new Thread(() -> new PUTClient(COAP_ENDPOINT_SWITCH_TV, String.valueOf(false)));
        Thread t3 = new Thread(() -> new PUTClient(COAP_ENDPOINT_SWITCH_WASHER, String.valueOf(false)));
        t1.start();
        t2.start();
        t3.start();
        t1.join(500);
        t2.join(500);
        t3.join(500);


    }

    public static void disablingEcomode() throws InterruptedException {
        System.err.println("ECOMODE IS FALSE : PUT REQUESTS FOR EACH DEVICE");
        Thread t1 = new Thread(() -> new PUTClient(COAP_ENDPOINT_SWITCH_LIGHTS, String.valueOf(true)));

        Thread t2 = new Thread(() -> new PUTClient(COAP_ENDPOINT_SWITCH_TV, String.valueOf(true)));
        Thread t3 = new Thread(() -> new PUTClient(COAP_ENDPOINT_SWITCH_WASHER, String.valueOf(true)));
        t1.start();
        t2.start();
        t3.start();
        t1.join(500);
        t2.join(500);
        t3.join(500);
    }

    private void TotalCostEuros(Double TotalConsumption) {
        System.out.println("Cost of the day is: " + (TotalConsumption * 0.06256) / 1000 + " euros");
    }

    private void createNewCoapClientObserving(String URLenergy, String URLswitch, String Who) {
        CoapClient client = new CoapClient(URLenergy);
        System.out.println("OBSERVING " + Who + "... @ " + URLenergy);
        //logger.info("OBSERVING LIGHTS... {}", URLenergy);

        Request request = new Request(CoAP.Code.GET);
        request.setOptions(new OptionSet().setAccept(MediaTypeRegistry.APPLICATION_SENML_JSON));
        request.setObserve();
        request.setConfirmable(true);
        int count=0;

        CoapObserveRelation relation = client.observe(request, new CoapHandler() {

            public void onLoad(CoapResponse response) {
                logger.info("Response Pretty Print: \n{}", Utils.prettyPrint(response));

                //The "CoapResponse" message contains the response.
                String text = response.getResponseText();
                logger.info("Payload: {}", text);
                logger.info("Message ID: " + response.advanced().getMID());
                logger.info("Token: " + response.advanced().getTokenString());
                logger.info("FROM : "+URLenergy);

                String[] ValuesSring = text.split(",");
                String value = ValuesSring[3].split(":")[1];

                if(URLenergy.equals(COAP_ENDPOINT_TEMPERATURE_THERMOSTAT) && ValuesSring.length>4){

                    double temperaturecaught=Double.parseDouble(value);
                    System.out.println("TEMPERATURE?S HOME IS : "+temperaturecaught);
                    if (temperaturecaught > 25.0) {
                        System.err.println("\n\nTemperature out of range, TOO HIGH TEMPERATURE!!! ==> TURN SWITCH OFF\n\n");
                        new Thread(() -> new POSTClient(URLswitch));
                    } else if (temperaturecaught < 21.5) {
                        System.err.println("\n\nTemperature out of range, TOO LOW TEMPERATURE!!!==> TURN SWITCH ON\n\n");
                        new Thread(() -> new POSTClient(URLswitch));
                    }
                }
                else{
                    double InstantConsumption = Double.parseDouble(value);
                    Consumption += InstantConsumption;

                    System.out.println("\n\nTotal Consumption: " + Consumption + " W");
                    System.out.println("Instant Consumption " + Who + " : " + InstantConsumption + " W\n\n");


                    Runnable runnable = () -> {


                        new Thread(() -> ControlUnit.Notificationconsumption(Who)).start();

                        new Thread(() -> new POSTClient(URLswitch)).start();


                    };
                    if (ControlUnit.checkConsumption(InstantConsumption, Who)) {
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
    private void createNewCoapClientObserving(String URL, String Who) {
        CoapClient client = new CoapClient(URL);
        System.out.println("OBSERVING " + Who + "... @ " + URL);
        //logger.info("OBSERVING LIGHTS... {}", URLenergy);

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
                logger.info("FROM "+URL);

                String[] ValuesSring = text.split(",");
                String value = ValuesSring[2].split(":")[1];

                if (value.equals("false")) {
                    try {
                        if(!isEcoMode()){
                            ControlUnit.settingEcomodeON();
                            EcoMode=true;

                        }

                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                } else {
                    try {
                        if(isEcoMode()){
                            ControlUnit.disablingEcomode();
                            EcoMode=false;
                        }

                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                /*try {
                    count = ControlUnit.turnOnSwitchCondition(InstantConsumption, URLswitch, count, URLenergy); //turn on the switch if lights are off for too much time
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }*/


            }

            public void onError() {
                System.err.println("OBSERVING" + Who + " FAILED");
                //logger.error("OBSERVING LIGHTS FAILED");
            }
        });
    }

}

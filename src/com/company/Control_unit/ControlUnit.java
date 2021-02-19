package com.company.Control_unit;


import com.company.Control_unit.ClientsType.POSTClient;
import com.company.Control_unit.ClientsType.PUTClient;
import com.company.Control_unit.UtilsTime.SimTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author Filippo Casari
 * @version final
 * @ControlUnit
 * @collaboration with: Luca Mantovani, Davide Casnici
 */


class ControlUnit {

    public final static Logger logger = LoggerFactory.getLogger(ControlUnit.class);

    private static final Double MAX_VALUE_WASHER = 97.5;
    private static final Double MAX_VALUE_LIGHTS = 4.75;
    private static final Double MAX_VALUE_TV = 57.5;


    private static final String COAP_ENDPOINT_ENERGY_LIGHTS = "coap://192.168.1.4:5683/lights/energy";
    public static final String COAP_ENDPOINT_SWITCH_LIGHTS = "coap://192.168.1.4:5683/lights/switch";
    private static final String COAP_ENDPOINT_SWITCH_TV = "coap://192.168.1.4:5683/TV/switch";
    public static final String COAP_ENDPOINT_ENERGY_TV = "coap://192.168.1.4:5683/TV/energy";
    private static final String COAP_ENDPOINT_ENERGY_WASHER = "coap://192.168.1.4:5683/washer/energy";
    private static final String COAP_ENDPOINT_SWITCH_WASHER = "coap://192.168.1.4:5683/washer/switch";
    //private static final String COAP_ENDPOINT_ENERGY_HEATING = "coap://127.0.0.1:5683/heating-system/energy";
    //private static final String COAP_ENDPOINT_SWITCH_FRIDGE = "coap://127.0.0.1:5683/fridge/switch";
    //private static final String COAP_ENDPOINT_SWITCH_HEATING = "coap://127.0.0.1:5683/heating-system/switch";
    private static final String COAP_ENDPOINT_ENERGY_FRIDGE = "coap://192.168.1.4:5683/fridge/energy";
    private static final String COAP_ENDPOINT_MOVEMENT_SENSOR = "coap://192.168.1.4:5683/detector/movement";

    private boolean EcoMode = false;
    private String Datedetails = null;


    public ControlUnit(SimTime simTime) throws InterruptedException {

        //Creation of Energy Consumption Monitoring Tasks
        TVConsumptionTask tvConsuptionTask = new TVConsumptionTask(COAP_ENDPOINT_ENERGY_TV, COAP_ENDPOINT_SWITCH_TV);
        FRIDGEConsumptionTask fridgeConsumptionTask = new FRIDGEConsumptionTask(COAP_ENDPOINT_ENERGY_FRIDGE);
        LIGHTSConsumptionTask lightsConsumptionTask = new LIGHTSConsumptionTask(COAP_ENDPOINT_ENERGY_LIGHTS, COAP_ENDPOINT_SWITCH_LIGHTS);
        //HEATINGConsumptionTask heatingConsumptionTask = new HEATINGConsumptionTask(COAP_ENDPOINT_ENERGY_HEATING, COAP_ENDPOINT_SWITCH_HEATING);
        WASHERConsumptionTask washerConsumptionTask = new WASHERConsumptionTask(COAP_ENDPOINT_ENERGY_WASHER, COAP_ENDPOINT_SWITCH_WASHER);
        MOVEMENTdetenctionTask movemenTdetenctionTask = new MOVEMENTdetenctionTask(COAP_ENDPOINT_MOVEMENT_SENSOR);

        simTime.setSpeed(1);
        simTime.start();


        Runnable PeriodicTask = () -> {
            String day = simTime.getDay().toString();

            while (true) {
                //control if day is different

                if (!day.equals(simTime.getDay().toString())) {
                    printTotalConsumptionfromAll(day, lightsConsumptionTask, fridgeConsumptionTask, tvConsuptionTask); //print total consumption
                }
                try {
                    if (!isEcoMode()) { //if ecomode is off
                        EcoMode = checkEcoMode(simTime); //check if is time to turn on ecomode
                        if (EcoMode) { // if Ecomode is true, put request to turn all switches off
                            settingEcomodeON(EcoMode);
                        }
                        System.out.println("Periodic Consumption Control...");
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                day = simTime.getDay().toString(); //check the day
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
        };
        Runnable printTimeStampTask = () -> {
            while (true) {
                Datedetails = createStringDate(simTime); // create a string of timestamp
                System.out.println(Datedetails);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        };
        Thread printTimeStamp = new Thread(printTimeStampTask);
        //print TimeStamp
        printTimeStamp.start();


        //create new Task for Energy Consumption
        Thread t1 = new Thread(fridgeConsumptionTask);
        t1.setPriority(6);
        Thread t2 = new Thread(lightsConsumptionTask);
        t2.setPriority(6);
        Thread t3 = new Thread(tvConsuptionTask);
        t3.setPriority(6);
        Thread t4 = new Thread(washerConsumptionTask);
        t4.setPriority(6);
        Thread t5 = new Thread(movemenTdetenctionTask);
        t5.setPriority(8);
        //start periodic task to check ecomode
        Thread periodicTask = new Thread(PeriodicTask);
        periodicTask.setPriority(3);

        //Thread t5 = new Thread(heatingConsumptionTask);

        //start thread for observable resource energy
        t1.start();
        t2.start();
        t3.start();
        t4.start();
        t5.start();
        periodicTask.start();
        t1.join(1000);
        t2.join(1000);
        t3.join(1000);
        t4.join(1000);
        t5.join(1000);

        periodicTask.join(1000);


    }

    public boolean isEcoMode() {
        return EcoMode;
    }

    private void printTotalConsumptionfromAll(String day, LIGHTSConsumptionTask lights, FRIDGEConsumptionTask fridge, TVConsumptionTask tv) {
        System.out.println("Daily consumption for the day : " + day + " is : ");
        System.out.println("for fridge: " + fridge.Consuption + " W");
        System.out.println("for tv: " + tv.Consuption + " W");
        System.out.println("for lights: " + lights.Consuption + " W");
        TotalCostEuros(lights.Consuption + tv.Consuption + fridge.Consuption);
        lights.Consuption = 0.0;
        fridge.Consuption = 0.0;
        tv.Consuption = 0.0;

    }

    private String createStringDate(SimTime simTime) {
        return simTime.getDay() + ", " + simTime.getHour() + " : " + simTime.getMinute();
    }


    public static void main(String[] args) throws InterruptedException {
        SimTime simTime = new SimTime();

        System.out.println("Starting Time...\nDay: " + simTime.getDay().toString());
        System.out.println("\tHour: " + simTime.getHour());
        System.out.println("\tMinute: " + simTime.getMinute());
        System.out.println("\tSecond: " + simTime.getSecond());

        new ControlUnit(simTime);


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

    public boolean checkEcoMode(SimTime simTime) throws InterruptedException {
        EcoMode = simTime.getDay().toString().equals("Sunday")
                || ((simTime.getHour() > 0 && simTime.getHour() < 5));
        return EcoMode;

    }

    public static void Notificationconsumption(String fromWho) {
        System.err.println("Too hight Consumption from " + fromWho + ": switch must be set off");
    }

    public static int turnOnSwitchCondition(double instantConsumption, String URLforPost, int count) {
        if (instantConsumption == 0.0) {
            count++;
        }
        if (count == 5) { //se ho piu' volte il fatto che sta consumando 0 kW, accendo lo switch
            new Thread(()->System.err.println("5 value '0.0 W' catch from " +
                    URLforPost + " , now POST request to turn the switch on **SIMULATION")).start();
            count = 0;

            new Thread(()->new POSTClient(URLforPost)).start();
        }
        return count;
    }

    public static void settingEcomodeON(boolean ecomode) throws InterruptedException {

        if (ecomode) {
            System.err.println("ECOMODE IS " + ecomode + ": PUT REQUESTS FOR EACH DEVICE");
            new Thread(() -> new PUTClient(COAP_ENDPOINT_SWITCH_LIGHTS, String.valueOf(false))).start();
            new Thread(() -> new PUTClient(COAP_ENDPOINT_SWITCH_TV, String.valueOf(false))).start();
            new Thread(() -> new PUTClient(COAP_ENDPOINT_SWITCH_WASHER, String.valueOf(false))).start();

        }


    }

    public static void disablingEcomode() throws InterruptedException {
        new Thread(()->System.err.println("ECOMODE IS FALSE : PUT REQUESTS FOR EACH DEVICE")).start();
        new Thread(() -> new PUTClient(COAP_ENDPOINT_SWITCH_LIGHTS, String.valueOf(true))).start();
        new Thread(() -> new PUTClient(COAP_ENDPOINT_SWITCH_TV, String.valueOf(true))).start();
        new Thread(() -> new PUTClient(COAP_ENDPOINT_SWITCH_WASHER, String.valueOf(true))).start();
    }

    private void TotalCostEuros(Double TotalConsumption) {
        System.out.println("Cost of the day is: " + (TotalConsumption * 0.06256) / 1000 + " euros");
    }
}

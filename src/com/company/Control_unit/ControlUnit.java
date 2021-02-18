package com.company.Control_unit;


import com.company.Control_unit.ClientsType.POSTClient;
import com.company.Control_unit.ClientsType.PUTClient;
import com.company.Control_unit.UtilsTime.SimTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class ControlUnit {

    public final static Logger logger = LoggerFactory.getLogger(ControlUnit.class);

    public boolean isEcoMode() {
        return EcoMode;
    }

    private static final String COAP_ENDPOINT_ENERGY_LIGHTS = "coap://127.0.0.1:5683/lights/energy";
    public static final String COAP_ENDPOINT_SWITCH_LIGHTS = "coap://127.0.0.1:5683/lights/switch";
    private static final String COAP_ENDPOINT_SWITCH_TV = "coap://127.0.0.1:5683/TV/switch";
    public static final String COAP_ENDPOINT_ENERGY_TV = "coap://127.0.0.1:5683/TV/energy";
    private static final String COAP_ENDPOINT_ENERGY_WASHER= "coap://127.0.0.1:5683/washer/energy";
    private static final String COAP_ENDPOINT_SWITCH_WASHER= "coap://127.0.0.1:5683/washer/switch";
    private static final String COAP_ENDPOINT_ENERGY_HEATING = "coap://127.0.0.1:5683/heating-system/energy";
    //private static final String COAP_ENDPOINT_SWITCH_FRIDGE = "coap://127.0.0.1:5683/fridge/switch";
    private static final String COAP_ENDPOINT_SWITCH_HEATING = "coap://127.0.0.1:5683/heating-system/switch";
    private static final String COAP_ENDPOINT_ENERGY_FRIDGE = "coap://127.0.0.1:5683/fridge/energy";
    private static final String COAP_ENDPOINT_MOVEMENT_SENSOR = "coap://127.0.0.1:5683/detector/movement";
    private boolean EcoMode = false;
    private String Datedetails = null;

    public ControlUnit(SimTime simTime) {

        //Creation of Energy Consumption Monitoring Tasks
        TVConsumptionTask tvConsuptionTask = new TVConsumptionTask(COAP_ENDPOINT_ENERGY_TV, COAP_ENDPOINT_SWITCH_TV);
        FRIDGEConsumptionTask fridgeConsumptionTask = new FRIDGEConsumptionTask(COAP_ENDPOINT_ENERGY_FRIDGE);
        LIGHTSConsumptionTask lightsConsumptionTask = new LIGHTSConsumptionTask(COAP_ENDPOINT_ENERGY_LIGHTS, COAP_ENDPOINT_SWITCH_LIGHTS);
        //HEATINGConsumptionTask heatingConsumptionTask = new HEATINGConsumptionTask(COAP_ENDPOINT_ENERGY_HEATING, COAP_ENDPOINT_SWITCH_HEATING);
        WASHERConsumptionTask washerConsumptionTask = new WASHERConsumptionTask(COAP_ENDPOINT_ENERGY_WASHER, COAP_ENDPOINT_SWITCH_WASHER);
        MOVEMENTdetenctionTask movemenTdetenctionTask =new MOVEMENTdetenctionTask(COAP_ENDPOINT_MOVEMENT_SENSOR);

        simTime.setSpeed(1000);
        simTime.start();


        Runnable PeriodicTask = () -> {
            String day = simTime.getDay().toString();

            while (true) {

                Datedetails = createStringDate(simTime);
                System.out.println(Datedetails);

                if (!day.equals(simTime.getDay().toString())) {
                    printTotalConsumptionfromAll(day, lightsConsumptionTask, fridgeConsumptionTask, tvConsuptionTask);
                }
                if (checkEcoMode(simTime)) {
                    System.out.println("Periodic Consumption Control...");

                }
                day = simTime.getDay().toString();
                try {
                    Thread.sleep(4000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
        };


        //create new Task for Energy Consumption
        Thread t1 = new Thread(fridgeConsumptionTask);
        Thread t2 = new Thread(lightsConsumptionTask);
        Thread t3 = new Thread(tvConsuptionTask);
        Thread t4 = new Thread(washerConsumptionTask);
        Thread t5 = new Thread(movemenTdetenctionTask);
        //Thread t5 = new Thread(heatingConsumptionTask);
        //start thread for observable resource energy
        t1.start();
        t2.start();
        t3.start();
        t4.start();
        //t5.start();
        //start periodic task to check ecomode
        Thread periodicTask = new Thread(PeriodicTask);
        periodicTask.start();


    }

    private void printTotalConsumptionfromAll(String day, LIGHTSConsumptionTask lights, FRIDGEConsumptionTask fridge, TVConsumptionTask tv) {
        System.out.println("Daily consumption for the day : " + day + " is : ");
        System.out.println("for fridge: " + fridge.Consuption+" kW");
        System.out.println("for tv: " + tv.Consuption+ " kW");
        System.out.println("for lights: " + lights.Consuption+" kW");
        TotalCostEuros(lights.Consuption+tv.Consuption+fridge.Consuption);
        lights.Consuption = 0.0;
        fridge.Consuption = 0.0;
        tv.Consuption = 0.0;

    }

    private String createStringDate(SimTime simTime) {
        return simTime.getDay() + ", " + simTime.getHour() + " : " + simTime.getMinute();
    }


    public static void main(String[] args) {
        SimTime simTime = new SimTime();

        System.out.println("Starting Time...\nDay: " + simTime.getDay().toString());
        System.out.println("\tHour: " + simTime.getHour());
        System.out.println("\tMinute: " + simTime.getMinute());
        System.out.println("\tSecond: " + simTime.getSecond());

        ControlUnit controUnit = new ControlUnit(simTime);
        simTime.hasChanged();


    }

    public static boolean checkConsumption(Double TotalConsumption, Double InstantConsumption) {
        return TotalConsumption > 50 || InstantConsumption > 2.0;

    }

    public boolean checkEcoMode(SimTime simTime) {
        EcoMode = simTime.getDay().toString().equals("Sunday")
                || ((simTime.getHour() > 23 || simTime.getHour() < 5));
        logger.info("Eco Mode: " + EcoMode);

        settingEcomode(EcoMode);

        return EcoMode;

    }

    public static void Notificationconsumption(String fromWho) {
        logger.info("Too hight Consumption from " + fromWho + ": switch must be set off");
    }
    public static int turnOnSwitchCondition(double instantConsumption, String URLforPost, int count) {
        if (instantConsumption == 0.0) {
            count++;
        }
        if (count == 5) { //se ho piu' volte il fatto che sta consumando 0 kW, accendo lo switch
            count=0;
            new Thread(() -> new POSTClient(URLforPost)).start();
        }
        return count;
    }
    public static void settingEcomode(boolean ecomode){


        new Thread(() -> new PUTClient(COAP_ENDPOINT_SWITCH_LIGHTS, String.valueOf(!ecomode))).start();
        new Thread(() -> new PUTClient(COAP_ENDPOINT_SWITCH_TV, String.valueOf(!ecomode))).start();
        new Thread(() -> new PUTClient(COAP_ENDPOINT_SWITCH_WASHER,String.valueOf(!ecomode))).start();


        System.err.println("ECOMODE IS"+ecomode+": POST REQUESTS FOR EACH DEVICE");
    }
    private void TotalCostEuros(Double TotalConsumption){
        System.out.println("Cost of the day is: "+TotalConsumption*0.06256+" euros");
    }
}

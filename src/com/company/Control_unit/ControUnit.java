package com.company.Control_unit;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


class ControlUnit {
    private final static Logger logger = LoggerFactory.getLogger(ControlUnit.class);

    private static final String COAP_ENDPOINT_ENERGY_LIGHTS = "coap://127.0.0.1:5683/lights/energy";
    private static final String COAP_ENDPOINT_SWITCH_LIGHTS = "coap://127.0.0.1:5683/lights/switch";

    private static final String COAP_ENDPOINT_ENERGY_TV = "coap://127.0.0.1:5683/TV/energy";
    private static final String COAP_ENDPOINT_SWITCH_TV = "coap://127.0.0.1:5683/TV/switch";

    //TODO HEATING - FRIDGE - MOVEMENT
    private static final String COAP_ENDPOINT_ENERGY_HEATING = "coap://192.168.0.169:5683/heating-system/energy";
    private static final String COAP_ENDPOINT_ENERGY_FRIDGE = "coap://127.0.0.1:5683/fridge/energy";

    public ControlUnit() {
        LIGHTSConsumptionTask lightsConsumptionTask = new LIGHTSConsumptionTask(COAP_ENDPOINT_ENERGY_LIGHTS, COAP_ENDPOINT_SWITCH_LIGHTS);
        //TVConsumptionTask tvConsuptionTask = new TVConsumptionTask(COAP_ENDPOINT_ENERGY_TV, COAP_ENDPOINT_SWITCH_TV);
        //FRIDGEConsumptionTask fridgeConsumptionTask = new FRIDGEConsumptionTask(COAP_ENDPOINT_ENERGY_FRIDGE);

        //HEATINGConsumptionTask heatingConsumptionTask = new HEATINGConsumptionTask(COAP_ENDPOINT_ENERGY_HEATING);
        //fridgeConsumptionTask.run();
        //heatingConsumptionTask.run();
        lightsConsumptionTask.start();
        //tvConsuptionTask.run();
    }


    public static void main(String[] args) {
        ControlUnit controlUnit = new ControlUnit();
    }

    public static boolean checkConsumption(Double TotalConsumption, Double InstantConsumption){
        return TotalConsumption > 50 || InstantConsumption>2.0;
    }


}

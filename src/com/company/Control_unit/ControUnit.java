package com.company.Control_unit;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


class ControlUnit {
    private final static Logger logger = LoggerFactory.getLogger(ControlUnit.class);

    private static final String COAP_ENDPOINT_ENERGY_LIGHTS = "coap://127.0.0.1/living-room/switch";
    private static final String COAP_ENDPOINT_ENERGY_TV = "coap://192.168.0.169:5683/hello-world";//"coap://127.0.0.1:5683/TV/energy"
    private static final String COAP_ENDPOINT_ENERGY_HEATING = "coap://192.168.0.169:5683/hello-world";
    private static final String COAP_ENDPOINT_ENERGY_FRIDGE = "coap://127.0.0.1/living-room/switch";
    public ControlUnit() {
        TVConsumptionTask tvConsuptionTask = new TVConsumptionTask(COAP_ENDPOINT_ENERGY_TV);
        FRIDGEConsumptionTask fridgeConsumptionTask = new FRIDGEConsumptionTask(COAP_ENDPOINT_ENERGY_FRIDGE);
        HEATINGConsumptionTask heatingConsumptionTask = new HEATINGConsumptionTask(COAP_ENDPOINT_ENERGY_HEATING);
        fridgeConsumptionTask.run();
        tvConsuptionTask.run();
        heatingConsumptionTask.run();
    }




    public static void main(String[] args) {
        ControlUnit controUnit = new ControlUnit();



    }


}

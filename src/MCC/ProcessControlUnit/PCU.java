package MCC.ProcessControlUnit;

import MCC.CoapResource.MovementResource;
import MCC.DataListener;
import MCC.EmulatedResource.Sensor.MovementSensor;
import MCC.SmartObject;
import MCC.CoapResource.EnergyResource;
import MCC.CoapResource.SwitchResource;
import MCC.CoapResource.TemperatureResource;
import MCC.EmulatedResource.Actuator.SwitchActuator;
import MCC.EmulatedResource.Sensor.EnergySensor;
import MCC.EmulatedResource.Sensor.TemperatureSensor;
import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.CoapServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;
import java.util.Random.*;
import java.util.UUID;
import java.math.*;

public class PCU extends CoapServer {
    private final static Logger logger = LoggerFactory.getLogger(PCU.class);
    private Random randomLights;
    private Random randomWasher = new Random();
    private Random randomTV;

    public PCU (){
        super();
        String deviceId = String.format("dipi:iot:%s", UUID.randomUUID().toString());
        this.add(createLightResource(deviceId));
        this.add(createFridgeResource(deviceId));
        this.add(createTvResource(deviceId));
        this.add(createWasherResource(deviceId));
        this.add(createDetectorResource(deviceId));
        this.add(createThermostatResource(deviceId));
    }

    private CoapResource createLightResource(String deviceId){

        CoapResource lightsRootResource = new CoapResource("lights");

        EnergySensor lightsEnergySensor = new EnergySensor(lightsRootResource.getName());
        SwitchActuator lightsSwitchActuator = new SwitchActuator();

        EnergyResource lightsEnergyResource = new EnergyResource(deviceId, "energy", lightsEnergySensor);
        SwitchResource lightsSwitchResource = new SwitchResource(deviceId, "switch", lightsSwitchActuator);

        if(!lightsSwitchResource.getOn()){
            lightsEnergyResource.setUpdatedEnergyValue(0.0);
        }else{
            lightsEnergySensor.setActive(true);
            //random = (Math.random() * ((3 - 1) + 1)) + 1;
            //lightsEnergyResource.setUpdatedEnergyValue(random);
        }

        lightsRootResource.add(lightsEnergyResource);
        lightsRootResource.add(lightsSwitchResource);

        //Handle Emulated Resource notification
        lightsSwitchActuator.addDataListener(new DataListener<Boolean>() {
            @Override
            public void onDataChanged(SmartObject<Boolean> resource, Boolean updatedValue) {
                logger.info("[LIGHTS-BEHAVIOUR] -> Updated Switch Value: {}", updatedValue);
                logger.info("[LIGHTS-BEHAVIOUR] -> Updating energy sensor configuration ...");
                lightsEnergySensor.setActive(updatedValue);
            }
        });

        lightsEnergySensor.addDataListener(new DataListener<Double>() {
            @Override
            public void onDataChanged(SmartObject<Double> resource, Double updatedValue) {
                if(resource != null && updatedValue != null)
                    logger.info("Device: {} \tNew Value Received: {}", resource.getId(), updatedValue);
                else
                    logger.error("onDataChanged Callback -> Null Resource or Updated Value !");
            }
        });

        return lightsRootResource;
    }

    private CoapResource createFridgeResource(String deviceId){
        CoapResource fridgeRootResource = new CoapResource("fridge");

        EnergySensor fridgeEnergySensor = new EnergySensor(fridgeRootResource.getName());
        TemperatureSensor fridgeTemperatureSensor = new TemperatureSensor(fridgeRootResource.getName());

        EnergyResource fridgeEnergyResource = new EnergyResource(deviceId, "energy", fridgeEnergySensor);
        TemperatureResource fridgeTemperatureResource = new TemperatureResource(deviceId, "temperature", fridgeTemperatureSensor);

        fridgeRootResource.add(fridgeEnergyResource);
        fridgeRootResource.add(fridgeTemperatureResource);

        fridgeEnergySensor.addDataListener(new DataListener<Double>() {
            @Override
            public void onDataChanged(SmartObject<Double> resource, Double updatedValue) {
                if(resource != null && updatedValue != null)
                    logger.info("Device: {} \tNew Value Received: {}", resource.getId(), updatedValue);
                else
                    logger.error("onDataChanged Callback -> Null Resource or Updated Value !");
            }
        });

        return fridgeRootResource;
    }

    private CoapResource createTvResource (String deviceId){
        CoapResource tvRootResource = new CoapResource("TV");

        EnergySensor tvEnergySensor = new EnergySensor(tvRootResource.getName());
        SwitchActuator tvSwitchActuator = new SwitchActuator();

        EnergyResource tvEnergyResource = new EnergyResource(deviceId, "energy", tvEnergySensor);
        SwitchResource tvSwitchResource = new SwitchResource(deviceId, "switch", tvSwitchActuator);

        if(!tvSwitchResource.getOn()){
            tvEnergyResource.setUpdatedEnergyValue(0.0);
        }else{
            tvEnergySensor.setActive(true);
            //random = (Math.random() * ((55 - 50) + 1)) + 50;
            //tvEnergyResource.setUpdatedEnergyValue(random);
        }

        tvRootResource.add(tvEnergyResource);
        tvRootResource.add(tvSwitchResource);

        tvSwitchActuator.addDataListener(new DataListener<Boolean>() {
            @Override
            public void onDataChanged(SmartObject<Boolean> resource, Boolean updatedValue) {
                logger.info("[TV-BEHAVIOUR] -> Updated Switch Value: {}", updatedValue);
                logger.info("[TV-BEHAVIOUR] -> Updating energy sensor configuration ...");
                tvEnergySensor.setActive(updatedValue);
            }
        });

        tvEnergySensor.addDataListener(new DataListener<Double>() {
            @Override
            public void onDataChanged(SmartObject<Double> resource, Double updatedValue) {
                if(resource != null && updatedValue != null)
                    logger.info("Device: {} \tNew Value Received: {}", resource.getId(), updatedValue);
                else
                    logger.error("onDataChanged Callback -> Null Resource or Updated Value !");
            }
        });

        return tvRootResource;
    }

    private CoapResource createWasherResource(String deviceId){
        CoapResource washerRootResource = new CoapResource("washer");

        EnergySensor washerEnergySensor = new EnergySensor(washerRootResource.getName());
        SwitchActuator washerSwitchActuator = new SwitchActuator();

        EnergyResource washerEnergyResource = new EnergyResource(deviceId, "energy", washerEnergySensor);
        SwitchResource washerSwitchResource = new SwitchResource(deviceId, "switch", washerSwitchActuator);

        washerRootResource.add(washerEnergyResource);
        washerRootResource.add(washerSwitchResource);

        washerSwitchActuator.addDataListener(new DataListener<Boolean>() {
            @Override
            public void onDataChanged(SmartObject<Boolean> resource, Boolean updatedValue) {
                logger.info("[WASHER-BEHAVIOUR] -> Updated Switch Value: {}", updatedValue);
                logger.info("[WASHER-BEHAVIOUR] -> Updating energy sensor configuration ...");
                if(!washerSwitchActuator.getActive()){
                    washerEnergySensor.setActive(updatedValue);
                    washerEnergyResource.setConsumptionNull();
                }else{
                    washerEnergySensor.setActive(updatedValue);
                    washerEnergySensor.setUpdatedValue(95.0);
                }
            }
        });

        return washerRootResource;
    }

    private CoapResource createDetectorResource (String deviceId){
        CoapResource detectorRootResource = new CoapResource("detector");

        MovementSensor detectorMovementSensor = new MovementSensor();
        MovementResource detectorMovementResource = new MovementResource(deviceId, "movement", detectorMovementSensor);

        detectorRootResource.add(detectorMovementResource);

        new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    for(int i=0; i<100; i++){
                        detectorMovementSensor.setActive(!detectorMovementSensor.loadUpdatedValue());
                        Thread.sleep(30000);
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }).start();

        detectorMovementSensor.addDataListener(new DataListener<Boolean>() {
            @Override
            public void onDataChanged(SmartObject<Boolean> resource, Boolean updatedValue) {
                logger.info("[MOVEMENT-BEHAVIOUR] -> Updated Movement Value: {}", updatedValue);
            }
        });

        return detectorRootResource;
    }

    private CoapResource createThermostatResource(String deviceId){
        CoapResource thermostatRootResource = new CoapResource("thermostat");

        EnergySensor thEnergySensor = new EnergySensor("thermostat");
        SwitchActuator thSwitchActuator = new SwitchActuator();
        TemperatureSensor thTemperatureSensor = new TemperatureSensor(thermostatRootResource.getName());

        EnergyResource thEnergyResource = new EnergyResource(deviceId, "energy", thEnergySensor);
        TemperatureResource thTemperatureResource = new TemperatureResource(deviceId, "temperature", thTemperatureSensor);
        SwitchResource thSwitchResource= new SwitchResource(deviceId, "switch", thSwitchActuator);

        thermostatRootResource.add(thEnergyResource);
        thermostatRootResource.add(thTemperatureResource);
        thermostatRootResource.add(thSwitchResource);

        return thermostatRootResource;
    }

    public static void main(String[] args){
        PCU pcu = new PCU();
        pcu.start();
        logger.info("Coap Server Started! Available resources: ");

        pcu.getRoot().getChildren().stream().forEach(resource -> {
            logger.info("Resource {} -> URI: {} (Observable: {})", resource.getName(), resource.getURI(), resource.isObservable());
            if(!resource.getURI().equals("/.well-known")){
                resource.getChildren().stream().forEach(childResource -> {
                    logger.info("\t Resource {} -> URI: {} (Observable: {})", childResource.getName(), childResource.getURI(), childResource.isObservable());
                });
            }
        });
    }
}
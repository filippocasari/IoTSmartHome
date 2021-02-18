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

import java.util.UUID;

public class PCU extends CoapServer {
    private final static Logger logger = LoggerFactory.getLogger(PCU.class);

    public PCU (){
        super();
        String deviceId = String.format("dipi:iot:%s", UUID.randomUUID().toString());
        this.add(createLightResource(deviceId));
        this.add(createFridgeResource(deviceId));
        this.add(createTvResource(deviceId));
        this.add(createWasherResource(deviceId));
        this.add(createDetectorResource(deviceId));
    }

    private CoapResource createLightResource(String deviceId){

        CoapResource lightsRootResource = new CoapResource("lights");

        EnergySensor lightsEnergySensor = new EnergySensor();
        SwitchActuator lightsSwitchActuator = new SwitchActuator();

        EnergyResource lightsEnergyResource = new EnergyResource(deviceId, "energy", lightsEnergySensor);
        SwitchResource lightsSwitchResource = new SwitchResource(deviceId, "switch", lightsSwitchActuator);
        if(!lightsSwitchResource.getOn()){
            lightsEnergyResource.setUpdatedEnergyValue(0.0);

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

        return lightsRootResource;
    }

    private CoapResource createFridgeResource(String deviceId){
        CoapResource fridgeRootResource = new CoapResource("fridge");

        EnergySensor fridgeEnergySensor = new EnergySensor();
        TemperatureSensor fridgeTemperatureSensor = new TemperatureSensor(fridgeRootResource.getName());

        EnergyResource fridgeEnergyResource = new EnergyResource(deviceId, "energy", fridgeEnergySensor);
        TemperatureResource fridgeTemperatureResource = new TemperatureResource(deviceId, "temperature", fridgeTemperatureSensor);

        fridgeRootResource.add(fridgeEnergyResource);
        fridgeRootResource.add(fridgeTemperatureResource);

        return fridgeRootResource;
    }

    private CoapResource createTvResource (String deviceId){
        CoapResource tvRootResource = new CoapResource("TV");

        EnergySensor tvEnergySensor = new EnergySensor();
        SwitchActuator tvSwitchActuator = new SwitchActuator();

        EnergyResource tvEnergyResource = new EnergyResource(deviceId, "energy", tvEnergySensor);
        SwitchResource tvSwitchResource = new SwitchResource(deviceId, "switch", tvSwitchActuator);

        if(!tvSwitchResource.getOn()){
            tvEnergyResource.setUpdatedEnergyValue(0.0);

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
        return tvRootResource;
    }

    private CoapResource createWasherResource(String deviceId){
        CoapResource washerRootResource = new CoapResource("washer");

        EnergySensor washerEnergySensor = new EnergySensor();
        SwitchActuator washerSwitchActuator = new SwitchActuator();

        EnergyResource washerEnergyResource = new EnergyResource(deviceId, "energy", washerEnergySensor);
        SwitchResource washerSwitchResource = new SwitchResource(deviceId, "switch", washerSwitchActuator);

        if(!washerSwitchResource.getOn()){
            washerEnergyResource.setUpdatedEnergyValue(0.0);

        }

        washerRootResource.add(washerEnergyResource);
        washerRootResource.add(washerSwitchResource);

        washerSwitchActuator.addDataListener(new DataListener<Boolean>() {
            @Override
            public void onDataChanged(SmartObject<Boolean> resource, Boolean updatedValue) {
                logger.info("[WASHER-BEHAVIOUR] -> Updated Switch Value: {}", updatedValue);
                logger.info("[WASHER-BEHAVIOUR] -> Updating energy sensor configuration ...");
                washerEnergySensor.setActive(updatedValue);
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
                        Thread.sleep(4000);
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }).start();

        return detectorRootResource;
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
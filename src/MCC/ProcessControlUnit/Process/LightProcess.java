package MCC.ProcessControlUnit.Process;

import MCC.DataListener;
import MCC.ProcessControlUnit.PCU;
import MCC.SmartObject;
import MCC.CoapResource.EnergyResource;
import MCC.CoapResource.SwitchResource;
import MCC.EmulatedResource.Actuator.SwitchActuator;
import MCC.EmulatedResource.Sensor.EnergySensor;
import org.eclipse.californium.core.CoapServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

public class LightProcess extends CoapServer {
    private final static Logger logger = LoggerFactory.getLogger(LightProcess.class);

    public LightProcess() {
        super();
        String deviceId = String.format("dipi:iot:%s", UUID.randomUUID().toString());
        EnergySensor lightsEnergySensor = new EnergySensor("lights");
        SwitchActuator lightsSwitchActuator = new SwitchActuator();

        EnergyResource lightsEnergyResource = new EnergyResource(deviceId, "energy", lightsEnergySensor);
        SwitchResource lightsSwitchResource = new SwitchResource(deviceId, "switch", lightsSwitchActuator);
        if(!lightsSwitchResource.getOn())
            lightsEnergyResource.setUpdatedEnergyValue(0.0);


        this.add(lightsEnergyResource);
        this.add(lightsSwitchResource);

        //Handle Emulated Resource notification
        lightsSwitchActuator.addDataListener(new DataListener<Boolean>() {
            @Override
            public void onDataChanged(SmartObject<Boolean> resource, Boolean updatedValue) {
                logger.info("[LIGHTS-BEHAVIOUR] -> Updated Switch Value: {}", updatedValue);
                logger.info("[LIGHTS-BEHAVIOUR] -> Updating energy sensor configuration ...");
                lightsEnergySensor.setActive(updatedValue);
            }
        });

        this.add(lightsEnergyResource);
        this.add(lightsSwitchResource);
    }

    public static void main(String[] args){
        LightProcess process = new LightProcess();
        process.start();
        logger.info("Coap Server Started! Available resources: ");

        process.getRoot().getChildren().stream().forEach(resource -> {
            logger.info("Resource {} -> URI: {} (Observable: {})", resource.getName(), resource.getURI(), resource.isObservable());
            if(!resource.getURI().equals("/.well-known")){
                resource.getChildren().stream().forEach(childResource -> {
                    logger.info("\t Resource {} -> URI: {} (Observable: {})", childResource.getName(), childResource.getURI(), childResource.isObservable());
                });
            }
        });
    }
}

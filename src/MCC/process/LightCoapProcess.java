package MCC.process;

import MCC.DataListener;
import MCC.SmartObject;
import MCC.coap.CoapEnergyConsumptionResource;
import MCC.coap.CoapSwitchActuatorResource;
import MCC.resource.actuator.SwitchActuator;
import MCC.resource.sensor.EnergySensor;
import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.CoapServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

public class LightCoapProcess extends CoapServer {
    private final static Logger logger = LoggerFactory.getLogger(LightCoapProcess.class);

    public LightCoapProcess() {
        super();
        String deviceId = String.format("dipi:iot:%s", UUID.randomUUID().toString());
        this.add(createLightResource(deviceId));
    }

    private CoapResource createLightResource(String deviceId){

        CoapResource compressorRootResource = new CoapResource("light");

        //INIT Emulated Physical Sensors and Actuators
        EnergySensor compressorEnergyRawSensor = new EnergySensor();
        SwitchActuator compressorSwitchRawActuator = new SwitchActuator();

        //Resource
        CoapEnergyConsumptionResource compressorEnergyResource = new CoapEnergyConsumptionResource(deviceId, "energy", compressorEnergyRawSensor);
        CoapSwitchActuatorResource compressorSwitchResource = new CoapSwitchActuatorResource(deviceId, "switch", compressorSwitchRawActuator);

        compressorRootResource.add(compressorEnergyResource);
        compressorRootResource.add(compressorSwitchResource);

        //Handle Emulated Resource notification
        compressorSwitchRawActuator.addDataListener(new DataListener<Boolean>() {
            @Override
            public void onDataChanged(SmartObject<Boolean> resource, Boolean updatedValue) {
                logger.info("[LIGHTS-BEHAVIOUR] -> Updated Switch Value: {}", updatedValue);
                logger.info("[LIGHTS-BEHAVIOUR] -> Updating energy sensor configuration ...");
                compressorEnergyRawSensor.setActive(updatedValue);
            }
        });
        return compressorRootResource;
    }

    public static void main(String[] args) {

        LightCoapProcess lightCoapProcess = new LightCoapProcess();
        lightCoapProcess.start();

        logger.info("Coap Server Started ! Available resources: ");

        lightCoapProcess.getRoot().getChildren().stream().forEach(resource -> {
            logger.info("Resource {} -> URI: {} (Observable: {})", resource.getName(), resource.getURI(), resource.isObservable());
            if(!resource.getURI().equals("/.well-known")){
                resource.getChildren().stream().forEach(childResource -> {
                    logger.info("\t Resource {} -> URI: {} (Observable: {})", childResource.getName(), childResource.getURI(), childResource.isObservable());
                });
            }
        });

    }
}

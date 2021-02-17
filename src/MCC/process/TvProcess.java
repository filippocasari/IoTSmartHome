package MCC.process;

import MCC.DataListener;
import MCC.SmartObject;
import MCC.coap.EnergyResource;
import MCC.coap.SwitchResource;
import MCC.resource.actuator.SwitchActuator;
import MCC.resource.sensor.EnergySensor;
import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.CoapServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

public class TvProcess extends CoapServer {
    private final static Logger logger = LoggerFactory.getLogger(TvProcess.class);
    public TvProcess() {
        super();
        String deviceId = String.format("dipi:iot:%s", UUID.randomUUID().toString());
        this.add(createTvResource(deviceId));
    }

    private CoapResource createTvResource (String deviceId){
        CoapResource tvRootResource = new CoapResource("TV");

        EnergySensor tvEnergySensor = new EnergySensor();
        SwitchActuator tvSwitchActuator = new SwitchActuator();

        EnergyResource lightsEnergyResource = new EnergyResource(deviceId, "energy", tvEnergySensor);
        SwitchResource lightsSwitchResource = new SwitchResource(deviceId, "switch", tvSwitchActuator);

        if(!lightsSwitchResource.getOn()){
            lightsEnergyResource.setUpdatedEnergyValue(0.0);

        }

        tvRootResource.add(lightsEnergyResource);
        tvRootResource.add(lightsSwitchResource);

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

    public static void main(String[] args) {

        TvProcess tvCoapProcess = new TvProcess();
        tvCoapProcess.start();

        logger.info("Coap Server Started! Available resources: ");

        tvCoapProcess.getRoot().getChildren().stream().forEach(resource -> {
            logger.info("Resource {} -> URI: {} (Observable: {})", resource.getName(), resource.getURI(), resource.isObservable());
            if(!resource.getURI().equals("/.well-known")){
                resource.getChildren().stream().forEach(childResource -> {
                    logger.info("\t Resource {} -> URI: {} (Observable: {})", childResource.getName(), childResource.getURI(), childResource.isObservable());
                });
            }
        });

    }

}

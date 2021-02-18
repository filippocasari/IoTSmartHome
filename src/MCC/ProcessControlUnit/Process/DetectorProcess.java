package MCC.ProcessControlUnit.Process;

import MCC.CoapResource.EnergyResource;
import MCC.CoapResource.MovementResource;
import MCC.EmulatedResource.Sensor.EnergySensor;
import MCC.EmulatedResource.Sensor.MovementSensor;
import org.eclipse.californium.core.CoapServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

public class DetectorProcess extends CoapServer {
    private final static Logger logger = LoggerFactory.getLogger(DetectorProcess.class);

    public DetectorProcess() {
        super();
        String deviceId = String.format("dipi:iot:%s", UUID.randomUUID().toString());

        EnergySensor detectorEnergySensor = new EnergySensor();
        MovementSensor detectorMovementSensor = new MovementSensor();

        EnergyResource detectorEnergyResource = new EnergyResource(deviceId, "energy", detectorEnergySensor);
        MovementResource detectorMovementResource = new MovementResource(deviceId, "movement", detectorMovementSensor);

        this.add(detectorEnergyResource);
        this.add(detectorMovementResource);

        new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    for(int i=0; i<100; i++){
                        detectorMovementSensor.setActive(!detectorMovementSensor.loadUpdatedValue());
                        Thread.sleep(1000);
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }).start();

    }

    public static void main(String[] args) {

        DetectorProcess detectorProcess = new DetectorProcess();
        detectorProcess.start();

        logger.info("Coap Server Started! Available resources: ");

        detectorProcess.getRoot().getChildren().stream().forEach(resource -> {
            logger.info("Resource {} -> URI: {} (Observable: {})", resource.getName(), resource.getURI(), resource.isObservable());
            if(!resource.getURI().equals("/.well-known")){
                resource.getChildren().stream().forEach(childResource -> {
                    logger.info("\t Resource {} -> URI: {} (Observable: {})", childResource.getName(), childResource.getURI(), childResource.isObservable());
                });
            }
        });

    }
}

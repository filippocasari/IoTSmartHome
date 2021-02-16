package MCC.sensor;

import MCC.DataListener;
import MCC.SmartObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;


public class MovementSensor extends SmartObject<Boolean> {
    private static Logger logger = LoggerFactory.getLogger(MovementSensor.class);

    /** LABEL **/
    private static final String LOG_DISPLAY_NAME = "MovementSensor";
    private static final String RESOURCE_TYPE = "sensor.movement";

    private Boolean isActive;

    public MovementSensor() {
        super(UUID.randomUUID().toString(), RESOURCE_TYPE);
        this.isActive = true;
    }

    public Boolean getActive() {
        return isActive;
    }

    public void setActive(Boolean active) {
        isActive = active;
        notifyUpdate(isActive);
    }


    public static void main(String[] args) {

        MovementSensor rawResource = new MovementSensor();
        logger.info("New {} Resource Created with Id: {} ! {} New Value: {}",
                rawResource.getType(),
                rawResource.getId(),
                LOG_DISPLAY_NAME,
                rawResource.loadUpdatedValue());

        new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    for(int i=0; i<100; i++){
                        rawResource.setActive(!rawResource.loadUpdatedValue());
                        Thread.sleep(5000);
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }).start();

        rawResource.addDataListener(new DataListener<Boolean>() {
            @Override
            public void onDataChanged(SmartObject<Boolean> resource, Boolean updatedValue) {

                if(resource != null && updatedValue != null)
                    logger.info("Device: {} -> New Value Received: {}", resource.getId(), updatedValue);
                else
                    logger.error("onDataChanged Callback -> Null Resource or Updated Value !");
            }
        });

    }

    @Override
    public Boolean loadUpdatedValue() {
        return null;
    }
}

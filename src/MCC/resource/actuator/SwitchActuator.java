package MCC.resource.actuator;

import MCC.DataListener;
import MCC.SmartObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.UUID;

public class SwitchActuator extends SmartObject<Boolean> {
    /** LABEL **/
    private static Logger logger = LoggerFactory.getLogger(SwitchActuator.class);
    private static final String LOG_DISPLAY_NAME = "SwitchActuator";
    private static final String RESOURCE_TYPE = "actuator.switch";

    private Boolean isActive;

    public SwitchActuator() {
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

    @Override
    public Boolean loadUpdatedValue() {
        return this.isActive;
    }

    public static void main(String[] args) {

        SwitchActuator actuatorResource = new SwitchActuator();
        logger.info("New {} Resource Created with Id: {} ! {} New Value: {}",
                actuatorResource.getType(),
                actuatorResource.getId(),
                LOG_DISPLAY_NAME,
                actuatorResource.loadUpdatedValue());
    /*
        new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    for(int i=0; i<3; i++){
                        actuatorResource.setActive(!actuatorResource.loadUpdatedValue());
                        Thread.sleep(1000);
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }).start();
    */
        actuatorResource.addDataListener(new DataListener<Boolean>() {
            @Override
            public void onDataChanged(SmartObject <Boolean> resource, Boolean updatedValue) {

                if(resource != null && updatedValue != null)
                    logger.info("Device: {} -> New Value Received: {}", resource.getId(), updatedValue);
                else
                    logger.error("onDataChanged Callback -> Null Resource or Updated Value !");
            }
        });

    }

}
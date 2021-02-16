package MCC.resource.sensor;

import MCC.DataListener;
import MCC.SmartObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

public class TemperatureSensor extends SmartObject<Double> {
    private static Logger logger = LoggerFactory.getLogger(TemperatureSensor.class);

    /** TEMPERATURE RANGE VALUE & VARIATION **/
    private static final double MIN_TEMPERATURE_VALUE = 23.0;
    private static final double MAX_TEMPERATURE_VALUE = 30.0;
    private static final double MIN_TEMPERATURE_VARIATION = 0.1;
    private static final double MAX_TEMPERATURE_VARIATION = 1.0;

    /** TIME CONSTRAINTS **/
    public static final long UPDATE_PERIOD = 2000;
    private static final long TASK_DELAY_TIME = 2000;

    /** LABEL **/
    private static final String LOG_DISPLAY_NAME = "TemperatureSensor";
    private static final String RESOURCE_TYPE = "sensor.temperature";

    private Double updatedValue;
    private Random random;
    private Timer updateTimer = null;

    public TemperatureSensor() {
        super(UUID.randomUUID().toString(), RESOURCE_TYPE);
        init();
    }

    private void init(){
        try{
            this.random = new Random(System.currentTimeMillis());
            this.updatedValue = MIN_TEMPERATURE_VALUE + this.random.nextDouble()*(MAX_TEMPERATURE_VALUE - MIN_TEMPERATURE_VALUE);
            startPeriodicEventValueUpdateTask();
        }catch (Exception e){
            logger.error("Error initializing the IoT Resource ! Msg: {}", e.getLocalizedMessage());
        }
    }

    private void startPeriodicEventValueUpdateTask(){
        try{
            logger.info("Starting periodic update task with period: {} ms", UPDATE_PERIOD);
            this.updateTimer = new Timer();
            this.updateTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    double variation = (MIN_TEMPERATURE_VARIATION + MAX_TEMPERATURE_VARIATION*random.nextDouble()) * (random.nextDouble() > 0.5 ? 1.0 : -1.0);
                    updatedValue = updatedValue + variation;
                    notifyUpdate(updatedValue);
                }
            }, TASK_DELAY_TIME, UPDATE_PERIOD);

        }catch (Exception e){
            logger.error("Error on executing periodic resource value: {}", e.getLocalizedMessage());
        }
    }

    @Override
    public Double loadUpdatedValue() {
        return this.updatedValue;
    }

}

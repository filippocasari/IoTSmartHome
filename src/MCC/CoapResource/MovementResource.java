package MCC.CoapResource;

import MCC.DataListener;
import MCC.EmulatedResource.Sensor.MovementSensor;
import MCC.SmartObject;
import MCC.Utils.CoreInterfaces;
import MCC.Utils.SenMLPack;
import MCC.Utils.SenMLRecord;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.coap.CoAP;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.eclipse.californium.core.server.resources.CoapExchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public class MovementResource extends CoapResource {
    private final static Logger logger = LoggerFactory.getLogger(MovementResource.class);

    private static final String OBJECT_TITLE = "MovementSensor";

    private static final Number SENSOR_VERSION = 0.1;

    private ObjectMapper objectMapper;

    private MovementSensor movementSensor;

    private Boolean isMoving = true;

    private String deviceId;


    public MovementResource(String deviceId, String name, MovementSensor movementSensor) {
        super(name);

        if(movementSensor != null && deviceId != null){

            this.deviceId = deviceId;

            this.movementSensor = movementSensor;

            //Jackson Object Mapper + Ignore Null Fields in order to properly generate the SenML Payload
            this.objectMapper = new ObjectMapper();
            this.objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

            setObservable(true); // enable observing
            setObserveType(CoAP.Type.CON); // configure the notification type to CONs

            getAttributes().setTitle(OBJECT_TITLE);
            getAttributes().setObservable();
            getAttributes().addAttribute("rt", movementSensor.getType());
            getAttributes().addAttribute("if", CoreInterfaces.CORE_A.getValue());
            getAttributes().addAttribute("ct", Integer.toString(MediaTypeRegistry.APPLICATION_SENML_JSON));
            getAttributes().addAttribute("ct", Integer.toString(MediaTypeRegistry.TEXT_PLAIN));

            movementSensor.addDataListener(new DataListener<Boolean>() {
                @Override
                public void onDataChanged(SmartObject<Boolean> resource, Boolean updatedValue) {
                    logger.info("Resource Notification Callback ! New Value: {}", updatedValue);
                    isMoving = updatedValue;
                    changed();
                }
            });
        }
        else
            logger.error("Error -> NULL Raw Reference !");

    }

    private Optional<String> getJsonSenmlResponse(){

        try{

            SenMLPack senMLPack = new SenMLPack();

            SenMLRecord senMLRecord = new SenMLRecord();
            senMLRecord.setBn(String.format("%s:%s", this.deviceId, this.getName()));
            senMLRecord.setBver(SENSOR_VERSION);
            senMLRecord.setVb(isMoving);
            senMLRecord.setT(System.currentTimeMillis());

            senMLPack.add(senMLRecord);

            return Optional.of(this.objectMapper.writeValueAsString(senMLPack));

        }catch (Exception e){
            return Optional.empty();
        }
    }

    @Override
    public void handleGET(CoapExchange exchange) {

        //If the request specify the MediaType as JSON or JSON+SenML
        if(exchange.getRequestOptions().getAccept() == MediaTypeRegistry.APPLICATION_SENML_JSON ||
                exchange.getRequestOptions().getAccept() == MediaTypeRegistry.APPLICATION_JSON){

            Optional<String> senmlPayload = getJsonSenmlResponse();

            if(senmlPayload.isPresent())
                exchange.respond(CoAP.ResponseCode.CONTENT, senmlPayload.get(), exchange.getRequestOptions().getAccept());
            else
                exchange.respond(CoAP.ResponseCode.INTERNAL_SERVER_ERROR);
        }
        //Otherwise respond with the default textplain payload
        else
            exchange.respond(CoAP.ResponseCode.CONTENT, String.valueOf(isMoving), MediaTypeRegistry.TEXT_PLAIN);


    }

}

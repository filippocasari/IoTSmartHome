package MCC.coap;

import MCC.DataListener;
import MCC.SmartObject;
import MCC.coap.model.ThermostatConfigurationModel;
import MCC.coap.model.ThermostatConfigurationParameter;
import MCC.utils.CoreInterfaces;
import MCC.utils.SenMLPack;
import MCC.utils.SenMLRecord;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.coap.CoAP;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.eclipse.californium.core.server.resources.CoapExchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

/**
 * @author Marco Picone, Ph.D. - picone.m@gmail.com
 * @project coap-demo-smarthome
 * @created 11/11/2020 - 15:22
 */
public class CoapThermostatConfigurationParameterResource extends CoapResource {

    private final static Logger logger = LoggerFactory.getLogger(CoapThermostatConfigurationParameterResource.class);

    private static final String OBJECT_TITLE = "ThermostatConfiguration";

    private static final Number VERSION = 0.1;

    private ThermostatConfigurationParameter thermostatRawConfigurationParameter;

    private ThermostatConfigurationModel configurationModelValue;

    private ObjectMapper objectMapper;

    private String deviceId;

    public CoapThermostatConfigurationParameterResource(String deviceId, String name, ThermostatConfigurationParameter thermostatRawConfigurationParameter) {

        super(name);

        if(thermostatRawConfigurationParameter != null && deviceId != null){

            this.deviceId = deviceId;

            this.thermostatRawConfigurationParameter = thermostatRawConfigurationParameter;
            this.configurationModelValue = thermostatRawConfigurationParameter.loadUpdatedValue();

            //Jackson Object Mapper + Ignore Null Fields in order to properly generate the SenML Payload
            this.objectMapper = new ObjectMapper();
            this.objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

            setObservable(true); // enable observing
            setObserveType(CoAP.Type.CON); // configure the notification type to CONs

            getAttributes().setTitle(OBJECT_TITLE);
            getAttributes().setObservable();
            getAttributes().addAttribute("rt", thermostatRawConfigurationParameter.getType());
            getAttributes().addAttribute("if", CoreInterfaces.CORE_P.getValue());
            getAttributes().addAttribute("ct", Integer.toString(MediaTypeRegistry.APPLICATION_SENML_JSON));
            getAttributes().addAttribute("ct", Integer.toString(MediaTypeRegistry.TEXT_PLAIN));
        }
        else
            logger.error("Error -> NULL Raw Reference !");

        this.thermostatRawConfigurationParameter.addDataListener(new DataListener<ThermostatConfigurationModel>() {
            @Override
            public void onDataChanged(SmartObject<ThermostatConfigurationModel> resource, ThermostatConfigurationModel updatedValue) {
                configurationModelValue = updatedValue;
                changed();
            }
        });

    }

    /**
     * Create the SenML Response with the updated value and the resource information
     * @return
     */
    private Optional<String> getJsonSenmlResponse(){

        try{

            SenMLPack senMLPack = new SenMLPack();

            SenMLRecord baseRecord = new SenMLRecord();
            baseRecord.setBn(String.format("%s:%s", this.deviceId, this.getName()));
            baseRecord.setBver(VERSION);

            SenMLRecord minTempRecord = new SenMLRecord();
            minTempRecord.setN("min_temperature");
            minTempRecord.setV(configurationModelValue.getMinTemperature());

            SenMLRecord maxTempRecord = new SenMLRecord();
            maxTempRecord.setN("max_temperature");
            maxTempRecord.setV(configurationModelValue.getMaxTemperature());

            SenMLRecord hvacUriRecord = new SenMLRecord();
            hvacUriRecord.setN("hvac_res_uri");
            hvacUriRecord.setVs(configurationModelValue.getHvacUnitResourceUri());

            SenMLRecord operationalModeRecord = new SenMLRecord();
            operationalModeRecord.setN("operational_mode");
            operationalModeRecord.setVs(configurationModelValue.getOperationalMode());

            senMLPack.add(baseRecord);
            senMLPack.add(minTempRecord);
            senMLPack.add(maxTempRecord);
            senMLPack.add(hvacUriRecord);
            senMLPack.add(operationalModeRecord);

            return Optional.of(this.objectMapper.writeValueAsString(senMLPack));

        }catch (Exception e){
            return Optional.empty();
        }
    }

    @Override
    public void handleGET(CoapExchange exchange) {

        try{

            //If the request specify the MediaType as JSON or JSON+SenML
            if(exchange.getRequestOptions().getAccept() == MediaTypeRegistry.APPLICATION_SENML_JSON){

                Optional<String> senmlPayload = getJsonSenmlResponse();

                if(senmlPayload.isPresent())
                    exchange.respond(CoAP.ResponseCode.CONTENT, senmlPayload.get(), exchange.getRequestOptions().getAccept());
                else
                    exchange.respond(CoAP.ResponseCode.INTERNAL_SERVER_ERROR);
            }
            //Otherwise respond with the default textplain payload
            else
                exchange.respond(CoAP.ResponseCode.CONTENT, this.objectMapper.writeValueAsBytes(configurationModelValue), MediaTypeRegistry.TEXT_PLAIN);

        }catch (Exception e){
            e.printStackTrace();
            exchange.respond(CoAP.ResponseCode.INTERNAL_SERVER_ERROR);
        }
    }
}

package MCC.resource;

import MCC.SmartObject;
import MCC.coap.model.ThermostatConfigurationModel;

import java.util.UUID;

public class ThermostatConfigurationParameter extends SmartObject<ThermostatConfigurationModel> {

    private ThermostatConfigurationModel thermostatConfigurationModel;

    private static final String RESOURCE_TYPE = "iot.config.thermostat";

    public ThermostatConfigurationParameter(ThermostatConfigurationModel thermostatConfigurationModel) {
        super(UUID.randomUUID().toString(), RESOURCE_TYPE);
        this.thermostatConfigurationModel = thermostatConfigurationModel;
    }

    @Override
    public ThermostatConfigurationModel loadUpdatedValue() {
        return this.thermostatConfigurationModel;
    }

    public ThermostatConfigurationModel getThermostatConfigurationModel() {
        return thermostatConfigurationModel;
    }

    public void setThermostatConfigurationModel(ThermostatConfigurationModel thermostatConfigurationModel) {
        this.thermostatConfigurationModel = thermostatConfigurationModel;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("ThermostatRawConfigurationParameter{");
        sb.append("thermostatConfigurationModel=").append(thermostatConfigurationModel);
        sb.append('}');
        return sb.toString();
    }
}

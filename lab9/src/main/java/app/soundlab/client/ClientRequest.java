package app.soundlab.client;

import java.util.Map;

public class ClientRequest {
    private String command;
    private Map<String, Object> parameters;

    public ClientRequest() {
    }

    public ClientRequest(String command, Map<String, Object> parameters) {
        this.command = command;
        this.parameters = parameters;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public Map<String, Object> getParameters() {
        return parameters;
    }

    public void setParameters(Map<String, Object> parameters) {
        this.parameters = parameters;
    }
}


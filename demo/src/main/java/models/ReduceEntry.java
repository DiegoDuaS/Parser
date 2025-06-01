package models;

import java.io.Serializable;

public class ReduceEntry implements Serializable{
    private static final long serialVersionUID = 1L;

    private String state;
    private String production_head;
    private String production_value;

    public ReduceEntry(String state, String production_head, String production_value) {
        this.state = state;
        this.production_head = production_head;
        this.production_value = production_value;
    }

    public String getState() {
        return state;
    }

    public String getProduction_head() {
        return production_head;
    }

    public String getProduction_value() {
        return production_value;
    }
}

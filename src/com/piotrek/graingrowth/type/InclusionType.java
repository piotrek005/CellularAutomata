package com.piotrek.graingrowth.type;

/**
 * Enum with inclusion types used on UI purpose.
 * Created by Piotr on 19.10.2016.
 * @author Piotr Hajder
 */
public enum InclusionType {
    SQUARE("Square"),
    CIRCULAR("Circular");

    private String name;

    InclusionType(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}

package com.piotrek.graingrowth.model.mc;

import com.piotrek.graingrowth.type.McNeighbourhood;

/**
 * Util used for creating Mc2d objects.
 * Created by Piotr on 29.11.2016.
 * @author Piotr Hajder
 */
public class McFactory {
    public static Mc2d getMc2dInstance(boolean periodical, Integer[][] states, McNeighbourhood neighbourhood) {
        Mc2d mc2d;
        switch(neighbourhood) {
            case VON_NEUMANN:
                mc2d = new VonNeumann(periodical, states);
                break;
            case MOORE:
                mc2d = new Moore(periodical, states);
                break;
            default:
                mc2d = null;
        }
        return mc2d;
    }
}

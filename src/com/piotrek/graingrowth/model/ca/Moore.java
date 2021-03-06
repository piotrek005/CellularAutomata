package com.piotrek.graingrowth.model.ca;

/**
 * Moore neighbourhood for cellular automata.
 * Created by Piotr on 18.10.2016.
 * @author Piotr Hajder
 */
class Moore extends Ca2d {
    private static final int[][] neigh = {
            {1,1,1},
            {1,1,1},
            {1,1,1}
    };

    Moore(boolean periodic, Integer[][] states) {
        super(periodic, states);
    }

    @Override
    protected Integer[][] getNeighbours(int x, int y) {
        return getNeighbours(x, y, neigh);
    }

    static int[][] getMooreNeighbourhood() {
        return neigh;
    }
}

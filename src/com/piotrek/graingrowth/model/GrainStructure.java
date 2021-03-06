package com.piotrek.graingrowth.model;

import com.piotrek.graingrowth.type.InclusionType;

import java.awt.Point;
import java.util.*;

/**
 * Basic model for grain growth algorithms.
 * Created by Piotr on 29.11.2016.
 * @author Piotrek
 */
public abstract class GrainStructure {
    private static final int INCLUSION_VALUE = -1;
    private static final int DEFAULT_ENERGY = 1;

    protected int boundaryValue;
    protected boolean periodical;

    protected boolean recrystallizationMode = false;
    private List<Point> recrystallizationBorderSeeds;

    protected Integer[][] states;
    protected Integer[][] storedEnergyH;

    protected GrainStructure(boolean periodical) {
        this.periodical = periodical;
        setDefaultBoundaryValue();
    }

    private static int modulo(int n, int p) {
        int tmp = n%p;
        if(tmp < 0)
            tmp += p;
        return tmp;
    }

    protected abstract Integer[][] getNeighbours(int x, int y);

    public abstract void process();
    public abstract void drawGrains(int numOfGrains);
    public abstract boolean isNotEnd();
    public abstract int getProgress();

    protected void drawInclusions(InclusionType type, int radius, int x, int y) {
        int value = type.equals(InclusionType.SQUARE) ?
                (int) (Math.round(Math.pow(2.0, -2.0) * radius)) : radius;
        for(int i=-value; i<value; i++) {
            for(int j=-value; j<value; j++) {
                if(type.equals(InclusionType.CIRCULAR))
                    if(i*i + j*j > radius) continue;
                if(periodical || (x+i >= 0 && y+j >= 0 && x+i < states.length && y+j < states[0].length))
                    states[modulo(x+i,states.length)][modulo(y+j,states[0].length)] = INCLUSION_VALUE;
            }
        }
    }

    private void initializeEnergy() {
        if(storedEnergyH == null) {
            storedEnergyH = new Integer[states.length][states[0].length];
        }
        for(Integer[] tab: storedEnergyH) {
            Arrays.fill(tab, 0);
        }
    }

    private List<Point> checkNeighbours(int x, int y) {
        List<Point> result = new ArrayList<>();
        for(int i=-1; i<2; i+=2) {
            if(x+i >= 0 && x+i < states.length) {
                if(!Objects.equals(states[x][y], states[x + i][y]))
                    result.add(new Point(x+i, y));
            }
            if(y+i >= 0 && y+i < states[0].length) {
                if(!Objects.equals(states[x][y], states[x][y + i]))
                    result.add(new Point(x, y+i));
            }
        }
        if(!result.isEmpty())
            result.add(new Point(x, y));
        return result;
    }

    private List<Point> findBorderSeeds() {
        List<Point> result = new ArrayList<>();
        List<Point> tmp;
        for(int i=0; i<states.length; i++) {
            for(int j=0; j<states[i].length; j++) {
                tmp = checkNeighbours(i, j);
                tmp.stream().filter(p -> !result.contains(p)).forEach(result::add);
            }
        }
        return result;
    }

    private int findMax() {
        Set<Integer> set = new HashSet<>();
        for(Integer[] t: states) {
            set.addAll(Arrays.asList(t));
        }
        return set.stream().max(Comparator.naturalOrder()).orElse(null);
    }

    protected Integer[][] getNeighbours(int x, int y, int[][] neighbourhood) {
        Integer[][] result = new Integer[3][3];

        for(int i=-1; i<2; i++) {
            for(int j=-1; j<2; j++) {
                if(i == j) {
                    result[1+i][1+j] = 0;
                    continue;
                }

                if(periodical || (x+i >= 0 && y+j >= 0 && x+i < states.length && y+j < states[0].length)) {
                    int val = states[modulo(x+i, states.length)][modulo(y+j, states[0].length)];
                    if(val > boundaryValue) {
                        result[1 + i][1 + j] = neighbourhood[1 + i][1 + j] * val;
                        continue;
                    }
                }
                result[1+i][1+j] = 0;
            }
        }

        return result;
    }

    /**
     * Drawing inclusions before simulation.
     * @param type Type of inclusion - circular or square
     * @param radius Radius length
     */
    public final void drawInclusionBefore(InclusionType type, int radius) {
        Random random = new Random();
        int x = random.nextInt(states.length), y = random.nextInt(states[0].length);

        drawInclusions(type, radius, x, y);
    }

    /**
     * Drawing inclusions after simulation. They are placed on grain boundaries.
     * @param type Type of inclusion - circular or square
     * @param radius Radius of inclusion
     */
    public final void drawInclusionsAfter(InclusionType type, int radius) {
        List<Point> borderSeeds = findBorderSeeds();
        Random random = new Random();
        int val = random.nextInt(borderSeeds.size());

        drawInclusions(type, radius, borderSeeds.get(val).x, borderSeeds.get(val).y);
    }

    /**
     * Distributes energy in material homogeneously.
     */
    public final void distributeHomogeneous() {
        initializeEnergy();
        for(int i=0; i<storedEnergyH.length; i++) {
            for(int j=0; j<storedEnergyH[i].length; j++) {
                storedEnergyH[i][j] = DEFAULT_ENERGY;
            }
        }
    }

    /**
     * Distributes energy in material only on grain boundaries.
     */
    public final void distributeOnGrainBoundaries() {
        initializeEnergy();
        List<Point> list = findBorderSeeds();
        for(Point p: list) {
            storedEnergyH[p.x][p.y] = DEFAULT_ENERGY;
        }
    }

    public final void reset() {
        recrystallizationMode = false;
        recrystallizationBorderSeeds = null;
        initializeEnergy();
        setDefaultBoundaryValue();
    }

    public void recrystallizationSetup() {
        recrystallizationMode = true;
        recrystallizationBorderSeeds = findBorderSeeds();
        boundaryValue = findMax();
    }

    public void recrystallize(RecrystallizationParams params) {
        int seedIter = 1, max = findMax();

        List<Point> generatedSeeds = params.isNucleationOnBoundaries() ?
                params.generateNucleons(recrystallizationBorderSeeds) :
                params.generateNucleons(states.length, states[0].length);
        for(Point p: generatedSeeds) {
            states[p.x][p.y] = max + seedIter++;
        }
        process();
    }

    public void setBoundaryValue(int boundaryValue) {
        this.boundaryValue = boundaryValue;
    }

    private void setDefaultBoundaryValue() {
        boundaryValue = INCLUSION_VALUE + 1;
    }

    public Integer[][] getStoredEnergyH() {
        return storedEnergyH;
    }
}

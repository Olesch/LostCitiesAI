package de.spieleck.util.nn;

public interface Trainer {
    void setNumberOfWeights(int connections); 
    void registerWeightChange(int id, double deltaWeight);
    void startAdjustments();
    double adjustment(int id);
    void endAdjustments();
    void clear();
}


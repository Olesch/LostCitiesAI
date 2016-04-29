package de.spieleck.util.nn;

import de.spieleck.util.nn.Trainer;

/**
 * own idea based on AdaMax
 */
public class SpeedlimitTrainer
    implements Trainer
{
    private final double limit, beta1, beta2;
    private int n = 0;
    private double[] m, v;
    private double beta1t, corr;

    public SpeedlimitTrainer() {
        this(0.1, 0.9, 0.98);
    }

    public SpeedlimitTrainer(double limit, double beta1, double beta2) {
        this.limit = limit;
        this.beta1 = beta1;
        this.beta2 = beta2;
        beta1t = 1.0; 
    }

    @Override
    public void setNumberOfWeights(int n) {
        this.n = n;
        clear();
    }

    @Override
    public void registerWeightChange(int i, double deltaWeight) {
        m[i] = beta1 * m[i] + (1.0 - beta1) * deltaWeight;
        v[i] = Math.max(beta2 * v[i], Math.abs(deltaWeight));
    }

    @Override
    public void startAdjustments() {
        beta1t *= beta1;
        corr = 1.0/(1.0 - beta1t);
    }

    @Override
    public void endAdjustments() {
    }

    @Override
    public double adjustment(int i) {
        return -corr * m[i]/(v[i] + Math.abs(m[i])/limit);
    }

    @Override
    public void clear() {
        m = new double[n];
        v = new double[n];
    }
}

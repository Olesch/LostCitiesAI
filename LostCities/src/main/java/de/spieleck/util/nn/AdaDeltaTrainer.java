package de.spieleck.util.nn;

import de.spieleck.util.nn.Trainer;

/**
 * cf. http://arxiv.org/pdf/1212.5701v1.pdf Algorithm1
 */
public class AdaDeltaTrainer
    implements Trainer
{
    private final double beta, epsilon;
    private int n = 0;
    private double[] m, v;

    public AdaDeltaTrainer() {
        this(0.9, 1e-8);
    }

    public AdaDeltaTrainer(double beta, double epsilon) {
        this.beta = beta;
        this.epsilon = epsilon;
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
        alpha2 = alpha/(1.0 - beta1t);
    }

    @Override
    public void endAdjustments() {
    }

    @Override
    public double adjustment(int i) {
        return -alpha2 * m[i]/v[i];
    }

    @Override
    public void clear() {
        m = new double[n];
        v = new double[n];
    }
}

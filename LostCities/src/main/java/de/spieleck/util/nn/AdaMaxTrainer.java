package de.spieleck.util.nn;

import de.spieleck.util.nn.Trainer;

/**
 * cf. http://arxiv.org/abs/1412.6980v8 Algorithm2
 */
public class AdaMaxTrainer
    implements Trainer
{
    private final double alpha, beta1, beta2;
    private int n = 0;
    private double[] m, v;
    private double beta1t, alpha2;

    public AdaMaxTrainer() {
        this(0.002, 0.9, 0.999);
    }

    public AdaMaxTrainer(double alpha, double beta1, double beta2) {
        this.alpha = alpha;
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

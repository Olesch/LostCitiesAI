package de.spieleck.util.nn;

import de.spieleck.util.nn.Trainer;

/**
 * cf. http://arxiv.org/abs/1412.6980v8 Algorithm1
 */
public class AdamTrainer
    implements Trainer
{
    private final double alpha, beta1, beta2, epsilon;
    private int n = 0;
    private double[] m, v;
    private double beta1t, beta2t;
    private double alpha2, corr2;

    public AdamTrainer() {
        this(0.001, 0.9, 0.999, 1e-8);
    }

    public AdamTrainer(double alpha, double beta1, double beta2, double epsilon) {
        this.alpha = alpha;
        this.beta1 = beta1;
        this.beta2 = beta2;
        this.epsilon = epsilon;
        beta1t = beta2t = 1.0; 
    }

    @Override
    public void setNumberOfWeights(int n) {
        this.n = n;
        clear();
    }

    @Override
    public void registerWeightChange(int i, double deltaWeight) {
        m[i] = beta1 * m[i] + (1.0 - beta1) * deltaWeight;
        v[i] = beta2 * v[i] + (1.0 - beta2) * deltaWeight*deltaWeight;
    }

    @Override
    public void startAdjustments() {
        beta1t *= beta1;
        beta2t *= beta2;
        alpha2 = alpha/(1.0-beta1t);
        corr2  = 1.0/(1.0-beta2t);
    }

    @Override
    public void endAdjustments() {
    }

    @Override
    public double adjustment(int i) {
        return -alpha2 * m[i]/(Math.sqrt(v[i]*corr2) + epsilon);
    }

    @Override
    public void clear() {
        m = new double[n];
        v = new double[n];
    }
}

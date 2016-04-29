package de.spieleck.util.nn;

import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.Random;
import java.util.Set;
import java.util.HashSet;

/**
 * Design Goal: Must not be memory efficient, should not be slow, 
 * construction must be easy.
 */
public class FeedForwardNetwork
{
    public interface TransferFunction {
        double forward(double x);
        double derivative(double y);
    }

    public static class Neuron {
        private final int id;
        private final TransferFunction tf;

        private Neuron(int id, TransferFunction tf) {
            this.id = id;
            this.tf = tf;
        }
        public final int id() { return id; }
        public final TransferFunction transferFunction() { return tf; } 
    }

    private final static int BIAS_NEURON_ID = 0;
    private final static Neuron BIAS_NEURON = new Neuron(BIAS_NEURON_ID, null);

    public static class Connection
    {
        private final int from, to, id;

        private Connection(int from, int to, int id) {
            this.from = from;
            this.to = to;
            this.id = id;
        }
        public final int id() { return id; }
        public final int from() { return from; }
        public final int to() { return to; }
    }

    public static class Builder {
        private final List<Neuron> neurons = new ArrayList<>();
        private final List<Connection> connections = new ArrayList();
        private final Set<Neuron> noInput = new HashSet<Neuron>();

        public Builder() {
            neurons.add(BIAS_NEURON);
        }

        public Neuron createNeuron(TransferFunction tf) {
            return createNeuron(tf, true);
        }
        public Neuron createNeuron(TransferFunction tf, boolean withBias) {
            Neuron res = new Neuron(neurons.size(), tf);
            neurons.add(res);
            // For construction lazyness, we do a double bookkeeping, before we link Bias
            if ( withBias ) noInput.add(res); // Bookkeeping for "inputs"
            return res;
        }

        public Connection createConnection(Neuron from, Neuron to) {
            if ( to.id() < from.id() ) {
                throw new IllegalArgumentException("Illegal backward connection.");
            }
            // See if we need to add an bias first
            // For construction lazyness, we do a double bookkeeping, before we link Bias
            if ( noInput.contains(to) ) {
                noInput.remove(to);
                Connection c2 = new Connection(BIAS_NEURON_ID, to.id(), connections.size());
                connections.add(c2);
            }
            Connection res = new Connection(from.id(), to.id(), connections.size());
            connections.add(res);
            return res;
        }

        public FeedForwardNetwork build(Random rand, double amplitude) {
            FeedForwardNetwork res = new FeedForwardNetwork(this);
            res.randomize(rand, amplitude);
            return res;
        }
    }

    private final double[] value;
    private final double[] weight;
    private final List<Neuron> neurons;
    private final List[] froms;
    private final List[] tos;
    private Trainer trainer;

    private FeedForwardNetwork(Builder b) {
        this.neurons = new ArrayList<>(b.neurons);
        // XXX sort by "to" for feedback?!
        value = new double[neurons.size()];
        value[BIAS_NEURON_ID] = 1.0; // The "simplified" bias
        weight = new double[b.connections.size()];
        froms = new ArrayList[neurons.size()];
        tos = new ArrayList[neurons.size()];
        for(Connection c : b.connections) {
            List<Connection> li;
            li = froms[c.from()];
            if ( li == null ) {
                li = new ArrayList<>();
                froms[c.from()] = li;
            }
            li.add(c);
            li = tos[c.to()];
            if ( li == null ) {
                li = new ArrayList<>();
                tos[c.to()] = li;
            }
            li.add(c);
        }
    }

    public void randomize(Random rand, double amplitude) {
        for(int i = 0; i < weight.length; i++) weight[i] = amplitude * (rand.nextDouble() - 0.5);
    }

    public void setValue(Neuron n, double d) {
        value[n.id()] = d;
    }

    public double getValue(Neuron n) {
        return value[n.id()];
    }

    public double getWeight(Connection c) {
        return weight[c.id()];
    }

    public void setTrainer(Trainer trainer) {
        this.trainer = trainer;
        trainer.setNumberOfWeights(weight.length);
    }

    public List<Neuron> getInputs() {
        ArrayList<Neuron> res = new ArrayList<>();
        for(Neuron n : neurons) {
            if ( n.id != 0 && tos[n.id()] == null ) res.add(n);
        }
        return res;
    }

    public List<Neuron> getOutputs() {
        ArrayList<Neuron> res = new ArrayList<>();
        for(Neuron n : neurons) {
            if ( froms[n.id()] == null ) res.add(n);
        }
        return res;
    }

    public void feedForward() {
        for(int i = 1; i < neurons.size(); i++) { // yes, 1
            List<Connection> to = (List<Connection>)tos[i];
            if ( to == null ) continue;
            double sum = 0.0;
            for(Connection c : to) {
                sum += value[c.from()] * weight[c.id()];    
            }
            value[i] = neurons.get(i).transferFunction().forward(sum);
        }
    }

    public void backPropagate(Neuron n, double error) {
        double[] deltas = new double[neurons.size()];
        for(int i = n.id(); i > 0; i--) {
            List<Connection> to = (List<Connection>)tos[i];
            if ( to == null ) continue;
            if ( i < n.id() ) error = deltas[i];
            double delta = error * neurons.get(i).transferFunction().derivative(value[i]);
// System.out.println("  - i="+i+"  "+error+" "+value[i]+" "+delta);
            for(Connection c : to) {
                int id = c.id();
                trainer.registerWeightChange(id, delta*value[c.from()]);
                deltas[c.from()] += delta * weight[id];
// System.out.println("    = i="+c.from()+"  "+delta+" "+value[c.from()]);
            }
        }
    }

    public double adjust() {
        trainer.startAdjustments();
        double change = 0.0;
        for(int i = 0; i < weight.length; i++) {
            double h = trainer.adjustment(i);
            weight[i] += h;
// System.out.println("    # i="+i+"  "+h);            
            change += Math.abs(h);
        }
        trainer.endAdjustments();
        return change;
    }

    public String toString() {
        return "[NN"+getInputs().size()+"("+weight.length+")"+getOutputs().size()+"]";
    }

    public void dump(Appendable a)
        throws IOException
    {
        for(int i = 0; i < neurons.size(); i++) {
            List<Connection> to = (List<Connection>)tos[i];
            if ( to == null ) continue;
            for(Connection c : to) {
                a.append(String.format("%5d %5d %7.3f\n", c.from(), c.to, weight[c.id()]));
            }
        }
    }

}

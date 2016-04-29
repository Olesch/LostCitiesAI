package de.spieleck.util.nn;

import java.io.IOException;
import java.util.Random;

import de.spieleck.util.nn.FeedForwardNetwork;
import de.spieleck.util.nn.FeedForwardNetwork.TransferFunction;
import de.spieleck.util.nn.FeedForwardNetwork.Builder;
import de.spieleck.util.nn.FeedForwardNetwork.Neuron;
import de.spieleck.util.nn.SigmoidTransfer;

public class NNRunner
{
    public static void main(String[] args)
        throws Exception
    {
        runSample3(300);
        runSample2(300);
        runSample1(100);
    }

    private static void runSample1(int loops)
        throws IOException
    {    
        TransferFunction tf1 = new SigmoidTransfer(); 
        Builder b1 = new Builder();
        Neuron in1 = b1.createNeuron(null);
        Neuron in2 = b1.createNeuron(null);
        Neuron out = b1.createNeuron(tf1);
        b1.createConnection(in1, out); 
        b1.createConnection(in2, out); 
        Random rand = new Random(42);
        FeedForwardNetwork nn = b1.build(rand, 0.1);
        System.out.println("\nnn="+nn);
        nn.setTrainer(new AdaMaxTrainer(0.25,  0.99, 0.99));
        for(int j = 0; j < loops; j++) {
            System.out.println(j+".");
            nn.dump(System.out);
            for(int i = 0; i < 4; i++) {
                nn.setValue(in1, 1.0*(i % 2));
                nn.setValue(in2, 1.0*(i / 2));
                nn.feedForward();
                double target = i == 3 ? 1.0 : 0.0; // ((i % 2) == (i /2)) ? 0.0 : 1.0;
                System.out.println(i+" --> ("+target+") "+nn.getValue(out));
                nn.backPropagate(out, nn.getValue(out) - target);
            }
            double d = nn.adjust();
            System.out.println("d="+d);
        }
    }

    private static void runSample2(int loops)
        throws IOException
    {
        TransferFunction tf1 = new SigmoidTransfer(); 
        Builder b1 = new Builder();
        Neuron in1 = b1.createNeuron(null);
        Neuron in2 = b1.createNeuron(null);
        Neuron inner = b1.createNeuron(tf1);
        Neuron out = b1.createNeuron(tf1);
        b1.createConnection(in1, inner); 
        b1.createConnection(in2, inner); 
        b1.createConnection(in1, out); 
        b1.createConnection(in2, out); 
        b1.createConnection(inner, out); 
        Random rand = new Random(42);
        FeedForwardNetwork nn = b1.build(rand, 0.1);
        System.out.println("\nnn="+nn);
        nn.setTrainer(new AdaMaxTrainer(0.1,  0.9, 0.9));
        for(int j = 0; j < loops; j++) {
            System.out.println(j+".");
            nn.dump(System.out);
            for(int i = 0; i < 4; i++) {
                nn.setValue(in1, 1.0*(i % 2));
                nn.setValue(in2, 1.0*(i / 2));
                nn.feedForward();
                double target = ((i % 2) == (i /2)) ? 0.0 : 1.0;
                System.out.println(i+" --> ("+target+") "+nn.getValue(out));
                nn.backPropagate(out, nn.getValue(out) - target);
            }
            double d = nn.adjust();
            System.out.println("d="+d);
        }
    }

    private static void runSample3(int loops)
        throws IOException
    {
        TransferFunction tf1 = new SigmoidTransfer(); 
        Builder b1 = new Builder();
        Neuron in1 = b1.createNeuron(null);
        Neuron in2 = b1.createNeuron(null);
        Neuron inner1 = b1.createNeuron(tf1);
        Neuron inner2 = b1.createNeuron(tf1);
        Neuron out = b1.createNeuron(new IdentityTransfer());
        b1.createConnection(in1, inner1); 
        b1.createConnection(in2, inner1); 
        b1.createConnection(in1, inner2); 
        b1.createConnection(in2, inner2); 
        b1.createConnection(inner1, out); 
        b1.createConnection(inner2, out); 
        Random rand = new Random(42);
        FeedForwardNetwork nn = b1.build(rand, 0.1);
        System.out.println("\nnn="+nn);
        // nn.setTrainer(new AdaMaxTrainer(0.1,  0.9, 0.99));
        // nn.setTrainer(new AdaMaxTrainer(0.2,  0.8, 0.9));
        // nn.setTrainer(new AdamTrainer(0.1, 0.9, 0.999, 1e-8));
        nn.setTrainer(new SpeedlimitTrainer());
        for(int j = 0; j < loops; j++) {
            System.out.println(j+".");
            nn.dump(System.out);
            for(int i = 0; i < 4; i++) {
                nn.setValue(in1, 1.0*(i % 2));
                nn.setValue(in2, 1.0*(i / 2));
                nn.feedForward();
                double target = ((i % 2) == (i /2)) ? 0.0 : 1.0;
                System.out.println(i+" --> ("+target+") "+nn.getValue(out));
                nn.backPropagate(out, nn.getValue(out) - target);
            }
            double d = nn.adjust();
            System.out.println("d="+d);
        }
    }
}

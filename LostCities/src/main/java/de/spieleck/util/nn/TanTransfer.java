package de.spieleck.util.nn;

import de.spieleck.util.nn.FeedForwardNetwork.TransferFunction;

public class TanTransfer
    implements TransferFunction
{
    @Override
    public double forward(double x) { return 1.0/(1.0 + Math.exp(-x)); }

    @Override
    public double derivative(double y) { return y*(1.0-y); }
}

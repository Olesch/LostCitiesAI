package de.spieleck.util.nn;

import de.spieleck.util.nn.FeedForwardNetwork.TransferFunction;


public class IdentityTransfer
    implements TransferFunction
{
    @Override
    public double forward(double x) { return x; }

    @Override
    public double derivative(double y) { return 1.0; }
}

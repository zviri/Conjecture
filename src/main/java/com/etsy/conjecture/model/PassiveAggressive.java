package com.etsy.conjecture.model;

import com.etsy.conjecture.Utilities;
import com.etsy.conjecture.data.BinaryLabel;
import com.etsy.conjecture.data.LabeledInstance;
import com.etsy.conjecture.data.StringKeyedVector;
import static com.google.common.base.Preconditions.checkArgument;

public class PassiveAggressive extends UpdateableLinearModel<BinaryLabel> {

    private static final long serialVersionUID = 1L;
    private double C = 1d; // aggresiveness parameter

    public PassiveAggressive() {
        super();
    }

    public PassiveAggressive(StringKeyedVector param) {
        super(param);
    }

    @Override
    public BinaryLabel predict(StringKeyedVector instance, double bias) {
        double inner = param.dot(instance);
        return new BinaryLabel(Utilities.logistic(inner + bias));
    }

    @Override
    public void updateRule(LabeledInstance<BinaryLabel> instance, double bias) {
        double label = instance.getLabel().getAsPlusMinus();
        double prediction = param.dot(instance.getVector()) + bias;

        double loss = Math.max(0, 1d - label * (prediction));
        if (loss > 0) {
            double norm = instance.getVector().LPNorm(2d);
            double tau = loss / (norm * norm + 1d / (2d * C));
            param.addScaled(instance.getVector(), tau * label);
        }
    }

    public PassiveAggressive setC(double C) {
        checkArgument(C > 0, "C must be greater than 0. Given: %s", C);
        this.C = C;
        return this;
    }

    @Override
    protected String getModelType() {
        return "passive_aggressive";
    }

}

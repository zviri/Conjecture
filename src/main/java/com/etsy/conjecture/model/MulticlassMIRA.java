package com.etsy.conjecture.model;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.etsy.conjecture.data.LabeledInstance;
import com.etsy.conjecture.data.MulticlassLabel;
import com.etsy.conjecture.data.MulticlassPrediction;
import com.etsy.conjecture.data.StringKeyedVector;

public class MulticlassMIRA implements
        UpdateableModel<MulticlassLabel, MulticlassMIRA> {

    static final long serialVersionUID = 666L;

    protected Map<String, StringKeyedVector> param = new HashMap<String, StringKeyedVector>();

    public MulticlassMIRA(String[] categories) {
        for (String s : categories) {
            param.put(s, new StringKeyedVector());
        }
    }

    public MulticlassPrediction predict(StringKeyedVector instance) {
        Map<String, Double> scores = new HashMap<String, Double>();
        for (Map.Entry<String, StringKeyedVector> e : param.entrySet()) {
            scores.put(e.getKey(), e.getValue().dot(instance));
        }
        // Give scores rather than class probabilities.
        // TODO: some kind of soft-max bs.
        return new MulticlassPrediction(scores);
    }

    public void setFreezeFeatureSet(boolean freeze) {
        for (Map.Entry<String, StringKeyedVector> e : param.entrySet()) {
            e.getValue().setFreezeKeySet(freeze);
        }
    }

    public void update(Collection<LabeledInstance<MulticlassLabel>> instances) {
        for (LabeledInstance<MulticlassLabel> instance : instances) {
            update(instance);
        }
    }

    public void update(LabeledInstance<MulticlassLabel> li) {
        MulticlassPrediction pred = predict(li.getVector());
        String ltrue = li.getLabel().getLabel();
        String lpred = pred.getLabel();
        if (!ltrue.equals(lpred)) {
            // Update both models involved.
            // make the smallest update to each parameter vector so that
            // the score of the true label >= score of the preicted label + 1
            double loss = pred.getMap().get(lpred) - pred.getMap().get(ltrue)
                    + 1.0;
            double norm = li.getVector().LPNorm(2d);
            double tau = loss / (2.0 * norm * norm);
            param.get(ltrue).addScaled(li.getVector(), tau);
            param.get(lpred).addScaled(li.getVector(), -tau);
        }
    }

    public void reScale(double scale) {
        for (String cat : param.keySet()) {
            param.get(cat).mul(scale);
        }
    }

    public void merge(MulticlassMIRA model, double scale) {
        for (String cat : param.keySet()) {
            param.get(cat).addScaled(model.param.get(cat), scale);
        }
    }

    public Iterator<Map.Entry<String, Double>> decompose() {
        throw new UnsupportedOperationException("not done yet");
    }

    public void setParameter(String name, double value) {
        throw new UnsupportedOperationException("not done yet");
    }

    public long getEpoch() {
        return 0;
    }

    public void setEpoch(long epoch) {
        // this class doesnt care about epoch.
    }
}

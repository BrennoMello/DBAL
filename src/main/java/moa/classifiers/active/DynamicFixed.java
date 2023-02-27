package moa.classifiers.active;

import com.yahoo.labs.samoa.instances.Instance;
import moa.classifiers.AbstractClassifier;
import moa.core.Measurement;

public class DynamicFixed  extends AbstractClassifier implements ALClassifier{
    @Override
    public double[] getVotesForInstance(Instance instance) {
        return new double[0];
    }

    @Override
    public void resetLearningImpl() {

    }

    @Override
    public void trainOnInstanceImpl(Instance instance) {

    }

    @Override
    protected Measurement[] getModelMeasurementsImpl() {
        return new Measurement[0];
    }

    @Override
    public void getModelDescription(StringBuilder stringBuilder, int i) {

    }

    @Override
    public int getLastLabelAcqReport() {
        return 0;
    }

    @Override
    public boolean isRandomizable() {
        return false;
    }
}

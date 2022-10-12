package moa.classificers.active;

import com.yahoo.labs.samoa.instances.Instance;
import com.yahoo.labs.samoa.instances.InstancesHeader;
import java.util.LinkedList;
import java.util.List;
import moa.classifiers.AbstractClassifier;
import moa.classifiers.Classifier;
import moa.classifiers.active.budget.BudgetManager;
import moa.core.Measurement;
import moa.options.ClassOption;
import moa.classifiers.active.ALClassifier;

public class DBAL extends AbstractClassifier implements ALClassifier{
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
    public void getModelDescription(java.lang.StringBuilder stringBuilder, int i) {

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

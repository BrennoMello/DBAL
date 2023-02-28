package moa.classifiers.active;

import com.github.javacliparser.FloatOption;
import com.github.javacliparser.IntOption;
import com.yahoo.labs.samoa.instances.Instance;
import moa.classifiers.AbstractClassifier;
import moa.classifiers.Classifier;
import moa.classifiers.core.driftdetection.AbstractChangeDetector;
import moa.core.Measurement;
import moa.options.ClassOption;
import utils.Uncertainty;

public class DynamicFixed  extends AbstractClassifier implements ALClassifier{

    public FloatOption budgetOption = new FloatOption ("Budget", 'b', "budget", 0.05, 0,1 );
    public IntOption deltaTimeOption = new IntOption ("Delta Time", 'd', "budget", 1000, 0,Integer.MAX_VALUE );
    public ClassOption baseLearnerOption = new ClassOption("baseLearner", 'l', "Classifier to train.",
            Classifier.class, "moa.classifiers.trees.HoeffdingTree");

    public ClassOption driftDetectorOption = new ClassOption("warningDetector", 'w', "Warning Detector for increasing" +
            " " +
            "budget", AbstractChangeDetector.class, "moa.classifiers.core.driftdetection.ADWINChangeDetector " +
            "-a 0.0001");

    public Uncertainty al_decider;
    public Classifier classifier;
    public double budget;
    public double deltaTime;




    @Override
    public double[] getVotesForInstance(Instance instance) {
        return new double[0];
    }

    @Override
    public void resetLearningImpl() {
        this.classifier = ((Classifier) this.getPreparedClassOption(this.baseLearnerOption)).copy();
        this.classifier.resetLearning();

        this.budget = this.budgetOption.getValue();
        this.deltaTime = this.deltaTimeOption.getValue();
        this.al_decider = new Uncertainty(1);

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

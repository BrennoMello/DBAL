package moa.classifiers.active;

import com.github.javacliparser.FloatOption;
import com.github.javacliparser.IntOption;
import com.yahoo.labs.samoa.instances.Instance;
import moa.classifiers.AbstractClassifier;
import moa.classifiers.Classifier;
import moa.classifiers.core.driftdetection.AbstractChangeDetector;
import moa.classifiers.core.driftdetection.ChangeDetector;
import moa.core.Measurement;
import moa.options.ClassOption;
import utils.Uncertainty;

import java.util.LinkedList;
import java.util.List;

public class DynamicFixed  extends AbstractClassifier implements ALClassifier{

    public FloatOption budgetOption = new FloatOption ("Budget", 'b', "budget", 0.05, 0,1 );
    public IntOption deltaTimeOption = new IntOption ("dtime", 'd', "budget", 1000, 0,Integer.MAX_VALUE );
    public ClassOption baseLearnerOption = new ClassOption("baseLearner", 'l', "Classifier to train.",
            Classifier.class, "moa.classifiers.trees.HoeffdingTree");

    public ClassOption driftDetectorOption = new ClassOption("driftDetector", 'w', "Warning Detector for increasing"+
            "budget", AbstractChangeDetector.class, "moa.classifiers.core.driftdetection.ADWINChangeDetector");

    public Uncertainty al_decider;
    public Classifier classifier;
    public double budget;
    public double lowBudget;
    public double highBudget;
    public double deltaTime;
    public ChangeDetector driftDetector;

    private int instances;

    private int t_drift;
    private int t_1;
    private int t_2;

    public int lastInstancesLabeled = 0;
    public int lastLabelAcq = 0;




    @Override
    public double[] getVotesForInstance(Instance instance) {
        return this.classifier.getVotesForInstance(instance);
    }

    @Override
    public void resetLearningImpl() {
        this.classifier = ((Classifier) this.getPreparedClassOption(this.baseLearnerOption)).copy();
        this.classifier.resetLearning();

        this.driftDetector = ((ChangeDetector) this.getPreparedClassOption(this.driftDetectorOption)).copy();
        this.driftDetector.resetLearning();

        this.budget = this.budgetOption.getValue();
        this.lowBudget = this.budget / 2;
        this.highBudget = 4*this.budget;
        this.deltaTime = this.deltaTimeOption.getValue();
        this.al_decider = new Uncertainty(1);
        this.al_decider.setNewBudget(this.budget);

        this.instances = 0;

    }

    @Override
    public void trainOnInstanceImpl(Instance instance) {

        this.instances++;

        if(al_decider.toLearn(this.classifier.getVotesForInstance(instance), -1)){
            this.driftDetector.input(this.classifier.correctlyClassifies(instance) ? 0.0D : 1.0D);
            this.classifier.trainOnInstance(instance);
            this.lastLabelAcq++;
        }

        if (this.driftDetector.getChange()){
            this.driftDetector = ((ChangeDetector) this.getPreparedClassOption(this.driftDetectorOption)).copy();

            this.t_drift = this.instances;
            this.t_1 = this.t_drift + (int) (Math.ceil(this.deltaTime*((this.budget - this.lowBudget) / (this.highBudget - this.lowBudget))));
            this.t_2 = this.t_1 + (int) (Math.ceil(this.deltaTime*((this.highBudget - this.budget) / (this.highBudget - this.lowBudget))));

        }

        if (this.instances >= t_drift && this.instances < t_1){
            this.al_decider.setNewBudget(this.highBudget);
        }else if(this.instances >= t_1 && this.instances < t_2){
            this.al_decider.setNewBudget(this.lowBudget);
        }else{
            this.al_decider.setNewBudget(this.budget);
        }


    }

    @Override
    protected Measurement[] getModelMeasurementsImpl() {
        List<Measurement> measurementList = new LinkedList<Measurement>();
        measurementList.add( new Measurement("Labeled Instances", this.getLastLabelAcqReport()));
        return measurementList.toArray(new Measurement[measurementList.size()]);
    }

    @Override
    public void getModelDescription(StringBuilder stringBuilder, int i) {

    }

    @Override
    public int getLastLabelAcqReport() {
        int labeledSoFar = this.lastLabelAcq;
        int labelAcq = labeledSoFar - this.lastInstancesLabeled;
        this.lastInstancesLabeled = labeledSoFar;

        return labelAcq;
    }

    @Override
    public boolean isRandomizable() {
        return false;
    }
}

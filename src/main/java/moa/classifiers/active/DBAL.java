package moa.classifiers.active;

import com.github.javacliparser.FloatOption;
import com.github.javacliparser.IntOption;
import com.yahoo.labs.samoa.instances.Instance;
import com.yahoo.labs.samoa.instances.InstancesHeader;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import moa.classifiers.AbstractClassifier;
import moa.classifiers.Classifier;
import moa.classifiers.active.budget.BudgetManager;
import moa.classifiers.core.driftdetection.ChangeDetector;
import moa.core.Measurement;
import moa.options.ClassOption;
import moa.classifiers.active.ALClassifier;
import moa.classifiers.core.driftdetection.AbstractChangeDetector;

public class DBAL extends AbstractClassifier implements ALClassifier{

    private static final long serialVersionUID = 1L;

    public ClassOption baseLearnerOption = new ClassOption("baseLearner", 'l', "Classifier to train.",
            Classifier.class, "moa.classifiers.trees.HoeffdingAdaptiveTree");

    public FloatOption minBudgetOption = new FloatOption ("minBudget", 'm', "Minimum Budget used for supervised drift" +
            " drift detectors", 0.1, 0,1 );
    public IntOption gracePeriodOption = new IntOption ("gracePeriod", 'g', "Number of fully labeled instances" , 100
            , 0,
            Integer.MAX_VALUE );

    public ClassOption driftDetectorOption = new ClassOption("driftDetector", 'd', "Drift Detector for increasing " +
            "budget", AbstractChangeDetector.class, "moa.classifiers.core.driftdetection.DDM");


    public Classifier classifier;
    public ChangeDetector driftDetector;
    public double budget;
    public boolean driftHappening;
    public int gracePeriod;
    public int labeledInstances;





    @Override
    public void resetLearningImpl() {
        this.classifier = ((Classifier) this.getPreparedClassOption(this.baseLearnerOption)).copy();
        this.classifier.resetLearning();
        this.driftDetector = ((ChangeDetector) this.getPreparedClassOption(this.driftDetectorOption)).copy();
        this.driftDetector.resetLearning();
        this.budget = this.minBudgetOption.getValue();
        this.driftHappening = false;
        this.classifierRandom = new Random(42);
        this.gracePeriod = this.gracePeriodOption.getValue();
        this.labeledInstances = 0;


    }

    @Override
    public void trainOnInstanceImpl(Instance instance) {
        double value = this.classifierRandom.nextDouble();

        if (this.labeledInstances < this.gracePeriod){
            this.classifier.trainOnInstance(instance);
        } else if (value >= 1.0D - this.budget){
            double [] votes = this.classifier.getVotesForInstance(instance);
            System.out.println(votes.length);
            this.driftDetector.input(votes[(int) instance.classValue()]);

            this.classifier.trainOnInstance(instance);

        }

        if (this.driftDetector.getChange()){
            System.out.println("Change Detected"); //Here we have to adjust
            this.budget = this.budget * 1.02;
            this.driftHappening = true;

        }else{
            if (this.driftHappening){
                this.budget = this.minBudgetOption.getValue();
                this.driftHappening = false;
            }
        }

    }

    @Override
    public double[] getVotesForInstance(Instance instance) {
        return this.classifier.getVotesForInstance(instance);
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

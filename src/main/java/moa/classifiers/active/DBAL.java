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
import moa.core.Example;
import moa.core.InstanceExample;
import moa.core.Measurement;
import moa.evaluation.ALMultiClassImbalancedPerformanceEvaluator;
import moa.evaluation.ImbalancedPerformanceEvaluator;
import moa.evaluation.MultiClassImbalancedPerformanceEvaluator;
import moa.options.ClassOption;
import moa.classifiers.active.ALClassifier;
import moa.classifiers.core.driftdetection.AbstractChangeDetector;
import moa.classifiers.core.driftdetection.DDM;
import moa.classifiers.core.driftdetection.ADWIN;
import weka.core.Utils;
//import moa.classifiers.trees.HoeffdingTree
public class DBAL extends AbstractClassifier implements ALClassifier{

    private static final long serialVersionUID = 1L;

    public ClassOption baseLearnerOption = new ClassOption("baseLearner", 'l', "Classifier to train.",
            Classifier.class, "moa.classifiers.trees.HoeffdingTree");

    public FloatOption minBudgetOption = new FloatOption ("minBudget", 'm', "Minimum Budget used for supervised drift" +
            " drift detectors", 1, 0,1 );
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
    public int instIndex;

    public int correctInstances;

    public MultiClassImbalancedPerformanceEvaluator evaluator;





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
        this.instIndex = 0;
        this.correctInstances = 0;
        this.evaluator = new MultiClassImbalancedPerformanceEvaluator();
        evaluator.widthOption.setValue(500);


    }

    @Override
    public void trainOnInstanceImpl(Instance instance) {
        double value = this.classifierRandom.nextDouble();
        this.instIndex ++;

        if (this.labeledInstances < this.gracePeriod){
            this.classifier.trainOnInstance(instance);
            this.labeledInstances++;
        } else if (value >= 1.0D - this.budget){
            double [] votes = this.classifier.getVotesForInstance(instance);
            int trueClass = (int) instance.classValue();
            int predictedClass = Utils.maxIndex(votes);
            evaluator.addResult(instance, votes);

            //System.out.println(votes.length);

            this.correctInstances = this.correctInstances  + (trueClass == predictedClass ? 1 : 0);
            int total_instances = this.labeledInstances - this.gracePeriod + 1;

            double accuracy = evaluator.getAucEstimator().getAccuracy();
            /*if (instIndex % 500 == 0) {
                //System.out.println("Index "+ this.instIndex);
                //System.out.println("Accuracy " + accuracy);
            }*/
            //System.out.println("Correct Instances "+ this.correctInstances);
            //System.out.println("Total Instances "+ total_instances);
            this.driftDetector.input(accuracy);

            this.classifier.trainOnInstance(instance);
            this.labeledInstances++;

        }

        if (this.driftDetector.getWarningZone()){
            System.out.println("Drift Warning Detected in position " + this.instIndex); //Here we have to adjust
            System.out.println("Estimation " + this.driftDetector.getEstimation());
            //System.out.println("Output " + this.driftDetector.getOutput());
            /*if (this.driftDetector.getEstimation() > 0.63){
                this.classifier.resetLearning();
            }*/

        }


        if (this.driftDetector.getChange()){
            if (!this.driftHappening){
                System.out.println("reset classifiers");
                //this.classifier.resetLearning();
                //this.driftDetector.resetLearning();
            }
            System.out.println("Drift Detected in position " + this.instIndex); //Here we have to adjust
            this.budget = this.budget * 1.02;
            this.driftHappening = true;


        }else{
            if (this.driftHappening){
                System.out.println("Drift gone in position " + this.instIndex); //Here we have to adjust
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

package moa.classifiers.active;

import com.github.javacliparser.FloatOption;
import com.github.javacliparser.IntOption;
import com.yahoo.labs.samoa.instances.Instance;
import com.yahoo.labs.samoa.instances.InstancesHeader;

import java.util.*;

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
import utils.Uncertainty;
import moa.classifiers.active.ALUncertainty;

public class DBAL extends AbstractClassifier implements ALClassifier{

    private static final long serialVersionUID = 1L;

    public ClassOption baseLearnerOption = new ClassOption("baseLearner", 'l', "Classifier to train.",
            Classifier.class, "moa.classifiers.trees.HoeffdingTree");

    public FloatOption minBudgetOption = new FloatOption ("minBudget", 'm', "Minimum Budget used for supervised drift" +
            " drift detectors", 0.05, 0,1 );
    public IntOption gracePeriodOption = new IntOption ("gracePeriod", 'g', "Number of fully labeled instances" , 100
            , 0,
            Integer.MAX_VALUE );


    public IntOption driftWindowOption = new IntOption ("driftWindow", 'q', "Drift window for increasing budget" , 100
            , 0,
            Integer.MAX_VALUE );

    public IntOption warningWindowOption = new IntOption ("warningWindow", 'r', "warning window for increasing budget" , 50
            , 0,
            Integer.MAX_VALUE );

    public ClassOption driftDetectorOption = new ClassOption("warningDetector", 'w', "Warning Detector for increasing" +
            " " +
            "budget", AbstractChangeDetector.class, "moa.classifiers.core.driftdetection.ADWINChangeDetector " +
            "-a 0.0001");

    public ClassOption warningDetectorOption = new ClassOption("driftDetector", 'd', "Drift Detector for increasing " +
            "budget", AbstractChangeDetector.class, "moa.classifiers.core.driftdetection.ADWINChangeDetector " +
            "-a 0.001");


    public Classifier classifier;
    public Classifier backgroundClassifier;
    public ChangeDetector driftDetector;
    public ChangeDetector warningDetector;
    public Uncertainty al_decider;
    public double budget;
    public boolean driftHappening;
    public boolean warningHappening;
    public int gracePeriod;
    public int lastLabelAcq;
    public int instIndex;
    public int spendedBudget;
    public int correctInstances;

    public int warningInstances;
    public int warningWindow;

    public int driftInstances;
    public int driftWindow;

    public MultiClassImbalancedPerformanceEvaluator evaluator;

    public double [][] probability_correct;
    public double [][] probability_incorrect;

    public int [] correct_classified_counter;
    public int [] incorrect_classified_counter;

    public int [] beforeDrift = new int[2];

    public int lastInstancesLabeled = 0;





    @Override
    public void resetLearningImpl() {
        this.classifier = ((Classifier) this.getPreparedClassOption(this.baseLearnerOption)).copy();
        this.classifier.resetLearning();
        this.backgroundClassifier = ((Classifier) this.getPreparedClassOption(this.baseLearnerOption)).copy();
        this.backgroundClassifier.resetLearning();
        this.driftDetector = ((ChangeDetector) this.getPreparedClassOption(this.driftDetectorOption)).copy();
        this.driftDetector.resetLearning();
        this.warningDetector = ((ChangeDetector) this.getPreparedClassOption(this.warningDetectorOption)).copy();
        this.warningDetector.resetLearning();
        this.budget = this.minBudgetOption.getValue();
        this.warningHappening = false;
        this.driftHappening = false;
        this.classifierRandom = new Random(42);
        this.gracePeriod = this.gracePeriodOption.getValue();
        this.lastLabelAcq = 0;
        this.instIndex = 0;
        this.correctInstances = 0;
        this.spendedBudget = 0;
        this.evaluator = new MultiClassImbalancedPerformanceEvaluator();
        evaluator.widthOption.setValue(300);
        this.al_decider = new Uncertainty(1);

        this.warningWindow = this.warningWindowOption.getValue();
        this.driftWindow = this.driftWindowOption.getValue();

        this.probability_correct = new double[2][2];
        for (double[] row: probability_correct)
            Arrays.fill(row, 0.0);
        this.probability_incorrect = new double[2][2];
        for (double[] row: probability_incorrect)
            Arrays.fill(row, 0.0);

        this.correct_classified_counter = new int[2];
        Arrays.fill(this.correct_classified_counter, 0);
        this.incorrect_classified_counter = new int[2];
        Arrays.fill(this.incorrect_classified_counter, 0);


    }

    private void printVotes (Instance inst, double[] votes){
        System.out.println("Class "+ inst.classValue());
        double sum = Arrays.stream(votes).sum();

        for (int i=0; i< votes.length; i++){
            System.out.print("["+votes[i]/sum+"] ");
        }
        System.out.println("");
    }

    @Override
    public void trainOnInstanceImpl(Instance instance) {
        double value = this.classifierRandom.nextDouble();
        this.instIndex ++;





        if (this.lastLabelAcq < this.gracePeriod){
            this.classifier.trainOnInstance(instance);
            this.lastLabelAcq++;
        } else if (al_decider.toLearn(this.classifier.getVotesForInstance(instance))) {
            this.driftDetector.input(this.classifier.correctlyClassifies(instance) ? 0.0D : 1.0D);
            this.warningDetector.input(this.classifier.correctlyClassifies(instance) ? 0.0D : 1.0D);
            this.classifier.trainOnInstance(instance);
            this.backgroundClassifier.trainOnInstance(instance);
            this.lastLabelAcq++;

            if (this.warningHappening){
                if (this.warningInstances < this.warningWindow) {
                    this.budget = Math.min(this.budget * 1.2, 0.1);
                    //System.out.println("Budget under drift warning "+ this.budget);
                    this.warningInstances++;
                }else{
                    this.warningInstances = 0;
                    this.budget = this.minBudgetOption.getValue();
                    this.warningHappening = false;

                    double [][] proportional =  this.getProportionsCorrect();

                }

                this.al_decider.setNewBudget(this.budget);
            }

            if (this.driftHappening){
                if (this.warningHappening){
                    this.warningHappening = false;
                    this.warningInstances = 0;
                }
                if (this.driftInstances < this.driftWindow) {
                    //System.out.println("Budget under drift "+ this.budget);
                    this.budget = Math.min(this.budget * 1.3, 0.1);
                    this.driftInstances++;
                }else{
                    this.driftInstances = 0;
                    this.driftHappening = false;
                    this.budget = this.minBudgetOption.getValue();
                    double [][] proportional =  this.getProportionsIncorrect();



                }
                this.al_decider.setNewBudget(this.budget);
            }



        }


        if (this.warningDetector.getChange()){
            //System.out.println("Drift Warning Detected in position " + this.instIndex); //Here we have to adjust
            this.warningDetector = ((ChangeDetector) this.getPreparedClassOption(this.warningDetectorOption)).copy();
            this.backgroundClassifier = ((Classifier) this.getPreparedClassOption(this.baseLearnerOption)).copy();
            this.warningHappening = true;
            this.warningInstances = 0;

        }




        if (this.driftDetector.getChange()){

            //System.out.println("reset classifiers");
            this.driftDetector = ((ChangeDetector) this.getPreparedClassOption(this.driftDetectorOption)).copy();
            //this.classifier = this.backgroundClassifier.copy();
            //System.out.println("Drift Detected in position " + this.instIndex); //Here we have to adjust

            //this.beforeDrift[0] = this.al_decider.lastLabelAcq;
            //this.beforeDrift[1] = this.al_decider.costLabeling;
            this.al_decider.lastLabelAcq = 0;
            this.al_decider.costLabeling = 0;

            this.driftHappening = true;
            this.driftInstances = 0;
        }

        if (warningHappening){
            if ((int) instance.classValue() == Utils.maxIndex(this.classifier.getVotesForInstance(instance))){
                //System.out.println(Arrays.toString(this.probability_correct[0]));
                //System.out.println(Arrays.toString(this.probability_correct[1]));
                this.probability_correct[(int) instance.classValue()][0] +=
                        this.classifier.getVotesForInstance(instance)[0] / Arrays.stream(this.classifier.getVotesForInstance(instance)).sum();
                this.probability_correct[(int) instance.classValue()][1] +=
                        this.classifier.getVotesForInstance(instance)[1] / Arrays.stream(this.classifier.getVotesForInstance(instance)).sum();

                this.correct_classified_counter[(int) instance.classValue()] +=1;


                //System.out.println("Instance after warning " + this.warningInstances);
                //this.printVotes(instance, this.classifier.getVotesForInstance(instance));
            }

            if ((int) instance.classValue() != Utils.maxIndex(this.classifier.getVotesForInstance(instance))){
                this.probability_incorrect[(int) instance.classValue()][0] +=
                        this.classifier.getVotesForInstance(instance)[0] / Arrays.stream(this.classifier.getVotesForInstance(instance)).sum();;
                this.probability_incorrect[(int) instance.classValue()][1] +=
                        this.classifier.getVotesForInstance(instance)[1] / Arrays.stream(this.classifier.getVotesForInstance(instance)).sum();;

                this.incorrect_classified_counter[(int) instance.classValue()] +=1;
                //System.out.println("Instance after warning " + this.warningInstances);
                //this.printVotes(instance, this.classifier.getVotesForInstance(instance));
            }

        }





    }

    @Override
    protected Measurement[] getModelMeasurementsImpl() {
        List<Measurement> measurementList = new LinkedList<Measurement>();
        measurementList.add( new Measurement("Labeled Instances", this.getLastLabelAcqReport()));
        return measurementList.toArray(new Measurement[measurementList.size()]);
    }

    @Override
    public double[] getVotesForInstance(Instance instance) {
        return this.classifier.getVotesForInstance(instance);
    }

    //@Override
    /*protected Measurement[] getModelMeasurementsImpl() {
        return new Measurement[0];
    }*/

    @Override
    public void getModelDescription(java.lang.StringBuilder stringBuilder, int i) {

    }

    @Override
    public int getLastLabelAcqReport() {

        int labeledSoFar = this.lastLabelAcq - this.gracePeriod;
        int labelAcq = labeledSoFar - this.lastInstancesLabeled;
        this.lastInstancesLabeled = labeledSoFar;

        return labelAcq;
    }

    @Override
    public boolean isRandomizable() {
        return false;
    }

    public double [][] getProportionsCorrect(){
        double [][] proportions = new double[2][2];
        proportions[0][0] = this.probability_correct[0][0] / this.correct_classified_counter[0];
        proportions[0][1] = this.probability_correct[0][1] / this.correct_classified_counter[0];
        proportions[1][0] = this.probability_correct[1][0] / this.correct_classified_counter[1];
        proportions[1][1] = this.probability_correct[1][1] / this.correct_classified_counter[1];

        return proportions;
    }

    public double [][] getProportionsIncorrect(){
        double [][] proportions = new double[2][2];
        proportions[0][0] = this.probability_incorrect[0][0] / this.incorrect_classified_counter[0];
        proportions[0][1] = this.probability_incorrect[0][1] / this.incorrect_classified_counter[0];
        proportions[1][0] = this.probability_incorrect[1][0] / this.incorrect_classified_counter[1];
        proportions[1][1] = this.probability_incorrect[1][1] / this.incorrect_classified_counter[1];

        return proportions;
    }
}

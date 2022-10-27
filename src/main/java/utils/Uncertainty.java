package utils;

import com.yahoo.labs.samoa.instances.Instance;
import moa.core.DoubleVector;
import moa.core.Utils;

import java.util.Random;

public class Uncertainty {

    public int costLabeling;
    public int iterationControl;
    public double newThreshold;
    public double maxPosterior;
    public double accuracyBaseLearner;
    private double outPosterior;
    private int numInstancesInitOption;
    public int lastLabelAcq;
    public double fixedThreshold;
    public double stepOption;
    public double budget;
    public Random classifierRandom;
    public int activeLearningChoiche;

    public Uncertainty(int activeLearningOption){
        this.costLabeling = 0;
        this.iterationControl = 0;
        this.newThreshold = 1.0D;
        this.accuracyBaseLearner = 0.0D;
        this.lastLabelAcq = 0;
        this.fixedThreshold = 0.9D;
        this.stepOption = 0.01D;
        this.classifierRandom = new Random(42);
        this.activeLearningChoiche = activeLearningOption;
        this.budget = 0.05;
    }
    public void setNewBudget(double value){
        this.budget = value;
    }
    private double getMaxPosterior(double[] incomingPrediction) {
        if (incomingPrediction.length > 1) {
            DoubleVector vote = new DoubleVector(incomingPrediction);
            if (vote.sumOfValues() > 0.0D) {
                vote.normalize();
            }

            incomingPrediction = vote.getArrayRef();
            this.outPosterior = incomingPrediction[Utils.maxIndex(incomingPrediction)];
        } else {
            this.outPosterior = 0.0D;
        }

        return this.outPosterior;
    }

    private boolean labelFixed(double incomingPosterior) {
        if (incomingPosterior < this.fixedThreshold) {

            ++this.costLabeling;
            ++this.lastLabelAcq;
            return true;
        }

        return false;

    }

    private boolean labelVar(double incomingPosterior) {
        if (incomingPosterior < this.newThreshold) {

            ++this.costLabeling;
            ++this.lastLabelAcq;
            this.newThreshold *= 1.0D - this.stepOption;
            return true;
        } else {
            this.newThreshold *= 1.0D + this.stepOption;
            return false;

        }

    }

    private boolean labelSelSampling(double incomingPosterior, int numberOfClasses) {
        double p = Math.abs(incomingPosterior - 1.0D / (double) numberOfClasses);
        double budget = this.budget / (this.budget + p);
        if (this.classifierRandom.nextDouble() < budget) {


            ++this.costLabeling;
            ++this.lastLabelAcq;
            return true;
        }
        return false;

    }

    public boolean toLearn(double [] votes){
        ++this.iterationControl;
        double costNow;
        if ((double)this.iterationControl <= this.numInstancesInitOption) {
            costNow = 0.0D;
            ++this.costLabeling;
            return true;
        } else {
            costNow = ((double)this.costLabeling - this.numInstancesInitOption) / ((double)this.iterationControl - this.numInstancesInitOption);
            if (costNow < this.budget) {
                switch(this.activeLearningChoiche) {
                    case 0:
                        this.maxPosterior = this.getMaxPosterior(votes);
                        return this.labelFixed(this.maxPosterior);

                    case 1:
                        this.maxPosterior = this.getMaxPosterior(votes);
                        return this.labelVar(this.maxPosterior);

                    case 2:
                        this.maxPosterior = this.getMaxPosterior(votes);
                        this.maxPosterior /= this.classifierRandom.nextGaussian() + 1.0D;
                        return this.labelVar(this.maxPosterior);

                    case 3:
                        this.maxPosterior = this.getMaxPosterior(votes);
                        return this.labelSelSampling(this.maxPosterior, votes.length);
                }
            }

        }
        return false;
    }


}

package experiments.active;

import moa.classifiers.active.DBAL;
import moa.classifiers.active.DynamicFixed;
import moa.core.InstanceExample;
import moa.core.TimingUtils;
import moa.evaluation.ALSingleMultiClassImbalancedPerformanceEvaluator;
import moa.streams.ConceptDriftStream;
//import moa.classifiers.meta.AdaptiveRandomForest
public class Debug {

	public static void main(String[] args) throws Exception
	{
		//ArffFileStream stream = new ArffFileStream("datasets/semi-synth/DJ30-D1.arff", -1);
		//AgrawalGenerator stream = new AgrawalGenerator();
		ConceptDriftStream stream = new ConceptDriftStream();

		stream.streamOption.setValueViaCLIString("moa.streams.generators.AgrawalGenerator -f 1 -b ");


		//SUDDEN DRIFTS

		String driftStreamCLI = "ConceptDriftStream -s (moa.streams.generators.AgrawalGenerator -f 2) -d " +
				"(ConceptDriftStream -s (moa.streams.generators.AgrawalGenerator -f 3) -d (moa.streams.generators" +
				".AgrawalGenerator -f 4) -p 25000 -w 1) -p 25000 -w 1";  // 3 drifts

		/*String driftStreamCLI = "ConceptDriftStream -s (moa.streams.generators.AgrawalGenerator -f 2) -d (moa" +
			".streams" +
				".generators.AgrawalGenerator -f 3) -p 30000 -w 1" ;*/  // 2 drifts

		//String driftStreamCLI = "moa.streams.generators.AgrawalGenerator -f 2 -b "; // 1 drift

		//INCREMENTAL DRIFTS

		/*String driftStreamCLI = "ConceptDriftStream -s (moa.streams.generators.AgrawalGenerator -f 2) -d " +
				"(ConceptDriftStream -s (moa.streams.generators.AgrawalGenerator -f 3) -d (moa.streams.generators" +
				".AgrawalGenerator -f 4) -p 25000 -w 12500) -p 25000 -w 12500";*/  // 3 drifts

		/*String driftStreamCLI = "ConceptDriftStream -s (moa.streams.generators.AgrawalGenerator -f 2) -d (moa" +
			".streams" +
				".generators.AgrawalGenerator -f 3) -p 30000 -w 15000" ; */ // 2 drifts

		//String driftStreamCLI = "moa.streams.generators.AgrawalGenerator -f 2 -b "; // 1 drift

		stream.driftstreamOption.setValueViaCLIString(driftStreamCLI);





		stream.positionOption.setValue(25000);
		stream.widthOption.setValue(1);



		stream.prepareForUse();



		//ALUncertainty classifier = new ALUncertainty();
		//classifier.activeLearningStrategyOption.setChosenIndex(1);


		//moa.classifiers.active.ALRandom -l classifier -b (moa.classifiers.active.budget.FixedBM -b budget)
		/*ALRandom classifier = new ALRandom();

		classifier.budgetManagerOption.setValueViaCLIString("moa.classifiers.active.budget.FixedBM -b 1");

		//classifier.baseLearnerOption.setValueViaCLIString("moa.classifiers.trees.HoeffdingTree");
		//classifier.budgetOption.setValue(0.05);

		classifier.prepareForUse();*/


		DBAL classifier = new DBAL();
		//import moa.classifiers.core.driftdetection.STEPD
		//import moa.classifiers.core.driftdetection.ADWINChangeDetector
		/*classifier.warningDetectorOption.setValueViaCLIString("moa.classifiers.core.driftdetection.ADWINChangeDetector " +
				"-a 0.001");
		classifier.driftDetectorOption.setValueViaCLIString("moa.classifiers.core.driftdetection.ADWINChangeDetector " +
				"-a 0.0001");*/

		classifier.prepareForUse();





		int numberInstances = 0;
		
		ALSingleMultiClassImbalancedPerformanceEvaluator evaluator = new ALSingleMultiClassImbalancedPerformanceEvaluator();
		int eval_size = evaluator.widthOption.getValue();

		long evaluateStartTime = TimingUtils.getNanoCPUTimeOfCurrentThread();

		double avg_accuracy = 0;
		double avg_gmean = 0;
		double avg_kappa = 0;
		int n_windows = 0;

		while (stream.hasMoreInstances() && numberInstances < 100000)
		{

			/*if (numberInstances == 5000){
				classifier.resetLearning();
			}*/
			InstanceExample instance = (InstanceExample) stream.nextInstance();
			
			evaluator.addResult(instance, classifier.getVotesForInstance(instance));
			
			//System.out.println(instance.getData().classValue() + "\t" + Utils.maxIndex(learner.getVotesForInstance(instance)));
			
			classifier.trainOnInstance(instance);
        	evaluator.doLabelAcqReport(instance, classifier.getLastLabelAcqReport());


			/*if (numberInstances%eval_size == 0){
				//System.out.println(evaluator.getPerformanceMeasurements()[1].getName() + "\t" + evaluator
				// .getPerformanceMeasurements()[1].getValue());

				avg_accuracy = avg_accuracy + evaluator.getPerformanceMeasurements()[4].getValue();
				avg_gmean = avg_gmean + evaluator.getPerformanceMeasurements()[6].getValue();

				if (evaluator.getPerformanceMeasurements()[5].getValue()>0) {
					avg_kappa = avg_kappa + evaluator.getPerformanceMeasurements()[5].getValue();
				}

				n_windows++;
			}*/

			numberInstances++;
		}

		System.out.println("AVG ACC \t" + avg_accuracy/n_windows);
		System.out.println("AVG GMEAN \t" + avg_gmean/n_windows);
		System.out.println("AVG KAPPA \t" + avg_kappa/n_windows);
		System.out.println("labeld instances \t" + classifier.getLastLabelAcqReport());

		double time = TimingUtils.nanoTimeToSeconds(TimingUtils.getNanoCPUTimeOfCurrentThread()- evaluateStartTime);

		System.out.println(numberInstances + " instances processed in " + time + " seconds");
		
		for(int i = 0; i < evaluator.getPerformanceMeasurements().length; i++)
			System.out.println(evaluator.getPerformanceMeasurements()[i].getName() + "\t" + evaluator.getPerformanceMeasurements()[i].getValue());
	}
}
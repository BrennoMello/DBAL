package experiments.active;

import moa.classifiers.active.DBAL;
import moa.core.InstanceExample;
import moa.core.TimingUtils;
import moa.evaluation.ALMultiClassImbalancedPerformanceEvaluator;
import moa.streams.generators.AgrawalGenerator;
import moa.streams.ConceptDriftStream;
public class Debug {

	public static void main(String[] args) throws Exception
	{
		//ArffFileStream stream = new ArffFileStream("datasets/semi-synth/DJ30-D1.arff", -1);
		//AgrawalGenerator stream = new AgrawalGenerator();
		ConceptDriftStream stream = new ConceptDriftStream();


		stream.prepareForUse();

		DBAL classifier = new DBAL();

		classifier.prepareForUse();





		int numberInstances = 0;
		
		ALMultiClassImbalancedPerformanceEvaluator evaluator = new ALMultiClassImbalancedPerformanceEvaluator();
		int eval_size = evaluator.widthOption.getValue();

		long evaluateStartTime = TimingUtils.getNanoCPUTimeOfCurrentThread();

		double avg_pmauc = 0;
		double avg_kappa = 0;
		int n_windows = 0;

		while (stream.hasMoreInstances() && numberInstances < 10000)
		{
			InstanceExample instance = stream.nextInstance();
			
			evaluator.addResult(instance, classifier.getVotesForInstance(instance));
			
			//System.out.println(instance.getData().classValue() + "\t" + Utils.maxIndex(learner.getVotesForInstance(instance)));
			
			classifier.trainOnInstance(instance);
        	evaluator.doLabelAcqReport(instance, classifier.getLastLabelAcqReport());


			if (numberInstances%eval_size == 0){
				//System.out.println(evaluator.getPerformanceMeasurements()[1].getName() + "\t" + evaluator
				// .getPerformanceMeasurements()[1].getValue());

				avg_pmauc = avg_pmauc + evaluator.getPerformanceMeasurements()[1].getValue();
				if (evaluator.getPerformanceMeasurements()[5].getValue()>0) {
					avg_kappa = avg_kappa + evaluator.getPerformanceMeasurements()[5].getValue();
				}

				n_windows++;
			}

			numberInstances++;
		}

		System.out.println("AVG PMAUC \t" + avg_pmauc/n_windows);
		System.out.println("AVG KAPPA \t" + avg_kappa/n_windows);

		double time = TimingUtils.nanoTimeToSeconds(TimingUtils.getNanoCPUTimeOfCurrentThread()- evaluateStartTime);

		System.out.println(numberInstances + " instances processed in " + time + " seconds");
		
		for(int i = 0; i < evaluator.getPerformanceMeasurements().length; i++)
			System.out.println(evaluator.getPerformanceMeasurements()[i].getName() + "\t" + evaluator.getPerformanceMeasurements()[i].getValue());
	}
}
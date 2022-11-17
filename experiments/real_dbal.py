import argparse
import subprocess
import time
import datetime
import os


imb_names = [
    "adult",
    "amazon-employee",
    "amazon",
    "census",
    "coil2000",
    "covtypeNorm-1-2vsAll",
    "creditcard",
    "Elec",
    "GMSC",
    "hepatitis",
    "internet_ads",
    "KDDCup",
    "nomao",
    "PAKDD",
    "poker-lsn-1-2vsAll",
    "tripadvisor",
    "twitter",
    "SPAM",
    "WEATHER",
]


standard_names = [
    "adult",
    "electricity",
    "kddcup",
    "powersupply",
    "airlines",
    "fars",
    "kr-vs-k",
    "shuttle",
    "bridges",
    "gas-sensor",
    "letter",
    "thyroid",
    "census",
    "GMSC",
    "lymph",
    "wine",
    "coil2000",
    "hepatitis",
    "magic",
    "zoo",
    "connect-4",
    "hypothyroid",
    "nomao",
    "covtype",
    "IntelLabSensors",
    "penbased",
    "dj30",
    "internet_ads",
    "poker",
]


imb_generators = [
    "ArffFileStream -f datasets/datasets-imbalanced-binary/{}.arff)".format(dataset)
    for dataset in imb_names
]

standard_generators = [
    "ArffFileStream -f datasets/datasets-standard/{}.arff)".format(dataset)
    for dataset in standard_names
]


generators = imb_generators + standard_generators

exp_names = imb_names + standard_generators


classifiers = [
    "moa.classifiers.trees.HoeffdingTree",
    "moa.classifiers.meta.AdaptiveRandomForest",
    "moa.classifiers.meta.StreamingRandomPatches",
    "moa.classifiers.meta.LeveragingBag",
    "moa.classifiers.bayes.NaiveBayes",
]

classifiers_name = ["HT", "ARF", "LB", "SRP", "NB"]


minimum_budget = ["0.01", "0.05", "0.1"]


driftWindowOptions = ["100"]

warningWindowOptions = ["50"]


def cmdlineparse(args):
    parser = argparse.ArgumentParser(description="Run MOA scripts")

    parser.add_argument(
        "--results-path",
        type=str,
        default="results/",
    )

    parser.add_argument(
        "--max-processes",
        type=int,
        default=1,
    )

    args = parser.parse_args(args)
    return args


def train(args):

    VMargs = "-Xms8g -Xmx1024g"
    jarFile = "DBAL-1.0-SNAPSHOT-jar-with-dependencies.jar"

    # al_budget = ["1.0", "0.2", "0.1", "0.05", "0.01"]

    # al_budget = ["0.5"]

    results = [
        (generator, classifier, driftWindow, budget)
        for generator in generators
        for classifier in classifiers
        for driftWindow in driftWindowOptions
        for budget in minimum_budget
        # for budget in al_budget
    ]

    print(
        f'>>>>>> START: {datetime.datetime.now().strftime("%Y-%m-%d %H:%M:%S")} >>>>>>'
    )

    processes = list()
    success_count = 0

    for (generator, classifier, driftWindow, budget) in results:
        exp_name = exp_names[generators.index(generator)]
        warningWindow = warningWindowOptions[driftWindowOptions.index(driftWindow)]

        cl_string = "moa.classifiers.active.DBAL -l {} -q {} -r {} -m {}".format(
            classifier, driftWindow, warningWindow, budget
        )

        cmd = (
            "java "
            + VMargs
            + " -javaagent:sizeofag-1.0.4.jar -cp "
            + jarFile
            + " "
            + "moa.DoTask moa.tasks.meta.ALPrequentialEvaluationTask"
            + ' -e "(ALMultiClassImbalancedPerformanceEvaluator -w 500)"'
            + ' -s "('
            + generator
            + ')"'
            + ' -l "('
            + cl_string
            + ')"'
            + " -i 100000 -f 500"
            + " -d "
            + args.results_path
            + classifiers_name[classifiers.index(classifier)]
            + "-"
            + driftWindow
            + "-"
            + warningWindow
            + "-"
            + budget
            + "-"
            + exp_name
            + ".csv &"
        )

        print(cmd)
        processes.append(subprocess.Popen(cmd, stdout=subprocess.PIPE, shell=True))

        while len(processes) >= args.max_processes:
            time.sleep(5)

            # Print outputs and remove finished processes from list
            finished_processes = [
                i for i, p in enumerate(processes) if p.poll() is not None
            ]
            for finished_i in finished_processes:
                try:
                    comm = processes[finished_i].communicate()
                    print(comm[0].decode("utf-8"))
                    return_code = processes[finished_i].poll()
                    if return_code == 0:
                        success_count += 1
                except:
                    pass
                processes = [p for i, p in enumerate(processes) if i != finished_i]

    while len(processes):
        time.sleep(5)

        # Print outputs and remove finished processes from list
        finished_processes = [
            i for i, p in enumerate(processes) if p.poll() is not None
        ]
        for finished_i in finished_processes:
            try:
                comm = processes[finished_i].communicate()
                print(comm[0].decode("utf-8"))
                return_code = processes[finished_i].poll()
                if return_code == 0:
                    success_count += 1
            except:
                pass
            processes = [p for i, p in enumerate(processes) if i != finished_i]

    print(
        f'>>>>>> END : {datetime.datetime.now().strftime("%Y-%m-%d %H:%M:%S")} >>>>>>'
    )


def main(args=None):
    args = cmdlineparse(args)
    train(args)


if __name__ == "__main__":
    main()

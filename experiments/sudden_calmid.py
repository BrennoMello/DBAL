import argparse
import subprocess
import time
import datetime
import os

generators = [
    "ConceptDriftStream -s (moa.streams.generators.AgrawalGenerator -i 1 -f 1 -b) -r 1"
    + " -d (moa.streams.generators.AgrawalGenerator -i 1 -f 2 -b) -w 1 -p 50000",
    "ConceptDriftStream -s (moa.streams.generators.AgrawalGenerator -i 1 -f 1 -b) -r 1 "
    + " -d (ConceptDriftStream -s (moa.streams.generators.AgrawalGenerator -i 1 -f 2 -b) -r 2 "
    + " -d  (moa.streams.generators.AgrawalGenerator -i 1 -f 3 -b) -w 1 -p 30000) -w 1 -p 30000",
    "ConceptDriftStream -s (moa.streams.generators.AgrawalGenerator -i 1 -f 1 -b) -r 1 "
    + " -d (ConceptDriftStream -s (moa.streams.generators.AgrawalGenerator -i 1 -f 2 -b) -r 2 "
    + " -d  (ConceptDriftStream -s (moa.streams.generators.AgrawalGenerator -i 1 -f 3 -b) -r 3"
    + " -d (moa.streams.generators.AgrawalGenerator -i 1 -f 4 -b)"
    + "-w 1 -p 250000)"
    + "-w 1 -p 25000)"
    + "-w 1 -p 25000",
    "ConceptDriftStream -s (moa.streams.generators.HyperplaneGenerator -i 1 -a 10 -c 2 -k 10 -t 0.1) -r 1 "
    + "-d (moa.streams.generators.HyperplaneGenerator -i 2 -a 10 -c 2 -k 10 -t 0.1) "
    + "-p 50000 -w 1",
    "ConceptDriftStream -s (moa.streams.generators.HyperplaneGenerator -i 1 -a 10 -c 2 -k 10 -t 0.1) -r 1 "
    + "-d (ConceptDriftStream -s (moa.streams.generators.HyperplaneGenerator -i 2 -a 10 -c 2 -k 10 -t 0.1) -r 2 "
    + "-d (moa.streams.generators.HyperplaneGenerator -i 3 -a 10 -c 2 -k 10 -t 0.1) -r 3 "
    + "-p 30000 -w 1) "
    + "-p 30000 -w 1",
    "ConceptDriftStream -s (moa.streams.generators.HyperplaneGenerator -i 1 -a 10 -c 2 -k 10 -t 0.1) -r 1 "
    + "-d (ConceptDriftStream -s (moa.streams.generators.HyperplaneGenerator -i 2 -a 10 -c 2 -k 10 -t 0.1) -r 2 "
    + "-d (ConceptDriftStream -s (moa.streams.generators.HyperplaneGenerator -i 3 -a 10 -c 2 -k 10 -t 0.1) -r 3 "
    + "-d (moa.streams.generators.HyperplaneGenerator -i 4 -a 10 -c 2 -k 10 -t 0.1) -r 4 "
    + "-p 25000 -w 1)"
    + "-p 25000 -w 1)"
    + "-p 25000 -w 1",
    "ConceptDriftStream -s (moa.streams.generators.RandomRBFGenerator -i 1 -a 10 -c 2 -r 1) -r 1 "
    + "-d (moa.streams.generators.RandomRBFGenerator -i 2 -a 10 -c 2 -r 2)"
    + "-p 50000 -w 1",
    "ConceptDriftStream -s (moa.streams.generators.RandomRBFGenerator -i 1 -a 10 -c 2 -r 1) -r 1 "
    + "-d (ConceptDriftStream -s (moa.streams.generators.RandomRBFGenerator -i 2 -a 10 -c 2 -r 2) -r 2 "
    + "-d (moa.streams.generators.RandomRBFGenerator -i 3 -a 10 -c 2 -r 3) "
    + "-p 30000 -w 1) "
    + "-p 30000 -w 1",
    "ConceptDriftStream -s (moa.streams.generators.RandomRBFGenerator -i 1 -a 10 -c 2 -r 1) -r 1 "
    + "-d (ConceptDriftStream -s (moa.streams.generators.RandomRBFGenerator -i 2 -a 10 -c 2 -r 2) -r 2 "
    + "-d (ConceptDriftStream -s (moa.streams.generators.RandomRBFGenerator -i 3 -a 10 -c 2 -r 3) -r 3 "
    + "-d (moa.streams.generators.RandomRBFGenerator -i 4 -a 10 -c 2 -r 4) "
    + "-p 25000 -w 1) "
    + "-p 25000 -w 1) "
    + "-p 25000 -w 1",
    "ConceptDriftStream -s (moa.streams.generators.RandomTreeGenerator -i 1 -o 5 -u 5 -c 2 -r 1) -r 1 "
    + "-d (moa.streams.generators.RandomTreeGenerator -i 2 -o 5 -u 5 -c 2 -r 2) "
    + "-p 50000 -w 1",
    "ConceptDriftStream -s (moa.streams.generators.RandomTreeGenerator -i 1 -o 5 -u 5 -c 2 -r 1) -r 1 "
    + "-d (ConceptDriftStream -s (moa.streams.generators.RandomTreeGenerator -i 2 -o 5 -u 5 -c 2 -r 2) -r 2 "
    + "-d (moa.streams.generators.RandomTreeGenerator -i 3 -o 5 -u 5 -c 2 -r 3) "
    + "-p 30000 -w 1)"
    + "-p 30000 -w 1",
    "ConceptDriftStream -s (moa.streams.generators.RandomTreeGenerator -i 1 -o 5 -u 5 -c 2 -r 1) -r 1 "
    + "-d (ConceptDriftStream -s (moa.streams.generators.RandomTreeGenerator -i 2 -o 5 -u 5 -c 2 -r 2) -r 2 "
    + "-d (ConceptDriftStream -s (moa.streams.generators.RandomTreeGenerator -i 3 -o 5 -u 5 -c 2 -r 3) -r 3 "
    + "-d (moa.streams.generators.RandomTreeGenerator -i 4 -o 5 -u 5 -c 2 -r 4) "
    + "-p 25000 -w 1) "
    + "-p 25000 -w 1) "
    + "-p 25000 -w 1",
    "ConceptDriftStream -s (moa.streams.generators.SineGenerator -i 1 -f 1) -r 1 "
    + "-d (moa.streams.generators.SineGenerator -i 2 -f 2)"
    + "-p 50000 -w 1",
    "ConceptDriftStream -s (moa.streams.generators.SineGenerator -i 1 -f 1) -r 1 "
    + "-d (ConceptDriftStream -s (moa.streams.generators.SineGenerator -i 2 -f 2) -r 2 "
    + "-d (moa.streams.generators.SineGenerator -i 3 -f 3)"
    + "-p 30000 -w 1) "
    + "-p 30000 -w 1",
    "ConceptDriftStream -s (moa.streams.generators.SineGenerator -i 1 -f 1) -r 1 "
    + "-d (ConceptDriftStream -s (moa.streams.generators.SineGenerator -i 2 -f 2) -r 2 "
    + "-d (ConceptDriftStream -s (moa.streams.generators.SineGenerator -i 3 -f 3) -r 3 "
    + "-d (moa.streams.generators.SineGenerator -i 4 -f 4) "
    + "-p 25000 -w 1) "
    + "-p 25000 -w 1) "
    + "-p 25000 -w 1",
    "ConceptDriftStream -s (moa.streams.generators.MixedGenerator -i 1 -f 1) -r 1 "
    + "-d (moa.streams.generators.MixedGenerator -i 2 -f 2) "
    + "-p 50000 -w 1",
    "ConceptDriftStream -s (moa.streams.generators.MixedGenerator -i 1 -f 1) -r 1 "
    + "-d (ConceptDriftStream -s (moa.streams.generators.MixedGenerator -i 2 -f 2) -r 2 "
    + "-d (moa.streams.generators.MixedGenerator -i 3 -f 1) "
    + "-p 30000 -w 1)"
    + "-p 30000 -w 1 ",
    "ConceptDriftStream -s (moa.streams.generators.MixedGenerator -i 1 -f 1) -r 1 "
    + "-d (ConceptDriftStream -s (moa.streams.generators.MixedGenerator -i 2 -f 2) -r 2 "
    + "-d (ConceptDriftStream -s (moa.streams.generators.MixedGenerator -i 3 -f 1) -r 3 "
    + "-d (moa.streams.generators.MixedGenerator -i 4 -f 2)  "
    + "-p 25000 -w 1) "
    + "-p 25000 -w 1) "
    + "-p 25000 -w 1 ",
    "ConceptDriftStream -s (moa.streams.generators.AssetNegotiationGenerator -i 1 -f 1) -r 1 "
    + "-d (moa.streams.generators.AssetNegotiationGenerator -i 2 -f 2)"
    + "-p 50000 -w 1",
    "ConceptDriftStream -s (moa.streams.generators.AssetNegotiationGenerator -i 1 -f 1) -r 1 "
    + "-d (ConceptDriftStream -s (moa.streams.generators.AssetNegotiationGenerator -i 2 -f 2) -r 2 "
    + "-d (moa.streams.generators.AssetNegotiationGenerator -i 3 -f 3)  "
    + "-p 30000 -w 1) "
    + "-p 30000 -w 1",
    "ConceptDriftStream -s (moa.streams.generators.AssetNegotiationGenerator -i 1 -f 1) -r 1 "
    + "-d (ConceptDriftStream -s (moa.streams.generators.AssetNegotiationGenerator -i 2 -f 2) -r 2 "
    + "-d (ConceptDriftStream -s (moa.streams.generators.AssetNegotiationGenerator -i 3 -f 3) -r 3 "
    + "-d (moa.streams.generators.AssetNegotiationGenerator -i 4 -f 4)  "
    + "-p 25000 -w 1) "
    + "-p 25000 -w 1) "
    + "-p 25000 -w 1",
]

exp_names = [
    "AGRAWAL_1_DRIFT",
    "AGRAWAL_2_DRIFT",
    "AGRAWAL_3_DRIFT",
    "HYPERPLANE_1_DRIFT",
    "HYPERPLANE_2_DRIFT",
    "HYPERPLANE_3_DRIFT",
    "RBF_1_DRIFT",
    "RBF_2_DRIFT",
    "RBF_3_DRIFT",
    "RT_1_DRIFT",
    "RT_2_DRIFT",
    "RT_3_DRIFT",
    "SINE_1_DRIFT",
    "SINE_2_DRIFT",
    "SINE_3_DRIFT",
    "MIXED_1_DRIFT",
    "MIXED_2_DRIFT",
    "MIXED_3_DRIFT",
    "ASSET_1_DRIFT",
    "ASSET_2_DRIFT",
    "ASSET_3_DRIFT",
]



evaluation_window = ["500"]


tasks  = ["EvaluateInterleavedTestThenTrain"]

evaluators = ["MultiClassImbalancedPerformanceEvaluator"]




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

    al_budget = ["1.0", "0.2", "0.1", "0.05", "0.01"]

    # al_budget = ["0.5"]

    results = [
        (generator, budget, evalWin)
        for generator in generators
        for budget in al_budget
        for evalWin in evaluation_window
    ]

    print(
        f'>>>>>> START: {datetime.datetime.now().strftime("%Y-%m-%d %H:%M:%S")} >>>>>>'
    )

    processes = list()
    success_count = 0

    for (
        generator,
        budget,
        evalWin,
    ) in results:
        exp_name = exp_names[generators.index(generator)]
        task = tasks[evaluation_window.index(evalWin)]
        evaluator = evaluators[evaluation_window.index(evalWin)]

        cl_string = (
            "moa.classifiers.active.CALMID -b {}".format(
                 budget
            )
        )

        cmd = (
            "java {}".format(VMargs)
            + " -javaagent:sizeofag-1.0.4.jar -cp {} ".format(jarFile)
            + "moa.DoTask {}".format(task)
            + ' -e "({} -w {})"'.format(evaluator, 500)
            + ' -s "({})"'.format(generator)
            + ' -l "({})"'.format(cl_string)
            + " -i 100000 -f {}".format(500)
            + " -d {} &".format(
                args.results_path
                + "CALMID"
                + "-"
                + budget
                + "-"
                + exp_name
                + "_eval"
                + evalWin
                + ".csv"
            )
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

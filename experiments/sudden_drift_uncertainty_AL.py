import argparse
import subprocess
import time
import datetime
import os

semi_synth_datasets = [
    "CRIMES-D1",
    "DJ30-D1",
    "GAS-D1",
    "OLYMPIC-D1",
    "POKER-D1",
    "SENSOR-D1",
    "TAGS-D1",
    "ACTIVITY_RAW-D1",
    "ACTIVITY-D1",
    "CONNECT4-D1",
    "COVERTYPE-D1",
]

real_datasets = [
    "activity",
    "connect-4",
    "CovPokElec",
    "covtype",
    "crimes",
    "fars",
    "gas",
    "hypothyroid",
    "kddcup",
    "kr-vs-k",
    "lymph",
    "olympic",
    "poker",
    "sensor",
    "shuttle",
    "tags",
    "thyroid",
    "zoo",
]


al_strategies = ["Random"]


def cmdlineparse(args):
    parser = argparse.ArgumentParser(description="Run MOA scripts")

    parser.add_argument(
        "--datasets", type=str, default=None,
    )

    parser.add_argument(
        "--results-path", type=str, default="results/uncertainty-kappaoversampling/",
    )

    parser.add_argument(
        "--max-processes", type=int, default=4,
    )

    args = parser.parse_args(args)
    return args


def train(args):

    if args.datasets == "SEMI":
        datasets = semi_synth_datasets
        args.results_path = args.results_path + "semi-synth/"
        dataset_path = "datasets/semi-synth/"
    else:
        datasets = real_datasets
        args.results_path = args.results_path + "real/"
        dataset_path = "datasets/real/"

    VMargs = "-Xms8g -Xmx1024g"
    jarFile = "kappaoversampling-1.0-jar-with-dependencies.jar"

    al_budget = ["1.0", "0.5", "0.2", "0.1", "0.01", "0.05", "0.005", "0.001"]

    imbalance_weight = ["1"]

    class_windows = [1000, 500, 200, 100, 50, 10, 10, 10]

    results = [
        (dt,budget)
        for dt in datasets
        
        for budget in al_budget
     
    ]

    print(
        f'>>>>>> START: {datetime.datetime.now().strftime("%Y-%m-%d %H:%M:%S")} >>>>>>'
    )

    processes = list()
    success_count = 0



    for (dataset,  budget) in results:
        class_window = class_windows[al_budget.index(budget)]

        cmd = ("java "
                + VMargs
                + " -javaagent:sizeofag-1.0.4.jar -cp "
                + jarFile
                + " "
                + "moa.DoTask moa.tasks.EvaluateInterleavedTestThenTrain"
                + ' -e "(MultiClassImbalancedPerformanceEvaluator -w 500)"'
                + ' -s "(ArffFileStream -f {}'.format(dataset_path)
                + dataset
                + '.arff)"'
                + ' -l "(moa.classifiers.meta.imbalanced.OSAMP -w {} -b {})"'.format(class_window, budget)
                + " -f 500"
                + " -d "
                + args.results_path
                + "OSAMP"
                + "-"
                + dataset
                + "-"
                + budget
                + ".csv & exit")
        

        print(cmd)
        processes.append(
            subprocess.Popen(cmd, stdout=subprocess.PIPE, shell=True, close_fds=True)
        )

        while len(processes) >= args.max_processes:
            time.sleep(30)

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
        time.sleep(30)

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

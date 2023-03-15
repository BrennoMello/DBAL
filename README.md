# Dynamic budget allocation for sparsely labeled drifting data streams

Learning classification models from non-stationary data streams presents challenges due to their evolving nature and concept drift. Moreover, assuming that all instances are labeled is not realistic in real-world applications. Numerous strategies have been proposed to learn from sparsely labeled data streams. However, they rely on fixed labeling budgets, which is a drawback when run on drifting streams. In this work, we propose a new active learning strategy that dynamically controls the labeling budget to optimize its usage and adapt quickly to concept drift. Our strategy is centered around the idea of monitoring the data stream to detect concept drifts. When a drift is detected, we increase the maximum labeling budget for a given limited time window, thereby providing the classifier with more flexibility to adapt to the new concept. We conducted experiments on 7 synthetic data generators with different types and numbers of concept drifts, as well as on 7 real-world data streams with varying labeling budgets, to evaluate the performance of our strategy across a range of scenarios. Results showed that our strategy improved the performance of classifiers and outperformed state-of-the-art active learning strategies, while maintaining a comparable or lower number of labeled instances than its competitors. We demonstrated that providing a flexible budget to the classifier can enhance performance to a greater extent than simply increasing a fixed budget.


*Manuscript submitted for publication at Information Sciences* (https://www.sciencedirect.com/journal/information-sciences)

## Implementation details

DBAL was implemented using MOA (https://github.com/Waikato/moa) and Java OpenJDK 18. 


## Data

Real-world data streams are available in the [link](https://drive.google.com/drive/folders/1LBi37mzEl_HS3JixbH-PoLndCaTy5_WR?usp=sharing).

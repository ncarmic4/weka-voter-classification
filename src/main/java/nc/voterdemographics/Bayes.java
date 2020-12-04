package nc.voterdemographics;

import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.bayes.NaiveBayes;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;

public class Bayes {
    public static void main(String[] args) throws Exception {
        DatasetUtil generalElections = new DatasetUtil();
        generalElections.run();

        Instances trainingSet = new DataSource("src/main/resources/TrainingData.csv").getDataSet();
        Instances dataSet = new DataSource("src/main/resources/Dataset.csv").getDataSet();
        trainingSet.setClassIndex(trainingSet.numAttributes() - 1);
        dataSet.setClassIndex(dataSet.numAttributes() - 1);

        Classifier classifier = new NaiveBayes();
        classifier.buildClassifier(trainingSet);

        Evaluation evaluation = new Evaluation(trainingSet);
        evaluation.evaluateModel(classifier, dataSet);
        System.out.println(evaluation.toSummaryString("\nResults\n======\n", false));
    }
}

package hex;

import hex.deeplearning.*;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import water.*;
import water.fvec.*;
import water.util.Log;

import java.util.HashSet;

public class DeepLearningAutoEncoderTest extends TestUtil {
  /*
    Visualize outliers with the following R code (from smalldata/anomaly dir):

    train <- scan("ecg_discord_train.csv", sep=",")
    test  <- scan("ecg_discord_test.csv",  sep=",")
    plot.ts(train)
    plot.ts(test)
  */

  static final String PATH = "smalldata/anomaly/ecg_discord_train.csv"; //first 20 points
  static final String PATH2 = "smalldata/anomaly/ecg_discord_test.csv"; //first 22 points

  @BeforeClass public static void stall() {
    stall_till_cloudsize(JUnitRunnerDebug.NODES);
  }

  @Test
  public void run() {
    long seed = 0xDECAF;

    Key file_train = NFSFileVec.make(find_test_file(PATH));
    Frame train = ParseDataset2.parse(Key.make(), new Key[]{file_train});
    Key file_test = NFSFileVec.make(find_test_file(PATH2));
    Frame test = ParseDataset2.parse(Key.make(), new Key[]{file_test});

    for (float sparsity_beta : new float[]{0, 0.1f}) {
      DeepLearning p = new DeepLearning();
      p.source = train;
      p.autoencoder = true;
      p.response = train.lastVec();
      p.classification = false;

      p.seed = seed;
      p.hidden = new int[]{100, 100};
      p.adaptive_rate = true;
      p.sparsity_beta = sparsity_beta;
      p.average_activation = -0.7;
      p.l1 = 1e-4;
//    p.l2 = 1e-4;
//    p.rate = 1e-5;
      p.activation = DeepLearning.Activation.Tanh;
      p.loss = DeepLearning.Loss.MeanSquare;
//    p.initial_weight_distribution = DeepLearning.InitialWeightDistribution.Normal;
//    p.initial_weight_scale = 1e-3;
      p.epochs = 500;
//    p.shuffle_training_data = true;
      p.force_load_balance = true;
      p.invoke();

      DeepLearningModel mymodel = UKV.get(p.dest());

      // Verification of results

      StringBuilder sb = new StringBuilder();
      sb.append("Verifying results.\n");

      // Training data

      // Reconstruct data using the same helper functions and verify that self-reported MSE agrees
      double quantile = 0.95;
      final Frame l2_frame_train = mymodel.scoreAutoEncoder(train);
      final Vec l2_train = l2_frame_train.anyVec();
      sb.append("Mean reconstruction error: " + l2_train.mean() + "\n");
      Assert.assertEquals(mymodel.mse(), l2_train.mean(), 1e-7);
      Assert.assertTrue(l2_train.mean() < 1e-1);

      // manually compute L2
      Frame reconstr = mymodel.score(train); //this creates real values in original space
      double mean_l2 = 0;
      for (int r = 0; r < reconstr.numRows(); ++r) {
        double my_l2 = 0;
        for (int c = 0; c < reconstr.numCols(); ++c) {
          my_l2 += Math.pow((reconstr.vec(c).at(r) - train.vec(c).at(r)) * mymodel.model_info().data_info()._normMul[c], 2); //undo normalization here
        }
        my_l2 /= reconstr.numCols();
        mean_l2 += my_l2;
      }
      mean_l2 /= reconstr.numRows();
      reconstr.delete();
      sb.append("Mean reconstruction error (train): " + l2_train.mean() + "\n");
      Assert.assertEquals(mymodel.mse(), mean_l2, 1e-7);

      // print stats and potential outliers
      sb.append("The following training points are reconstructed with an error above the " + quantile * 100 + "-th percentile - check for \"goodness\" of training data.\n");
      double thresh_train = mymodel.calcOutlierThreshold(l2_train, quantile);
      for (long i = 0; i < l2_train.length(); i++) {
        if (l2_train.at(i) > thresh_train) {
          sb.append(String.format("row %d : l2_train error = %5f\n", i, l2_train.at(i)));
        }
      }

      // Test data

      // Reconstruct data using the same helper functions and verify that self-reported MSE agrees
      final Frame l2_frame_test = mymodel.scoreAutoEncoder(test);
      final Vec l2_test = l2_frame_test.anyVec();
      double mult = 10;
      double thresh_test = mult * thresh_train;
      sb.append("\nFinding outliers.\n");
      sb.append("Mean reconstruction error (test): " + l2_test.mean() + "\n");

      // print stats and potential outliers
      sb.append("The following test points are reconstructed with an error greater than " + mult + " times the mean reconstruction error of the training data:\n");
      HashSet<Long> outliers = new HashSet<Long>();
      for (long i = 0; i < l2_test.length(); i++) {
        if (l2_test.at(i) > thresh_test) {
          outliers.add(i);
          sb.append(String.format("row %d : l2 error = %5f\n", i, l2_test.at(i)));
        }
      }
      Log.info(sb);

      // check that the all outliers are found (and nothing else)
      Assert.assertTrue(outliers.contains(new Long(20)));
      Assert.assertTrue(outliers.contains(new Long(21)));
      Assert.assertTrue(outliers.contains(new Long(22)));
      Assert.assertTrue(outliers.size() == 3);

      // cleanup
      p.delete();
      mymodel.delete();
      l2_frame_train.delete();
      l2_frame_test.delete();
    }
    train.delete();
    test.delete();
  }
}


package water.suites;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
  hex.CholTest.class,
  hex.deeplearning.DropoutTest.class,
  hex.deeplearning.NeuronsTest.class,
  hex.DeepLearningAutoEncoderCategoricalTest.class,
  hex.DeepLearningAutoEncoderTest.class,
  hex.DeepLearningIrisTest.Short.class,
  hex.DeepLearningIrisTest.Long.class,
  //hex.DeepLearningMLPReference.class,
  hex.DeepLearningProstateTest.Short.class,
  hex.DeepLearningProstateTest.Long.class,
  hex.DeepLearningSpiralsTest.class,
  hex.DeepLearningVsNeuralNet.class,
  hex.drf.DRFCheckpointTest.class,
  hex.drf.DRFModelAdaptTest.class,
  hex.drf.DRFTest.class,
  hex.drf.DRFTest2.class,
  hex.FrameSplitterTest.class,
  hex.gbm.GBMCheckpointTest.class,
  hex.gbm.GBMDomainTest.class,
  hex.gbm.GBMTest.class,
  hex.gbm.GBMTest2.class,
  hex.GLMTest.class,
  hex.GLMTest2.class,
  hex.GramMatrixTest.class,
  hex.HistogramTest.class,
  hex.HttpTest.class,
  hex.JobArgsTest.class,
  hex.KMeans2Test.class,
  hex.KMeansTest.class,
  hex.MinorityClassTest.class,
  hex.MRUtilsTest.class,
  hex.NeuralNetIrisTest.class,
  //hex.NeuralNetMLPReference.class,
  hex.NeuralNetSpiralsTest.class,
  hex.NFoldFrameExtractorTest.class,
  hex.PCATest.class,
  hex.purelogic.EmbeddedPseudoCranTest.class,
  hex.purelogic.UserSpecifiedNetworkTest.class,
  hex.rf.RandomForestTest.class,
  hex.rf.RFPredDomainTest.class,
  hex.singlenoderf.SpeeDRFTest.class,
  hex.singlenoderf.SpeeDRFTest2.class,
  hex.Summary2Test.class,
  //hex.SummaryTest.class,
  water.api.ConfusionMatrixTest.class,
  water.api.HitRatioTest.class,
  water.api.RStringTest.class,
  water.api.StableAPITest.class,
  water.AtomicTest.class,
  water.AutoBuffer2JSONTest.class,
  water.AutoSerialTest.class,
  water.BitCmpTest.class,
  water.ConcurrentKeyTest.class,
  water.CoreTest.class,
  //water.exec.DdplyTest.class,
  //water.exec.Expr2Test.class,
  water.FlowTest.class,
  water.FlowTest2.class,
  water.fvec.CBSChunkTest.class,
  //water.fvec.FVecTest.class,
  water.fvec.NewVectorTest.class,
  //water.fvec.ParserTest2.class,
  water.fvec.ParseTimeTest.class,
  //water.fvec.RebalanceDatasetTest.class,
  water.fvec.SparseTest.class,
  water.fvec.StringTest.class,
  water.fvec.TransfVecTest.class,
  water.fvec.VecStatsTest.class,
  water.fvec.VecTest.class,
  water.fvec.WordCountTest.class,
  water.GridSplitTest.class,
  water.JStackTest.class,
  water.KeyToString.class,
  water.KVSpeedTest.class,
  water.KVTest.class,
  //water.MRThrow.class,
  water.parser.DatasetCornerCasesTest.class,
  water.parser.ParseCompressedAndXLSTest.class,
//  water.parser.ParseFolderTest.class, //don't run this now, added empty files to testParse2, va outdated anyways
  water.parser.ParseFolderTestBig.class,
  water.parser.ParseProgressTest.class,
  water.parser.ParserTest.class,
  water.parser.RReaderTest.class,
  water.score.ScorePmmlTest.class,
  water.score.ScoreTest.class,
  water.serial.ModelSerializationTest.class,
  water.TestKeySnapshot.class,
  water.TypeAheadTest.class,
  water.util.ModelUtilsTest.class,
  water.util.SBTest.class,
  water.util.UtilsTest.class,
  water.ValueArrayToFrameTest.class,
  //water.ValueArrayToFrameTestAll.class,
})
public class AllTestsSuite {
  /* FIXME collect test at runtime:
   * See http://junit.sourceforge.net/doc/faq/faq.htm - DirectorySuiteBuilder and ArchiveSuiteBuilder
   */
}

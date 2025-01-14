package water.fvec;

import org.junit.Assert;
import org.junit.Test;
import water.TestUtil;

import java.util.Arrays;

public class C16ChunkTest extends TestUtil {
  @Test
  public void test_inflate_impl() {
    for (int l=0; l<2; ++l) {
      NewChunk nc = new NewChunk(null, 0);

      long[] vals = new long[]{Long.MIN_VALUE, Long.MAX_VALUE-1, 1l, 12312421425l, 23523523423l, Long.MIN_VALUE+1, Long.MAX_VALUE-1, -823048234l, -123123l};
      if (l==1) nc.addNA();
      for (long v : vals) nc.addUUID(v,v);
      nc.addNA();

      Chunk cc = nc.compress();
      Assert.assertEquals(vals.length + 1 + l, cc.len());
      Assert.assertTrue(cc instanceof C16Chunk);
      if (l==1) Assert.assertTrue(cc.isNA0(0));
      if (l==1) Assert.assertTrue(cc.isNA(0));
      for (int i = 0; i < vals.length; ++i) Assert.assertEquals(vals[i], cc.at16l0(l+i));
      for (int i = 0; i < vals.length; ++i) Assert.assertEquals(vals[i], cc.at16l(l+i));
      for (int i = 0; i < vals.length; ++i) Assert.assertEquals(vals[i], cc.at16h0(l+i));
      for (int i = 0; i < vals.length; ++i) Assert.assertEquals(vals[i], cc.at16h(l+i));
      Assert.assertTrue(cc.isNA0(vals.length+l));
      Assert.assertTrue(cc.isNA(vals.length+l));

      nc = cc.inflate_impl(new NewChunk(null, 0));
      Assert.assertEquals(vals.length + 1 + l, nc.len());

      if (l==1) Assert.assertTrue(nc.isNA0(0));
      if (l==1) Assert.assertTrue(nc.isNA(0));
      for (int i = 0; i < vals.length; ++i) Assert.assertEquals(vals[i], nc.at16l0(l+i));
      for (int i = 0; i < vals.length; ++i) Assert.assertEquals(vals[i], nc.at16l(l+i));
      for (int i = 0; i < vals.length; ++i) Assert.assertEquals(vals[i], nc.at16h0(l+i));
      for (int i = 0; i < vals.length; ++i) Assert.assertEquals(vals[i], nc.at16h(l+i));
      Assert.assertTrue(nc.isNA0(vals.length+l));
      Assert.assertTrue(nc.isNA(vals.length+l));

      Chunk cc2 = nc.compress();
      Assert.assertEquals(vals.length + 1 + l, cc.len());
      Assert.assertTrue(cc2 instanceof C16Chunk);
      if (l==1) Assert.assertTrue(cc2.isNA0(0));
      if (l==1) Assert.assertTrue(cc2.isNA(0));
      for (int i = 0; i < vals.length; ++i) Assert.assertEquals(vals[i], cc2.at16l0(l+i));
      for (int i = 0; i < vals.length; ++i) Assert.assertEquals(vals[i], cc2.at16l(l+i));
      for (int i = 0; i < vals.length; ++i) Assert.assertEquals(vals[i], cc2.at16h0(l+i));
      for (int i = 0; i < vals.length; ++i) Assert.assertEquals(vals[i], cc2.at16h(l+i));
      Assert.assertTrue(cc2.isNA0(vals.length+l));
      Assert.assertTrue(cc2.isNA(vals.length+l));

      Assert.assertTrue(Arrays.equals(cc._mem, cc2._mem));
    }
  }
}

/*
The MIT License

Copyright (c) 2016-2021 kong <congcoi123@gmail.com>

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
*/

package com.tenio.common.data;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.tenio.common.data.element.ZeroData;
import com.tenio.common.data.implement.ZeroArrayImpl;
import com.tenio.common.data.implement.ZeroObjectImpl;
import com.tenio.common.data.utility.ZeroDataSerializerUtility;
import java.util.ArrayList;
import java.util.Collection;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public final class ZeroDataTest {

  private static Collection<Boolean> booleans;
  private static Collection<Short> shorts;
  private static Collection<Integer> integers;
  private static Collection<Long> longs;
  private static Collection<Float> floats;
  private static Collection<Double> doubles;
  private static Collection<String> strings;

  @BeforeAll
  public static void initialization() {
    booleans = new ArrayList<Boolean>();
    booleans.add(true);
    booleans.add(false);
    booleans.add(true);

    shorts = new ArrayList<Short>();
    shorts.add((short) 10);
    shorts.add((short) 11);
    shorts.add((short) 12);
    shorts.add((short) 13);
    shorts.add((short) 14);
    shorts.add((short) 15);

    integers = new ArrayList<Integer>();
    integers.add(100);
    integers.add(101);
    integers.add(102);
    integers.add(103);

    longs = new ArrayList<Long>();
    longs.add(1000L);
    longs.add(2000L);

    floats = new ArrayList<Float>();
    floats.add(1001.1f);
    floats.add(1002.2f);
    floats.add(1003.3f);
    floats.add(1004.4f);
    floats.add(1005.5f);
    floats.add(1006.6f);

    doubles = new ArrayList<Double>();
    doubles.add(1000000.11111);

    strings = new ArrayList<String>();
    strings.add("zero");
    strings.add("data");
    strings.add("testing");
    strings.add("is");
    strings.add("awesome");
  }

  @AfterAll
  public static void finish() {
    booleans.clear();
    shorts.clear();
    integers.clear();
    longs.clear();
    floats.clear();
    doubles.clear();
    strings.clear();
  }

  @Test
  public void primitiveDataInArrayShouldMatch() {
    var origin = ZeroArrayImpl.newInstance();
    origin.addBoolean(true).addShort((short) 11).addInteger(1000).addFloat(101.1f).addLong(1000L)
        .addDouble(1010101.101);
    var binary = origin.toBinary();
    var newOne = ZeroDataSerializerUtility.binaryToArray(binary);

    assertAll("primitiveDataInArrayShouldMatch",
        () -> assertTrue(newOne.getBoolean(0)),
        () -> assertEquals(newOne.getShort(1), (short) 11),
        () -> assertEquals(newOne.getInteger(2), 1000),
        () -> assertEquals(newOne.getFloat(3), 101.1f),
        () -> assertEquals(newOne.getLong(4), 1000L),
        () -> assertEquals(newOne.getDouble(5), 1010101.101)
    );
  }

  @Test
  public void instanceDataInArrayShouldMatch() {
    var origin = ZeroArrayImpl.newInstance();
    origin.addNull().addZeroData(ZeroData.newInstance(ZeroDataType.BOOLEAN, false))
        .addString("test");
    var binary = origin.toBinary();
    var newOne = ZeroDataSerializerUtility.binaryToArray(binary);

    assertAll("instanceDataInArrayShouldMatch",
        () -> assertTrue(newOne.isNull(0)),
        () -> assertAll("zeroDataShouldMatch",
            () -> assertEquals(newOne.getZeroData(1).getType(), ZeroDataType.BOOLEAN),
            () -> assertFalse((boolean) newOne.getZeroData(1).getElement())
        ),
        () -> assertEquals(newOne.getString(2), "test")
    );
  }

  @Test
  public void collectionDataInArrayShouldMatch() {
    var origin = ZeroArrayImpl.newInstance();
    origin.addBooleanArray(booleans).addShortArray(shorts).addIntegerArray(integers)
        .addLongArray(longs).addFloatArray(floats).addDoubleArray(doubles).addStringArray(strings);
    var binary = origin.toBinary();
    var newOne = ZeroDataSerializerUtility.binaryToArray(binary);

    assertAll("collectionDataInArrayShouldMatch",
        () -> assertEquals(newOne.getBooleanArray(0).toString(), booleans.toString()),
        () -> assertEquals(newOne.getShortArray(1).toString(), shorts.toString()),
        () -> assertEquals(newOne.getIntegerArray(2).toString(), integers.toString()),
        () -> assertEquals(newOne.getLongArray(3).toString(), longs.toString()),
        () -> assertEquals(newOne.getFloatArray(4).toString(), floats.toString()),
        () -> assertEquals(newOne.getDoubleArray(5).toString(), doubles.toString()),
        () -> assertEquals(newOne.getStringArray(6).toString(), strings.toString())
    );
  }

  @Test
  public void duplicatedValueInArrayShouldWork() {
    var origin = ZeroArrayImpl.newInstance();
    origin.addBooleanArray(booleans).addShortArray(shorts).addBooleanArray(booleans)
        .addShortArray(shorts);
    var binary = origin.toBinary();
    var newOne = ZeroDataSerializerUtility.binaryToArray(binary);

    assertAll("duplicatedValueInArrayShouldWork",
        () -> assertEquals(newOne.getBooleanArray(0).toString(), booleans.toString()),
        () -> assertEquals(newOne.getShortArray(1).toString(), shorts.toString()),
        () -> assertEquals(newOne.getBooleanArray(2).toString(), booleans.toString()),
        () -> assertEquals(newOne.getShortArray(3).toString(), shorts.toString())
    );
  }

  @Test
  public void zeroObjectInArrayShouldMatch() {
    var origin = ZeroArrayImpl.newInstance();
    var zeroObject = ZeroObjectImpl.newInstance();
    zeroObject.putBoolean("b", true)
        .putShort("s", (short) 10)
        .putInteger("i", 100)
        .putShortArray("sa", shorts);
    origin.addZeroObject(zeroObject);
    var binary = origin.toBinary();
    var newOne = ZeroDataSerializerUtility.binaryToArray(binary);

    assertEquals(zeroObject.toString(), newOne.getZeroObject(0).toString());
  }

  @Test
  public void primitiveDataInObjectShouldMatch() {
    var origin = ZeroObjectImpl.newInstance();
    origin.putBoolean("b", true)
        .putShort("s", (short) 11)
        .putInteger("i", 1000)
        .putFloat("f", 101.1f)
        .putLong("l", 1000L)
        .putDouble("d", 1010101.101)
        .putZeroArray("za", ZeroArrayImpl.newInstance().addDoubleArray(doubles));
    var binary = origin.toBinary();
    var newOne = ZeroDataSerializerUtility.binaryToObject(binary);

    assertAll("primitiveDataInObjectShouldMatch",
        () -> assertTrue(newOne.getBoolean("b")),
        () -> assertEquals(newOne.getShort("s"), (short) 11),
        () -> assertEquals(newOne.getInteger("i"), 1000),
        () -> assertEquals(newOne.getFloat("f"), 101.1f),
        () -> assertEquals(newOne.getLong("l"), 1000L),
        () -> assertEquals(newOne.getDouble("d"), 1010101.101)
    );
  }

  @Test
  public void instanceDataInObjectShouldMatch() {
    var origin = ZeroObjectImpl.newInstance();
    origin.putNull("n")
        .putZeroData("z", ZeroData.newInstance(ZeroDataType.BOOLEAN, false))
        .putString("s", "test");
    var binary = origin.toBinary();
    var newOne = ZeroDataSerializerUtility.binaryToObject(binary);

    assertAll("instanceDataInObjectShouldMatch",
        () -> assertTrue(newOne.isNull("n")),
        () -> assertAll("zeroDataShouldMatch",
            () -> assertEquals(newOne.getZeroData("z").getType(), ZeroDataType.BOOLEAN),
            () -> assertFalse((boolean) newOne.getZeroData("z").getElement())
        ),
        () -> assertEquals(newOne.getString("s"), "test")
    );
  }

  @Test
  public void collectionDataInObjectShouldMatch() {
    var origin = ZeroObjectImpl.newInstance();
    origin.putBooleanArray("b", booleans)
        .putShortArray("s", shorts)
        .putIntegerArray("i", integers)
        .putLongArray("l", longs)
        .putFloatArray("f", floats)
        .putDoubleArray("d", doubles)
        .putStringArray("ss", strings);
    var binary = origin.toBinary();
    var newOne = ZeroDataSerializerUtility.binaryToObject(binary);

    assertAll("collectionDataInObjectShouldMatch",
        () -> assertEquals(newOne.getBooleanArray("b").toString(), booleans.toString()),
        () -> assertEquals(newOne.getShortArray("s").toString(), shorts.toString()),
        () -> assertEquals(newOne.getIntegerArray("i").toString(), integers.toString()),
        () -> assertEquals(newOne.getLongArray("l").toString(), longs.toString()),
        () -> assertEquals(newOne.getFloatArray("f").toString(), floats.toString()),
        () -> assertEquals(newOne.getDoubleArray("d").toString(), doubles.toString()),
        () -> assertEquals(newOne.getStringArray("ss").toString(), strings.toString())
    );
  }

  @Test
  public void duplicatedValueInObjectShouldWork() {
    var origin = ZeroObjectImpl.newInstance();
    origin.putBooleanArray("b1", booleans).putShortArray("s1", shorts)
        .putBooleanArray("b2", booleans).putShortArray("s2", shorts);
    var binary = origin.toBinary();
    var newOne = ZeroDataSerializerUtility.binaryToObject(binary);

    assertAll("duplicatedValueInObjectShouldWork",
        () -> assertEquals(newOne.getBooleanArray("b1").toString(), booleans.toString()),
        () -> assertEquals(newOne.getShortArray("s1").toString(), shorts.toString()),
        () -> assertEquals(newOne.getBooleanArray("b2").toString(), booleans.toString()),
        () -> assertEquals(newOne.getShortArray("s2").toString(), shorts.toString())
    );
  }

  @Test
  public void zeroObjectInObjectShouldMatch() {
    var origin = ZeroObjectImpl.newInstance();
    var zeroObject = ZeroObjectImpl.newInstance();
    zeroObject.putBoolean("b", true)
        .putShort("s", (short) 10)
        .putInteger("i", 100)
        .putBooleanArray("ba", booleans)
        .putZeroArray("za", ZeroArrayImpl.newInstance().addDoubleArray(doubles));
    origin.putZeroObject("z", zeroObject);
    var binary = origin.toBinary();
    var newOne = ZeroDataSerializerUtility.binaryToObject(binary);

    assertEquals(zeroObject.toString(), newOne.getZeroObject("z").toString());
  }
}

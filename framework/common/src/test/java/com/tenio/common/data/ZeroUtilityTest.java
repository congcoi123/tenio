/*
The MIT License

Copyright (c) 2016-2025 kong <congcoi123@gmail.com>

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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.tenio.common.data.zero.ZeroType;
import com.tenio.common.data.zero.utility.ZeroUtility;
import com.tenio.common.utility.ByteUtility;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Unit Test Cases For Zero Utility")
class ZeroUtilityTest {

  private static Collection<Boolean> booleans;
  private static byte[] binaries;
  private static Collection<Short> shorts;
  private static Collection<Integer> integers;
  private static Collection<Long> longs;
  private static Collection<Float> floats;
  private static Collection<Double> doubles;
  private static Collection<String> strings;

  @BeforeAll
  static void initialization() {
    booleans = new ArrayList<>(3);
    booleans.add(true);
    booleans.add(false);
    booleans.add(true);

    binaries = new byte[3];
    binaries[0] = (byte) 1;
    binaries[1] = (byte) 2;
    binaries[2] = (byte) 3;

    shorts = new ArrayList<>(6);
    shorts.add((short) 10);
    shorts.add((short) 11);
    shorts.add((short) 12);
    shorts.add((short) 13);
    shorts.add((short) 14);
    shorts.add((short) 15);

    integers = new ArrayList<>(4);
    integers.add(100);
    integers.add(101);
    integers.add(102);
    integers.add(103);

    longs = new ArrayList<>(2);
    longs.add(1000L);
    longs.add(2000L);

    floats = new ArrayList<>(6);
    floats.add(1001.1f);
    floats.add(1002.2f);
    floats.add(1003.3f);
    floats.add(1004.4f);
    floats.add(1005.5f);
    floats.add(1006.6f);

    doubles = new ArrayList<>(1);
    doubles.add(1000000.11111);

    strings = new ArrayList<>(5);
    strings.add("zero");
    strings.add("data");
    strings.add("testing");
    strings.add("is");
    strings.add("awesome");
  }

  @AfterAll
  static void finish() {
    booleans.clear();
    shorts.clear();
    integers.clear();
    longs.clear();
    floats.clear();
    doubles.clear();
    strings.clear();
  }

  @Test
  @DisplayName("Throw an exception when the class's instance is attempted creating")
  void createNewInstanceShouldThrowException() throws NoSuchMethodException {
    var constructor = ZeroUtility.class.getDeclaredConstructor();
    assertTrue(Modifier.isPrivate(constructor.getModifiers()));
    assertThrows(InvocationTargetException.class, () -> {
      constructor.setAccessible(true);
      constructor.newInstance();
    });
  }

  @Test
  @DisplayName("Checking whether binaries data is a collection should work")
  void itShouldReturnCorrectDataCollection() {
    var zeroMap = ZeroUtility.newZeroMap().putBoolean("a", true);
    var checkZeroMap = ZeroUtility.binariesToCollection(zeroMap.toBinaries());
    assertEquals(zeroMap.toString(), checkZeroMap.toString());

    var zeroArray = ZeroUtility.newZeroArray().addBoolean(true);
    var checkZeroArray = ZeroUtility.binariesToCollection(zeroArray.toBinaries());

    assertEquals(zeroArray.toString(), checkZeroArray.toString());
    assertThrows(UnsupportedOperationException.class,
        () -> ZeroUtility.binariesToCollection(new byte[] {(byte) 1}));
  }

  @Test
  @DisplayName("Allow adding and fetching primitive data to/from ZeroArray")
  void primitiveDataInArrayShouldMatch() {
    var origin = ZeroUtility.newZeroArray();
    origin.addBoolean(true).addByte((byte) 1).addShort((short) 11).addInteger(1000).addFloat(101.1f)
        .addLong(1000L).addDouble(1010101.101);
    var actuality = ZeroUtility.binariesToArray(origin.toBinaries());

    assertAll("primitiveDataInArrayShouldMatch",
        () -> assertTrue(actuality.getBoolean(0)),
        () -> assertEquals((byte) 1, actuality.getByte(1)),
        () -> assertEquals((short) 11, actuality.getShort(2)),
        () -> assertEquals(1000, actuality.getInteger(3)),
        () -> assertEquals(101.1f, actuality.getFloat(4)),
        () -> assertEquals(1000L, actuality.getLong(5)),
        () -> assertEquals(1010101.101, actuality.getDouble(6))
    );
  }

  @Test
  @DisplayName("Allow adding and fetching nested ZeroArray data to/from ZeroArray")
  void instanceDataInArrayShouldMatch() {
    var origin = ZeroUtility.newZeroArray();
    origin.addNull().addZeroElement(ZeroUtility.newZeroElement(ZeroType.BOOLEAN, false))
        .addString("test");
    var actuality = ZeroUtility.binariesToArray(origin.toBinaries());

    assertAll("instanceDataInArrayShouldMatch",
        () -> assertTrue(actuality.isNull(0)),
        () -> assertAll("zeroDataShouldMatch",
            () -> assertEquals(ZeroType.BOOLEAN, actuality.getZeroElement(1).getType()),
            () -> assertFalse((boolean) actuality.getZeroElement(1).getData())
        ),
        () -> assertEquals("test", actuality.getString(2))
    );
  }

  @Test
  @DisplayName("Allow adding and fetching arrays of primitive data to/from ZeroArray")
  void collectionDataInArrayShouldMatch() {
    var origin = ZeroUtility.newZeroArray();
    var zeroArray = ZeroUtility.newZeroArray().addString("newZeroArray");
    origin.addBooleanArray(booleans).addByteArray(binaries).addShortArray(shorts)
        .addIntegerArray(integers)
        .addLongArray(longs).addFloatArray(floats).addDoubleArray(doubles).addStringArray(strings)
        .addZeroArray(zeroArray);
    var actuality = ZeroUtility.binariesToArray(origin.toBinaries());

    assertAll("collectionDataInArrayShouldMatch",
        () -> assertEquals(booleans.toString(), actuality.getBooleanArray(0).toString()),
        () -> assertEquals(binaries.length, actuality.getByteArray(1).length),
        () -> assertEquals(shorts.toString(), actuality.getShortArray(2).toString()),
        () -> assertEquals(integers.toString(), actuality.getIntegerArray(3).toString()),
        () -> assertEquals(longs.toString(), actuality.getLongArray(4).toString()),
        () -> assertEquals(floats.toString(), actuality.getFloatArray(5).toString()),
        () -> assertEquals(doubles.toString(), actuality.getDoubleArray(6).toString()),
        () -> assertEquals(strings.toString(), actuality.getStringArray(7).toString()),
        () -> assertEquals(zeroArray.toString(), actuality.getZeroArray(8).toString())
    );
  }

  @Test
  @DisplayName("Allow adding and fetching duplicated data to/from ZeroArray")
  void duplicatedValueInArrayShouldWork() {
    var origin = ZeroUtility.newZeroArray();
    origin.addBooleanArray(booleans).addShortArray(shorts).addBooleanArray(booleans)
        .addShortArray(shorts);
    var actuality = ZeroUtility.binariesToArray(origin.toBinaries());

    assertAll("duplicatedValueInArrayShouldWork",
        () -> assertEquals(booleans.toString(), actuality.getBooleanArray(0).toString()),
        () -> assertEquals(shorts.toString(), actuality.getShortArray(1).toString()),
        () -> assertEquals(booleans.toString(), actuality.getBooleanArray(2).toString()),
        () -> assertEquals(shorts.toString(), actuality.getShortArray(3).toString())
    );
  }

  @Test
  @DisplayName("Allow adding and fetching ZeroMap data to/from ZeroArray")
  void zeroMapInArrayShouldMatch() {
    var origin = ZeroUtility.newZeroArray();
    var zeroMap = ZeroUtility.newZeroMap();
    zeroMap.putBoolean("b", true)
        .putShort("s", (short) 10)
        .putInteger("i", 100)
        .putShortArray("sa", shorts);
    origin.addZeroMap(zeroMap);
    var actuality = ZeroUtility.binariesToArray(origin.toBinaries());

    assertEquals(zeroMap.toString(), actuality.getZeroMap(0).toString());
  }

  @Test
  @DisplayName("Allow adding and fetching primitive data to/from ZeroMap")
  void primitiveDataInMapShouldMatch() {
    var origin = ZeroUtility.newZeroMap();
    origin.putBoolean("b", true)
        .putShort("s", (short) 11)
        .putInteger("i", 1000)
        .putFloat("f", 101.1f)
        .putLong("l", 1000L)
        .putDouble("d", 1010101.101)
        .putZeroArray("za", ZeroUtility.newZeroArray().addDoubleArray(doubles));
    var actuality = ZeroUtility.binariesToMap(origin.toBinaries());

    assertAll("primitiveDataInObjectShouldMatch",
        () -> assertTrue(actuality.getBoolean("b")),
        () -> assertEquals((short) 11, actuality.getShort("s")),
        () -> assertEquals(1000, actuality.getInteger("i")),
        () -> assertEquals(101.1f, actuality.getFloat("f")),
        () -> assertEquals(1000L, actuality.getLong("l")),
        () -> assertEquals(1010101.101, actuality.getDouble("d"))
    );
  }

  @Test
  @DisplayName("Allow adding and fetching nested ZeroMap data to/from ZeroMap")
  void instanceDataInMapShouldMatch() {
    var origin = ZeroUtility.newZeroMap();
    origin.putNull("n")
        .putZeroElement("z", ZeroUtility.newZeroElement(ZeroType.BOOLEAN, false))
        .putString("s", "test");
    var actuality = ZeroUtility.binariesToMap(origin.toBinaries());

    assertAll("instanceDataInObjectShouldMatch",
        () -> assertTrue(actuality.isNull("n")),
        () -> assertAll("zeroDataShouldMatch",
            () -> assertEquals(ZeroType.BOOLEAN, actuality.getZeroElement("z").getType()),
            () -> assertFalse((boolean) actuality.getZeroElement("z").getData())
        ),
        () -> assertEquals("test", actuality.getString("s"))
    );
  }

  @Test
  @DisplayName("Allow adding and fetching arrays of primitive data to/from ZeroMap")
  void collectionDataInMapShouldMatch() {
    var origin = ZeroUtility.newZeroMap();
    var zeroArray = ZeroUtility.newZeroArray().addString("newZeroArray");
    origin.putBooleanArray("b", booleans)
        .putShortArray("s", shorts)
        .putIntegerArray("i", integers)
        .putLongArray("l", longs)
        .putFloatArray("f", floats)
        .putDoubleArray("d", doubles)
        .putStringArray("ss", strings)
        .putZeroArray("za", zeroArray);
    var actuality = ZeroUtility.binariesToMap(origin.toBinaries());

    assertAll("collectionDataInObjectShouldMatch",
        () -> assertEquals(booleans.toString(), actuality.getBooleanArray("b").toString()),
        () -> assertEquals(shorts.toString(), actuality.getShortArray("s").toString()),
        () -> assertEquals(integers.toString(), actuality.getIntegerArray("i").toString()),
        () -> assertEquals(longs.toString(), actuality.getLongArray("l").toString()),
        () -> assertEquals(floats.toString(), actuality.getFloatArray("f").toString()),
        () -> assertEquals(doubles.toString(), actuality.getDoubleArray("d").toString()),
        () -> assertEquals(strings.toString(), actuality.getStringArray("ss").toString()),
        () -> assertEquals(zeroArray.toString(), actuality.getZeroArray("za").toString())
    );
  }

  @Test
  @DisplayName("Allow adding and fetching duplicated arrays of primitive data to/from ZeroMap")
  void duplicatedValueInMapShouldWork() {
    var origin = ZeroUtility.newZeroMap();
    origin.putBooleanArray("b1", booleans).putShortArray("s1", shorts)
        .putBooleanArray("b2", booleans).putShortArray("s2", shorts);
    var actuality = ZeroUtility.binariesToMap(origin.toBinaries());

    assertAll("duplicatedValueInObjectShouldWork",
        () -> assertEquals(booleans.toString(), actuality.getBooleanArray("b1").toString()),
        () -> assertEquals(shorts.toString(), actuality.getShortArray("s1").toString()),
        () -> assertEquals(booleans.toString(), actuality.getBooleanArray("b2").toString()),
        () -> assertEquals(shorts.toString(), actuality.getShortArray("s2").toString())
    );
  }

  @Test
  @DisplayName("Allow adding and fetching nested ZeroMap data to/from ZeroMap")
  void zeroMapInMapShouldMatch() {
    var origin = ZeroUtility.newZeroMap();
    var zeroMap = ZeroUtility.newZeroMap();
    zeroMap.putBoolean("b", true)
        .putShort("s", (short) 10)
        .putInteger("i", 100)
        .putBooleanArray("ba", booleans)
        .putZeroArray("za", ZeroUtility.newZeroArray().addDoubleArray(doubles));
    origin.putZeroMap("z", zeroMap);
    var actuality = ZeroUtility.binariesToMap(origin.toBinaries());

    assertEquals(zeroMap.toString(), actuality.getZeroMap("z").toString());
  }

  @Test
  @DisplayName("The list of elements when using getReadonlyZeroArray() method should be correct")
  void getReadonlyZeroArrayShouldReturnTheSameElements() {
    var origin = ZeroUtility.newZeroArray();
    origin.addInteger(10).addInteger(20).addInteger(30);
    var readonlyOrigin = origin.getReadonlyZeroArray();

    assertAll("getReadonlyZeroArrayShouldReturnTheSameElements",
        () -> assertEquals(origin.getInteger(0), readonlyOrigin.getInteger(0)),
        () -> assertEquals(origin.getInteger(1), readonlyOrigin.getInteger(1)),
        () -> assertEquals(origin.getInteger(2), readonlyOrigin.getInteger(2)));
  }

  @Test
  @DisplayName("Fetching elements from a zero array should give expected results")
  void fetchElementsFromZeroArrayShouldHaveExpectedResults() {
    var origin = ZeroUtility.newZeroArray();
    origin.addInteger(10).addInteger(20).addInteger(30).addNull();

    assertAll("fetchElementsFromZeroArrayShouldHaveExpectedResults",
        () -> assertEquals(10, (int) origin.getDataForElementAt(0)),
        () -> assertNull(origin.getDataForElementAt(3)),
        () -> assertTrue(origin.contains(20)),
        () -> assertFalse(origin.contains(40)),
        () -> assertTrue(origin.contains(null)));

    origin.removeElementAt(3);
    assertEquals(3, origin.size());
    origin.removeElementAt(0);
    assertFalse(origin.contains(10));
  }

  @Test
  @DisplayName("Working with binaries in ZeroArray should deliver expected results")
  void workingWithBinariesInZeroArrayShouldReturnExpectedResults() {
    var origin = ZeroUtility.newZeroArray();
    origin.addByte((byte) 1).addByteArray(new byte[] {(byte) 1, (byte) 2, (byte) 3});

    assertAll("workingWithBinariesInZeroArrayShouldReturnExpectedResults",
        () -> assertEquals((byte) 1, origin.getByte(0)),
        () -> assertEquals((byte) 1, origin.getByteArray(1)[0]),
        () -> assertEquals((byte) 2, origin.getByteArray(1)[1]),
        () -> assertEquals((byte) 3, origin.getByteArray(1)[2]));

    var iterator = origin.iterator();
    while (iterator.hasNext()) {
      var element = iterator.next();
      if (element.getType() == ZeroType.BYTE) {
        assertEquals((byte) 1, element.getData());
      } else {
        assertEquals(3, ((byte[]) element.getData()).length);
      }
    }
  }

  @Test
  @DisplayName("The map of elements when using getReadonlyZeroMap() method should be correct")
  void getReadonlyZeroMapShouldReturnTheSameElements() {
    var origin = ZeroUtility.newZeroMap();
    origin.putInteger("a", 10).putInteger("b", 20).putInteger("c", 30);
    var readonlyOrigin = origin.getReadonlyZeroMap();
    var readonlyKeyOrigin = origin.getReadonlyKeys();

    assertAll("getReadonlyZeroMapShouldReturnTheSameElements",
        () -> assertEquals(origin.getInteger("a"), readonlyOrigin.getInteger("a")),
        () -> assertEquals(origin.getInteger("b"), readonlyOrigin.getInteger("b")),
        () -> assertEquals(origin.getInteger("c"), readonlyOrigin.getInteger("c")));

    assertTrue(readonlyKeyOrigin.contains("a"));
    assertTrue(readonlyKeyOrigin.contains("b"));
    assertTrue(readonlyKeyOrigin.contains("c"));
    assertFalse(readonlyKeyOrigin.contains("d"));
  }

  @Test
  @DisplayName("Fetching elements from a zero map should give expected results")
  void fetchElementsFromZeroMapShouldHaveExpectedResults() {
    var origin = ZeroUtility.newZeroMap();
    origin.putInteger("a", 10).putInteger("b", 20).putInteger("c", 30)
        .putNull("d");

    assertAll("fetchElementsFromZeroMapShouldHaveExpectedResults",
        () -> assertEquals(10, (int) origin.getZeroElement("a").getData()),
        () -> assertNull(origin.getZeroElement("d").getData()),
        () -> assertTrue(origin.containsKey("a")),
        () -> assertFalse(origin.containsKey("f")));

    origin.removeElement("d");
    assertEquals(3, origin.size());
    origin.removeElement("a");
    assertFalse(origin.containsKey("a"));
  }

  @Test
  @DisplayName("Working with binaries in ZeroMap should deliver expected results")
  void workingWithBinariesInZeroMapShouldReturnExpectedResults() {
    var origin = ZeroUtility.newZeroMap();
    origin.putByte("a", (byte) 1).putByteArray("b", new byte[] {(byte) 1, (byte) 2, (byte) 3});
    assertAll("workingWithBinariesInZeroMapShouldReturnExpectedResults",
        () -> assertEquals((byte) 1, origin.getByte("a")),
        () -> assertEquals((byte) 1, origin.getByteArray("b")[0]),
        () -> assertEquals((byte) 2, origin.getByteArray("b")[1]),
        () -> assertEquals((byte) 3, origin.getByteArray("b")[2]));

    var iterator = origin.iterator();
    while (iterator.hasNext()) {
      var element = iterator.next();
      if (element.getKey().equals("a")) {
        assertEquals((byte) 1, element.getValue().getData());
      } else {
        assertEquals(3, ((byte[]) element.getValue().getData()).length);
      }
    }
  }

  @Test
  @DisplayName("Test ZeroArray toString()")
  void testZeroArrayToStringShouldGiveExpectedResult() {
    var origin = ZeroUtility.newZeroArray();
    var zeroArray = ZeroUtility.newZeroArray();
    zeroArray.addString("newZeroArray");
    var zeroMap = ZeroUtility.newZeroMap();
    zeroMap.putBoolean("b", true)
        .putShort("s", (short) 10)
        .putInteger("i", 100)
        .putBooleanArray("ba", booleans)
        .putZeroArray("za", ZeroUtility.newZeroArray().addDoubleArray(doubles));

    origin.addBoolean(true).addByte((byte) 1).addShort((short) 2).addInteger(3).addLong(4L)
        .addFloat(5.0f).addDouble(6.0).addString("7").addNull();
    origin.addBooleanArray(booleans).addByteArray(binaries).addShortArray(shorts)
        .addIntegerArray(integers).addLongArray(longs).addFloatArray(floats).addDoubleArray(doubles)
        .addStringArray(strings);
    origin.addZeroArray(zeroArray);
    origin.addZeroMap(zeroMap);

    assertEquals("""
        ZeroArray{ (boolean) true; (byte) 1; (short) 2; (integer) 3; (long) 4; (float) 5.0; (double) 6.0; (string) 7; (null) null; (boolean_array) [true, false, true]; (byte_array) byte[3]; (short_array) [10, 11, 12, 13, 14, 15]; (integer_array) [100, 101, 102, 103]; (long_array) [1000, 2000]; (float_array) [1001.1, 1002.2, 1003.3, 1004.4, 1005.5, 1006.6]; (double_array) [1000000.11111]; (string_array) [zero, data, testing, is, awesome]; (zero_array) ZeroArray{ (string) newZeroArray }; (zero_map) ZeroMap{ (boolean) b: true; (short) s: 10; (zero_array) za: ZeroArray{ (double_array) [1000000.11111] }; (integer) i: 100; (boolean_array) ba: [true, false, true] } }
        """.trim(), origin.toString());
  }

  @Test
  @DisplayName("Test ZeroMap toString()")
  void testZeroMapToStringShouldGiveExpectedResult() {
    var origin = ZeroUtility.newZeroMap();
    var zeroArray = ZeroUtility.newZeroArray();
    zeroArray.addString("newZeroArray");
    var zeroMap = ZeroUtility.newZeroMap();
    zeroMap.putBoolean("b", true)
        .putShort("s", (short) 10)
        .putInteger("i", 100)
        .putBooleanArray("ba", booleans)
        .putZeroArray("za", ZeroUtility.newZeroArray().addDoubleArray(doubles));

    origin.putBoolean("a", true).putByte("b", (byte) 1).putShort("c", (short) 2).putInteger("d",
            3).putLong("e", 4L).putFloat("f", 5.0f).putDouble("g", 6.0).putString("h", "7")
        .putNull("i");
    origin.putBooleanArray("k", booleans).putByteArray("l", binaries).putShortArray("m", shorts)
        .putIntegerArray("n", integers).putLongArray("o", longs).putFloatArray("p", floats)
        .putDoubleArray("q", doubles).putStringArray("r", strings);
    origin.putZeroArray("r", zeroArray);
    origin.putZeroMap("s", zeroMap);

    assertEquals("""
        ZeroMap{ (boolean) a: true; (byte) b: 1; (short) c: 2; (integer) d: 3; (long) e: 4; (float) f: 5.0; (double) g: 6.0; (string) h: 7; (null) i: null; (boolean_array) k: [true, false, true]; (byte_array) l: byte[3]; (short_array) m: [10, 11, 12, 13, 14, 15]; (integer_array) n: [100, 101, 102, 103]; (long_array) o: [1000, 2000]; (float_array) p: [1001.1, 1002.2, 1003.3, 1004.4, 1005.5, 1006.6]; (double_array) q: [1000000.11111]; (zero_array) r: ZeroArray{ (string) newZeroArray }; (zero_map) s: ZeroMap{ (boolean) b: true; (short) s: 10; (zero_array) za: ZeroArray{ (double_array) [1000000.11111] }; (integer) i: 100; (boolean_array) ba: [true, false, true] } }
        """.trim(), origin.toString());
  }

  @Test
  @DisplayName("It should throw exceptions when invalid conversions  were called")
  void itShouldThrowExceptionsWhenInvalidConversionCalled() {
    // array size is insufficient
    assertThrows(IllegalStateException.class,
        () -> ZeroUtility.binariesToArray(new byte[] {(byte) 1}));
    // it is not the zero array
    assertThrows(IllegalStateException.class,
        () -> ZeroUtility.binariesToArray(new byte[] {(byte) 1, (byte) 2, (byte) 3}));
    // an array with negative size
    var negativeArraySizeInShort = ByteUtility.shortToBytes((short) -2);
    assertThrows(NegativeArraySizeException.class,
        () -> ZeroUtility.binariesToArray(
            new byte[] {(byte) ZeroType.ZERO_ARRAY.getValue(), negativeArraySizeInShort[0],
                negativeArraySizeInShort[1]}));
    // unrecognized zero type
    var arraySizeInShort = ByteUtility.shortToBytes((short) 2);
    assertThrows(IllegalArgumentException.class,
        () -> ZeroUtility.binariesToArray(
            new byte[] {(byte) ZeroType.ZERO_ARRAY.getValue(), arraySizeInShort[0],
                arraySizeInShort[1], (byte) 19, (byte) 1}));
    // failed to decode boolean
    assertThrows(IllegalArgumentException.class,
        () -> ZeroUtility.binariesToArray(
            new byte[] {(byte) ZeroType.ZERO_ARRAY.getValue(), arraySizeInShort[0],
                arraySizeInShort[1],
                (byte) ZeroType.BOOLEAN.getValue(), (byte) 2}));
    // failed to decode string
    assertThrows(IllegalArgumentException.class,
        () -> ZeroUtility.binariesToArray(
            new byte[] {(byte) ZeroType.ZERO_ARRAY.getValue(), arraySizeInShort[0],
                arraySizeInShort[1],
                (byte) ZeroType.STRING.getValue(), negativeArraySizeInShort[0],
                negativeArraySizeInShort[1]}));
    // failed to decode collection
    assertThrows(IllegalArgumentException.class,
        () -> ZeroUtility.binariesToArray(
            new byte[] {(byte) ZeroType.ZERO_ARRAY.getValue(), arraySizeInShort[0],
                arraySizeInShort[1],
                (byte) ZeroType.BOOLEAN_ARRAY.getValue(), negativeArraySizeInShort[0],
                negativeArraySizeInShort[1]}));
    // failed to decode boolean array
    assertThrows(IllegalArgumentException.class,
        () -> ZeroUtility.binariesToArray(
            new byte[] {(byte) ZeroType.ZERO_ARRAY.getValue(), arraySizeInShort[0],
                arraySizeInShort[1],
                (byte) ZeroType.BOOLEAN_ARRAY.getValue(), arraySizeInShort[0], arraySizeInShort[1],
                (byte) 2}));
    // failed to decode byte array
    var negativeArraySizeInInteger = ByteUtility.intToBytes((short) -1);
    assertThrows(IllegalArgumentException.class,
        () -> ZeroUtility.binariesToArray(
            new byte[] {(byte) ZeroType.ZERO_ARRAY.getValue(), arraySizeInShort[0],
                arraySizeInShort[1],
                (byte) ZeroType.BYTE_ARRAY.getValue(), negativeArraySizeInInteger[0],
                negativeArraySizeInInteger[1], negativeArraySizeInInteger[2],
                negativeArraySizeInInteger[3]}));
    // failed to decode string array
    assertThrows(IllegalArgumentException.class,
        () -> ZeroUtility.binariesToArray(
            new byte[] {(byte) ZeroType.ZERO_ARRAY.getValue(), arraySizeInShort[0],
                arraySizeInShort[1],
                (byte) ZeroType.STRING_ARRAY.getValue(), negativeArraySizeInShort[0],
                negativeArraySizeInShort[1]}));

    assertThrows(IllegalStateException.class, () -> ZeroUtility.binariesToMap(new byte[] {(byte) 1}));
    // it is not the zero map
    assertThrows(IllegalStateException.class,
        () -> ZeroUtility.binariesToMap(new byte[] {(byte) 1, (byte) 2, (byte) 3}));
    // a map with negative size
    assertThrows(NegativeArraySizeException.class,
        () -> ZeroUtility.binariesToMap(
            new byte[] {(byte) ZeroType.ZERO_MAP.getValue(), negativeArraySizeInShort[0],
                negativeArraySizeInShort[1]}));
    // unrecognized zero type
    assertThrows(IllegalStateException.class,
        () -> ZeroUtility.binariesToMap(
            new byte[] {(byte) ZeroType.ZERO_ARRAY.getValue(), arraySizeInShort[0],
                arraySizeInShort[1], (byte) 19, (byte) 1}));
    // failed to decode boolean
    assertThrows(IllegalArgumentException.class,
        () -> ZeroUtility.binariesToMap(
            new byte[] {(byte) ZeroType.ZERO_MAP.getValue(), arraySizeInShort[0],
                arraySizeInShort[1], arraySizeInShort[0], arraySizeInShort[1], (byte) 61,
                (byte) ZeroType.BOOLEAN.getValue(), (byte) 2}));
    // failed to decode unrecognized element
    assertThrows(IllegalArgumentException.class,
        () -> ZeroUtility.binariesToMap(
            new byte[] {(byte) ZeroType.ZERO_MAP.getValue(), arraySizeInShort[0],
                arraySizeInShort[1], arraySizeInShort[0], arraySizeInShort[1], (byte) 61,
                (byte) 0, (byte) 19}));
  }

  @Test
  @DisplayName("Insufficient ByteBuffer should be appended")
  void insufficientByteBufferShouldBeAppended() {
    var origin = ZeroUtility.newZeroMap();
    var zeroMap = ZeroUtility.newZeroMap();
    zeroMap.putBoolean("b", true)
        .putShort("s", (short) 10)
        .putInteger("i", 100)
        .putFloat("f", 10.0f)
        .putShortArray("sa", shorts);
    var binaryMap = zeroMap.toBinaries();
    for (char i = 'a'; i <= 'z'; i++) {
      origin.putZeroMap(String.valueOf(i), ZeroUtility.binariesToMap(binaryMap));
    }

    origin.toBinaries();
  }
}

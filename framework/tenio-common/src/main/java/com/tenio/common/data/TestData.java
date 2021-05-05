package com.tenio.common.data;

import java.util.ArrayList;

public final class TestData {

	public static void main(String[] args) {
		var testArray = ZeroArrayImpl.newInstance();
		testArray.addBoolean(true).addByte((byte) 1).addShort((short) 2).addInteger(4).addLong(8).addFloat(4.0f)
				.addDouble(8.0f).addString("test");
		testArray.addByteArray(new byte[10]);
		var testShortArray = new ArrayList<Short>();
		testShortArray.add((short)10);
		testShortArray.add((short)11);
		testShortArray.add((short)12);
		testShortArray.add((short)13);
		testShortArray.add((short)14);
		testArray.addShortArray(testShortArray);
		
		var testStringArray = new ArrayList<String>();
		testStringArray.add("sub");
		testStringArray.add("string");
		testStringArray.add("array");
		testArray.addStringArray(testStringArray);
		System.err.println(testArray);
		
		byte[] testArrayByte = testArray.toBinary();
		var recoveredArray = ZeroArrayImpl.newInstance(testArrayByte);
		System.out.println(recoveredArray);
		
		
		var testObject = ZeroObjectImpl.newInstance();
		testObject.putBoolean("bool", false).putDouble("doub", 8.0f).putByteArray("barray", new byte[20]);
		System.err.println(testObject);
		
		byte[] testObjectByte = testObject.toBinary();
		var recoveredObject = ZeroObjectImpl.newInstance(testObjectByte);
		System.out.println(recoveredObject);

	}

}

package com.tenio.core.monitoring.system;

/**
 * @author kong
 */
public enum SytemInfoType {

	OS_NAME("os.name"),
	OS_ARCH("os.arch"),
	OS_VERSION("os.version"),
	JAVA_VERSION("java.version"),
	JAVA_VENDOR("java.vendor"),
	JAVA_VENDOR_URL("java.vendor.url"),
	JAVA_VM_SPEC_VERSION("java.vm.specification.version"),
	JAVA_VM_VERSION("java.vm.version"),
	JAVA_VM_VENDOR("java.vm.vendor"),
	JAVA_VM_NAME("java.vm.name"),
	JAVA_IO_TMPDIR("java.io.tmpdir");
	
	private final String value;
	
	private SytemInfoType(final String value) {
		this.value = value;
	}
	
	public final String getValue() {
		return this.value;
	}
	
	@Override
	public String toString() {
		return this.name();
	}
	
}

package io.github.xanthic.cache.core.provider;

import org.junit.jupiter.api.Disabled;

public class SimpleMapProviderTest extends ProviderTestBase {

	public SimpleMapProviderTest() {
		super(new SimpleMapProvider());
	}

	@Disabled
	@Override
	public void sizeEvictionTest() {
		// skip test; SimpleMapProvider does not implement a size constraint
	}

	@Disabled
	@Override
	public void sizeEvictionListenerTest() {
		// skip test; SimpleMapProvider does not implement a size constraint
	}

	@Disabled
	@Override
	public void timeEvictionTest() {
		// skip test; SimpleMapProvider does not implement a time constraint
	}

	@Disabled
	@Override
	public void timeEvictionListenerTest() {
		// skip test; SimpleMapProvider does not implement a time constraint
	}

	@Disabled
	@Override
	public void replacedListenerTest() {
		// skip test; SimpleMapProvider does not implement removal listener
	}

	@Disabled
	@Override
	public void manualRemovalListenerTest() {
		// skip test; SimpleMapProvider does not implement removal listener
	}

	@Disabled
	@Override
	public void registeredAsDefaultTest() {
		// skip test; SimpleMapProvider is deliberately not automatically set as a possible default
	}

}

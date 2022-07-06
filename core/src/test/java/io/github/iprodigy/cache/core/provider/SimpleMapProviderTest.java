package io.github.iprodigy.cache.core.provider;

public class SimpleMapProviderTest extends ProviderTestBase {

	public SimpleMapProviderTest() {
		super(new SimpleMapProvider());
	}

	@Override
	public void registeredAsDefaultTest() {
		// skip test; SimpleMapProvider is deliberately not automatically set as a possible default
	}

}

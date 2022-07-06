package io.github.iprodigy.cache.provider.androidx;

import io.github.iprodigy.cache.core.provider.ProviderTestBase;

public class AndroidExpiringLruProviderTest extends ProviderTestBase {

	public AndroidExpiringLruProviderTest() {
		super(new AndroidExpiringLruProvider());
	}

}

package io.github.xanthic.cache.provider.androidx;

import io.github.xanthic.cache.core.provider.ProviderTestBase;

public class AndroidExpiringLruProviderTest extends ProviderTestBase {

	public AndroidExpiringLruProviderTest() {
		super(new AndroidExpiringLruProvider());
	}

}

package io.github.xanthic.provider.androidx;

import io.github.xanthic.core.provider.ProviderTestBase;

public class AndroidExpiringLruProviderTest extends ProviderTestBase {

	public AndroidExpiringLruProviderTest() {
		super(new AndroidExpiringLruProvider());
	}

}

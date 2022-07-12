package io.github.xanthic.provider.androidx;

import io.github.xanthic.core.CacheApiSettings;
import io.github.xanthic.core.provider.ProviderTestBase;
import org.junit.jupiter.api.Disabled;

public class AndroidLruProviderTest extends ProviderTestBase {

	public AndroidLruProviderTest() {
		super(new AndroidLruProvider());
		CacheApiSettings.getInstance().setDefaultCacheProvider(provider); // since AndroidExpiringLruProvider takes precedence
	}

	@Disabled
	@Override
	public void timeEvictionTest() {
		// skip test; AndroidLruProvider does not implement a time constraint
	}

	@Disabled
	@Override
	public void timeEvictionListenerTest() {
		// skip test; AndroidLruProvider does not implement a time constraint
	}

}

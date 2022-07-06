package io.github.iprodigy.cache.provider.androidx;

import io.github.iprodigy.cache.core.CacheApiSettings;
import io.github.iprodigy.cache.core.provider.ProviderTestBase;
import org.junit.jupiter.api.Disabled;

public class AndroidLruProviderTest extends ProviderTestBase {

	public AndroidLruProviderTest() {
		super(new AndroidLruProvider());
		CacheApiSettings.getInstance().setDefaultCacheProvider(provider); // since AndroidExpiringLruProvider takes precedence
	}

	@Disabled
	@Override
	public void timeEvictionTest() {
		// skip test; AndroidLruProvider does not implement a size constraint
	}

}

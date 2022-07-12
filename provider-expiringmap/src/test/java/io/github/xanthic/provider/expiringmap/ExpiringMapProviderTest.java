package io.github.xanthic.provider.expiringmap;

import io.github.xanthic.core.provider.ProviderTestBase;
import org.junit.jupiter.api.Disabled;

public class ExpiringMapProviderTest extends ProviderTestBase {

	public ExpiringMapProviderTest() {
		super(new ExpiringMapProvider());
	}

	@Disabled
	@Override
	public void replacedListenerTest() {
		// skip test; library limitations of ExpiringMap don't allow for this granularity
	}

	@Disabled
	@Override
	public void manualRemovalListenerTest() {
		// skip test; library limitations of ExpiringMap don't allow for this granularity
	}

}

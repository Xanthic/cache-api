package io.github.iprodigy.cache;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum RemovalCause {
	SIZE(true),
	TIME(true),
	REPLACED(false),
	MANUAL(false),
	OTHER(false);

	@Getter
	private final boolean eviction;
}

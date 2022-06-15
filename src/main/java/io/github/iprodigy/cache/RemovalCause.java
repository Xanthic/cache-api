package io.github.iprodigy.cache;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum RemovalCause {
	SIZE(true),
	TIME(true),
	REPLACED(false),
	MANUAL(false),
	OTHER(true);

	@Getter
	private final boolean eviction;
}

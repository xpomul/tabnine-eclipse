package com.tabnine.binary;

import com.tabnine.binary.exceptions.TabNineInvalidResponseException;

public interface BinaryRequest<R extends BinaryResponse> {
	Class<R> response();

	Object serialize();

	default boolean validate(R response) {
		return true;
	}

	default boolean shouldBeAllowed(TabNineInvalidResponseException e) {
		return false;
	}
}

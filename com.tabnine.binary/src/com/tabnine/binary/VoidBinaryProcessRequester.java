package com.tabnine.binary;

import com.tabnine.binary.exceptions.TabNineDeadException;

public class VoidBinaryProcessRequester implements BinaryProcessRequester {
	private static final BinaryProcessRequester INSTANCE = new VoidBinaryProcessRequester();

	public static BinaryProcessRequester instance() {
		return INSTANCE;
	}

	@Override
	public <R extends BinaryResponse> R request(BinaryRequest<R> request) throws TabNineDeadException {
		return null;
	}

	@Override
	public Long pid() {
		return 0L;
	}

	@Override
	public void destroy() {
	}
}

package com.tabnine.binary;

import com.tabnine.binary.exceptions.TabNineDeadException;

public interface BinaryProcessRequester {
	<R extends BinaryResponse> R request(BinaryRequest<R> request) throws TabNineDeadException;

	Long pid();

	void destroy();
}

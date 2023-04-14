package com.tabnine.binary;

import static java.lang.String.format;

import java.io.IOException;
import java.util.Optional;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.tabnine.binary.exceptions.TabNineDeadException;
import com.tabnine.binary.exceptions.TabNineInvalidResponseException;

public class ParsedBinaryIO {
	private final Gson gson;
	private final BinaryProcessGateway binaryProcessGateway;

	public ParsedBinaryIO(Gson gson, BinaryProcessGateway binaryProcessGateway) {
		this.gson = gson;
		this.binaryProcessGateway = binaryProcessGateway;
	}

	public <R> R readResponse(Class<R> responseClass)
			throws IOException, TabNineDeadException, TabNineInvalidResponseException {
		String rawResponse = binaryProcessGateway.readRawResponse();

		try {
			return Optional.ofNullable(gson.fromJson(rawResponse, responseClass))
					.orElseThrow(() -> new TabNineInvalidResponseException("Binary returned null as a response"));
		} catch (TabNineInvalidResponseException | JsonSyntaxException e) {
			throw new TabNineInvalidResponseException(format("Binary returned illegal response: %s", rawResponse), e,
					rawResponse);
		}
	}

	public void writeRequest(Object request) throws IOException {
		var rawRequest = gson.toJson(request) + "\n";
		binaryProcessGateway.writeRequest(rawRequest);
	}

	public Long pid() {
		return binaryProcessGateway.pid();
	}

	public boolean isDead() {
		return binaryProcessGateway.isDead();
	}

	public void destroy() {
		binaryProcessGateway.destroy();
	}
}

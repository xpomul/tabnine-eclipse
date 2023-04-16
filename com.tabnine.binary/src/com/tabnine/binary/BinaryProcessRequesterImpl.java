package com.tabnine.binary;

import static com.tabnine.general.StaticConfig.ILLEGAL_RESPONSE_THRESHOLD;
import static com.tabnine.general.StaticConfig.wrapWithBinaryRequest;

import java.io.IOException;

import com.tabnine.Log;
import com.tabnine.binary.exceptions.TabNineDeadException;
import com.tabnine.binary.exceptions.TabNineInvalidResponseException;

public class BinaryProcessRequesterImpl implements BinaryProcessRequester {
	private int illegalResponsesGiven = 0;

	private final ParsedBinaryIO parsedBinaryIO;

	public BinaryProcessRequesterImpl(ParsedBinaryIO parsedBinaryIO) {
		this.parsedBinaryIO = parsedBinaryIO;
	}

	/**
	 * Request a prediction from TabNine's binary.
	 *
	 * @param request
	 * @return an AutocompleteResponse
	 * @throws TabNineDeadException if process's dead.
	 * @throws TabNineDeadException if process's BufferedReader has reached its end
	 *                              (also mean dead...).
	 * @throws TabNineDeadException if there was an IOException communicating to the
	 *                              process.
	 * @throws TabNineDeadException if the result from the process was invalid
	 *                              multiple times.
	 */
	@Override
	public synchronized <R extends BinaryResponse> R request(BinaryRequest<R> request) throws TabNineDeadException {
		if (parsedBinaryIO.isDead()) {
			throw new TabNineDeadException("Binary is dead");
		}

		try {
			parsedBinaryIO.writeRequest(wrapWithBinaryRequest(request.serialize()));

			return readResult(request);
		} catch (IOException e) {
			Log.warning("Exception communicating with the binary", e);

			throw new TabNineDeadException(e);
		}
	}

	@Override
	public Long pid() {
		return parsedBinaryIO.pid();
	}

	@Override
	public void destroy() {
		parsedBinaryIO.destroy();
	}

	private <R extends BinaryResponse> R readResult(BinaryRequest<R> request) throws IOException, TabNineDeadException {
		try {
			R response = parsedBinaryIO.readResponse(request.response());

			if (!request.validate(response)) {
				throw new TabNineInvalidResponseException();
			}

			this.illegalResponsesGiven = 0;

			return response;
		} catch (TabNineInvalidResponseException exception) {
			if (request.shouldBeAllowed(exception)) {
				return null;
			}

			if (++illegalResponsesGiven > ILLEGAL_RESPONSE_THRESHOLD) {
				illegalResponsesGiven = 0;
				throw new TabNineDeadException("Too many illegal responses given");
			} else {
				Log.warning("Unexpected Exception", exception);

				return null;
			}
		}
	}
}

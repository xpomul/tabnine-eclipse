package com.tabnine.binary.exceptions;

public class BinaryRequestTimeoutException extends RuntimeException {
	public BinaryRequestTimeoutException() {
		super("Requests to TabNine's binary are consistently taking too long. Restarting the binary.");
	}
}

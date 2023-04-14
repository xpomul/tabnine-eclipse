package com.tabnine.binary.requests.autocomplete;

import com.tabnine.general.CompletionKind;

public class ResultEntry {
	public String new_prefix;
	public String old_suffix;
	public String new_suffix;
	public CompletionMetadata completion_metadata;

	public boolean isSnippet() {
		if (this.completion_metadata == null) {
			return false;
		}

		return this.completion_metadata.getCompletionKind() == CompletionKind.Snippet;
	}
}

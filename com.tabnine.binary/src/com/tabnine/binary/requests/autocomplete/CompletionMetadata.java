package com.tabnine.binary.requests.autocomplete;

import java.util.Map;

import com.tabnine.general.CompletionKind;
import com.tabnine.general.CompletionOrigin;

public class CompletionMetadata {
	private CompletionOrigin origin;
	private String detail;
	private CompletionKind completionKind;
	private Map<String, Object> snippetContext;
	private Boolean isCached;
	private Boolean deprecated;

	public CompletionMetadata() {
	}

	public CompletionMetadata(CompletionOrigin origin, String detail, CompletionKind completionKind,
			Map<String, Object> snippetContext, Boolean isCached, Boolean deprecated) {
		this.origin = origin;
		this.detail = detail;
		this.completionKind = completionKind;
		this.snippetContext = snippetContext;
		this.isCached = isCached;
		this.deprecated = deprecated;
	}

	public CompletionOrigin getOrigin() {
		return origin;
	}

	public void setOrigin(CompletionOrigin origin) {
		this.origin = origin;
	}

	public String getDetail() {
		return detail;
	}

	public void setDetail(String detail) {
		this.detail = detail;
	}

	public CompletionKind getCompletionKind() {
		return completionKind;
	}

	public void setCompletionKind(CompletionKind completionKind) {
		this.completionKind = completionKind;
	}

	public Map<String, Object> getSnippetContext() {
		return snippetContext;
	}

	public void setSnippetContext(Map<String, Object> snippetContext) {
		this.snippetContext = snippetContext;
	}

	public Boolean getIsCached() {
		return isCached;
	}

	public void setIsCached(Boolean isCached) {
		this.isCached = isCached;
	}

	public Boolean getDeprecated() {
		return deprecated;
	}

	public void setDeprecated(Boolean deprecated) {
		this.deprecated = deprecated;
	}

	public boolean getIsDeprecated() {
		return deprecated != null && deprecated;
	}
}

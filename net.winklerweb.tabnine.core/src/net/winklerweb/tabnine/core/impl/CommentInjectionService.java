package net.winklerweb.tabnine.core.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.eclipse.core.runtime.preferences.InstanceScope;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.prefs.BackingStoreException;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.tabnine.Log;

import net.winklerweb.tabnine.core.ICommentInjectionService;

@Component
public class CommentInjectionService implements ICommentInjectionService {

	private static final String PREF_KEY = "recentComments";
	private static final String PREF_QUALIFIER = "net.winklerweb.tabnine.core";
	private List<String> recentComments = new ArrayList<String>();

	@Activate
	public void activate() {
		try {
			var preferences = InstanceScope.INSTANCE.getNode(PREF_QUALIFIER);
			var json = preferences.get(PREF_KEY, null);
			if (json != null) {
				List<String> commentsFromPref = new Gson().fromJson(json, new TypeToken<List<String>>() {}.getType());
				recentComments.addAll(commentsFromPref);
			}
		} catch (Exception e) {
			Log.error("Error while loading recent comments", e);
		}
	}

	@Override
	public Optional<String> getCurrentInjectionComment() {
		if (recentComments.isEmpty()) {
			return Optional.empty();
		}
		return Optional.of(recentComments.get(recentComments.size() - 1));
	}

	@Override
	public void setCurrentInjectionComment(String newComment) {
		recentComments.remove(newComment);
		recentComments.add(newComment);
		if (recentComments.size() > 10) {
			recentComments.remove(0);
		}

		var preferences = InstanceScope.INSTANCE.getNode(PREF_QUALIFIER);
		Gson gson = new Gson();
		var json = gson.toJson(recentComments);
		preferences.put(PREF_KEY, json);
		try {
			preferences.flush();
		} catch (BackingStoreException e) {
			Log.error("Error while storing recent comments", e);
		}
	}

	@Override
	public List<String> getRecentInjectionComments() {
		return recentComments;
	}

}

package net.winklerweb.tabnine.core;

import java.util.List;
import java.util.Optional;

public interface ICommentInjectionService {

	Optional<String> getCurrentInjectionComment();
	
	void setCurrentInjectionComment(String newComment);
	
	List<String> getRecentInjectionComments();
}

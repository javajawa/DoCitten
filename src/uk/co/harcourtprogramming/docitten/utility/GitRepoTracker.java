package uk.co.harcourtprogramming.docitten.utility;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.errors.AmbiguousObjectException;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.RevisionSyntaxException;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;

public class GitRepoTracker {
	private Git git;
	private ObjectId head;
	private final String name;
	
	/**
	 * 
	 * @param f The git repository of the distro to represent
	 * @throws IOException 
	 */
	public GitRepoTracker(File f, String name) throws IOException {
		this.name = name;
		this.git = Git.open(f);
		this.updateHead();
	}
	
	public GitRepoTracker(File f) throws IOException {
		this(f, f.getName());
	}
	
	protected GitRepoTracker(Repository repo) {
		this.name = "test repo";
		this.git = new Git(repo);
		this.updateHead();
	}
	
	/**
	 * Updates the distro to point to the newest HEAD, and returns the old HEAD Object
	 * @return
	 * @throws IOException 
	 * @throws IncorrectObjectTypeException 
	 * @throws AmbiguousObjectException 
	 * @throws RevisionSyntaxException 
	 */
	private ObjectId updateHead() {
		ObjectId oldHead = this.head;
		try {
			this.git.getRepository().scanForRepoChanges();
			this.head = this.git.getRepository().resolve(Constants.HEAD);
		} catch (Exception e) {
			// FIXME: I don't know what to do here.
		}
		return oldHead;
	}
	
	/**
	 * Fetch commits since from
	 * @param from Commit to begin log from
	 * @return Iterable of commits, newest first.
	 */
	public Iterable<RevCommit> fetchUpdates(ObjectId from) throws Exception {
		Iterable<RevCommit> ret;
		try{
			ret = this.git.log().addRange(from, this.head).call();
		} catch (Exception e) {
			ret = new ArrayList<RevCommit>();
		}
		return ret;
	}
	
	/**
	 * Fetch commits since the last time updateHead() or fetch*() was called
	 * @return Iterable of commits, newest first.
	 */
	public Iterable<RevCommit> fetchUpdates() throws Exception {
		Iterable<RevCommit> ret;
		try {
			ObjectId oldHead = this.updateHead();
			ret = this.fetchUpdates(oldHead);
		} catch (Exception e) {
			ret = new ArrayList<RevCommit>();
		}
		return ret;
	}
	
	/**
	 * Fetch short commit messages committed since last updateHead() for fetch*() was called
	 * @return Iterable of commit messages, oldest first.
	 */
	public Iterable<String> fetchStringUpdates() throws Exception {
		return this.fetchStringUpdates(this.fetchUpdates());
	}
	
	/**
	 * Fetch short commit messages committed since a given revision
	 * @param from Commit ref
	 * @return Iterable of commit messages, oldest first.
	 */
	public Iterable<String> fetchStringUpdates(String from) throws Exception {
		ObjectId commit = this.git.getRepository().resolve(from);
		return this.fetchStringUpdates(this.fetchUpdates(commit));
	}
	
	private Iterable<String> fetchStringUpdates(Iterable<RevCommit> commits) {
		LinkedList<String> strs = new LinkedList<String>();
		for (RevCommit c : commits) {
			strs.push(c.getShortMessage());
		}
		return strs;
	}
	
	public String getName() {
		return this.name;
	}
	
	
}
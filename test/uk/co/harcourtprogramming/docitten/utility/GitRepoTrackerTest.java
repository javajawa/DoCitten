package uk.co.harcourtprogramming.docitten.utility;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.Iterator;

import org.eclipse.jgit.dircache.DirCache;
import org.eclipse.jgit.errors.UnmergedPathException;
import org.eclipse.jgit.internal.storage.dfs.DfsRepositoryDescription;
import org.eclipse.jgit.internal.storage.dfs.InMemoryRepository;
import org.eclipse.jgit.lib.CommitBuilder;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectInserter;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.RefUpdate;
import org.eclipse.jgit.lib.Repository;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class GitRepoTrackerTest {
	private Repository repo;
	
	@Before
	public void createTestRepo() throws IOException {
		this.repo = new InMemoryRepository(new DfsRepositoryDescription());
		commit("Initial commit");
	}
	
	private void commit(String message) throws UnmergedPathException, IOException {
		final PersonIdent person = new PersonIdent("name", "email");
		ObjectInserter odi = this.repo.newObjectInserter();
		
		CommitBuilder commit = new CommitBuilder();
		commit.setMessage(message);
		commit.setAuthor(person);
		commit.setCommitter(person);
		
		/* Set commit parent */
		ObjectId head = repo.resolve(Constants.HEAD);
		if (head != null) commit.setParentId(head);
		
		/* Set commit tree */
		DirCache index = DirCache.newInCore();
		ObjectId tree = index.writeTree(odi);
		commit.setTreeId(tree);
		
		ObjectId commitid = odi.insert(commit);
		odi.flush();
		
		RefUpdate ru = repo.updateRef(Constants.HEAD);
		ru.setNewObjectId(commitid);
		ru.update();
	}
	
	@After
	public void destroyTestRepo() {
		this.repo.close();
		this.repo = null;
	}
	
	@Test
	public void testConstructRepo() throws Exception {
		GitRepoTracker d = new GitRepoTracker(this.repo);
		assertFalse(d.fetchStringUpdates().iterator().hasNext());
	}

	@Test
	public void testNewCommitReturned() throws Exception {
		String msg1 = "test";
		String msg2 = "test2";
		GitRepoTracker d = new GitRepoTracker(this.repo);
		commit(msg1);
		commit(msg2);
		Iterator<String> it = d.fetchStringUpdates().iterator();
		assertEquals(msg1, it.next());
		assertEquals(msg2, it.next());
	}
}
